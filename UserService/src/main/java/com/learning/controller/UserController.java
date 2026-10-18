package com.learning.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.model.OtpDto;
import com.learning.model.UserResponseDto;
import com.learning.service.UserService;
import com.learning.model.UserDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserDto userDTO) {
        UserDto res =  userService.createUser(userDTO);
        UserResponseDto userResponseDto = objectMapper.convertValue(res, UserResponseDto.class);
        return ResponseEntity.ok(userResponseDto);
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or principal == #id")
    public ResponseEntity<UserResponseDto> getUser(
            @PathVariable String id,
            @RequestHeader(value = "${authorization.token.header.name:Authorization}", required = false) String jwtToken) {
        // Example: fetch user by id and map to UserDto
        UserDto userDto = userService.findUserByUserId(id);

        UserResponseDto userResponseDto = objectMapper.convertValue(userDto, UserResponseDto.class);

        return ResponseEntity.ok(userResponseDto);
    }

    @GetMapping("/otp/{id}")
    // @PreAuthorize("principal == #id")
    public ResponseEntity<Void> sendOtp(@PathVariable String id) {
        userService.createOtp(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/otp/{id}")
    // @PreAuthorize("principal == #id")
    public ResponseEntity<Void> otpVerify(@PathVariable String id, @RequestBody OtpDto otp) {
        userService.verifyOtp(id, otp);
        return ResponseEntity.ok().build();
    }
}
