package com.api.domain.room.repository.jooq;

import com.api.common.utils.DateUtil;
import com.api.domain.room.model.SearchRoomRequest;
import com.api.domain.room.model.SelectRoomForAdminDTO;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.api.jooq.Tables.ROOM;

@Repository
@RequiredArgsConstructor
public class RoomDSLRepository {
    private final DSLContext dslContext;

    public Page<SelectRoomForAdminDTO> findAllBySearchCondition(SearchRoomRequest search, Pageable pageable) {
        // 검색 조건 설정
        Condition condition = switch (search.searchType()) {
            case NAME -> ROOM.ROOM_ID.likeIgnoreCase("%" + search.searchText() + "%");
            case DESCRIPTION -> ROOM.ROOM_NAME.likeIgnoreCase("%" + search.searchText() + "%");
            case DATE -> ROOM.CREATE_DATE.between(
                    DateUtil.stringToDateTimeAtStartOfDay(search.startDate(), "yyyy-MM-dd"),
                    DateUtil.stringToDateTimeAtEndOfDay(search.endDate(), "yyyy-MM-dd")
            );
            default -> DSL.trueCondition();
        };

        // 총 레코드 수 조회
        Integer total = dslContext
                .selectCount()
                .from(ROOM)
                .where(condition)
                .fetchOne(0, Integer.class);

        if (total == null || total == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // 페이징 적용하여 데이터 조회
        List<SelectRoomForAdminDTO> result = dslContext
                .select(ROOM.ROOM_ID, ROOM.ROOM_NAME, ROOM.ROOM_DESCRIPTION, ROOM.users().USERNAME, ROOM.DELETED, ROOM.CREATE_DATE)
                .from(ROOM)
                .where(condition)
                .orderBy(ROOM.CREATE_DATE.desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetchInto(SelectRoomForAdminDTO.class);

        return new PageImpl<>(result, pageable, total);
    }
}
