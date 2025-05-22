package com.api.domain.HistoryMessage.model;

public record SelectHistoryMessageForAdminDTO(
        Long id,
        String roomId,
        String sender,
        String content,
        String createdAt
) {
}
