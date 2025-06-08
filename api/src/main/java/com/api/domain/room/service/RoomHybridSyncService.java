package com.api.domain.room.service;


import com.api.common.exception.UserForbiddenException;
import com.api.common.model.CustomSlice;
import com.api.common.utils.RoleUtil;
import com.api.domain.chat.model.ChatMessage;
import com.api.domain.chat.redis.repository.RedisRoomRepository;
import com.api.domain.room.entity.RoomEntity;
import com.api.domain.room.model.RequestCreateRoomDTO;
import com.api.domain.room.model.ResponseReadRoomDTO;
import com.api.domain.room.repository.RoomRepository;
import com.api.domain.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomHybridSyncService {
    private final RoomRepository roomRepository;
    private final RedisRoomRepository redisRoomRepository;

    /**
     * 방을 생성합니다.
     * 회원은 한번에 하나의 방만 생성할 수 있으며, Manager 권한을 가진 회원만 방을 생성할 수 있습니다.
     * DB에 방 정보를 저장하고, Redis에 방 정보를 추가합니다.
     */
    @Transactional
    public Long createRoom(RequestCreateRoomDTO requestDTO, UserEntity creator) {
        // 회원은 한번에 하나의 방만 생성 가능하며 Manager 권한을 가진 회원만 방을 생성할 수 있다.
        if (!RoleUtil.isManager(creator.getRole())) {
            throw new UserForbiddenException("방을 생성할 수 있는 권한이 없습니다.");
        }

        if (roomRepository.existsByCreatorAndDeletedFalse(creator)) {
            throw new UserForbiddenException("이미 방을 생성한 회원입니다.");
        }

        // 방 생성 로직
        RoomEntity room = roomRepository.save(RoomEntity.builder()
                .roomName(requestDTO.getName())
                .roomDescription(requestDTO.getDescription())
                .currentCapacity(0) // 현재 인원 수
                .maxCapacity(requestDTO.getMaxCapacity())
                .creator(creator)
                .build());

        // Redis에 방 정보 추가
        redisRoomRepository.addRoom(room.getRoomId(), room.getMaxCapacity());

        return room.getRoomId();
    }

    /**
     * 방을 조회합니다.
     */
    public CustomSlice<ResponseReadRoomDTO> readRoomSlice(Pageable pageable) {
        // 방 조회 로직
        Slice<RoomEntity> roomSlice = roomRepository.findAllByDeletedFalse(pageable);

        List<ResponseReadRoomDTO> list = new ArrayList<>();

        for (RoomEntity room : roomSlice) {
            // Redis에서 현재 인원 수 가져오기
            String thumbnail = redisRoomRepository.getThumbnail(room.getRoomId());

            if (thumbnail == null) {
                list.add(ResponseReadRoomDTO.convert(room));
            } else {
                list.add(ResponseReadRoomDTO.convert(room, thumbnail));
            }
        }

        return CustomSlice.<ResponseReadRoomDTO>builder()
                .content(list)
                .hasNext(roomSlice.hasNext())
                .hasPrevious(roomSlice.hasPrevious())
                .number(roomSlice.getNumber())
                .numberOfElements(roomSlice.getNumberOfElements())
                .size(roomSlice.getSize())
                .build();
    }

    /**
     * 방의 현재 인원 수를 업데이트합니다.
     * Redis와 DB 모두에 반영됩니다.
     */
    @Transactional
    public Long updateRoomCurrentCapacity(Long roomId, long increment) {
        // 방 인원 수 업데이트 로직
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));

        // Redis에 방 정보 업데이트
        Long current = redisRoomRepository.increaseCurrentCapacity(roomId, increment);

        room.updateCurrentCapacity(current.intValue());

        // 명시적으로 save 호출
        roomRepository.save(room);

        return current;
    }

    /**
     * 채팅방 인원 수를 업데이트합니다.
     * Redis에서 현재 인원 수를 가져와서 DB의 Room 엔티티에 반영합니다.
     * -1이 반환되면 업데이트할 필요가 없다는 의미입니다.
     * 그렇지 않으면 현재 인원 수를 DB에 업데이트합니다.
     */
    @Transactional
    public void updateRoomUserCount(ChatMessage message) {
        int currentCapacity = redisRoomRepository.switchRoomAndGetCurrentCapacity(message);

        // -1은 업데이트 할 필요가 없다는 의미.
        if (currentCapacity == -1) return;

        // DB의 Room 엔티티에도 동기화
        Long roomId = Long.valueOf(message.getRoomId());

        // 방 인원 수 업데이트 로직
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));

        room.updateCurrentCapacity(currentCapacity);

        // 명시적으로 save 호출
        roomRepository.save(room);
    }

    /**
     * 현재 방의 인원 수를 Redis에서 업데이트합니다.
     */
    public Long updateOnlyCurrentCapacity(Long roomId, long increment) {
        // Redis에 방 정보 업데이트
        return redisRoomRepository.increaseCurrentCapacity(roomId, increment);
    }

    /**
     * 레디스에 저장된 채팅방의 과거 채팅 기록을 가져옵니다.
     * 방 ID를 기준으로 채팅 메시지 리스트를 반환합니다.
     */
    public List<ChatMessage> getAllChatHistory(String roomId) {
        return redisRoomRepository.getAllChatHistory(roomId);
    }

    /**
     * 메세지를 레디스에 저장합니다.
     */
    public void addRoomEvent(ChatMessage message) {
        redisRoomRepository.addRoomEvent(message);
    }

    public void saveThumbnail(Long roomId, String dataUrl, Duration duration) {
        redisRoomRepository.saveThumbnail(roomId, dataUrl, duration);
    }
}
