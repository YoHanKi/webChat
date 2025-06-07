package com.api.common.exception.handler.implement;

import com.api.common.exception.handler.AbstractExceptionHandler;
import com.api.common.exception.model.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.validation.ObjectError;

import java.util.stream.Collectors;

@Component
public class ValidationExceptionHandler extends AbstractExceptionHandler<MethodArgumentNotValidException> {

    public ValidationExceptionHandler() {
        super(MethodArgumentNotValidException.class);
    }

    @Override
    protected ApiError createError(Exception e) {
        if (e instanceof MethodArgumentNotValidException ex) {
            Assert.notNull(ex.getBindingResult(), "MethodArgumentNotValidException must not be null");

            String message = ex.getBindingResult().getAllErrors().stream()
                    .map(ObjectError::getDefaultMessage)
                    .collect(Collectors.joining(", "));

            return new ApiError(HttpStatus.BAD_REQUEST.value(), "Validation failed: " + message);
        } else {
            return new ApiError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred.");
        }
    }
}