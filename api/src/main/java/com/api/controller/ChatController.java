package com.api.controller;

import com.api.Service.RedisPublisher;
import com.api.Service.RedisSubscriber;
import com.api.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Objects;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final RedisPublisher redisPublisher;
    private final RedisSubscriber redisSubscriber;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage message) {
        // Redis Pub/Sub으로 발행
        redisPublisher.publish(message);
    }

//    @MessageMapping("/chat.addUser")
//    public void addUser(@Payload ChatMessage message,
//                        SimpMessageHeaderAccessor headerAccessor) {
//        Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("username", message.getSender());
//
//        redisSubscriber.onMessage(message);
//    }
}