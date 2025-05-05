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
    private Long id;
    private String name;
    private String description;
    private String creatorName;
    private Integer currentCapacity;
    private Integer maxCapacity;
    private boolean deleted;

    public static ResponseReadRoomDTO convert(RoomEntity room) {
        return ResponseReadRoomDTO.builder()
                .id(room.getRoomId())
                .name(room.getRoomName())
                .description(room.getRoomDescription())
                .creatorName(room.getCreator().getUsername())
                .currentCapacity(room.getCurrentCapacity())
                .maxCapacity(room.getMaxCapacity())
                .deleted(room.isDeleted())
                .build();
    }

    public void updateCurrentCapacity(int currentCapacity) {
        this.currentCapacity = currentCapacity;
    }
}
