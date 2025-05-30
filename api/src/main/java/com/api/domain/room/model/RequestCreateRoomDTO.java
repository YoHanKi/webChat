package com.api.domain.room.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestCreateRoomDTO {
    private String name;
    private String description;
    private Integer maxCapacity;
}
