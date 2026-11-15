package com.casrusil.siierpai.modules.financial_shield.infrastructure.adapter;

import com.casrusil.siierpai.modules.financial_shield.domain.service.CreditRiskService;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CreditRiskApiAdapter implements CreditRiskService {

    @Override
    public List<String> getRiskFactors(CompanyId companyId) {
        // En producción, aquí se integraría con la API real de crédito (Ej: Equifax / DICOM / Poder Judicial).
        // Por ahora retorna una lista vacía para no bloquear los análisis de riesgo.
        List<String> riskFactors = new ArrayList<>();
        return riskFactors;
    }
}
