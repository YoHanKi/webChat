package com.api.domain.Notice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectNoticeForAdminDTO {
    private Long id;

    private String title;

    private String content;

    private String author;

    private boolean isMainNotice;

    private boolean isDeleted;

    private String createdAt;
}
