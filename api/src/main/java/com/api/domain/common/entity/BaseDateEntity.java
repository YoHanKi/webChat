package com.api.domain.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseDateEntity implements Serializable {
    @CreatedDate
    @Column(updatable = false, nullable = false, columnDefinition = "TIMESTAMP(6)")
    private LocalDateTime createDate;

    @LastModifiedDate
    @Column(nullable = false, columnDefinition = "TIMESTAMP(6)")
    private LocalDateTime modifiedDate;
}