package com.casrusil.siierpai.modules.annual_close.application.service;

import com.casrusil.siierpai.modules.annual_close.domain.dto.AnnualCloseResponse;

public interface AnnualCloseUseCase {
    AnnualCloseResponse calculateAnnualTaxes(int fiscalYear, String companyId);
}
