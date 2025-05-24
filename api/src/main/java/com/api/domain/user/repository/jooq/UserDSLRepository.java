package com.api.domain.user.repository.jooq;

import com.api.domain.user.model.SearchUserRequest;
import com.api.domain.user.model.SelectUserForAdminDTO;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.api.jooq.tables.Users.USERS;

@Repository
@RequiredArgsConstructor
public class UserDSLRepository {

    private final DSLContext dslContext;

   public Page<SelectUserForAdminDTO> findAllBySearchCondition(SearchUserRequest search, Pageable pageable) {
       // 검색 조건 설정
       Condition condition;

       switch (search.searchType()) {
           case USERNAME -> condition = USERS.USERNAME.likeIgnoreCase("%" + search.searchText() + "%");
           case ID -> {
               try {
                   Long id = Long.parseLong(search.searchText());
                   condition = USERS.ID.eq(id);
               } catch (NumberFormatException e) {
                   condition = DSL.falseCondition(); // ID가 숫자 형식이 아닌 경우
               }
           }
           case ROLE -> condition = USERS.ROLE.likeIgnoreCase("%" + search.searchText() + "%");
           default -> condition = DSL.trueCondition();
       }

       // 총 레코드 수 조회
       Integer total = dslContext
               .selectCount()
               .from(USERS)
               .where(condition)
               .fetchOne(0, Integer.class);

       if (total == null || total == 0) {
              return new PageImpl<>(List.of(), pageable, 0);
       }

       // 페이징 적용하여 데이터 조회
       // 페이징 적용하여 데이터 조회 (password 제외)
       List<SelectUserForAdminDTO> userDTOs = dslContext
               .select(USERS.ID, USERS.USERNAME, USERS.ROLE, USERS.CREATE_DATE, USERS.MODIFIED_DATE)
               .from(USERS)
               .where(condition)
               .orderBy(USERS.ID.asc())
               .limit(pageable.getPageSize())
               .offset(pageable.getOffset())
               .fetchInto(SelectUserForAdminDTO.class);

       // Page 객체 생성하여 반환
       return new PageImpl<>(userDTOs, pageable, total);
   }
}
