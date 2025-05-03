package com.api.domain.room.controller;

import com.api.domain.common.model.CustomSlice;
import com.api.domain.room.model.RequestCreateRoomDTO;
import com.api.domain.room.model.RequestUpdateRoomDTO;
import com.api.domain.room.model.ResponseReadRoomDTO;
import com.api.domain.room.service.RoomService;
import com.api.security.model.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/room")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    // 방 생성
    @PostMapping("/create")
    public ResponseEntity<?> createRoom(
            @RequestBody RequestCreateRoomDTO requestDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        roomService.createRoom(requestDTO, userDetails.getUserEntity());
        return ResponseEntity.ok().build();
    }

    // 방 조회
    @GetMapping("/read")
    public ResponseEntity<CustomSlice<ResponseReadRoomDTO>> readRoom(
            @PageableDefault(size = 6, page = 0, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(roomService.readRoomSlice(pageable));
    }

    // 방 수정
    @PutMapping("/update")
    public ResponseEntity<?> updateRoom(
            @RequestBody RequestUpdateRoomDTO requestDTO,
            Long roomId
    ) {
        roomService.updateRoom(
                requestDTO.getRoomName(),
                requestDTO.getRoomDescription(),
                roomId
        );

        return ResponseEntity.ok().build();
    }

    // 방 삭제
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteRoom(Long roomId) {
        roomService.deleteRoom(roomId);
        return ResponseEntity.ok().build();
    }

}
