package com.casrusil.siierpai.modules.treasury.domain.port.in;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import java.math.BigDecimal;
import java.util.List;

public interface TreasuryAiPredictorUseCase {
    AiForecastReport predictCashFlow(CompanyId companyId, int daysToPredict);

    record AiForecastReport(
        BigDecimal currentLiquidity,
        BigDecimal predictedLiquidity,
        String trend,
        List<String> riskAlerts,
        List<ForecastPoint> dailyForecast
    ) {}

    record ForecastPoint(
        String date,
        BigDecimal estimatedAmount
    ) {}
}
