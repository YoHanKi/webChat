package com.api.domain.chat.websocket.handler;

import com.api.common.utils.SocketSessionUtil;
import com.api.domain.chat.model.ChatMessage;
import com.api.domain.chat.redis.service.RedisPublisher;
import com.api.domain.room.exception.RoomFullException;
import com.api.domain.room.service.RoomService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final RoomService roomService;
    private final RedisPublisher redisPublisher;
    private final Map<String, Set<WebSocketSession>> sessionsByRoom = new ConcurrentHashMap<>();

    /**
     * WebSocket 연결이 수립되면 호출되는 메서드.
     * @param session WebSocketSession
     */
    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        // 1) roomId 파싱 (쿼리 파라미터로 전달된 값)
        String roomId = SocketSessionUtil.getRoomIdFromHandshake(session);

        // TODO: 먼저 인원이 찼는지 확인 후 업데이트 해야한다.
        Long current = roomService.updateOnlyCurrentCapacity(Long.valueOf(roomId), 1L);

        if (current == -1) {
            throw new RoomFullException("방이 가득 찼습니다.");
        }

        sessionsByRoom.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet())
                .add(session);

        // 2) Redis에서 히스토리 조회
        List<ChatMessage> history = roomService.getAllChatHistory(roomId);

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
    protected void handleTextMessage(
            @NonNull WebSocketSession session,
            final TextMessage message
    ) {
        ChatMessage chat = ChatMessage.fromJson(message.getPayload());

        if (chat.getType() == ChatMessage.MessageType.JOIN) {
            // 1) 세션에 username 저장
            session.getAttributes().put("username", chat.getSender());
            session.getAttributes().put("roomId", chat.getRoomId());

            // 2) 입장 메시지를 모든 클라이언트에 알림
            chat = switchChatMessageType(ChatMessage.MessageType.JOIN, chat.getSender(), chat.getRoomId());
        } else if (chat.getType() == ChatMessage.MessageType.KICK) {
            // 1) 강퇴 메시지 처리
            chat = switchChatMessageType(ChatMessage.MessageType.KICK, chat);

            // DB 조회하며, 방장인지 확인
            roomService.kickUser(
                    Long.valueOf(chat.getRoomId()),
                    chat.getSender(),
                    session.getAttributes().get("username").toString(),
                    chat.getContent()
            );

            // 2) 강퇴된 사용자의 세션을 종료
            ChatMessage finalChat = chat;
            sessionsByRoom.getOrDefault(chat.getRoomId(), Set.of())
                    .stream()
                    .filter(sess -> finalChat.getContent().equals(sess.getAttributes().get("username")))
                    .findFirst()
                    .ifPresent(sess -> {
                        try {
                            sess.close(CloseStatus.NOT_ACCEPTABLE);
                        } catch (IOException e) {
                            log.error("Error closing session: {}", e.getMessage());
                        }
                    });
        }

        // TODO : 채팅방 인원 목록에 추가 및 RoomEntity를 업데이트 해준다.
         roomService.updateRoomUserCount(chat);

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
            ChatMessage leave = switchChatMessageType(ChatMessage.MessageType.LEAVE, username, roomId);

            // 방 인원 수 감소
            Long current = roomService.updateRoomCurrentCapacity(Long.valueOf(roomId), -1L);

//            if (current == 0) {
//                // 방 삭제 로직
//                roomService.deleteRoom(Long.valueOf(roomId));
//            }

            redisPublisher.publish(leave);
        }
    }

    /**
     * Redis에 발행된 메시지를 수신하여 같은 방에 존재하는 WebSocket 세션으로 전송.
     * @param chat Redis에서 발행된 메시지
     */
    public void broadcast(ChatMessage chat) {
        // 회원 목록 갱신은 JOIN/LEAVE 메시지에 대해서만 수행
        if (chat.getType() == ChatMessage.MessageType.JOIN || chat.getType() == ChatMessage.MessageType.LEAVE) {
            roomService.addRoomEvent(chat);
            chat.setCurrentUserList(roomService.getRoomMembersDetailed(chat.getRoomId()));
        }
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

    private ChatMessage switchChatMessageType(ChatMessage.MessageType type, String sender, String roomId) {
        return switchChatMessageType(type, new ChatMessage(type, sender, "", roomId));
    }

    private ChatMessage switchChatMessageType(ChatMessage.MessageType type, ChatMessage chat) {
        if (type == ChatMessage.MessageType.JOIN) {
            return new ChatMessage(
                    ChatMessage.MessageType.JOIN,
                    chat.getSender(),
                    chat.getSender() + "님이 입장하셨습니다.",
                    chat.getRoomId()
            );
        } else if (type == ChatMessage.MessageType.LEAVE) {
            return new ChatMessage(
                    ChatMessage.MessageType.LEAVE,
                    chat.getSender(),
                    chat.getSender() + "님이 퇴장하셨습니다.",
                    chat.getRoomId()
            );
        } else if (type == ChatMessage.MessageType.KICK) {
            return new ChatMessage(
                    ChatMessage.MessageType.KICK,
                    chat.getSender(),
                    chat.getSender() + "님이 " + chat.getContent() + "님을 " + "강퇴하였습니다.",
                    chat.getRoomId()
            );
        }
        return chat;
    }
}