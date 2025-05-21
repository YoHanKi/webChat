package com.api.domain.Notice.service;

import com.api.domain.Notice.model.SearchNoticeRequest;
import com.api.domain.Notice.model.SelectNoticeForAdminDTO;
import com.api.domain.Notice.repository.NoticeEntityRepository;
import com.api.domain.Notice.repository.jooq.NoticeDSLRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeEntityRepository noticeRepository;
    private final NoticeDSLRepository noticeDSLRepository;

    public Page<SelectNoticeForAdminDTO> findAllBySearchCondition(SearchNoticeRequest search, Pageable pageable) {
        return noticeDSLRepository.findAllBySearchCondition(search, pageable);
    }
}
