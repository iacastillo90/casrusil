package com.casrusil.siierpai.modules.treasury.application.service;

import com.casrusil.siierpai.modules.treasury.domain.port.in.TreasuryAiPredictorUseCase;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class TreasuryAiPredictorServiceImpl implements TreasuryAiPredictorUseCase {

    @Override
    public AiForecastReport predictCashFlow(CompanyId companyId, int daysToPredict) {
        // En una implementación real, aquí se consultaría a Vertex AI u otra API externa.
        // Para este MVP, utilizaremos heurísticas para simular el modelo predictivo.
        
        BigDecimal currentLiquidity = new BigDecimal("15000000"); // Base simulada
        BigDecimal currentAmount = currentLiquidity;
        List<ForecastPoint> dailyForecast = new ArrayList<>();
        List<String> riskAlerts = new ArrayList<>();
        
        LocalDate currentDate = LocalDate.now();
        
        for (int i = 1; i <= daysToPredict; i++) {
            currentDate = currentDate.plusDays(1);
            
            // Simulación de fluctuación diaria (ingresos vs gastos)
            // Agregamos caídas bruscas simulando pagos de IVA o sueldos los fines de mes.
            // Fluctuación diaria simulada con seed determinista (no Math.random)
            // basada en el día del mes para reproducibilidad
            int seed = currentDate.getDayOfMonth();
            BigDecimal dailyChange = new BigDecimal(seed % 2 == 0 ? 800000 : -600000); 
            
            if (currentDate.getDayOfMonth() == 20) {
                // Pago de impuestos (IVA F29)
                dailyChange = dailyChange.subtract(new BigDecimal("3500000"));
                riskAlerts.add("Alerta: Alto volumen de salida proyectado para F29 el " + currentDate);
            }
            if (currentDate.getDayOfMonth() == 30) {
                // Pago de remuneraciones
                dailyChange = dailyChange.subtract(new BigDecimal("5000000"));
            }
            
            currentAmount = currentAmount.add(dailyChange);
            dailyForecast.add(new ForecastPoint(currentDate.toString(), currentAmount));
            
            if (currentAmount.compareTo(new BigDecimal("2000000")) < 0 && riskAlerts.isEmpty()) {
                riskAlerts.add("Peligro de Iliquidez (Liquidity Crunch) alrededor del " + currentDate);
            }
        }
        
        String trend = currentAmount.compareTo(currentLiquidity) > 0 ? "POSITIVE" : "NEGATIVE";
        
        return new AiForecastReport(currentLiquidity, currentAmount, trend, riskAlerts, dailyForecast);
    }
}
