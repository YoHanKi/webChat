package com.api.domain.chat.redis.repository;

import com.api.domain.chat.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
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
    private final RedisTemplate<String, String> stringRedisTemplate;
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
     * 신규 방 생성 (Hash)
     */
    public void addRoom(long roomId, int maxCapacity) {
        String key = "room:allowance:" + roomId;
        allowanceRedisTemplate.delete(key);  // 기존 value 키가 있으면 제거
        allowanceRedisTemplate.opsForHash().put(key, "currentCapacity", "0");
        allowanceRedisTemplate.opsForHash().put(key, "maxCapacity",     String.valueOf(maxCapacity));
    }

    /**
     * 방에 인원 목록 갱신
     */
    public int switchRoomAndGetCurrentCapacity(ChatMessage message) {
        String roomKey = "room:members:" + message.getRoomId();
        String userId  = message.getSender();  // 혹은 userId 필드

        switch (message.getType()) {
            case JOIN:
                // JOIN → 멤버 셋에 추가
                stringRedisTemplate.opsForSet().add(roomKey, userId);
                break;

            case LEAVE:
                // LEAVE → 멤버 셋에서 제거
                stringRedisTemplate.opsForSet().remove(roomKey, userId);
                break;

            case KICK:
                // KICK → 멤버 셋에서 제거
                String kickUserId = message.getContent();
                stringRedisTemplate.opsForSet().remove(roomKey, kickUserId);
                break;

            default:
                // 그 외의 경우는 무시
                return -1;
        }

        // 원자적 Set 연산 뒤에 바로 카드 산출
        Long count = stringRedisTemplate.opsForSet().size(roomKey);
        return (count != null ? count.intValue() : 0);
    }

    /**
     * 현재 인원 변경
     */
    public void updateCurrentCapacity(long roomId, long currentCapacity) {
        String key = "room:allowance:" + roomId;
        if (allowanceRedisTemplate.hasKey(key)) {
            allowanceRedisTemplate.opsForHash().put(key, "currentCapacity", String.valueOf(currentCapacity));
        }
    }

    /**
     * 최대 인원 변경
     */
    public void updateMaxCapacity(long roomId, int maxCapacity) {
        String key = "room:allowance:" + roomId;
        if (allowanceRedisTemplate.hasKey(key)) {
            allowanceRedisTemplate.opsForHash().put(key, "maxCapacity", String.valueOf(maxCapacity));
        }
    }

    /**
     * 방 삭제
     */
    public void deleteRoom(long roomId) {
        allowanceRedisTemplate.delete("room:allowance:" + roomId);
    }

    /**
     * Lua 스크립트 실행 (증가 or -1)
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Long increaseCurrentCapacity(long roomId, long increment) {
        String key = "room:allowance:" + roomId;
        // ARGV는 반드시 문자열로 넘깁니다.
        return allowanceRedisTemplate.execute(roomAllowanceScript, List.of(key), String.valueOf(increment));
    }

    /**
     * 방 이벤트를 Redis Stream에 ObjectRecord로 추가합니다.
     */
    public void addRoomEvent(ChatMessage message) {
        String streamKey = "room:events";

        // ChatMessage 객체 자체를 레코드 값으로 직렬화해서 저장
        ObjectRecord<String, ChatMessage> record = StreamRecords
                .objectBacked(message)          // V 타입이 ChatMessage
                .withStreamKey(streamKey);      // 스트림 키 지정

        // RedisTemplate<String,ChatMessage>의 StreamOperations 사용
        redisTemplate.opsForStream().add(record);
    }
}
