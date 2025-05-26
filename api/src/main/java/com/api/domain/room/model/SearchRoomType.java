package com.api.domain.room.model;

public enum SearchRoomType {
    NAME, DESCRIPTION, DATE, ALL;

    public static SearchRoomType from(String type) {
        if (type == null) return ALL;
        try {
            return SearchRoomType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return ALL;
        }
    }
}
