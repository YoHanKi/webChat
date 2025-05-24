package com.api.domain.HistoryMessage.repository.jooq;

import com.api.common.utils.DateUtil;
import com.api.domain.HistoryMessage.model.SearchHistoryMessageRequest;
import com.api.domain.HistoryMessage.model.SelectHistoryMessageForAdminDTO;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.api.jooq.tables.HistoryMessage.HISTORY_MESSAGE;

@Repository
@RequiredArgsConstructor
public class HistoryMessageDSLRepository {
    private final DSLContext dslContext;

    public Page<SelectHistoryMessageForAdminDTO> findAllBySearchCondition(SearchHistoryMessageRequest search, Pageable pageable) {
        Condition condition = switch (search.searchType()) {
            case ROOM_ID -> HISTORY_MESSAGE.ROOM_ID.likeIgnoreCase("%" + search.searchText() + "%");
            case SENDER -> HISTORY_MESSAGE.SENDER.likeIgnoreCase("%" + search.searchText() + "%");
            case CONTENT -> HISTORY_MESSAGE.CONTENT.likeIgnoreCase("%" + search.searchText() + "%");
            case DATE -> HISTORY_MESSAGE.CREATE_DATE.between(
                    DateUtil.stringToDateTimeAtStartOfDay(search.startDate(), "yyyy-MM-dd"),
                    DateUtil.stringToDateTimeAtEndOfDay(search.endDate(), "yyyy-MM-dd")
            );
            default -> DSL.trueCondition();
        };

        // 총 레코드 수 조회
        Integer total = dslContext
                .selectCount()
                .from(HISTORY_MESSAGE)
                .where(condition)
                .fetchOne(0, Integer.class);

        if (total == null || total == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // 페이징 적용하여 데이터 조회
        List<SelectHistoryMessageForAdminDTO> result = dslContext
                .select(HISTORY_MESSAGE.ID, HISTORY_MESSAGE.ROOM_ID, HISTORY_MESSAGE.SENDER, HISTORY_MESSAGE.CONTENT, HISTORY_MESSAGE.CREATE_DATE)
                .from(HISTORY_MESSAGE)
                .where(condition)
                .orderBy(HISTORY_MESSAGE.CREATE_DATE.desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetchInto(SelectHistoryMessageForAdminDTO.class);

        return new PageImpl<>(result, pageable, total);
    }
}
