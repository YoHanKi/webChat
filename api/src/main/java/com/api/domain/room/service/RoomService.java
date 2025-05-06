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

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final RoomUserRepository roomUserRepository;
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
                .currentCapacity(0) // 현재 인원 수
                .maxCapacity(requestDTO.getMaxCapacity())
                .creator(creator)
                .build());

        // Redis에 방 정보 추가
        redisRoomRepository.addRoom(room.getRoomId(), room.getMaxCapacity());

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

    // 방 상세 조회
    public ResponseReadRoomDTO readRoomById(Long roomId) {
        // 방 상세 조회 로직
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));

        return ResponseReadRoomDTO.convert(room);
    }

    // 방 수정
    @Transactional
    public void updateRoom(Long roomId, String roomName, String roomDescription, Integer maxCapacity) {
        // 방 수정 로직
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));

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

        // 명시적으로 save 호출
        roomRepository.save(room);
    }

    // 방 인원 수 증감 및 Room 엔티티 업데이트
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

    @Transactional
    public void updateOnlyRoomCurrentCapacity(Long roomId, int current) {
        // 방 인원 수 업데이트 로직
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));

        room.updateCurrentCapacity(current);

        // 명시적으로 save 호출
        roomRepository.save(room);
    }

    // 방 인원 수 증감 및 결과만 먼저 호출
    public Long updateOnlyCurrentCapacity(Long roomId, long increment) {
        // Redis에 방 정보 업데이트
        return redisRoomRepository.increaseCurrentCapacity(roomId, increment);
    }

    // 채팅방 유저 목록 엔티티 저장
    @Transactional
    public void createRoomUser(Long roomId, String username) {
        // 방 인원 수 업데이트 로직
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 방 인원 수 업데이트
        roomUserRepository.save(RoomUserEntity.builder()
                .room(room)
                .user(user)
                .build());
    }

    // -1이 아니라면 인원 수에는 입장할 수 있는 것이므로, 채팅방 인원 목록에 추가 및 RoomEntity 및 RoomUserEntity를 업데이트 해준다.
    @Transactional
    public void updateRoomUserCount(ChatMessage message) {
        int currentCapacity = redisRoomRepository.switchRoomAndGetCurrentCapacity(message);

        // -1은 업데이트 할 필요가 없다는 의미.
        if (currentCapacity == -1) return;

        // DB의 Room 엔티티에도 동기화
        updateOnlyRoomCurrentCapacity(Long.valueOf(message.getRoomId()), currentCapacity);
    }

    // 방 인원 수 업데이트
    @Transactional
    public Long updateCurrentCapacity(Long roomId, long current) {
        // 방 인원 수 업데이트 로직
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));

        room.updateCurrentCapacity((int) current);

        // Redis에 방 정보 업데이트
        redisRoomRepository.updateCurrentCapacity(roomId, current);

        // 명시적으로 save 호출
        roomRepository.save(room);

        return current;
    }

    // 레디스 과거 채팅 기록 가져오기
    public List<ChatMessage> getAllChatHistory(String roomId) {
        return redisRoomRepository.getAllChatHistory(roomId);
    }

    /**
     * 방에 대한 접속 인원(currentUserCount)을
     * Redis Set 으로 관리한 뒤, 그 크기를 Room 엔티티에 반영합니다.
     */
    @Transactional
    public void updateCurrentUserCount(ChatMessage message) {
        int currentCapacity = redisRoomRepository.switchRoomAndGetCurrentCapacity(message);

        // -1은 업데이트 할 필요가 없다는 의미.
        if (currentCapacity == -1) return;

        // DB의 Room 엔티티에도 동기화
        updateOnlyRoomCurrentCapacity(Long.valueOf(message.getRoomId()), currentCapacity);
    }

    /**
     * Redis에서 가져온 회원 ID 리스트를 바탕으로,
     * DB에서 username, imageUrl 등의 상세 정보를 조회해 DTO로 반환합니다.
     */
    public List<RequestReadUserDTO> getRoomMembersDetailed(String roomId) {
        List<RequestReadUserDTO> users;
        // TODO : Redis에서 회원 정보를 모두 저장하여 가져오는 것이 더 효율적일 수 있음. -> 현재는 Redis에서 ID만 저장함.(수정 완료) -> 추후 개선 예정

        // 1) 실패 시 DB에서 유저 엔티티 일괄 조회
        RoomEntity room = roomRepository.findById(Long.valueOf(roomId))
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));

        users = roomUserRepository.findAllByRoomAndLeavedIsFalse(room).stream()
                .map(RequestReadUserDTO::of).toList();

        return users;
    }

    public void addRoomEvent(ChatMessage message) {
        redisRoomRepository.addRoomEvent(message);
    }

    // TODO : JOIN 이라면 RoomUser 저장하고, LEAVE 라면 해당 Room에 User leave true 처리
    @Transactional
    public boolean updateRoomUser(ChatMessage message) {
        UserEntity user = userRepository.findByUsername(message.getSender())
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        RoomEntity room = roomRepository.findById(Long.valueOf(message.getRoomId()))
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));

        if (message.getType() == ChatMessage.MessageType.JOIN) {
            // JOIN
            roomUserRepository.save(RoomUserEntity.builder()
                    .room(room)
                    .user(user)
                    .build());

            return true;
        } else if (message.getType() == ChatMessage.MessageType.LEAVE) {
            // LEAVE → leave true 처리
            roomUserRepository.findFirstByRoomAndUserAndLeavedIsFalse(room, user).ifPresent(RoomUserEntity::nowLeave);

            return true;
        }

        return false;
    }

    /**
     *  채팅방 인원 리스트를 조회합니다.
     */
    public List<RequestReadUserDTO> getRoomUserList(Long roomId) {
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));

        return roomUserRepository.findAllByRoomAndLeavedIsFalse(room)
                .stream().map(RequestReadUserDTO::of)
                .toList();
    }

    /**
     *  방에 특정 유저를 벤합니다.
     */
    public void kickUser(Long roomId, String userName, String sessionId, String kickUserName) {
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));

        // 방장만 유저를 벤할 수 있습니다.
        if (!room.getCreator().getUsername().equals(sessionId)) {
            throw new UserForbiddenException("방장만 유저를 벤할 수 있습니다.");
        }

        UserEntity user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 해당 유저 조회
        RoomUserEntity roomUser = roomUserRepository.findFirstByRoomAndUserAndLeavedIsFalse(room, user)
                .orElseThrow(() -> new IllegalArgumentException("방에 유저가 없습니다."));

        roomUser.nowLeave();

        // 명시적으로 save 호출
        roomUserRepository.save(roomUser);
    }

}
