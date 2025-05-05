package com.api.domain.room.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RoomAllowance {
    private int currentCapacity;
    private int maxCapacity;
}
