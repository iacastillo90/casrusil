package com.casrusil.siierpai.modules.crm.domain.port.out;

import com.casrusil.siierpai.modules.crm.domain.model.Lead;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeadRepository {
    Lead save(Lead lead);
    Optional<Lead> findById(UUID id);
    List<Lead> findByCompanyId(CompanyId companyId);
}
