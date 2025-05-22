package com.api.domain.HistoryMessage.model;

import com.api.domain.HistoryMessage.model.enums.SearchHistoryMessageType;

public record SearchHistoryMessageRequest(
        SearchHistoryMessageType searchHistoryMessageType,
        String searchText,
        String startDate,
        String endDate
) {

    public SearchHistoryMessageRequest {
        if (searchHistoryMessageType == null) searchHistoryMessageType = SearchHistoryMessageType.ALL;
        if (searchText == null) searchText = "";
    }
}
