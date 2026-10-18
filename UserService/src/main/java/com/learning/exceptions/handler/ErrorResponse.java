package com.learning.exceptions.handler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
class ErrorResponse {
    private String code;
    private String message;
    private Map<String, String> fieldErrors;
    
}