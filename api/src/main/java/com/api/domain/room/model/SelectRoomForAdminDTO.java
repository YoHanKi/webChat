package com.api.domain.room.model;

import com.api.common.utils.DateUtil;
import com.api.domain.room.entity.RoomEntity;

public record SelectRoomForAdminDTO(
        Long id,
        String roomId,
        String roomName,
        String roomType,
        boolean isDeleted,
        String createdAt
) {
    public SelectRoomForAdminDTO {
        if (roomId == null) roomId = "";
        if (roomName == null) roomName = "";
        if (roomType == null) roomType = "";
        if (createdAt == null) createdAt = "";
    }

    public SelectRoomForAdminDTO(RoomEntity entity) {
        this(
                entity.getRoomId(),
                entity.getRoomName(),
                entity.getRoomDescription(),
                entity.getCreator().getUsername(),
                entity.isDeleted(),
                DateUtil.dateTimeToString(entity.getCreateDate(), "yyyy-MM-dd")
        );
    }
}
