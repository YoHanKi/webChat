package com.api.domain.common.exception.handler;

import com.api.domain.common.exception.model.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.stereotype.Component;
import org.springframework.validation.ObjectError;

import java.util.stream.Collectors;

@Component
public class ValidationExceptionHandler
        extends AbstractExceptionHandler<MethodArgumentNotValidException> {

    public ValidationExceptionHandler() {
        super(MethodArgumentNotValidException.class);
    }

    @Override
    protected ApiError createError(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().stream()
            .map(ObjectError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        return new ApiError(HttpStatus.BAD_REQUEST.value(), "Validation failed: " + message);
    }
}