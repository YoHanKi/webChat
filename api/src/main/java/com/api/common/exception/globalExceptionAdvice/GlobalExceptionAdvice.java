package com.api.common.exception.globalExceptionAdvice;

import com.api.common.exception.handler.ExceptionHandlerStrategy;
import com.api.common.exception.model.ApiError;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionAdvice {
    private final List<ExceptionHandlerStrategy> handlers;

    public GlobalExceptionAdvice(List<ExceptionHandlerStrategy> handlers) {
        this.handlers = handlers;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAll(Exception e) {
        return handlers.stream()
                .filter(h -> h.supports(e))
                .findFirst()
                .orElseThrow()  // 절대 없으면 빈껍데기이므로 보통 DefaultExceptionHandler가 잡음
                .handle(e);
    }
}
