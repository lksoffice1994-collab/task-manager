package com.learning.service;

import com.learning.dto.LoginRequest;
import com.learning.dto.UserDTO;

import java.util.Map;

public interface UserService {

    void userRegister(UserDTO userDTO);

    Map<String, String> userLogin(LoginRequest loginRequest);

    void createOtp(String id);

    void validateOtp(String id, String otp);

}
