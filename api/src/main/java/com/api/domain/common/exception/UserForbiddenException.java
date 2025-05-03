package com.api.domain.common.exception;

import org.springframework.security.core.AuthenticationException;

public class UserForbiddenException extends AuthenticationException {
    public UserForbiddenException(String message) {
        super(message);
    }
}
