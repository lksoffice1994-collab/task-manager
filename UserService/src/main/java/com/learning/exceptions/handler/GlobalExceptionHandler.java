package com.learning.exceptions.handler;

import com.learning.exceptions.UserServiceExceptions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        ErrorResponse response = new ErrorResponse(
                "VALIDATION_ERROR_CODE",
                "Input validation failed",
                fieldErrors
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserServiceExceptions.class)
    public ResponseEntity<ErrorResponse> handleUserServiceExceptions(UserServiceExceptions ex) {
        ErrorResponse response = new ErrorResponse(
            "BUSINESS_ERROR",       // or a specific code you prefer
            ex.getMessage(),
            null                   // No field errors map, just general error
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
}
