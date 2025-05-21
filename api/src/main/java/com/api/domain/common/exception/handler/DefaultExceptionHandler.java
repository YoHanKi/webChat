package com.api.domain.common.exception.handler;

import com.api.domain.common.exception.model.ApiError;
import org.springframework.stereotype.Component;

@Component
public class DefaultExceptionHandler
        extends AbstractExceptionHandler<Exception> {

    public DefaultExceptionHandler() {
        super(Exception.class);
    }

    @Override
    protected ApiError createError(Exception e) {
        return new ApiError(500, "알 수 없는 오류가 발생했습니다.");
    }
}