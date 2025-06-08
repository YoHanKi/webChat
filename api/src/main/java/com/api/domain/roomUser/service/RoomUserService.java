package com.api.domain.roomUser.service;

import com.api.domain.room.entity.RoomEntity;
import com.api.domain.roomUser.entity.RoomUserEntity;
import com.api.domain.roomUser.repository.RoomUserRepository;
import com.api.domain.user.entity.UserEntity;
import com.api.domain.user.model.RequestReadUserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomUserService {
    private final RoomUserRepository roomUserRepository;

    public List<RequestReadUserDTO> getUsersInRoom(RoomEntity room) {
        // 방에 속한 사용자 목록을 조회합니다.
        return roomUserRepository.findAllByRoomAndLeavedIsFalse(room).stream()
                .map(RequestReadUserDTO::of).toList();
    }

    public void saveRoomUser(RoomUserEntity room) {
        roomUserRepository.save(room);
    }

    public void leaveRoomUser(RoomEntity room, UserEntity user) {
        // LEAVE → leave true 처리
        // 해당 유저 조회
        RoomUserEntity roomUser = roomUserRepository.findFirstByRoomAndUserAndLeavedIsFalse(room, user)
                .orElseThrow(() -> new IllegalArgumentException("방에 유저가 없습니다."));

        roomUser.nowLeave();

        // 명시적으로 save 호출
        roomUserRepository.save(roomUser);
    }

    /**
     *  채팅방 인원 리스트를 조회합니다.
     */
    public List<RequestReadUserDTO> getRoomUserList(RoomEntity room) {
        return roomUserRepository.findAllByRoomAndLeavedIsFalse(room)
                .stream().map(RequestReadUserDTO::of)
                .toList();
    }
}
