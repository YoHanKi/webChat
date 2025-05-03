package com.api.domain.HistoryMessage.repository;

import com.api.domain.HistoryMessage.entity.HistoryMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryMessageRepository extends JpaRepository<HistoryMessageEntity, Long> {
}