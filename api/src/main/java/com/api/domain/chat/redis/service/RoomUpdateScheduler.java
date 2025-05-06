package com.api.domain.chat.redis.service;

import com.api.domain.chat.model.ChatMessage;
import com.api.domain.room.service.RoomService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomUpdateScheduler {
    private static final String STREAM_KEY = "room:events";
    private static final String GROUP      = "room-events-group";

    private final RedisTemplate<String, ChatMessage> redisTemplate;
    private final RoomService roomService;
    private final String consumerName = UUID.randomUUID().toString();

    /**
     * 애플리케이션 시작 시점에 Consumer Group이 없으면 생성
     */
    @PostConstruct
    public void initGroup() {
        try {
            redisTemplate.opsForStream().createGroup(STREAM_KEY, GROUP);
        } catch (Exception ignored) {
            // 그룹이 이미 존재하면 무시
            log.info("Redis Stream Group already exists: {}", GROUP);
        }
    }

    /**
     * Redis Stream에서 메시지를 읽어와 처리하는 메서드
     * 1초마다 실행
     */
    @Scheduled(fixedDelay = 1000)
    public void processStream() {
        try {
            // Consumer 구성
            Consumer consumer = Consumer.from(GROUP, consumerName);

            // Stream 옵션 설정
            StreamReadOptions options = StreamReadOptions.empty()
                    .count(100)
                    .block(Duration.ofMillis(500));

            // 마지막 ACK 부터 읽기
            StreamOffset<String> offset = StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed());

            // Redis Stream에서 메시지 읽기
            List<ObjectRecord<String, ChatMessage>> read = redisTemplate.opsForStream().read(
                    ChatMessage.class,
                    consumer,
                    options,
                    offset
            );

            if (read != null && !read.isEmpty()) {
                for (ObjectRecord<String, ChatMessage> record : read) {
                    // 레코드 ID
                    RecordId recordId = record.getId();

                    // 메시지 처리
                    ChatMessage message = record.getValue();
                    log.debug("Processing message: {}", message);

                    // 방 인원 수 관련 서버 반영
                    boolean updated = roomService.updateRoomUser(message);


                    if (!updated && message.getType() != ChatMessage.MessageType.CHAT) {
                        log.warn("Failed to update room user count for message: {}", message);
                    }

                    // ACK
                    redisTemplate.opsForStream()
                            .acknowledge(STREAM_KEY, GROUP, recordId);
                }
            }
        } catch (Exception e) {
            // 스케줄러는 에러 발생 시 스레드가 멈추기 때문에 예외 처리 필요
            log.error("Error processing Redis Stream: {}", e.getMessage());
        }
    }
}
