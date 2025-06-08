package com.api.domain.room.service;

import com.api.domain.chat.model.ChatMessage;
import com.api.domain.chat.redis.repository.RedisRoomRepository;
import com.api.common.exception.UserForbiddenException;
import com.api.common.model.CustomSlice;
import com.api.common.utils.RoleUtil;
import com.api.domain.room.entity.RoomEntity;
import com.api.domain.room.model.RequestCreateRoomDTO;
import com.api.domain.room.model.ResponseReadRoomDTO;
import com.api.domain.room.repository.RoomRepository;
import com.api.domain.roomUser.entity.RoomUserEntity;
import com.api.domain.roomUser.repository.RoomUserRepository;
import com.api.domain.user.entity.UserEntity;
import com.api.domain.user.model.RequestReadUserDTO;
import com.api.domain.user.repository.UserRepository;
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
public class RoomService {
    private final RoomRepository roomRepository;

    /**
     * 방 상세 조회
     * 방 ID로 방을 조회합니다.
     * 반환되는 DTO는 방의 상세 정보를 포함합니다.
     */
    public ResponseReadRoomDTO readRoomById(Long roomId) {
        // 방 상세 조회 로직
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));

        return ResponseReadRoomDTO.convert(room);
    }

    /**
     * 방을 수정합니다.
     * DB 의 Room 엔티티를 업데이트합니다.
     */
    @Transactional
    public void updateRoom(Long roomId, String roomName, String roomDescription, Integer maxCapacity) {
        // 방 수정 로직
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));

        room.update(roomName, roomDescription, maxCapacity);

        // 명시적으로 save 호출
        roomRepository.save(room);
    }

    /**
     * 방을 삭제합니다.
     * DB 의 Room 엔티티에서 deleted 플래그를 true로 설정하여 논리 삭제합니다.
     */
    @Transactional
    public void deleteRoom(Long roomId) {
        // 방 삭제 로직
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));

        room.delete();

        // 명시적으로 save 호출
        roomRepository.save(room);
    }

    /**
     * 채팅방 조회
     */
    public RoomEntity getRoomById(String roomId) {
        return roomRepository.findById(Long.valueOf(roomId))
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));
    }
}
