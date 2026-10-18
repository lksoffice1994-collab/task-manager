package com.learning.service.impl;

import com.learning.config.security.CustomUserDetails;
import com.learning.entity.RoleEntity;
import com.learning.entity.UserEntity;
import com.learning.entity.UserType;
import com.learning.exceptions.OtpNotVerifiedException;
import com.learning.exceptions.UserServiceExceptions;
import com.learning.model.OtpDto;
import com.learning.model.UserDto;
import com.learning.repository.RoleRepository;
import com.learning.repository.UserRepository;
import com.learning.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.utils.UserServiceUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public UserServiceImpl(UserRepository userRepository, ObjectMapper objectMapper, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
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

        if (Objects.equals(userEntity.getOtp(), otpDto.getOtp())) {
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
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = random.nextInt(1_000_000); // Generates a number from 0 to 999999
        return String.format("%06d", otp);// Pads with leading zero
    }

}
