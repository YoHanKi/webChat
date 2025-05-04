package com.api.domain.room.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RequestUpdateRoomDTO {
    private String roomName;
    private String roomDescription;
    private Integer maxCapacity;

    public static RequestUpdateRoomDTO of(String roomName, String roomDescription, Integer maxCapacity) {
        return new RequestUpdateRoomDTO(roomName, roomDescription, maxCapacity);
    }
}
