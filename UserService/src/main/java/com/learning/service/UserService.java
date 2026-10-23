package com.learning.service;

import com.learning.model.OtpDto;
import com.learning.model.UserDto;
import com.learning.model.UserProfilePhoto;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import org.springframework.security.core.userdetails.UserDetailsService;


public interface UserService extends UserDetailsService {
    UserDto createUser(UserDto userDTO);
    UserDto login(String usernameOrEmail, String password);

    UserDto findUserByEmailOrUserId(String email, String userId);

    UserDto findUserByEmail(String email);

    UserDto findUserByUserId(String userId);

    void verifyOtp(String userId, OtpDto otpDto);

    void createOtp(String userId);

    void updateProfilePhoto(String userId, MultipartFile photo);

    Optional<UserProfilePhoto> getProfilePhoto(String userId);
}
