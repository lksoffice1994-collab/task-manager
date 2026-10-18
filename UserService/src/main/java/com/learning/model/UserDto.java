package com.learning.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.learning.entity.UserType;
import com.learning.exceptions.validation.ValidEmailId;
import com.learning.exceptions.validation.ValidUserId;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Validated
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {

    @NotBlank(message = "User Id can not be null")
    @ValidUserId(message = "User Id already Exist")
    private String userId;

    @NotBlank(message = "Username can not be null")
    private String username;

    @NotBlank(message = "Email Id can not be null")
    @Email(message = "Please add valid email Id")
    @ValidEmailId(message = "email Id already Exist")
    private String email;

    @Pattern(regexp = "\\d{10}", message = "Contact number must be 10 digits")
    private String contactNumber;

    @NotBlank(message = "Password can not be null")
    @Size(min = 6, max = 10, message = "Password size should be between 6 to 10")
    private String password;
    private String address;

    @Enumerated(EnumType.STRING)
    private UserType userType;

    private Set<RoleDto> roles;

    private String otp;
}
