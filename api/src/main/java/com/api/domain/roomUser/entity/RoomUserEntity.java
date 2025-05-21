package com.api.domain.roomUser.entity;

import com.api.common.entity.BaseDateEntity;
import com.api.domain.room.entity.RoomEntity;
import com.api.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@BatchSize(size = 50)
@Table(name = "room_user", indexes = {
        @Index(name = "idx_room_user_leaved", columnList = "room_id, user_id, leaved, createDate")
})
public class RoomUserEntity extends BaseDateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 방 정보
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private RoomEntity room;

    // 사용자 정보
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Builder.Default
    private boolean leaved = false; // 방 나가기 여부

    public void nowLeave() {
        this.leaved = true;
    }
}
