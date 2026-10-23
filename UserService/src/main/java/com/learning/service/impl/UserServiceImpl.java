package com.learning.service.impl;

import com.learning.config.security.CustomUserDetails;
import com.learning.entity.RoleEntity;
import com.learning.entity.UserEntity;
import com.learning.entity.UserType;
import com.learning.exceptions.UserServiceExceptions;
import com.learning.model.OtpDto;
import com.learning.model.UserDto;
import com.learning.publisher.OtpPublisher;
import com.learning.publisher.model.OtpPublisherModel;
import com.learning.repository.RoleRepository;
import com.learning.repository.UserRepository;
import com.learning.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.utils.UserServiceUtils;
import com.learning.valkey.ValkeyService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    private final OtpPublisher otpPublisher;

    // this is allowed to be null if otp.cache.enabled is false, so we mark it as required = false
    @Autowired(required = false)
    private ValkeyService valkeyService;

    @Value("${otp.cache.enabled:false}")
    private boolean otpCacheEnabled;

    // Using ObjectProvider to avoid circular dependency issues and to allow for optional injection
    // Spring will only inject ValkeyService if it's available in the context, otherwise it will be null.
    // Its lazy nature allows us to avoid issues with circular dependencies.
    // Here we are using ObjectProvider to get the ValkeyService bean only when needed means if otpCacheEnabled is true.
    private final ObjectProvider<ValkeyService> valkeyServiceProvider;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, ObjectMapper objectMapper, PasswordEncoder passwordEncoder, RoleRepository roleRepository, OtpPublisher otpPublisher, ObjectProvider<ValkeyService> valkeyServiceProvider) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.otpPublisher = otpPublisher;
        this.valkeyServiceProvider = valkeyServiceProvider;
    }

    @Override
    public UserDto createUser(UserDto userInputModel) {

        UserEntity entity = objectMapper.convertValue(userInputModel, UserEntity.class);

        entity.setUserType(UserType.CUSTOMER); // Default or set accordingly

        Set<RoleEntity> roleEntities = new HashSet<>();

        roleEntities.add(roleRepository.findByRoleName("ROLE_USER_NON_VALIDATED"));

        entity.setRoles(roleEntities);

        entity.setActive(true);

        // Encode password before saving
        String encoded = passwordEncoder.encode(entity.getPassword());
        entity.setPassword(encoded);

        userRepository.save(entity);

        return userInputModel;
    }

    @Override
    public UserDto login(String usernameOrEmail, String password) {
        UserEntity user = userRepository.findByUsernameAndPassword(usernameOrEmail, password);
        if (user == null) {
            user = userRepository.findByEmailAndPassword(usernameOrEmail, password);
        }
        if (user == null) {
            return null; // or throw new RuntimeException("Invalid credentials");
        }
        return objectMapper.convertValue(user, UserDto.class);
    }

    @Override
    public UserDto findUserByEmailOrUserId(String email, String userId) {
        UserEntity user = userRepository.findByEmailOrUserId(email, userId);
        if (user == null) {
            return null;
        }
        return objectMapper.convertValue(user, UserDto.class);
    }

    @Override
    public UserDto findUserByEmail(String email) {
        UserEntity user = userRepository.findByEmail(email);
        if (user == null) {
            return null;
        }
        return objectMapper.convertValue(user, UserDto.class);
    }

    @Override
    public UserDto findUserByUserId(String userId) {
        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) {
            return null;
        }
        return objectMapper.convertValue(user, UserDto.class);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserEntity userEntity = null;
        if (UserServiceUtils.isEmail(username)) {
            userEntity = userRepository.findByEmail(username);
        } else {
            userEntity = userRepository.findByUserId(username);
        }
        if (userEntity == null) {
            throw new UsernameNotFoundException(username);
        }

//        if (!userEntity.isOtpValidated()) {
//            throw new OtpNotVerifiedException("OTP verification failed");
//        }

        return new CustomUserDetails(username, userEntity.getPassword(), userEntity.isActive(), true, true, true, List.of(new SimpleGrantedAuthority("ROLE_" + userEntity.getUserType().name())), userEntity.isOtpValidated());
    }

    @Override
    public void verifyOtp(String userId, OtpDto otpDto) {

        UserEntity userEntity = userRepository.findByUserId(userId);

        if ( isOtpValid(userEntity.getEmail(), otpDto.getOtp()) ) {
            userEntity.setOtpValidated(true);
            userEntity.getRoles().clear();
            userEntity.getRoles().add(roleRepository.findByRoleName("ROLE_USER_VALIDATED"));
            userRepository.save(userEntity);
            return;
        }

       throw new UserServiceExceptions("OTP verification failed");
    }

    @Override
    public void createOtp(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        userEntity.setOtp(generateOtp());
        userRepository.save(userEntity);

        if( otpCacheEnabled ) {
            saveInValkey(userEntity.getEmail(), userEntity.getOtp());
        }
        // Publish OTP to SNS topic after saving it to the database, so that the OTP is available for sending in the email.
        otpPublisher.publishOtp(OtpPublisherModel.builder()
                .userName(userEntity.getUsername())
                .senderEmail(userEntity.getEmail())
                .otp(userEntity.getOtp())
                .build());
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = random.nextInt(1_000_000); // Generates a number from 0 to 999999
        return String.format("%06d", otp);// Pads with leading zero
    }

    private boolean isOtpValid(String emailId, String otp) {

        if (otpCacheEnabled) {
            String storedOtp = getFromValkey(emailId);
            return storedOtp != null && storedOtp.equals(otp);
        } else {
            UserEntity userEntity = userRepository.findByEmail(emailId);
            return userEntity != null && Objects.equals(userEntity.getOtp(), otp);
        }


    }

    private void saveInValkey(String emailId, String otp) {
        valkeyService.saveOtp(emailId, otp);
    }

    private String getFromValkey(String emailId) {
        return valkeyService.getOtp(emailId);
    }

    private void deleteFromValkey(String emailId) {
        valkeyService.deleteOtp(emailId);
    }

    // Additional method to get the ValkeyService bean, throwing an exception if it's not available when OTP cache is enabled
    private ValkeyService getValkeyService() {
        ValkeyService service = valkeyServiceProvider.getIfAvailable();

        if (service == null) {
            throw new IllegalStateException(
                    "OTP cache is enabled, but no ValkeyService bean is configured");
        }

        return service;
    }

}
