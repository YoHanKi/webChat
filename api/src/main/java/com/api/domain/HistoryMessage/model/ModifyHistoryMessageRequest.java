package com.api.domain.HistoryMessage.model;

public record ModifyHistoryMessageRequest(
        Long id,
        String content,
        String sender
) {
    public ModifyHistoryMessageRequest {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("id must be greater than 0");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content must not be null or empty");
        }
        if (sender == null || sender.isBlank()) {
            throw new IllegalArgumentException("sender must not be null or empty");
        }
    }

    public static ModifyHistoryMessageRequest of(Long id, String content, String sender) {
        return new ModifyHistoryMessageRequest(id, content, sender);
    }
}
