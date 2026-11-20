package com.casrusil.siierpai.modules.payroll.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Solicitud para crear o abrir el período de remuneraciones.
 *
 * <p>La UF y la UTM son opcionales: si no se envían se usan los parámetros
 * tributarios cargados para el año ({@code tax_parameters}).</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenPayrollPeriodRequest {

    @NotNull(message = "El año es obligatorio")
    @Min(value = 2000, message = "El año debe ser mayor o igual a 2000")
    @Max(value = 2100, message = "El año debe ser menor o igual a 2100")
    private Integer year;

    @NotNull(message = "El mes es obligatorio")
    @Min(value = 1, message = "El mes debe estar entre 1 y 12")
    @Max(value = 12, message = "El mes debe estar entre 1 y 12")
    private Integer month;

    @DecimalMin(value = "0.01", message = "La UF debe ser mayor a 0")
    private BigDecimal ufValue;

    @DecimalMin(value = "0.01", message = "La UTM debe ser mayor a 0")
    private BigDecimal utmValue;
}
