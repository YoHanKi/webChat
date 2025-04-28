package com.api.Service;

import com.api.model.ChatMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisSubscriber {
    private final SimpMessagingTemplate messagingTemplate;

    public RedisSubscriber(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // Redis로부터 메시지 수신 시 WebSocket으로 브로드캐스트
    public void onMessage(Object message, byte[] pattern) {
        if (message instanceof ChatMessage chat) {
            String destination = "/topic/" + chat.getRoomId();
            messagingTemplate.convertAndSend(destination, chat);
        }
    }
}