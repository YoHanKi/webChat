package com.api.domain.user.controller;

import com.api.domain.user.UsernameAlreadyExistsException;
import com.api.domain.user.model.RegisterRequest;
import com.api.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class AuthController {
    private final UserService authService;

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