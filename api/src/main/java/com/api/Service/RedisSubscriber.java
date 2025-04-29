package com.api.Service;

import com.api.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RedisSubscriber {
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, ChatMessage> redisTemplate;

    // Redis로부터 메시지 수신 시 WebSocket으로 브로드캐스트
    public void onMessage(Object message) {
        if (message instanceof ChatMessage chat) {
            // 1) 과거 기록 로드
            String roomId = chat.getRoomId();
            ListOperations<String, ChatMessage> ops = redisTemplate.opsForList();
            List<ChatMessage> history = ops.range("chat:history:" + roomId, 0, -1);
            if (history != null) {
                for (ChatMessage msg : history) {
                    messagingTemplate.convertAndSend("/topic/" + roomId, msg);
                }
            }

            // 2) JOIN 메시지 브로드캐스트
            messagingTemplate.convertAndSend("/topic/" + roomId, message);
        }
    }
}