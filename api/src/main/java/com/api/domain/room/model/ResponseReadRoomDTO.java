package com.api.domain.room.model;

import com.api.domain.room.entity.RoomEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseReadRoomDTO {
    private Long roomId;
    private String roomName;
    private String roomDescription;
    private String creatorName;
    private boolean isDeleted;

    public static ResponseReadRoomDTO convert(RoomEntity room) {
        return ResponseReadRoomDTO.builder()
                .roomId(room.getRoomId())
                .roomName(room.getRoomName())
                .roomDescription(room.getRoomDescription())
                .creatorName(room.getCreator().getUsername())
                .isDeleted(room.isDeleted())
                .build();
    }
}
