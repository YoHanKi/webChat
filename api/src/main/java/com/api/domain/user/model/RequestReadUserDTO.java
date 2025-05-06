package com.api.domain.user.model;


import com.api.domain.roomUser.entity.RoomUserEntity;
import com.api.domain.user.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestReadUserDTO {
    private Long userId;
    private String userName;
    private String userRole;
    private String userStatus; // "ACTIVE", "INACTIVE", "BANNED"
    private String userImageUrl; // 프로필 이미지 URL

    public static RequestReadUserDTO of(UserEntity user) {
        return RequestReadUserDTO.builder()
                .userId(user.getId())
                .userName(user.getUsername())
                .userRole(user.getRole().name())
                // .userStatus(user.getStatus().name()) // 추후 필요 시 상태 추가
                // .userImageUrl(user.getImageUrl()) // // 추후 필요 시 이미지 URL 추가
                .build();
    }

    public static RequestReadUserDTO of(RoomUserEntity roomUser) {
        UserEntity user = roomUser.getUser();
        return RequestReadUserDTO.builder()
                .userId(user.getId())
                .userName(user.getUsername())
                .userRole(user.getRole().name())
                // .userStatus(user.getStatus().name()) // 추후 필요 시 상태 추가
                // .userImageUrl(user.getImageUrl()) // // 추후 필요 시 이미지 URL 추가
                .build();
    }
}
