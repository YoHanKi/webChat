package com.api.domain.user.controller;

import com.api.domain.user.exception.UsernameAlreadyExistsException;
import com.api.domain.user.model.RegisterRequest;
import com.api.domain.user.service.UserService;
import com.api.security.model.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class AuthController {
    private final UserService authService;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal CustomUserDetails principal) {
        // 200 OK: 현재 사용자 정보 반환
        if (principal == null) {
            // 401 Unauthorized: 인증되지 않은 사용자
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(principal);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            authService.register(request);
            // 201 Created: 정상 등록
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (UsernameAlreadyExistsException ex) {
            // 400 Bad Request: 중복 아이디
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        }
    }
}