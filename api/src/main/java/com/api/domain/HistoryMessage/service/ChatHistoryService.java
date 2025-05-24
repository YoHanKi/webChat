package com.api.domain.HistoryMessage.service;

import com.api.domain.HistoryMessage.entity.HistoryMessageEntity;
import com.api.domain.HistoryMessage.model.ModifyHistoryMessageRequest;
import com.api.domain.HistoryMessage.model.SearchHistoryMessageRequest;
import com.api.domain.HistoryMessage.model.SelectHistoryMessageForAdminDTO;
import com.api.domain.HistoryMessage.repository.HistoryMessageRepository;
import com.api.domain.HistoryMessage.repository.jooq.HistoryMessageDSLRepository;
import com.api.domain.chat.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatHistoryService {
    private final HistoryMessageRepository historyMessageRepository;
    private final HistoryMessageDSLRepository historyMessageDSLRepository;


    /**
     * 비동기적으로 채팅 메시지를 영속화합니다.
     * @param msg 채팅 메시지
     */
    @Async("chatPersistenceExecutor")
    public void asyncPersistChatHistory(ChatMessage msg) {
        historyMessageRepository.save(HistoryMessageEntity.builder()
                .roomId(msg.getRoomId())
                .sender(msg.getSender())
                .content(msg.getContent())
                .build());
    }

    @Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
    public Page<SelectHistoryMessageForAdminDTO> findAllBySearchCondition(SearchHistoryMessageRequest search, Pageable pageable) {
        return historyMessageDSLRepository.findAllBySearchCondition(search, pageable);
    }

    @Transactional(readOnly = true)
    public SelectHistoryMessageForAdminDTO findById(Long id) {
        return historyMessageRepository.findById(id)
                .map(SelectHistoryMessageForAdminDTO::new)
                .orElseThrow(() -> new IllegalArgumentException("채팅 기록을 찾을 수 없습니다."));
    }

    public void modifyHistoryMessage(ModifyHistoryMessageRequest request) {
        historyMessageRepository.findById(request.id())
                .ifPresentOrElse(historyMessage -> {
                    historyMessage.update(request.content(), request.sender());
                    historyMessageRepository.save(historyMessage);
                }, () -> {
                    throw new IllegalArgumentException("채팅 기록을 찾을 수 없습니다.");
                });
    }

    public void deleteHistoryMessage(Long id) {
        historyMessageRepository.findById(id)
                .ifPresentOrElse(historyMessageRepository::delete, () -> {
                    throw new IllegalArgumentException("채팅 기록을 찾을 수 없습니다.");
                });
    }
}