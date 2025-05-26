package com.api.web.admin.user.controller;

import com.api.domain.user.entity.UserEntity;
import com.api.domain.user.model.CreateUserRequest;
import com.api.domain.user.model.ModifyUserRequest;
import com.api.domain.user.model.SearchUserRequest;
import com.api.domain.user.model.SelectUserForAdminDTO;
import com.api.domain.user.service.UserService;
import com.api.security.model.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class UserAdminController {
    private final UserService userService;

    @GetMapping("/search")
    public ResponseEntity<Page<SelectUserForAdminDTO>> searchUsers(
            SearchUserRequest search,
            @PageableDefault Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 관리자 확인
        if (userDetails == null || !userDetails.getUserEntity().getRole().equals(UserEntity.Role.ADMIN)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        // 검색 로직 구현
        Page<SelectUserForAdminDTO> searchResult = userService.findAllBySearchCondition(search, pageable);
        return ResponseEntity.ok(searchResult);
    }

    @PostMapping("/create")
    public ResponseEntity<Void> createUser(
            @RequestBody CreateUserRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 관리자 확인
        if (userDetails == null || !userDetails.getUserEntity().getRole().equals(UserEntity.Role.ADMIN)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        // 사용자 생성 로직 구현
        userService.createUser(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/modify")
    public ResponseEntity<Void> modifyUser(
            @RequestBody ModifyUserRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 관리자 확인
        if (userDetails == null || !userDetails.getUserEntity().getRole().equals(UserEntity.Role.ADMIN)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        // 사용자 수정 로직 구현
        userService.modifyUser(request);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUser(
            @RequestParam Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 관리자 확인
        if (userDetails == null || !userDetails.getUserEntity().getRole().equals(UserEntity.Role.ADMIN)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        // 사용자 삭제 로직 구현
        userService.deleteUser(id);

        return ResponseEntity.ok().build();
    }

}
