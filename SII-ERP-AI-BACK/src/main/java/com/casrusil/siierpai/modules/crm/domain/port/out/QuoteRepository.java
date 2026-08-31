package com.casrusil.siierpai.modules.crm.domain.port.out;

import com.casrusil.siierpai.modules.crm.domain.model.Quote;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuoteRepository {
    Quote save(Quote quote);
    Optional<Quote> findById(UUID id);
    List<Quote> findByCompanyId(CompanyId companyId);
}
