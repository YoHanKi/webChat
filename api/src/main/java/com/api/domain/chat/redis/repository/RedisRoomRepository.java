package com.api.domain.chat.redis.repository;

import com.api.domain.chat.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RedisRoomRepository {
    private final RedisTemplate<String, ChatMessage> redisTemplate;
    private final RedisTemplate<String, String> allowanceRedisTemplate;
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

    /** 1) 신규 방 생성 (Hash) */
    public void addRoom(long roomId, int maxCapacity) {
        String key = "room:allowance:" + roomId;
        allowanceRedisTemplate.delete(key);  // 기존 value 키가 있으면 제거
        allowanceRedisTemplate.opsForHash().put(key, "currentCapacity", "0");
        allowanceRedisTemplate.opsForHash().put(key, "maxCapacity",     String.valueOf(maxCapacity));
    }

    /** 2) 최대 인원 변경 */
    public void updateMaxCapacity(long roomId, int maxCapacity) {
        String key = "room:allowance:" + roomId;
        if (allowanceRedisTemplate.hasKey(key)) {
            allowanceRedisTemplate.opsForHash().put(key, "maxCapacity", String.valueOf(maxCapacity));
        }
    }

    /** 3) 방 삭제 */
    public void deleteRoom(long roomId) {
        allowanceRedisTemplate.delete("room:allowance:" + roomId);
    }

    /** 4) Lua 스크립트 실행 (증가 or -1) */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Long increaseCurrentCapacity(long roomId, long increment) {
        String key = "room:allowance:" + roomId;
        // ARGV는 반드시 문자열로 넘깁니다.
        return allowanceRedisTemplate.execute(roomAllowanceScript, List.of(key), String.valueOf(increment));
    }
}
