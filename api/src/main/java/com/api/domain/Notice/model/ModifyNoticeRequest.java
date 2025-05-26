package com.api.domain.Notice.model;

public record ModifyNoticeRequest(
        Long id,
        String title,
        String content,
        String author,
        boolean isMainNotice
) {

    public ModifyNoticeRequest {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("id must be greater than 0");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be null or empty");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content must not be null or empty");
        }
        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException("author must not be null or empty");
        }
    }

    public static ModifyNoticeRequest of(Long id, String title, String content, String author, boolean isMainNotice) {
        return new ModifyNoticeRequest(id, title, content, author, isMainNotice);
    }
}
