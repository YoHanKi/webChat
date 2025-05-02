package com.api.domain.chat.redis.service;

import com.api.domain.chat.model.ChatMessage;
import com.api.domain.chat.websocket.handler.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {
    private final ChatWebSocketHandler webSocketHandler;

    /**
     * Redis 토픽에 발행된 메시지를 수신하면,
     * WebSocketHandler를 통해 모든 세션에 브로드캐스트합니다.
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        // Redis에서 넘어온 메시지(바디)를 JSON 문자열로 읽어들임
        String json = new String(message.getBody(), StandardCharsets.UTF_8);
        ChatMessage chat = ChatMessage.fromJson(json);

        // ChatWebSocketHandler에 브로드캐스트를 위임
        try {
            webSocketHandler.broadcast(chat);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}