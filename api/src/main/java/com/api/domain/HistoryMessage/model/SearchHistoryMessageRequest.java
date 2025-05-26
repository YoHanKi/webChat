package com.api.domain.HistoryMessage.model;

import com.api.domain.HistoryMessage.model.enums.SearchHistoryMessageType;

public record SearchHistoryMessageRequest(
        SearchHistoryMessageType searchType,
        String searchText,
        String startDate,
        String endDate
) {

    public SearchHistoryMessageRequest {
        if (searchType == null) searchType = SearchHistoryMessageType.ALL;
        if (searchText == null) searchText = "";
    }
}
