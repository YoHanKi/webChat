package com.api.domain.HistoryMessage.service;

import com.api.domain.HistoryMessage.entity.HistoryMessageEntity;
import com.api.domain.HistoryMessage.repository.HistoryMessageRepository;
import com.api.domain.chat.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ChatHistoryPersistenceService {

    private final HistoryMessageRepository repository;

    @Async("chatPersistenceExecutor")
    public void persist(ChatMessage msg) {
        HistoryMessageEntity entity = HistoryMessageEntity.builder()
                .roomId(msg.getRoomId())
                .sender(msg.getSender())
                .content(msg.getContent())
                .build();
        repository.save(entity);
    }
}