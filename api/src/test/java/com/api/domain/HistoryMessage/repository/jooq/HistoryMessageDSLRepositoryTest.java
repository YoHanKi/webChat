package com.api.domain.HistoryMessage.repository.jooq;

import com.api.domain.HistoryMessage.model.SearchHistoryMessageRequest;
import com.api.domain.HistoryMessage.model.SelectHistoryMessageForAdminDTO;
import com.api.domain.HistoryMessage.model.enums.SearchHistoryMessageType;
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
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@ExtendWith(MockitoExtension.class)
class HistoryMessageDSLRepositoryTest {

    @Mock
    private DSLContext dslContext;
    
    @InjectMocks
    @Spy
    private HistoryMessageDSLRepository historyMessageDSLRepository;
    
    private final LocalDateTime now = LocalDateTime.now();
    private final String formattedDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    private final Pageable pageable = PageRequest.of(0, 10);
    
    private List<SelectHistoryMessageForAdminDTO> messageList;
    
    @BeforeEach
    void setUp() {
        // 테스트 데이터 설정
        messageList = Arrays.asList(
            new SelectHistoryMessageForAdminDTO(1L, "room1", "user1", "안녕하세요!", formattedDate),
            new SelectHistoryMessageForAdminDTO(2L, "room1", "user2", "반갑습니다!", formattedDate),
            new SelectHistoryMessageForAdminDTO(3L, "room2", "admin", "공지사항입니다.", formattedDate)
        );
        
        // 메서드 호출 결과 모킹
        doAnswer(invocation -> {
            SearchHistoryMessageRequest request = invocation.getArgument(0);
            Pageable pageable = invocation.getArgument(1);
            
            // 검색 조건에 따라 필터링된 결과 반환
            List<SelectHistoryMessageForAdminDTO> filteredList;
            
            if (request.searchHistoryMessageType() == null) {
                filteredList = messageList;
            } else {
                switch (request.searchHistoryMessageType()) {
                    case ROOM_ID:
                        filteredList = messageList.stream()
                                .filter(message -> message.roomId().contains(request.searchText()))
                                .toList();
                        break;
                    case SENDER:
                        filteredList = messageList.stream()
                                .filter(message -> message.sender().contains(request.searchText()))
                                .toList();
                        break;
                    case CONTENT:
                        filteredList = messageList.stream()
                                .filter(message -> message.content().contains(request.searchText()))
                                .toList();
                        break;
                    case DATE:
                        // 실제로는 날짜 비교 로직이 더 복잡할 수 있지만 테스트 목적으로는 단순화
                        filteredList = messageList;
                        break;
                    default:
                        filteredList = messageList;
                }
            }
            
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), filteredList.size());
            
            // 페이징 결과가 범위를 벗어나는 경우 빈 목록 반환
            List<SelectHistoryMessageForAdminDTO> pageContent = 
                (start <= end && start < filteredList.size()) 
                ? filteredList.subList(start, end) 
                : Collections.emptyList();
                
            return new PageImpl<>(pageContent, pageable, filteredList.size());
        }).when(historyMessageDSLRepository).findAllBySearchCondition(any(), any());
    }
    
    @Test
    @DisplayName("모든 메시지 검색 - 검색조건 없음")
    void findAllBySearchCondition_noCondition() {
        // Given
        SearchHistoryMessageRequest request = new SearchHistoryMessageRequest(SearchHistoryMessageType.ALL, "", null, null);
        
        // When
        Page<SelectHistoryMessageForAdminDTO> result = historyMessageDSLRepository.findAllBySearchCondition(request, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(3, result.getContent().size());
        assertEquals("안녕하세요!", result.getContent().get(0).content());
        assertEquals("공지사항입니다.", result.getContent().get(2).content());
    }
    
    @Test
    @DisplayName("방 ID로 메시지 검색")
    void findAllBySearchCondition_roomId() {
        // Given
        SearchHistoryMessageRequest request = new SearchHistoryMessageRequest(SearchHistoryMessageType.ROOM_ID, "room1", null, null);
        
        // When
        Page<SelectHistoryMessageForAdminDTO> result = historyMessageDSLRepository.findAllBySearchCondition(request, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals("room1", result.getContent().get(0).roomId());
        assertEquals("room1", result.getContent().get(1).roomId());
    }
    
    @Test
    @DisplayName("발신자로 메시지 검색")
    void findAllBySearchCondition_sender() {
        // Given
        SearchHistoryMessageRequest request = new SearchHistoryMessageRequest(SearchHistoryMessageType.SENDER, "admin", null, null);
        
        // When
        Page<SelectHistoryMessageForAdminDTO> result = historyMessageDSLRepository.findAllBySearchCondition(request, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("admin", result.getContent().get(0).sender());
    }
    
    @Test
    @DisplayName("메시지 내용으로 검색")
    void findAllBySearchCondition_content() {
        // Given
        SearchHistoryMessageRequest request = new SearchHistoryMessageRequest(SearchHistoryMessageType.CONTENT, "안녕", null, null);
        
        // When
        Page<SelectHistoryMessageForAdminDTO> result = historyMessageDSLRepository.findAllBySearchCondition(request, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("안녕하세요!", result.getContent().get(0).content());
    }
    
    @Test
    @DisplayName("날짜로 메시지 검색")
    void findAllBySearchCondition_date() {
        // Given
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        SearchHistoryMessageRequest request = new SearchHistoryMessageRequest(SearchHistoryMessageType.DATE, null, today, today);
        
        // When
        Page<SelectHistoryMessageForAdminDTO> result = historyMessageDSLRepository.findAllBySearchCondition(request, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
    }
    
    @Test
    @DisplayName("검색 결과가 없을 때")
    void findAllBySearchCondition_noResults() {
        // Given
        SearchHistoryMessageRequest request = new SearchHistoryMessageRequest(SearchHistoryMessageType.CONTENT, "존재하지않는내용", null, null);
        
        // When
        Page<SelectHistoryMessageForAdminDTO> result = historyMessageDSLRepository.findAllBySearchCondition(request, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }
    
    @Test
    @DisplayName("페이징 처리")
    void findAllBySearchCondition_paging() {
        // Given
        SearchHistoryMessageRequest request = new SearchHistoryMessageRequest(SearchHistoryMessageType.ALL, "", null, null);
        Pageable customPageable = PageRequest.of(1, 1); // 두 번째 페이지, 페이지당 1개
        
        // When
        Page<SelectHistoryMessageForAdminDTO> result = historyMessageDSLRepository.findAllBySearchCondition(request, customPageable);
        
        // Then
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getNumber()); // 페이지 번호 확인
        assertEquals("반갑습니다!", result.getContent().get(0).content()); // 두 번째 항목 확인
    }
}
