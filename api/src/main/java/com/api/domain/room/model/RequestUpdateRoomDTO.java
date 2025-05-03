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

    public static RequestUpdateRoomDTO of(String roomName, String roomDescription) {
        return new RequestUpdateRoomDTO(roomName, roomDescription);
    }
}
