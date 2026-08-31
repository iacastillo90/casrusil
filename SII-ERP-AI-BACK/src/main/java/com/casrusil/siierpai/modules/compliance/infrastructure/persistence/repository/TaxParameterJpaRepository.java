package com.casrusil.siierpai.modules.compliance.infrastructure.persistence.repository;
import com.casrusil.siierpai.modules.compliance.infrastructure.persistence.entity.TaxParameterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
public interface TaxParameterJpaRepository extends JpaRepository<TaxParameterEntity, String> {}