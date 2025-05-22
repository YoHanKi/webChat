package com.api.domain.room.model;

public record SearchRoomRequest(
        SearchRoomType searchRoomType,
        String searchText,
        String startDate,
        String endDate
) {

    public SearchRoomRequest {
        if (searchRoomType == null) searchRoomType = SearchRoomType.ALL;
        if (searchText == null) searchText = "";
    }

    public static SearchRoomRequest of(String searchRoomType, String searchText, String startDate, String endDate) {
        return new SearchRoomRequest(SearchRoomType.valueOf(searchRoomType), searchText, startDate, endDate);
    }
}
