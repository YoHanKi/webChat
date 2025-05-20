package com.api.domain.user.model;

public record ModifyUserRequest(
        Long id,
        String username,
        String password,
        String role
) {
    public ModifyUserRequest {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("id must be greater than 0");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be null or empty");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("password must not be null or empty");
        }
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("role must not be null or empty");
        }
    }

    public static ModifyUserRequest of(Long id, String username, String password, String role) {
        return new ModifyUserRequest(id, username, password, role);
    }
}
