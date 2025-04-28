package com.api.controller;

import com.api.Service.RedisPublisher;
import com.api.model.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {
    private final RedisPublisher redisPublisher;

    public ChatController(RedisPublisher redisPublisher) {
        this.redisPublisher = redisPublisher;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage message) {
        // Redis Pub/Sub으로 발행
        redisPublisher.publish(message);
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage message) {
        message.setType(ChatMessage.MessageType.JOIN);
        redisPublisher.publish(message);
    }
}