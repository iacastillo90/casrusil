package com.casrusil.siierpai.modules.period_close.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.period_close.infrastructure.persistence.entity.AiInsightEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiInsightJpaRepository extends JpaRepository<AiInsightEntity, String> {
}
