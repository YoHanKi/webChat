package com.api.domain.room.service;

import com.api.domain.room.model.ModifyRoomRequest;
import com.api.domain.room.model.SearchRoomRequest;
import com.api.domain.room.model.SelectRoomForAdminDTO;
import com.api.domain.room.repository.RoomRepository;
import com.api.domain.room.repository.jooq.RoomDSLRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomAdminService {
    private final RoomRepository roomRepository;
    private final RoomDSLRepository roomDSLRepository;

    @Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
    public Page<SelectRoomForAdminDTO> findAllBySearchCondition(SearchRoomRequest search, Pageable pageable) {
        return roomDSLRepository.findAllBySearchCondition(search, pageable);
    }

    @Transactional(readOnly = true)
    public SelectRoomForAdminDTO findById(Long id) {
        return roomRepository.findById(id)
                .map(SelectRoomForAdminDTO::new)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));
    }

    public void modifyRoom(ModifyRoomRequest request) {
        roomRepository.findById(request.id())
                .ifPresentOrElse(room -> {
                    room.update(request.roomName(), request.roomDescription(), request.isDeleted());
                    roomRepository.save(room);
                }, () -> {
                    throw new IllegalArgumentException("방을 찾을 수 없습니다.");
                });
    }
}
