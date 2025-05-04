package com.api.domain.chat.redis.repository;

import com.api.domain.chat.model.ChatMessage;
import com.api.domain.room.exception.RoomFullException;
import com.api.domain.room.model.RoomAllowance;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RedisRoomRepository {
    private final RedisTemplate<String, ChatMessage> redisTemplate;
    private final RedisTemplate<String, RoomAllowance> allowanceRedisTemplate;
    private final RedisScript<Long> roomAllowanceScript;

    /**
     * 과거 채팅 기록을 Redis List에서 가져옵니다.
     */
    public List<ChatMessage> getChatHistory(String roomId, int offset, int limit) {
        ListOperations<String, ChatMessage> ops = redisTemplate.opsForList();
        String historyKey = "chat_history:" + roomId;

        // Redis List에서 offset부터 limit까지의 메시지를 가져옴
        return ops.range(historyKey, offset, offset + limit - 1);
    }

    /**
     * 과거 채팅 기록을 Redis List 모두 가져옵니다.
     */
    public List<ChatMessage> getAllChatHistory(String roomId) {
        return this.getChatHistory(roomId, 0, 0);
    }

    /**
     * 새로운 방에 대한 정보를 추가합니다.
     */
    public void addRoom(long roomId, int maxCapacity) {
        String key = "room:allowance:" + roomId;
        RoomAllowance roomAllowance = new RoomAllowance(0, maxCapacity);

        // Redis에 방 정보 추가
        allowanceRedisTemplate.opsForValue().set(key, roomAllowance);
    }

    /**
     * 방에 대한 최대 인원 수를 업데이트합니다.
     */
    public void updateMaxCapacity(long roomId, int maxCapacity) {
        String key = "room:allowance:" + roomId;
        RoomAllowance roomAllowance = allowanceRedisTemplate.opsForValue().get(key);
        if (roomAllowance != null) {
            roomAllowance.setMaxCapacity(maxCapacity);
            allowanceRedisTemplate.opsForValue().set(key, roomAllowance);
        }
    }

    /**
     * 방을 삭제합니다.
     */
    public void deleteRoom(long roomId) {
        String key = "room:allowance:" + roomId;
        allowanceRedisTemplate.delete(key);
    }

    /**
     * 방 현재 인원 수를 업데이트합니다. (Lua 스크립트 사용)
     */
    public Long updateCurrentCapacity(long roomId, long increment) {
        String key = "room:allowance:" + roomId;
        Long result = allowanceRedisTemplate.execute(roomAllowanceScript, List.of(key), increment);

        if (result == -1) {
            // 최대 용량 도달 처리
            throw new RoomFullException("방이 가득 찼습니다.");
        } else {
            // result 에 증가 후 currentCapacity 값
            return result;
        }
    }
}
