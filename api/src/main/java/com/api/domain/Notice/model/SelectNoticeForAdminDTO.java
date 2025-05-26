package com.api.domain.Notice.model;

import com.api.common.utils.DateUtil;
import com.api.domain.Notice.entity.NoticeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectNoticeForAdminDTO {
    private Long id;

    private String title;

    private String content;

    private String author;

    private boolean isMainNotice;

    private boolean isDeleted;

    private String createdAt;

    public SelectNoticeForAdminDTO(NoticeEntity notice) {
        this.id = notice.getId();
        this.title = notice.getTitle();
        this.content = notice.getContent();
        this.author = notice.getAuthor();
        this.isMainNotice = notice.isMainNotice();
        this.isDeleted = notice.isDeleted();
        this.createdAt = DateUtil.dateTimeToString(notice.getCreateDate(), "yyyy-MM-dd");
    }
}
