package com.casrusil.siierpai.modules.annual_close.domain.port;

import com.casrusil.siierpai.modules.annual_close.domain.model.CompanyTaxProfile;
import java.util.Optional;

public interface CompanyTaxProfileRepositoryPort {
    CompanyTaxProfile save(CompanyTaxProfile profile);
    Optional<CompanyTaxProfile> findByCompanyId(String companyId);
}
