package com.api.domain.user.model;

import com.api.domain.user.model.enums.SearchUserType;

public record SearchUserRequest(SearchUserType searchSearchUserType, String searchText) {
    public SearchUserRequest {
        if (searchSearchUserType == null) searchSearchUserType = SearchUserType.ALL;
        if (searchText == null) searchText = "";
    }

    public static SearchUserRequest of(SearchUserType searchSearchUserType, String searchText) {
        return new SearchUserRequest(searchSearchUserType, searchText);
    }
}