package com.api.domain.user.service;

import com.api.domain.user.exception.UsernameAlreadyExistsException;
import com.api.domain.user.entity.UserEntity;
import com.api.domain.user.model.RegisterRequest;
import com.api.domain.user.model.SearchUserRequest;
import com.api.domain.user.model.SelectUserForAdminDTO;
import com.api.domain.user.repository.UserRepository;
import com.api.domain.user.repository.jooq.UserDSLRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDSLRepository userDSLRepository;

    public void register(RegisterRequest request) {
        userRepository.findByUsername(request.getUsername())
            .ifPresent(u -> {
                throw new UsernameAlreadyExistsException("이미 사용 중인 아이디입니다.");
            });

        userRepository.save(UserEntity.builder()
                        .username(request.getUsername())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .role(UserEntity.Role.USER)
                .build());
    }

    public Page<SelectUserForAdminDTO> findAllBySearchCondition(SearchUserRequest search, Pageable pageable) {
        return userDSLRepository.findAllBySearchCondition(search, pageable);
    }
}