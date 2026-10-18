package com.learning.exceptions;

import org.springframework.security.core.AuthenticationException;

public class OtpNotVerifiedException extends AuthenticationException {
    public OtpNotVerifiedException(String msg) {
        super(msg);
    }
}
