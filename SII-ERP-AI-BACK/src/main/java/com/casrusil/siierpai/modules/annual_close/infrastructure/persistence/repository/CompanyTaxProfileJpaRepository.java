package com.casrusil.siierpai.modules.annual_close.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.annual_close.infrastructure.persistence.entity.CompanyTaxProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyTaxProfileJpaRepository extends JpaRepository<CompanyTaxProfileEntity, String> {
}
