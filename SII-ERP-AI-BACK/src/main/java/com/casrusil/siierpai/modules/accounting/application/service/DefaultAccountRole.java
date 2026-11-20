package com.casrusil.siierpai.modules.accounting.application.service;

import com.casrusil.siierpai.modules.accounting.domain.model.AccountType;

/**
 * Roles contables que necesitan una cuenta por defecto.
 *
 * <p>
 * El código concreto no vive en los servicios: se resuelve contra el plan de
 * cuentas real de la empresa (ver {@link DefaultAccountResolver}).
 * </p>
 */
public enum DefaultAccountRole {

    CASH(AccountType.ASSET),
    SALES(AccountType.REVENUE),
    OTHER_INCOME(AccountType.REVENUE),
    OFFICE_SUPPLIES(AccountType.EXPENSE),
    SERVICES(AccountType.EXPENSE),
    OTHER_EXPENSE(AccountType.EXPENSE),
    DEPRECIATION_EXPENSE(AccountType.EXPENSE),
    ACCUMULATED_DEPRECIATION(AccountType.ASSET);

    private final AccountType expectedType;

    DefaultAccountRole(AccountType expectedType) {
        this.expectedType = expectedType;
    }

    public AccountType getExpectedType() {
        return expectedType;
    }
}
