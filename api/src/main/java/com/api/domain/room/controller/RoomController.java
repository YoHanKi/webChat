package com.api.domain.room.controller;

import com.api.domain.common.model.CustomSlice;
import com.api.domain.room.model.ImageVO;
import com.api.domain.room.model.RequestCreateRoomDTO;
import com.api.domain.room.model.RequestUpdateRoomDTO;
import com.api.domain.room.model.ResponseReadRoomDTO;
import com.api.domain.room.service.RoomService;
import com.api.domain.user.model.RequestReadUserDTO;
import com.api.security.model.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;

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
        Long roomId = roomService.createRoom(requestDTO, userDetails.getUserEntity());
        return ResponseEntity.ok().body(roomId);
    }

    // 방 조회
    @GetMapping("/read")
    public ResponseEntity<CustomSlice<ResponseReadRoomDTO>> readRoom(
            @PageableDefault(size = 6, page = 0, sort = "createDate") Pageable pageable
    ) {
        return ResponseEntity.ok(roomService.readRoomSlice(pageable));
    }

    @GetMapping("/read/{roomId}")
    public ResponseEntity<ResponseReadRoomDTO> readRoomById(
            @PathVariable Long roomId
    ) {
        return ResponseEntity.ok(roomService.readRoomById(roomId));
    }

    // 방 수정
    @PutMapping("/update")
    public ResponseEntity<?> updateRoom(
            @RequestBody RequestUpdateRoomDTO requestDTO
    ) {
        roomService.updateRoom(
                requestDTO.getRoomId(),
                requestDTO.getRoomName(),
                requestDTO.getRoomDescription(),
                requestDTO.getMaxCapacity()
        );

        return ResponseEntity.ok().build();
    }

    // 방 삭제
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteRoom(Long roomId) {
        roomService.deleteRoom(roomId);
        return ResponseEntity.ok().build();
    }

    // 방 인원 목록 조회
    @GetMapping("/list/{roomId}")
    public ResponseEntity<List<RequestReadUserDTO>> getRoomMembers(
            @PathVariable Long roomId
    ) {
        return ResponseEntity.ok(roomService.getRoomUserList(roomId));
    }

    /**
     * 업데이트된 Base64 썸네일 저장
     */
    @PostMapping("/{roomId}/thumbnail")
    public ResponseEntity<Void> uploadThumbnail(
            @PathVariable Long roomId,
            @RequestBody ImageVO imageVO) {

        String dataUrl = imageVO.getImage();
        if (dataUrl == null || !dataUrl.startsWith("data:image")) {
            return ResponseEntity.badRequest().build();
        }

        // Redis에 30초 TTL로 저장 (짧게 캐싱)
        roomService.saveThumbnail(roomId, dataUrl, Duration.ofSeconds(30));

        return ResponseEntity.ok().build();
    }

}
