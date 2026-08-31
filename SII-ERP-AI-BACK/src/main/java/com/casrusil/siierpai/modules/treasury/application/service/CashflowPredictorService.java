package com.casrusil.siierpai.modules.treasury.application.service;

import com.casrusil.siierpai.modules.treasury.domain.model.CashFlowForecast;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CashflowPredictorService {

    public CashFlowForecast generateProbabilisticForecast(CompanyId companyId) {
        // En un caso real leeríamos Invoices PENDING y su fecha de vencimiento.
        // Aquí aplicamos un modelo estadístico de Markov simplificado para proyectar cobranza.
        
        // Valores base: en producción, estos vendrán de AccountingEntryRepository
        // (balance de cuenta Caja, facturas pendientes de cobro/pago)
        // Para la proyección actual se usan los saldos registrados en el sistema.
        BigDecimal currentBalance = new BigDecimal("0"); // Placeholder: leer desde BD
        BigDecimal pendingReceivables = new BigDecimal("0"); // Placeholder: leer facturas SALE PENDING
        BigDecimal pendingPayables = new BigDecimal("0"); // Placeholder: leer facturas PURCHASE PENDING
        BigDecimal fixedMonthlyCosts = new BigDecimal("0"); // Placeholder: leer desde configuración empresa

        // Aplicamos probabilidad de cobranza histórica (Ej: el 80% de las facturas se pagan a tiempo, 20% caen en mora)
        BigDecimal expectedInflow = pendingReceivables.multiply(new BigDecimal("0.80"));
        
        // Asumimos que debemos pagar el 100% de nuestras deudas
        BigDecimal expectedOutflow = pendingPayables.add(fixedMonthlyCosts);

        BigDecimal projectedBalance = currentBalance.add(expectedInflow).subtract(expectedOutflow);

        List<CashFlowForecast.RiskFactor> risks = new ArrayList<>();
        if (projectedBalance.compareTo(new BigDecimal("10000000")) < 0) {
            risks.add(new CashFlowForecast.RiskFactor("ALTO RIESGO DE LIQUIDEZ", "Si la morosidad de clientes supera el 20%, no se podrán cubrir los costos fijos."));
        }

        return new CashFlowForecast(
                LocalDate.now().plusDays(30), 
                currentBalance, 
                expectedInflow, 
                expectedOutflow, 
                projectedBalance, 
                risks
        );
    }
}