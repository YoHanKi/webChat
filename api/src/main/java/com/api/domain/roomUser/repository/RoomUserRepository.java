package com.api.domain.roomUser.repository;

import com.api.domain.room.entity.RoomEntity;
import com.api.domain.roomUser.entity.RoomUserEntity;
import com.api.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomUserRepository extends JpaRepository<RoomUserEntity, Long> {
  
    // 특정 방에 이미 입장해 있는지 검사
    boolean existsByRoomAndUser(RoomEntity room, UserEntity user);

    // 방에 속한 모든 사용자를 조회
    List<RoomUserEntity> findAllByRoomAndLeavedIsFalse(RoomEntity room);

    // 특정 방에 속한 사용자를 조회
    Optional<RoomUserEntity> findFirstByRoomAndUserAndLeavedIsFalse(RoomEntity room, UserEntity user);

    // 특정 방에서 사용자 제거
    void deleteByRoomAndUser(RoomEntity room, UserEntity user);

    // 방 입장 인원 수 조회
    long countByRoom(RoomEntity room);
}