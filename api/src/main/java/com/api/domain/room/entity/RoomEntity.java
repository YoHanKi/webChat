package com.api.domain.room.entity;

import com.api.common.entity.BaseDateEntity;
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

    private Integer currentCapacity;

    private Integer maxCapacity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private UserEntity creator;

    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;

    public void update(String roomName, String roomDescription, Integer maxCapacity) {
        this.roomName = roomName != null ? roomName : this.roomName;
        this.roomDescription = roomDescription != null ? roomDescription : this.roomDescription;
        this.maxCapacity = maxCapacity != null ? maxCapacity : this.maxCapacity;
    }

    public void updateCurrentCapacity(int currentCapacity) {
        this.currentCapacity = currentCapacity;
    }

    public void delete() {
        this.deleted = true;
    }
}
