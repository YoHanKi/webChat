package com.api.common.utils;

import com.api.domain.user.entity.UserEntity;

public class RoleUtil {

    public static boolean isManager(UserEntity.Role role) {
        if (role == null) return false;

        return role == UserEntity.Role.MANAGER || role == UserEntity.Role.ADMIN;
    }
}
