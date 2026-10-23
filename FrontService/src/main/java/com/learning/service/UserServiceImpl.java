package com.learning.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.client.UserServiceClient;
import com.learning.dto.LoginRequest;
import com.learning.dto.OtpDto;
import com.learning.dto.UserDTO;
import com.learning.dto.ValidationErrorResponse;
import com.learning.exceptions.UserValidationExceptions;
import feign.FeignException;
import feign.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Collection;

@Service
public class UserServiceImpl implements UserService {

    @Autowired 
    private UserServiceClient userServiceClient;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void userRegister(UserDTO userDTO) {

        try {
            UserDTO res = userServiceClient.createUser(userDTO);
        } catch (FeignException ex) {

            if (ex.status() == 400) {

                try {
                    ValidationErrorResponse validationErrorResponse =
                            objectMapper.readValue(ex.responseBody().get().array(),ValidationErrorResponse.class);

                    if (Objects.nonNull(validationErrorResponse)
                            && "VALIDATION_ERROR_CODE".equals(validationErrorResponse.getCode())) {

                        throw new UserValidationExceptions("validation error",
                                validationErrorResponse.getFieldErrors().keySet().stream().toList());
                    }
                } catch (IOException e) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    @Override
    public Map<String, String> userLogin(LoginRequest loginRequest) {

        Response res = userServiceClient.login(loginRequest);

        HashMap<String, String> map = new HashMap<>();
        map.put("JwtToken", res.headers().get("authorization").toArray()[0].toString());
        map.put("role", res.headers().get("role").toArray()[0].toString());
        map.put("userId", headerValue(res.headers(), "userId").orElse(loginRequest.getUsernameOrEmail()));

        return map;

    }

    @Override
    public void createOtp(String id) {
        userServiceClient.sendOtp(id);
    }

    @Override
    public void validateOtp(String id, String otp) {
        userServiceClient.otpVerify(id, new OtpDto(otp));
    }

    @Override
    public void updateProfilePhoto(String id, MultipartFile photo, String authorization) {
        userServiceClient.updateProfilePhoto(id, photo, authorization);
    }

    @Override
    public ResponseEntity<byte[]> getProfilePhoto(String id, String authorization) {
        try {
            ResponseEntity<byte[]> response = userServiceClient.getProfilePhoto(id, authorization);
            if (response.getBody() == null || response.getBody().length == 0) {
                return ResponseEntity.noContent().build();
            }
            return response;
        } catch (RuntimeException ex) {
            return ResponseEntity.noContent().build();
        }
    }

    private java.util.Optional<String> headerValue(Map<String, Collection<String>> headers, String name) {
        return headers.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                .map(Map.Entry::getValue)
                .filter(values -> values != null && !values.isEmpty())
                .map(values -> values.iterator().next())
                .findFirst();
    }


}
