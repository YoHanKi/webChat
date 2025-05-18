package com.api.domain.user.model;

import java.time.LocalDateTime;

public record SelectUserForAdminDTO(
        Long id,
        String username,
        String role,
        LocalDateTime createDate,
        LocalDateTime modifiedDate
) {
    public SelectUserForAdminDTO {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("id must be greater than 0");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be null or empty");
        }
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("role must not be null or empty");
        }
    }

    public static SelectUserForAdminDTO of(
            Long id,
            String username,
            String role,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new SelectUserForAdminDTO(id, username, role, createdAt, updatedAt);
    }
}
