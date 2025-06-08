package com.api.common.exception.handler;

import com.api.common.exception.model.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Slf4j
public abstract class AbstractExceptionHandler<T extends Exception> implements ExceptionHandlerStrategy {

    private final List<Class<? extends Exception>> exceptionTypes;

    protected AbstractExceptionHandler(Class<T> exceptionType) {
        this.exceptionTypes = List.of(exceptionType);
    }

    protected AbstractExceptionHandler(List<Class<? extends Exception>> exceptionTypes) {
        this.exceptionTypes = exceptionTypes;
    }

    @Override
    public final boolean supports(Exception e) {
        return exceptionTypes.stream()
                .anyMatch(type -> type.isInstance(e));
    }

    @Override
    public final ResponseEntity<ApiError> handle(Exception e) {
        logException(e);
        ApiError error = createError(e);
        return buildResponse(error);
    }

    /**
     * 공통 로깅
     */
    protected void logException(Exception e) {
        Class<?> exceptionType = e.getClass();
        log.error("[Exception] {}: {}", exceptionType.getSimpleName(), e.getMessage(), NestedExceptionUtils.getMostSpecificCause(e));
        e.printStackTrace();
    }

    /**
     * 서브클래스가 구현할, 예외별 에러 응답 생성
     */
    protected abstract ApiError createError(Exception e);

    /**
     * 공통 응답 빌드
     */
    protected ResponseEntity<ApiError> buildResponse(ApiError error) {
        return ResponseEntity
                .status(error.getStatus())
                .body(error);
    }
}