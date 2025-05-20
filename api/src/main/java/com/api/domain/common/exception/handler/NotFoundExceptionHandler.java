package com.api.domain.common.exception.handler;

import com.api.domain.common.exception.model.ApiError;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class NotFoundExceptionHandler extends AbstractExceptionHandler<EntityNotFoundException> {

    public NotFoundExceptionHandler() {
        super(EntityNotFoundException.class);
    }

    @Override
    protected ApiError createError(EntityNotFoundException e) {
        return new ApiError(404, e.getMessage());
    }
}