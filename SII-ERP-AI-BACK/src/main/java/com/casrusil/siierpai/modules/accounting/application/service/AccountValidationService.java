package com.casrusil.siierpai.modules.accounting.application.service;

import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntryLine;
import com.casrusil.siierpai.modules.accounting.domain.model.AuditAlert;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Servicio para validar el uso correcto de cuentas contables.
 * Previene el uso de cuentas inapropiadas según el tipo de negocio.
 */
@Service
public class AccountValidationService {

    // Cuentas que NO deben usarse en empresas de servicios.
    // Se usa el prefijo del plan canónico (1.1.03) porque el plan detallado no
    // define cuentas de inventario.
    private static final Set<String> INVENTORY_ACCOUNTS = Set.of(
            "1.1.03" // Existencias
    );

    // Cuentas que requieren documentación especial (provisiones del plan detallado).
    private static final Set<String> SPECIAL_ACCOUNTS = Set.of(
            "2106", // Provisiones
            "210601", // Provisión Vacaciones
            "2.1.06" // Provisión (plan canónico, si se agrega)
    );

    // Cuentas que no deben tener saldo negativo.
    private static final Set<String> POSITIVE_ONLY_ACCOUNTS = Set.of(
            "1101", // Caja (plan detallado)
            "1102", // Bancos (plan detallado)
            "1.1.01", // Caja (plan canónico)
            "1.1.03" // Existencias (plan canónico)
    );

    /**
     * Valida un asiento contable y retorna alertas si encuentra problemas.
     */
    public List<AuditAlert> validateEntry(AccountingEntry entry, String companyBusinessType) {
        List<AuditAlert> alerts = new ArrayList<>();

        for (AccountingEntryLine line : entry.getLines()) {
            // Validar uso de cuentas de inventario en empresas de servicios
            if ("SERVICE".equals(companyBusinessType) && isInventoryAccount(line.accountCode())) {
                alerts.add(createInvalidAccountAlert(
                        entry,
                        line.accountCode(),
                        "Las empresas de servicios no deberían usar cuentas de inventario"));
            }

            // Validar cuentas especiales
            if (isSpecialAccount(line.accountCode())) {
                alerts.add(createSpecialAccountAlert(entry, line.accountCode()));
            }

            // Validar cuentas que no deben tener saldo negativo
            if (isPositiveOnlyAccount(line.accountCode())) {
                // Nota: Aquí necesitaríamos el saldo actual de la cuenta
                // Por ahora solo alertamos si hay un crédito grande
                if (line.credit().compareTo(line.debit()) > 0) {
                    alerts.add(createNegativeBalanceWarning(entry, line.accountCode()));
                }
            }
        }

        return alerts;
    }

    /**
     * Valida que un código de cuenta exista y esté bien formado.
     */
    public boolean isValidAccountCode(String accountCode) {
        if (accountCode == null || accountCode.isEmpty()) {
            return false;
        }

        // Validar formato: plan detallado (4-6 dígitos) o plan canónico
        // (segmentos separados por punto, ej. 1.1.01.001).
        if (!accountCode.matches("^(\\d{4,6}|\\d{1,2}(\\.\\d{1,3}){1,4})$")) {
            return false;
        }

        // Validar que el primer dígito sea válido (1-5 para activo, pasivo, patrimonio,
        // ingresos, gastos)
        char firstDigit = accountCode.charAt(0);
        return firstDigit >= '1' && firstDigit <= '5';
    }

    private boolean isInventoryAccount(String accountCode) {
        return INVENTORY_ACCOUNTS.stream()
                .anyMatch(inv -> accountCode.startsWith(inv));
    }

    private boolean isSpecialAccount(String accountCode) {
        return SPECIAL_ACCOUNTS.stream()
                .anyMatch(special -> accountCode.startsWith(special));
    }

    private boolean isPositiveOnlyAccount(String accountCode) {
        return POSITIVE_ONLY_ACCOUNTS.stream()
                .anyMatch(positive -> accountCode.startsWith(positive));
    }

    private AuditAlert createInvalidAccountAlert(AccountingEntry entry, String accountCode, String reason) {
        String title = "Cuenta Contable Inapropiada";
        String description = String.format(
                "El asiento '%s' usa la cuenta %s. %s",
                entry.getDescription(),
                accountCode,
                reason);

        return new AuditAlert(
                AuditAlert.Type.INVALID_ACCOUNT,
                AuditAlert.Severity.WARNING,
                title,
                description,
                entry.getId().toString(),
                "Revisar si la cuenta es apropiada para este tipo de transacción.");
    }

    private AuditAlert createSpecialAccountAlert(AccountingEntry entry, String accountCode) {
        String title = "Cuenta Especial Detectada";
        String description = String.format(
                "El asiento '%s' usa la cuenta especial %s que requiere documentación adicional.",
                entry.getDescription(),
                accountCode);

        return new AuditAlert(
                AuditAlert.Type.INVALID_ACCOUNT,
                AuditAlert.Severity.INFO,
                title,
                description,
                entry.getId().toString(),
                "Asegurar que existe la documentación de respaldo apropiada.");
    }

    private AuditAlert createNegativeBalanceWarning(AccountingEntry entry, String accountCode) {
        String title = "Posible Saldo Negativo";
        String description = String.format(
                "El asiento '%s' podría generar un saldo negativo en la cuenta %s, lo cual no es permitido.",
                entry.getDescription(),
                accountCode);

        return new AuditAlert(
                AuditAlert.Type.INVALID_ACCOUNT,
                AuditAlert.Severity.WARNING,
                title,
                description,
                entry.getId().toString(),
                "Verificar el saldo de la cuenta antes de registrar el asiento.");
    }
}
