package com.api.domain.user.model;

import com.api.domain.user.model.enums.SearchUserType;

public record SearchUserRequest(SearchUserType searchType, String searchText) {
    public SearchUserRequest {
        if (searchType == null) searchType = SearchUserType.ALL;
        if (searchText == null) searchText = "";
    }

    public static SearchUserRequest of(SearchUserType searchSearchUserType, String searchText) {
        return new SearchUserRequest(searchSearchUserType, searchText);
    }
}