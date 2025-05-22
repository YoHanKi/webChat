package com.api.domain.HistoryMessage.model.enums;

public enum SearchHistoryMessageType {
    SENDER, CONTENT, DATE, ROOM_ID, ALL;

    public static SearchHistoryMessageType from(String type) {
        if (type == null) return ALL;
        try {
            return SearchHistoryMessageType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return ALL;
        }
    }
}
