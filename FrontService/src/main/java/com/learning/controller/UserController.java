package com.learning.controller;

import com.learning.dto.LoginRequest;
import com.learning.dto.UserDTO;
import com.learning.exceptions.UserValidationExceptions;
import com.learning.service.UserService;
import feign.FeignException;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.util.Map;


@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/signup")
    public String showSignUpForm(Model model) {
        model.addAttribute("user", new UserDTO());
        return "signup";
    }

    @PostMapping("/signup")
    public String handleSignUp(@Valid @ModelAttribute UserDTO userDTO, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("user", userDTO);
            model.addAttribute("error", "Invalid field values");
            return "signup";
        }
        try {
            userService.userRegister(userDTO);
        } catch (UserValidationExceptions e) {
            StringBuilder error = new StringBuilder();
            e.getFieldErrors().forEach(fieldError -> error.append(fieldError).append(","));
            error.append("are invalid");
            model.addAttribute("error", error.toString());
            return "signup";
        }

        //return "forward:/signin?userId=" + userDTO.getUserId() + "&password=" + userDTO.getPassword();

        return "redirect:/otp-confirmation?userId=" + userDTO.getUserId();
    }

    @GetMapping("/signup-success")
    public String showSuccessPage() {
        return "signup-success";
    }
    @GetMapping("/signin")
    public String showSignInForm(Model model) {
        model.addAttribute("user", new UserDTO());
        return "signin";
    }

    @PostMapping("/signin")
    public String handleSignIn(@RequestParam String loginId,
                               @RequestParam String password,
                               Model model) {

        try {
            Map<String, String> map = userService.userLogin(LoginRequest.builder()
                    .usernameOrEmail(loginId).password(password).build());

            if (map.get("role").equals("ROLE_USER_NON_VALIDATED")) {
                return "redirect:/otp-confirmation?userId=" + map.get("userId") + "&jwt=" + map.get("JwtToken");
            }
            return "redirect:/tasks?userId=" + map.get("userId") + "&jwt=" + map.get("JwtToken");

        } catch (FeignException e) {
            if (e.status() == HttpStatus.UNAUTHORIZED.value() || e.status() == HttpStatus.FORBIDDEN.value()) {
                model.addAttribute("error", "Invalid email/user ID or password.");
                return "signin";
            } else {
                throw e;
            }
        }
    }

    @GetMapping("/home")
    public String showHome(@RequestParam String username, Model model) {
        model.addAttribute("username", username);
        return "home";
    }

    @GetMapping("/otp-confirmation")
    public String showOtpPage(//@RequestParam("username") String username,
                              @RequestParam("userId") String userId,
                              //@RequestParam("jwt") String jwtToken,
                              Model model) {
        //model.addAttribute("username", username);
        model.addAttribute("userId", userId);
        // Mock JWT: Replace with real JWT logic as needed
        model.addAttribute("jwtToken", "jwtToken");
        userService.createOtp(userId);
        return "otp-confirmation";
    }

    @PostMapping("/otp-confirmation")
    public String handleOtp(@RequestParam("otp") String otp,
                            @RequestParam("jwt") String jwtToken,
                            @RequestParam("userId") String userId) {
        // Logic to verify OTP, JWT, and user association as needed
//        System.out.println("Received OTP: " + otp);
//        System.out.println("Received JWT: " + jwtToken);
//        System.out.println("Received userId: " + userId);

        userService.validateOtp(userId, otp);

        return "redirect:/signup-success";
    }

    @GetMapping("/logout")
    public String showLogout() {
        return "logout";
    }

    @PutMapping(value = "/profile/photo/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<Void> updateProfilePhoto(
            @org.springframework.web.bind.annotation.PathVariable String id,
            @RequestPart("photo") MultipartFile photo,
            @RequestHeader("Authorization") String authorization) {
        userService.updateProfilePhoto(id, photo, authorization);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profile/photo/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> getProfilePhoto(
            @org.springframework.web.bind.annotation.PathVariable String id,
            @RequestHeader("Authorization") String authorization) {
        ResponseEntity<byte[]> photo = userService.getProfilePhoto(id, authorization);
        if (photo.getBody() == null) {
            return ResponseEntity.noContent().build();
        }
        MediaType contentType = photo.getHeaders().getContentType();
        return ResponseEntity.ok()
                .contentType(contentType == null ? MediaType.APPLICATION_OCTET_STREAM : contentType)
                .body(photo.getBody());
    }
}
