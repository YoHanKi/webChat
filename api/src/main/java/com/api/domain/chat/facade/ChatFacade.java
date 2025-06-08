package com.api.domain.chat.facade;

import com.api.common.annotation.Facade;
import com.api.common.exception.UserForbiddenException;
import com.api.domain.chat.model.ChatMessage;
import com.api.domain.room.entity.RoomEntity;
import com.api.domain.room.service.RoomHybridSyncService;
import com.api.domain.room.service.RoomService;
import com.api.domain.roomUser.entity.RoomUserEntity;
import com.api.domain.roomUser.service.RoomUserService;
import com.api.domain.user.entity.UserEntity;
import com.api.domain.user.model.RequestReadUserDTO;
import com.api.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Facade
@RequiredArgsConstructor
public class ChatFacade {
    private final UserService userService;
    private final RoomService roomService;
    private final RoomUserService roomUserService;
    private final RoomHybridSyncService roomHybridSyncService;

    /**
     * 방의 현재 인원 수를 업데이트합니다.
     * Redis와 DB 모두에 반영됩니다.
     */
    public Long updateRoomCurrentCapacity(Long roomId, long increment) {
        return roomHybridSyncService.updateRoomCurrentCapacity(roomId, increment);
    }

    /**
     * 채팅방 인원 수를 업데이트합니다.
     * Redis에서 현재 인원 수를 가져와서 DB의 Room 엔티티에 반영합니다.
     * -1이 반환되면 업데이트할 필요가 없다는 의미입니다.
     * 그렇지 않으면 현재 인원 수를 DB에 업데이트합니다.
     */
    public void updateRoomUserCount(ChatMessage message) {
        roomHybridSyncService.updateRoomUserCount(message);
    }

    /**
     * 현재 방의 인원 수를 Redis에서 업데이트합니다.
     */
    public Long updateOnlyCurrentCapacity(Long roomId, long increment) {
        return roomHybridSyncService.updateOnlyCurrentCapacity(roomId, increment);
    }

    /**
     * 레디스에 저장된 채팅방의 과거 채팅 기록을 가져옵니다.
     * 방 ID를 기준으로 채팅 메시지 리스트를 반환합니다.
     */
    public List<ChatMessage> getAllChatHistory(String roomId) {
        return roomHybridSyncService.getAllChatHistory(roomId);
    }

    /**
     * Redis에서 가져온 회원 ID 리스트를 바탕으로,
     * DB에서 username, imageUrl 등의 상세 정보를 조회해 DTO로 반환합니다.
     */
    public List<RequestReadUserDTO> getRoomMembersDetailed(String roomId) {
        RoomEntity roomById = roomService.getRoomById(roomId);

        return roomUserService.getUsersInRoom(roomById);
    }

    public void addRoomEvent(ChatMessage message) {
        roomHybridSyncService.addRoomEvent(message);
    }

    @Transactional
    public boolean updateRoomUser(ChatMessage message) {
        UserEntity user = userService.getUserByName(message.getSender());
        RoomEntity room = roomService.getRoomById(message.getRoomId());

        if (message.getType() == ChatMessage.MessageType.JOIN) {
            // JOIN
            roomUserService.saveRoomUser(RoomUserEntity.builder()
                    .room(room)
                    .user(user)
                    .build());

            return true;
        } else if (message.getType() == ChatMessage.MessageType.LEAVE) {
            // LEAVE → leave true 처리
            roomUserService.leaveRoomUser(room, user);

            return true;
        }
        return false;
    }

    /**
     *  채팅방 인원 리스트를 조회합니다.
     */
    public List<RequestReadUserDTO> getRoomUserList(Long roomId) {
        RoomEntity room = roomService.getRoomById(String.valueOf(roomId));

        return roomUserService.getRoomUserList(room);
    }

    /**
     *  방에 특정 유저를 벤합니다.
     */
    @Transactional
    public void kickUser(Long roomId, String userName, String sessionId, String kickUserName) {
        RoomEntity room = roomService.getRoomById(String.valueOf(roomId));

        // 방장만 유저를 벤할 수 있습니다.
        if (!room.getCreator().getUsername().equals(sessionId)) {
            throw new UserForbiddenException("방장만 유저를 벤할 수 있습니다.");
        }

        UserEntity user = userService.getUserByName(userName);

        // 해당 유저 leave 변경
        roomUserService.leaveRoomUser(room, user);
    }
}
