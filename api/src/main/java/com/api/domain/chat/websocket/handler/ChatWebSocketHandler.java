package com.api.domain.chat.websocket.handler;

import com.api.domain.chat.redis.service.RedisPublisher;
import com.api.domain.chat.model.ChatMessage;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final RedisPublisher redisPublisher;
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws IOException {
        ChatMessage chat = ChatMessage.fromJson(message.getPayload());

        if (chat.getType() == ChatMessage.MessageType.JOIN) {
            // 1) 세션에 username 저장
            session.getAttributes().put("username", chat.getSender());
            session.getAttributes().put("roomId", chat.getRoomId());

            // 2) 입장 메시지를 모든 클라이언트에 알림
            ChatMessage joinNotice = new ChatMessage(
                    ChatMessage.MessageType.JOIN,
                    chat.getSender(),
                    chat.getSender() + "님이 입장하셨습니다.",
                    chat.getRoomId()
            );
            redisPublisher.publish(joinNotice);
            return;
        }

        // CHAT / LEAVE 등 나머지 메시지는 기존 로직 그대로 Redis에 발행
        redisPublisher.publish(chat);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws IOException {
        sessions.remove(session);
        String username = (String) session.getAttributes().get("username");
        String roomId = (String) session.getAttributes().get("roomId");
        if (username != null) {
            ChatMessage leave = new ChatMessage(
                    ChatMessage.MessageType.LEAVE,
                    username,
                    username + "님이 퇴장하셨습니다.",
                    roomId
            );
            redisPublisher.publish(leave);
        }
    }

    public void broadcast(ChatMessage chat) throws IOException {
        TextMessage packet = new TextMessage(chat.toJson());
        for (WebSocketSession sess : sessions) {
            if (sess.isOpen()) {
                sess.sendMessage(packet);
            }
        }
    }
}