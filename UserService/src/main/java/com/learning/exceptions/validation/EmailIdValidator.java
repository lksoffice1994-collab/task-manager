package com.learning.exceptions.validation;

import com.learning.service.UserService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class EmailIdValidator implements ConstraintValidator<ValidEmailId, String> {

    @Autowired
    private UserService userService;

    @Override
    public boolean isValid(String emailId, ConstraintValidatorContext context) {
        return Objects.isNull(userService.findUserByEmail(emailId));
    }
}
