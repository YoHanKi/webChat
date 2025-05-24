package com.api.web.admin.room.controller;

import com.api.domain.room.model.ModifyRoomRequest;
import com.api.domain.room.model.SearchRoomRequest;
import com.api.domain.room.model.SelectRoomForAdminDTO;
import com.api.domain.room.service.RoomAdminService;
import com.api.domain.user.entity.UserEntity;
import com.api.security.model.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/room")
@RequiredArgsConstructor
public class RoomAdminController {
    private final RoomAdminService roomAdminService;

    @GetMapping("/search")
    public ResponseEntity<Page<SelectRoomForAdminDTO>> getRoom(
            SearchRoomRequest search,
            @PageableDefault Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 관리자 확인
        if (userDetails == null || !userDetails.getUserEntity().getRole().equals(UserEntity.Role.ADMIN)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        Page<SelectRoomForAdminDTO> allBySearchCondition = roomAdminService.findAllBySearchCondition(search, pageable);
        return ResponseEntity.ok(allBySearchCondition);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SelectRoomForAdminDTO> getRoomDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 관리자 확인
        if (userDetails == null || !userDetails.getUserEntity().getRole().equals(UserEntity.Role.ADMIN)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        SelectRoomForAdminDTO room = roomAdminService.findById(id);
        return ResponseEntity.ok(room);
    }

    @PutMapping
    public ResponseEntity<Void> updateRoom(
            @RequestBody ModifyRoomRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 관리자 확인
        if (userDetails == null || !userDetails.getUserEntity().getRole().equals(UserEntity.Role.ADMIN)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        roomAdminService.modifyRoom(request);
        return ResponseEntity.ok().build();
    }
}
