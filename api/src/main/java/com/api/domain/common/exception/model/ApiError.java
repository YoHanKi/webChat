package com.api.domain.common.exception.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ApiError {
    private int status;
    private String message;

    public ApiError(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
