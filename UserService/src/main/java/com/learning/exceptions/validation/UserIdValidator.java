package com.learning.exceptions.validation;

import com.learning.service.UserService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class UserIdValidator implements ConstraintValidator<ValidUserId, String> {

    @Autowired
    private UserService userService;

    @Override
    public boolean isValid(String userId, ConstraintValidatorContext context) {
        return Objects.isNull(userService.findUserByUserId(userId));
    }
}
