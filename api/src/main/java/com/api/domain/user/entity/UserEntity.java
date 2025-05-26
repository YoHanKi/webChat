package com.api.domain.user.entity;

import com.api.common.entity.BaseDateEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity extends BaseDateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    // 참여자, 방장, 관리자
    public enum Role {
        USER,
        MANAGER,
        ADMIN
    }

    public void update(String username, String password, String role) {
        Role roleEnum = Optional.ofNullable(role)
                .map(Role::valueOf)
                .orElse(this.role);

        this.username = Optional.ofNullable(username).orElse(this.username);
        this.password = Optional.ofNullable(password).orElse(this.password);
        this.role = roleEnum;
    }
}
