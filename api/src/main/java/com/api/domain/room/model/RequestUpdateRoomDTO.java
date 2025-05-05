package com.api.domain.room.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RequestUpdateRoomDTO {
    private Long roomId;
    private String roomName;
    private String roomDescription;
    private Integer maxCapacity;

    public static RequestUpdateRoomDTO of(Long roomId, String roomName, String roomDescription, Integer maxCapacity) {
        return new RequestUpdateRoomDTO(roomId, roomName, roomDescription, maxCapacity);
    }
}
