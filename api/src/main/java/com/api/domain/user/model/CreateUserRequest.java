package com.api.domain.user.model;

import com.api.domain.user.entity.UserEntity;

public record CreateUserRequest(
        String username,
        String password,
        UserEntity.Role role
) {

    public CreateUserRequest {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or blank");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
    }

    public static CreateUserRequest of(String username, String password, String role) {
        UserEntity.Role userRole = UserEntity.Role.valueOf(role.toUpperCase());
        return new CreateUserRequest(username, password, userRole);
    }
}
