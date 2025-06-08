package com.api.domain.user.service;

import com.api.domain.user.exception.UsernameAlreadyExistsException;
import com.api.domain.user.entity.UserEntity;
import com.api.domain.user.model.*;
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

    /**
     * 사용자 등록
     */
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

    /**
     * 사용자 정보 조회
     */
    public Page<SelectUserForAdminDTO> findAllBySearchCondition(SearchUserRequest search, Pageable pageable) {
        return userDSLRepository.findAllBySearchCondition(search, pageable);
    }

    /**
     * 사용자 정보 수정
     */
    public void modifyUser(ModifyUserRequest request) {
        userRepository.findById(request.id())
                .ifPresentOrElse(user -> {
                    user.update(request.username(), passwordEncoder.encode(request.password()), request.role());
                    userRepository.save(user);
                }, () -> {
                    throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
                });
    }

    /**
     * 사용자 삭제
     */
    public void deleteUser(Long id) {
        userRepository.findById(id)
                .ifPresentOrElse(userRepository::delete, () -> {
                    throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
                });
    }

    public void createUser(CreateUserRequest request) {
         userRepository.save(UserEntity.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .build());
    }

    public UserEntity getUserByName(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
    }
}