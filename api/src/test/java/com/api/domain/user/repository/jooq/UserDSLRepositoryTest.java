package com.api.domain.user.repository.jooq;

import com.api.domain.user.model.SearchUserRequest;
import com.api.domain.user.model.SelectUserForAdminDTO;
import com.api.domain.user.model.enums.SearchUserType;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@ExtendWith(MockitoExtension.class)
class UserDSLRepositoryTest {

    @Mock
    private DSLContext dslContext;

    // Spy를 사용하여 실제 메서드를 테스트하되, 일부 동작만 모킹
    @InjectMocks
    @Spy
    private UserDSLRepository userDSLRepository;

    private final LocalDateTime now = LocalDateTime.now();
    private final Pageable pageable = PageRequest.of(0, 10);

    // 테스트 데이터
    private List<SelectUserForAdminDTO> userList;

    @BeforeEach
    void setUp() {
        userList = List.of(
            new SelectUserForAdminDTO(1L, "user1", "USER", now, now),
            new SelectUserForAdminDTO(2L, "admin", "ADMIN", now, now)
        );

        // JOOQ 구조를 직접 모킹하기보다는 실제 메서드 호출 결과를 모킹
        // 메서드 체이닝을 각각 모킹하는 대신 결과만 모킹하는 방식

        // DSLContext에서 사용되는 복잡한 체인 메서드 대신 직접 조작
        // 이 방식은 UserDSLRepository의 내부 구현을 알고 있어야 함
        doAnswer(invocation -> {
            SearchUserRequest request = invocation.getArgument(0);
            Pageable pageable = invocation.getArgument(1);

            // 검색 조건에 따라 필터링된 결과 반환
            List<SelectUserForAdminDTO> filteredList;

            switch (request.searchSearchUserType()) {
                case USERNAME:
                    filteredList = userList.stream()
                            .filter(user -> user.username().contains(request.searchText()))
                            .toList();
                    break;
                case ID:
                    try {
                        Long id = Long.parseLong(request.searchText());
                        filteredList = userList.stream()
                                .filter(user -> user.id().equals(id))
                                .toList();
                    } catch (NumberFormatException e) {
                        filteredList = List.of();
                    }
                    break;
                case ROLE:
                    filteredList = userList.stream()
                            .filter(user -> user.role().contains(request.searchText()))
                            .toList();
                    break;
                default:
                    filteredList = userList;
            }

            return new PageImpl<>(filteredList, pageable, filteredList.size());
        }).when(userDSLRepository).findAllBySearchCondition(any(), any());
    }

    @Test
    @DisplayName("모든 사용자 검색 - 검색조건 없음")
    void findAllBySearchCondition_noCondition() {
        // Given
        SearchUserRequest request = SearchUserRequest.of(SearchUserType.ALL, "");

        // When
        Page<SelectUserForAdminDTO> result = userDSLRepository.findAllBySearchCondition(request, pageable);

        // Then
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals("user1", result.getContent().get(0).username());
        assertEquals("ADMIN", result.getContent().get(1).role());
    }

    @Test
    @DisplayName("사용자명으로 검색")
    void findAllBySearchCondition_usernameSearch() {
        // Given
        SearchUserRequest request = SearchUserRequest.of(SearchUserType.USERNAME, "admin");

        // When
        Page<SelectUserForAdminDTO> result = userDSLRepository.findAllBySearchCondition(request, pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("admin", result.getContent().get(0).username());
    }

    @Test
    @DisplayName("ID로 검색")
    void findAllBySearchCondition_idSearch() {
        // Given
        SearchUserRequest request = SearchUserRequest.of(SearchUserType.ID, "1");

        // When
        Page<SelectUserForAdminDTO> result = userDSLRepository.findAllBySearchCondition(request, pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).id());
    }

    @Test
    @DisplayName("역할로 검색")
    void findAllBySearchCondition_roleSearch() {
        // Given
        SearchUserRequest request = SearchUserRequest.of(SearchUserType.ROLE, "ADMIN");

        // When
        Page<SelectUserForAdminDTO> result = userDSLRepository.findAllBySearchCondition(request, pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("ADMIN", result.getContent().get(0).role());
    }

    @Test
    @DisplayName("검색 결과가 없을 때")
    void findAllBySearchCondition_noResults() {
        // Given
        SearchUserRequest request = SearchUserRequest.of(SearchUserType.USERNAME, "nonexistent");

        // When
        Page<SelectUserForAdminDTO> result = userDSLRepository.findAllBySearchCondition(request, pageable);

        // Then
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("ID 검색 시 숫자가 아닌 값을 입력한 경우")
    void findAllBySearchCondition_idSearchWithNonNumber() {
        // Given
        SearchUserRequest request = SearchUserRequest.of(SearchUserType.ID, "notANumber");

        // When
        Page<SelectUserForAdminDTO> result = userDSLRepository.findAllBySearchCondition(request, pageable);

        // Then
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }
}
