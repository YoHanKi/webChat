package com.api.domain.chat.Service;

import com.api.domain.chat.model.ChatMessage;
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

    /**
     * 채팅 메시지를 Redis Pub/Sub 토픽에 발행하고, 룸별 히스토리를 Redis List에 저장합니다.
     */
    public void publish(ChatMessage message) {
        // 1) 메시지를 JSON 문자열로 변환해 토픽으로 발행
        redisTemplate.convertAndSend(topic.getTopic(), message.toJson());

        // 2) 룸별 채팅 기록을 왼쪽으로 push하고, 최대 100개까지만 보관
        ListOperations<String, ChatMessage> ops = redisTemplate.opsForList();
        String historyKey = "chat_history:" + message.getRoomId();
        ops.leftPush(historyKey, message);
        ops.trim(historyKey, 0, 99);
    }
}