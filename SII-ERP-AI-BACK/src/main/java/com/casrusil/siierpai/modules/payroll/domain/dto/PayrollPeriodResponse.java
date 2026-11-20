package com.casrusil.siierpai.modules.payroll.domain.dto;

import com.casrusil.siierpai.modules.payroll.domain.model.PayrollPeriod;

import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * Período de remuneraciones expuesto al front.
 *
 * @param id      identificador del período
 * @param period  período en formato {@code yyyy-MM}
 * @param ufValue valor de la UF usado en el cálculo
 * @param utmValue valor de la UTM usado en el cálculo
 * @param closed  si el período está cerrado
 */
public record PayrollPeriodResponse(
        Long id,
        String period,
        BigDecimal ufValue,
        BigDecimal utmValue,
        boolean closed) {

    public static PayrollPeriodResponse from(PayrollPeriod period) {
        YearMonth ym = period.getPeriod();
        return new PayrollPeriodResponse(
                period.getId(),
                ym != null ? ym.toString() : null,
                period.getUfValue(),
                period.getUtmValue(),
                period.isClosed());
    }
}
