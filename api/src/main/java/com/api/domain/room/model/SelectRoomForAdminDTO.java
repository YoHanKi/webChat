package com.api.domain.room.model;

import com.api.common.utils.DateUtil;
import com.api.domain.room.entity.RoomEntity;

public record SelectRoomForAdminDTO(
        Long id,
        String roomName,
        String roomDescriptions,
        String creator,
        boolean isDeleted,
        String createdAt
) {
    public SelectRoomForAdminDTO {
        if (roomName == null) roomName = "";
        if (roomDescriptions == null) roomDescriptions = "";
        if (creator == null) creator = "";
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
