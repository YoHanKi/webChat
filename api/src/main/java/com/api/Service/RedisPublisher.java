package com.api.Service;

import com.api.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisPublisher {
    private final RedisTemplate<String, ChatMessage> redisTemplate;
    private final ChannelTopic topic;

    public void publish(ChatMessage message) {
        // 1) Pub/Sub으로 브로드캐스트
        redisTemplate.convertAndSend(topic.getTopic(), message);

        // 2) 채팅 기록을 List에 저장 (최대 100건 유지)
        String key = historyKey(message.getRoomId());
        ListOperations<String, ChatMessage> ops = redisTemplate.opsForList();
        ops.rightPush(key, message);
        ops.trim(key, -100, -1);
    }

    // 방별 채팅 기록을 저장할 키 패턴
    private String historyKey(String roomId) {
        return "chat:history:" + roomId;
    }
}