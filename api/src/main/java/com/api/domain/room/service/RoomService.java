package com.api.domain.room.service;

import com.api.domain.chat.model.ChatMessage;
import com.api.domain.chat.redis.repository.RedisRoomRepository;
import com.api.domain.common.exception.UserForbiddenException;
import com.api.domain.common.model.CustomSlice;
import com.api.domain.common.utils.RoleUtil;
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

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {
    private final RoomRepository roomRepository;
    private final RedisRoomRepository redisRoomRepository;

    // 방 생성
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
                .creator(creator)
                .build());

        return room.getRoomId();
    }

    // 방 조회 (Slice, Pagination 등)
    public CustomSlice<ResponseReadRoomDTO> readRoomSlice(Pageable pageable) {
        // 방 조회 로직
        Slice<RoomEntity> roomSlice = roomRepository.findAllByDeletedFalse(pageable);

        return CustomSlice.<ResponseReadRoomDTO>builder()
                .content(roomSlice.map(ResponseReadRoomDTO::convert).getContent())
                .hasNext(roomSlice.hasNext())
                .hasPrevious(roomSlice.hasPrevious())
                .number(roomSlice.getNumber())
                .numberOfElements(roomSlice.getNumberOfElements())
                .size(roomSlice.getSize())
                .build();
    }

    // 방 수정
    @Transactional
    public void updateRoom(String roomName, String roomDescription, Integer maxCapacity, Long roomId) {
        // 방 수정 로직
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));

        // 방이 수정/생성된 지 10분이 지나지 않았다면 수정 불가
        if (room.getCreateDate().plusMinutes(10).isAfter(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("방 생성 후 10분 이내에는 수정할 수 없습니다.");
        }

        room.update(roomName, roomDescription, maxCapacity);

        // 명시적으로 save 호출
        roomRepository.save(room);
    }

    // 방 삭제
    @Transactional
    public void deleteRoom(Long roomId) {
        // 방 삭제 로직
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));

        room.delete();

        // Redis에 방 정보 삭제
        redisRoomRepository.deleteRoom(roomId);

        // 명시적으로 save 호출
        roomRepository.save(room);
    }

    // 방 인원 수 업데이트
    @Transactional
    public Long updateRoomCurrentCapacity(Long roomId, long increment) {
        // 방 인원 수 업데이트 로직
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));

        // Redis에 방 정보 업데이트
        Long current = redisRoomRepository.updateCurrentCapacity(roomId, increment);

        room.updateCurrentCapacity(current.intValue());

        // 명시적으로 save 호출
        roomRepository.save(room);

        return current;
    }

    // 레디스 과거 채팅 기록 가져오기
    public List<ChatMessage> getAllChatHistory(String roomId) {
        return redisRoomRepository.getAllChatHistory(roomId);
    }

}
