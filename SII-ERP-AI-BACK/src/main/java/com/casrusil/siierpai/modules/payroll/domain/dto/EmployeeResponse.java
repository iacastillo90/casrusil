package com.casrusil.siierpai.modules.payroll.domain.dto;

import com.casrusil.siierpai.modules.payroll.domain.model.Employee;

import java.math.BigDecimal;

/**
 * Empleado expuesto al front (contrato de /payroll/employees).
 *
 * @param id     identificador
 * @param name   nombre completo
 * @param rut    RUT
 * @param base   sueldo base
 * @param afp    AFP
 * @param isapre sistema de salud (Fonasa/Isapre)
 */
public record EmployeeResponse(
        Long id,
        String name,
        String rut,
        BigDecimal base,
        String afp,
        String isapre) {

    public static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getFullName(),
                employee.getRut(),
                employee.getBaseSalary(),
                employee.getAfp(),
                employee.getHealthSystem());
    }
}
