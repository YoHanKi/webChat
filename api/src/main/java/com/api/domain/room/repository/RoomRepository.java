package com.api.domain.room.repository;

import com.api.domain.room.entity.RoomEntity;
import com.api.domain.user.entity.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<RoomEntity, Long> {
    Slice<RoomEntity> findAllByDeletedFalse(Pageable pageable);

    boolean existsByCreatorAndDeletedFalse(UserEntity creator);
}
