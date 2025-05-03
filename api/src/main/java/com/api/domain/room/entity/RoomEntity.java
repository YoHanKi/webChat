package com.api.domain.room.entity;

import com.api.domain.common.entity.BaseDateEntity;
import com.api.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "room")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomEntity extends BaseDateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;

    private String roomName;

    private String roomDescription;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private UserEntity creator;

    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;

    public void update(String roomName, String roomDescription) {
        this.roomName = roomName != null ? roomName : this.roomName;
        this.roomDescription = roomDescription != null ? roomDescription : this.roomDescription;
    }

    public void delete() {
        this.deleted = true;
    }
}
