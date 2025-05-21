package com.api.domain.Notice.repository.jooq;

import com.api.common.utils.DateUtil;
import com.api.domain.Notice.model.SearchNoticeRequest;
import com.api.domain.Notice.model.SelectNoticeForAdminDTO;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.api.jooq.Tables.NOTICES;

@Repository
@RequiredArgsConstructor
public class NoticeDSLRepository {
    private final DSLContext dslContext;

    public Page<SelectNoticeForAdminDTO> findAllBySearchCondition(SearchNoticeRequest search, Pageable pageable) {
        // 검색 조건 설정
        Condition condition = switch (search.searchNoticeType()) {
            case TITLE -> NOTICES.TITLE.likeIgnoreCase("%" + search.searchText() + "%");
            case CONTENT -> NOTICES.CONTENT.likeIgnoreCase("%" + search.searchText() + "%");
            case AUTHOR -> NOTICES.AUTHOR.likeIgnoreCase("%" + search.searchText() + "%");
            case DATE -> NOTICES.CREATE_DATE.between(
                    DateUtil.stringToDateTimeAtStartOfDay(search.startDate(), "yyyy-MM-dd"),
                    DateUtil.stringToDateTimeAtEndOfDay(search.endDate(), "yyyy-MM-dd")
            );
            default -> DSL.trueCondition();
        };

        // 총 레코드 수 조회
        Integer total = dslContext
                .selectCount()
                .from(NOTICES)
                .where(condition)
                .fetchOne(0, Integer.class);

        if (total == null || total == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // 페이징 적용하여 데이터 조회
        List<SelectNoticeForAdminDTO> result = dslContext
                .select(NOTICES.ID, NOTICES.TITLE, NOTICES.CONTENT, NOTICES.AUTHOR, NOTICES.IS_MAIN_NOTICE, NOTICES.IS_DELETED, NOTICES.CREATE_DATE)
                .from(NOTICES)
                .where(condition)
                .orderBy(NOTICES.CREATE_DATE.desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetchInto(SelectNoticeForAdminDTO.class);

        return new PageImpl<>(result, pageable, total);
    }
}
