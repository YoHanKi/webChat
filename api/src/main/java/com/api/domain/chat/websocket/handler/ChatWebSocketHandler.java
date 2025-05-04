package com.api.domain.chat.websocket.handler;

import com.api.domain.chat.model.ChatMessage;
import com.api.domain.chat.redis.service.RedisPublisher;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final RedisPublisher redisPublisher;
    private final RedisTemplate<String, ChatMessage> redisTemplate;
    Map<String, Set<WebSocketSession>> sessionsByRoom = new ConcurrentHashMap<>();

    /**
     * WebSocket 연결이 수립되면 호출되는 메서드.
     * @param session WebSocketSession
     */
    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {;
        // 1) roomId 파싱 (쿼리 파라미터로 전달된 값)
        String roomId = getRoomIdFromHandshake(session);

        sessionsByRoom.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet())
                .add(session);

        String historyKey = "chat_history:" + roomId;

        // 2) Redis에서 히스토리 조회
        ListOperations<String, ChatMessage> ops = redisTemplate.opsForList();
        List<ChatMessage> history = ops.range(historyKey, 0, -1);
        if (history != null && !history.isEmpty()) {
            Collections.reverse(history);
            for (ChatMessage past : history) {
                session.sendMessage(new TextMessage(past.toJson()));
            }
        }
    }

    /**
     * WebSocket 메시지를 수신하면 호출되는 메서드.
     * @param session WebSocketSession
     * @param message 수신한 메시지
     */
    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws IOException {
        ChatMessage chat = ChatMessage.fromJson(message.getPayload());

        if (chat.getType() == ChatMessage.MessageType.JOIN) {
            // 1) 세션에 username 저장
            session.getAttributes().put("username", chat.getSender());
            session.getAttributes().put("roomId", chat.getRoomId());

            // 2) 입장 메시지를 모든 클라이언트에 알림
            chat = new ChatMessage(
                    ChatMessage.MessageType.JOIN,
                    chat.getSender(),
                    chat.getSender() + "님이 입장하셨습니다.",
                    chat.getRoomId()
            );
        }

        // CHAT / LEAVE 등 나머지 메시지는 기존 로직 그대로 Redis에 발행
        redisPublisher.publish(chat);
    }

    /**
     * WebSocket 연결이 종료되면 호출되는 메서드.
     * @param session WebSocketSession
     * @param status 종료 상태
     */
    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws IOException {
        String username = (String) session.getAttributes().get("username");
        String roomId = (String) session.getAttributes().get("roomId");

        sessionsByRoom.getOrDefault(roomId, Set.of())
                .remove(session);

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

    /**
     * Redis에 발행된 메시지를 수신하여 같은 방에 존재하는 WebSocket 세션으로 전송.
     * @param chat Redis에서 발행된 메시지
     */
    public void broadcast(ChatMessage chat) {
        TextMessage packet = new TextMessage(chat.toJson());
        sessionsByRoom.getOrDefault(chat.getRoomId(), Set.of())
                .forEach(sess -> {
                    if (sess.isOpen()) {
                        try {
                            sess.sendMessage(packet);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

    /**
     * WebSocketSession의 URI 쿼리 파라미터에서 roomId 값을 꺼내 리턴.
     */
    private String getRoomIdFromHandshake(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null || uri.getQuery() == null) {
            throw new IllegalArgumentException("WebSocket URI or query string is null");
        }
        String[] params = uri.getQuery().split("&");
        for (String param : params) {
            String[] kv = param.split("=", 2);
            if (kv.length == 2 && "roomId".equals(kv[0])) {
                return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            }
        }
        throw new IllegalArgumentException("roomId parameter is missing in WebSocket URI");
    }

}