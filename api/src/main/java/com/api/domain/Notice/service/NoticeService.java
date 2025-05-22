package com.api.domain.Notice.service;

import com.api.domain.Notice.model.ModifyNoticeRequest;
import com.api.domain.Notice.model.SearchNoticeRequest;
import com.api.domain.Notice.model.SelectNoticeForAdminDTO;
import com.api.domain.Notice.repository.NoticeEntityRepository;
import com.api.domain.Notice.repository.jooq.NoticeDSLRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeEntityRepository noticeRepository;
    private final NoticeDSLRepository noticeDSLRepository;

    @Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
    public Page<SelectNoticeForAdminDTO> findAllBySearchCondition(SearchNoticeRequest search, Pageable pageable) {
        return noticeDSLRepository.findAllBySearchCondition(search, pageable);
    }

    @Transactional(readOnly = true)
    public SelectNoticeForAdminDTO findById(Long id) {
        return noticeRepository.findById(id)
                .map(SelectNoticeForAdminDTO::new)
                .orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다."));
    }

    public void modifyNotice(ModifyNoticeRequest request) {
        noticeRepository.findById(request.id())
                .ifPresentOrElse(notice -> {
                    notice.update(request.title(), request.content(), request.author(), request.isMainNotice());
                    noticeRepository.save(notice);
                }, () -> {
                    throw new IllegalArgumentException("공지사항을 찾을 수 없습니다.");
                });
    }

    public void deleteNotice(Long id) {
        noticeRepository.findById(id)
                .ifPresentOrElse(noticeRepository::delete, () -> {
                    throw new IllegalArgumentException("공지사항을 찾을 수 없습니다.");
                });
    }
}
