package com.api.domain.web.admin.user.controller;

import com.api.domain.user.model.SearchUserRequest;
import com.api.domain.user.model.SelectUserForAdminDTO;
import com.api.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class UserAdminController {
    private final UserService userService;

    @GetMapping("/search")
    public ResponseEntity<Page<SelectUserForAdminDTO>> searchUsers(
            SearchUserRequest search,
            @PageableDefault Pageable pageable
    ) {
        // 검색 로직 구현
        Page<SelectUserForAdminDTO> searchResult = userService.findAllBySearchCondition(search, pageable);
        return ResponseEntity.ok(searchResult);
    }
}
