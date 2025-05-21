package com.api.web.admin.notice.controller;

import com.api.domain.Notice.model.SearchNoticeRequest;
import com.api.domain.Notice.model.SelectNoticeForAdminDTO;
import com.api.domain.Notice.service.NoticeService;
import com.api.domain.user.entity.UserEntity;
import com.api.security.model.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/notice")
@RequiredArgsConstructor
public class NoticeAdminController {
    private final NoticeService noticeService;

    // 공지사항 페이지 조회
    @GetMapping("/search")
    public ResponseEntity<Page<SelectNoticeForAdminDTO>> getNotice(
            SearchNoticeRequest search,
            @PageableDefault Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 관리자 확인
        if (userDetails == null || !userDetails.getUserEntity().getRole().equals(UserEntity.Role.ADMIN)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        Page<SelectNoticeForAdminDTO> allBySearchCondition = noticeService.findAllBySearchCondition(search, pageable);

        return ResponseEntity.ok(allBySearchCondition);
    }
}
