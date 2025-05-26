package com.api.common.exception.handler;

import com.api.common.exception.model.ApiError;
import org.springframework.http.ResponseEntity;

public interface ExceptionHandlerStrategy {
    boolean supports(Exception e);

    ResponseEntity<ApiError> handle(Exception e);
}