package com.api.domain.room.model;

public record ModifyRoomRequest(
        Long id,
        String roomName,
        String roomDescription,
        String creator,
        boolean isDeleted
) {
    public ModifyRoomRequest {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("id must be greater than 0");
        }
        if (roomName == null || roomName.isBlank()) {
            throw new IllegalArgumentException("roomName must not be null or empty");
        }
        if (roomDescription == null || roomDescription.isBlank()) {
            throw new IllegalArgumentException("roomDescription must not be null or empty");
        }
        if (creator == null || creator.isBlank()) {
            throw new IllegalArgumentException("creator must not be null or empty");
        }
    }

    public static ModifyRoomRequest of(Long id, String roomName, String roomDescription, String creator, boolean isDeleted) {
        return new ModifyRoomRequest(id, roomName, roomDescription, creator, isDeleted);
    }
}
