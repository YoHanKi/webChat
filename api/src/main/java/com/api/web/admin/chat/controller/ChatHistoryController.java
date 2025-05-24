package com.api.web.admin.chat.controller;

import com.api.domain.HistoryMessage.model.ModifyHistoryMessageRequest;
import com.api.domain.HistoryMessage.model.SearchHistoryMessageRequest;
import com.api.domain.HistoryMessage.model.SelectHistoryMessageForAdminDTO;
import com.api.domain.HistoryMessage.service.ChatHistoryService;
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
@RequestMapping("/admin/chat/history")
@RequiredArgsConstructor
public class ChatHistoryController {
    private final ChatHistoryService chatHistoryService;

    // 채팅 히스토리 페이지 조회
    @GetMapping("/search")
    public ResponseEntity<Page<SelectHistoryMessageForAdminDTO>> getChatHistory(
            SearchHistoryMessageRequest search,
            @PageableDefault Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 관리자 확인
        if (userDetails == null || !userDetails.getUserEntity().getRole().equals(UserEntity.Role.ADMIN)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        Page<SelectHistoryMessageForAdminDTO> allBySearchCondition = chatHistoryService.findAllBySearchCondition(search, pageable);

        return ResponseEntity.ok(allBySearchCondition);
    }

    // 채팅 히스토리 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<SelectHistoryMessageForAdminDTO> getChatHistoryDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 관리자 확인
        if (userDetails == null || !userDetails.getUserEntity().getRole().equals(UserEntity.Role.ADMIN)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        SelectHistoryMessageForAdminDTO chatHistory = chatHistoryService.findById(id);

        return ResponseEntity.ok(chatHistory);
    }

    // 채팅 히스토리 수정
    @PutMapping
    public ResponseEntity<Void> updateChatHistory(
            ModifyHistoryMessageRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 관리자 확인
        if (userDetails == null || !userDetails.getUserEntity().getRole().equals(UserEntity.Role.ADMIN)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        chatHistoryService.modifyHistoryMessage(request);

        return ResponseEntity.ok().build();
    }

    // 채팅 히스토리 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChatHistory(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 관리자 확인
        if (userDetails == null || !userDetails.getUserEntity().getRole().equals(UserEntity.Role.ADMIN)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        chatHistoryService.deleteHistoryMessage(id);

        return ResponseEntity.ok().build();
    }
}
