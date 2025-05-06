package com.api.domain.room.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestKickUserDTO {
    private String username; // 방에서 퇴장할 사용자 ID
    private Long roomId; // 방 ID
    private String reason; // 퇴장 사유

    public static RequestKickUserDTO of(String username, Long roomId, String reason) {
        return RequestKickUserDTO.builder()
                .username(username)
                .roomId(roomId)
                .reason(reason)
                .build();
    }
}
