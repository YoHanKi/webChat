package com.api.domain.Notice.repository;

import com.api.domain.Notice.entity.NoticeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeEntityRepository extends JpaRepository<NoticeEntity, Long> {
}
