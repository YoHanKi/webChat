package com.api.domain.Notice.model;

import com.api.domain.Notice.model.enums.SearchNoticeType;

public record SearchNoticeRequest(
        SearchNoticeType searchType,
        String searchText,
        String startDate,
        String endDate
) {
    public SearchNoticeRequest {
        if (searchType == null) searchType = SearchNoticeType.ALL;
        if (searchText == null) searchText = "";
    }

    public static SearchNoticeRequest of(String searchNoticeType, String searchText, String startDate, String endDate) {
        return new SearchNoticeRequest(SearchNoticeType.valueOf(searchNoticeType), searchText, startDate, endDate);
    }
}
