package com.learning.exceptions;

import java.util.List;

public class UserValidationExceptions extends RuntimeException {

    private List<String> fieldErrors;

    public UserValidationExceptions(String message, List<String> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
    }

    public List<String> getFieldErrors() {
        return fieldErrors;
    }
}
