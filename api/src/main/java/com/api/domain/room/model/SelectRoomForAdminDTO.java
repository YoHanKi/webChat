package com.api.domain.room.model;

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
}
