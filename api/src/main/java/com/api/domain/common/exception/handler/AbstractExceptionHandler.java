package com.api.domain.common.exception.handler;

import com.api.domain.common.exception.model.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

@Slf4j
public abstract class AbstractExceptionHandler<T extends Exception> implements ExceptionHandlerStrategy {

    private final Class<T> exceptionType;

    protected AbstractExceptionHandler(Class<T> exceptionType) {
        this.exceptionType = exceptionType;
    }

    @Override
    public final boolean supports(Exception e) {
        return exceptionType.isInstance(e);
    }

    @Override
    public final ResponseEntity<ApiError> handle(Exception e) {
        @SuppressWarnings("unchecked")
        T ex = (T) e;
        logException(ex);
        ApiError error = createError(ex);
        return buildResponse(error);
    }

    /** 공통 로깅 */
    protected void logException(T e) {
        log.error("[Exception] {}: {}", exceptionType.getSimpleName(), e.getMessage(), e);
    }

    /** 서브클래스가 구현할, 예외별 에러 응답 생성 */
    protected abstract ApiError createError(T e);

    /** 공통 응답 빌드 */
    protected ResponseEntity<ApiError> buildResponse(ApiError error) {
        return ResponseEntity
                .status(error.getStatus())
                .body(error);
    }
}