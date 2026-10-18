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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Validated
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserResponseDto {

    private String userId;
    private String username;
    private String email;
    private String contactNumber;
    private String address;
}
