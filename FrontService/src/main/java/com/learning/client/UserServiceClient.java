package com.learning.client;

import com.learning.dto.UserDTO;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import com.learning.dto.LoginRequest;
import com.learning.dto.OtpDto;

@FeignClient(name = "userServiceClient", url = "${user.service.url}")
public interface UserServiceClient {

    @PostMapping(value = "/user", consumes = "application/json")
    UserDTO createUser(@RequestBody UserDTO userDTO);

    @PostMapping(value = "/user/login", consumes = "application/json")
    Response login(@RequestBody LoginRequest loginRequest);

    @GetMapping("/user/otp/{id}")
    void sendOtp(@PathVariable("id") String id);

    @PostMapping("/user/otp/{id}")
    void otpVerify(@PathVariable("id") String id, @RequestBody OtpDto otp);

    @PutMapping(value = "/user/{id}/photo", consumes = "multipart/form-data")
    ResponseEntity<Void> updateProfilePhoto(
            @PathVariable("id") String id,
            @RequestPart("photo") MultipartFile photo,
            @RequestHeader("Authorization") String authorization);

    @GetMapping("/user/{id}/photo")
    ResponseEntity<byte[]> getProfilePhoto(
            @PathVariable("id") String id,
            @RequestHeader("Authorization") String authorization);

}
