package com.api.domain.Notice.entity;


import com.api.common.entity.BaseDateEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notices")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeEntity extends BaseDateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private boolean isMainNotice;

    private boolean isDeleted;

    @PrePersist
    public void prePersist() {
        this.isMainNotice = false;
        this.isDeleted = false;
    }

    public void update(String title, String content, String author, boolean mainNotice) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.isMainNotice = mainNotice;
    }
}
