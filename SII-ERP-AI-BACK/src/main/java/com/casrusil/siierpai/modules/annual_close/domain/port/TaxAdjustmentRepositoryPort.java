package com.casrusil.siierpai.modules.annual_close.domain.port;

import com.casrusil.siierpai.modules.annual_close.domain.model.TaxAdjustment;
import java.util.List;

public interface TaxAdjustmentRepositoryPort {
    TaxAdjustment save(TaxAdjustment adjustment);
    List<TaxAdjustment> findByCompanyIdAndFiscalYear(String companyId, int fiscalYear);
}
