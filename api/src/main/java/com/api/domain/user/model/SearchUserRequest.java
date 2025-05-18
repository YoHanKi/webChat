package com.api.domain.user.model;

import com.api.domain.user.model.enums.SearchUserType;

public record SearchUserRequest(SearchUserType searchSearchUserType, String searchText) {
    public SearchUserRequest {
        if (searchSearchUserType == null) {
            throw new IllegalArgumentException("searchType must not be null");
        }
        if (searchText == null || searchText.isBlank()) {
            throw new IllegalArgumentException("searchText must not be null or empty");
        }
    }

    public static SearchUserRequest of(SearchUserType searchSearchUserType, String searchText) {
        return new SearchUserRequest(searchSearchUserType, searchText);
    }
}