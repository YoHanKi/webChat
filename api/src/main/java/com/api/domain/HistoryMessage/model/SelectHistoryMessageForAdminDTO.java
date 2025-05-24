package com.api.domain.HistoryMessage.model;

import com.api.common.utils.DateUtil;
import com.api.domain.HistoryMessage.entity.HistoryMessageEntity;

public record SelectHistoryMessageForAdminDTO(
        Long id,
        String roomId,
        String sender,
        String content,
        String createdAt
) {

    public SelectHistoryMessageForAdminDTO(HistoryMessageEntity entity) {
        this(
                entity.getId(),
                entity.getRoomId(),
                entity.getSender(),
                entity.getContent(),
                DateUtil.dateTimeToString(entity.getCreateDate(), "yyyy-MM-dd")
        );
    }
}
