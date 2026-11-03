package com.learning.controller;

import com.learning.dto.LoginRequest;
import com.learning.dto.UserDTO;
import com.learning.exceptions.UserValidationExceptions;
import com.learning.service.UserService;
import feign.FeignException;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;


@Controller
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/signup")
    public String showSignUpForm(Model model) {
        log.info("Accessed Sign Up Page");
        model.addAttribute("user", new UserDTO());
        return "signup";
    }

    @PostMapping("/signup")
    public String handleSignUp(@Valid @ModelAttribute UserDTO userDTO, BindingResult result, Model model) {
        log.info("Handling Sign Up - userDTO: {}", userDTO);
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

        log.info("User registered successfully, redirecting to OTP confirmation for userId: {}", userDTO.getUserId());
        return "redirect:/otp-confirmation?userId=" + userDTO.getUserId();
    }

    @GetMapping("/signup-success")
    public String showSuccessPage() {
        return "signup-success";
    }
    @GetMapping("/signin")
    public String showSignInForm(Model model) {
        log.info("Accessed Sign In Page");
        model.addAttribute("user", new UserDTO());
        return "signin";
    }

    @PostMapping("/signin")
    public String handleSignIn(@RequestParam String loginId,
                               @RequestParam String password,
                               Model model) {

        try {
            log.info("Handling Sign In - loginId: {}", loginId);
            Map<String, String> map = userService.userLogin(LoginRequest.builder()
                    .usernameOrEmail(loginId).password(password).build());

            if (map.get("role").equals("ROLE_USER_NON_VALIDATED")) {
                return "redirect:/otp-confirmation?userId=" + loginId + "&jwt=" + map.get("JwtToken");
            }
            log.info("User logged in successfully, redirecting to tasks for userId: {}", loginId);
            return "redirect:/tasks?userId=" + loginId + "&jwt=" + map.get("JwtToken");

        } catch (FeignException e) {
            if (e.status() == HttpStatus.UNAUTHORIZED.value() || e.status() == HttpStatus.FORBIDDEN.value()) {
                model.addAttribute("error", "Invalid email/user ID or password.");
                log.warn("Sign In failed for loginId: {}. Reason: Invalid credentials.", loginId);
                return "signin";
            } else {
                log.error("Sign In failed for loginId: {}. Reason: {}", loginId, e.getMessage());
                throw e;
            }
        }
    }

    @GetMapping("/home")
    public String showHome(@RequestParam String username, Model model) {
        log.info("Accessed Home Page for username: {}", username);
        model.addAttribute("username", username);
        return "home";
    }

    @GetMapping("/otp-confirmation")
    public String showOtpPage(//@RequestParam("username") String username,
                              @RequestParam("userId") String userId,
                              //@RequestParam("jwt") String jwtToken,
                              Model model) {
        log.info("Accessed OTP Confirmation Page for userId: {}", userId);
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

        log.info("Handling OTP Confirmation - userId: {}, otp: {}, jwt: {}", userId, otp, jwtToken);
        userService.validateOtp(userId, otp);

        return "redirect:/signup-success";
    }

    @GetMapping("/logout")
    public String showLogout() {
        log.info("Accessed Logout Page");
        return "logout";
    }
}
