package com.api.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class CustomSlice<T> {
    private final List<T> content;
    private boolean hasNext;
    private boolean hasPrevious;
    private int numberOfElements;
    private int size;
    private int number;

}
