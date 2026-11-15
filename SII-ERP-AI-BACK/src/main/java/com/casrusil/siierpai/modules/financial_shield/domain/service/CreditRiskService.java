package com.casrusil.siierpai.modules.financial_shield.domain.service;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import java.util.List;

public interface CreditRiskService {
    List<String> getRiskFactors(CompanyId companyId);
}
