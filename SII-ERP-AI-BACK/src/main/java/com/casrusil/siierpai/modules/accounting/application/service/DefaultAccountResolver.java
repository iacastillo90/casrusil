package com.casrusil.siierpai.modules.accounting.application.service;

import com.casrusil.siierpai.modules.accounting.domain.model.Account;
import com.casrusil.siierpai.modules.accounting.domain.port.out.AccountRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Resuelve cuentas contables por rol contra el plan de cuentas real de cada
 * empresa.
 *
 * <p>
 * Estrategia:
 * </p>
 * <ol>
 * <li>Usa el código configurado en {@code app.accounts.*} si existe y está
 * activo.</li>
 * <li>Si no, prueba códigos alternativos del otro plan sembrado (hoy conviven
 * el plan canónico {@code 1.1.01.001} y el detallado {@code 110101}).</li>
 * <li>Si no, busca por tipo contable y nombre (ej. "caja", "otros ingresos",
 * "gastos varios").</li>
 * </ol>
 *
 * <p>
 * Así una empresa recién creada funciona aunque solo tenga el plan detallado, y
 * una empresa existente puede usar el plan canónico. No hay códigos literales
 * repartidos en los servicios de negocio.
 * </p>
 */
@Service
public class DefaultAccountResolver {

    private final AccountRepository accountRepository;
    private final Map<DefaultAccountRole, String> configuredCodes;
    private final Map<DefaultAccountRole, List<String>> fallbackCodes;
    private final Map<DefaultAccountRole, Pattern> namePatterns;

    public DefaultAccountResolver(
            AccountRepository accountRepository,
            @Value("${app.accounts.cash:1.1.01.001}") String cash,
            @Value("${app.accounts.sales:4.1.01.001}") String sales,
            @Value("${app.accounts.other-income:4201}") String otherIncome,
            @Value("${app.accounts.office-supplies:5.1.03.025}") String officeSupplies,
            @Value("${app.accounts.services:5.1.03.025}") String services,
            @Value("${app.accounts.other-expense:5.9.99.999}") String otherExpense,
            @Value("${app.accounts.depreciation-expense:5.9.99.999}") String depreciationExpense,
            @Value("${app.accounts.accumulated-depreciation:1.1.03.999}") String accumulatedDepreciation) {
        this.accountRepository = accountRepository;

        Map<DefaultAccountRole, String> configured = new EnumMap<>(DefaultAccountRole.class);
        configured.put(DefaultAccountRole.CASH, cash);
        configured.put(DefaultAccountRole.SALES, sales);
        configured.put(DefaultAccountRole.OTHER_INCOME, otherIncome);
        configured.put(DefaultAccountRole.OFFICE_SUPPLIES, officeSupplies);
        configured.put(DefaultAccountRole.SERVICES, services);
        configured.put(DefaultAccountRole.OTHER_EXPENSE, otherExpense);
        configured.put(DefaultAccountRole.DEPRECIATION_EXPENSE, depreciationExpense);
        configured.put(DefaultAccountRole.ACCUMULATED_DEPRECIATION, accumulatedDepreciation);
        this.configuredCodes = Map.copyOf(configured);

        Map<DefaultAccountRole, List<String>> fallback = new EnumMap<>(DefaultAccountRole.class);
        fallback.put(DefaultAccountRole.CASH, List.of("110101", "1101", "1"));
        fallback.put(DefaultAccountRole.SALES, List.of("410101", "4101", "41", "4"));
        fallback.put(DefaultAccountRole.OTHER_INCOME, List.of("4201", "42", "4"));
        fallback.put(DefaultAccountRole.OFFICE_SUPPLIES, List.of("5204", "5202", "510101", "5"));
        fallback.put(DefaultAccountRole.SERVICES, List.of("5204", "5202", "510101", "5"));
        fallback.put(DefaultAccountRole.OTHER_EXPENSE, List.of("5204", "5202", "510101", "5"));
        fallback.put(DefaultAccountRole.DEPRECIATION_EXPENSE, List.of("5204", "5202", "510101", "5"));
        fallback.put(DefaultAccountRole.ACCUMULATED_DEPRECIATION, List.of("1102", "110101", "1"));
        this.fallbackCodes = Map.copyOf(fallback);

        Map<DefaultAccountRole, Pattern> patterns = new EnumMap<>(DefaultAccountRole.class);
        patterns.put(DefaultAccountRole.CASH, Pattern.compile("caja|efectivo"));
        patterns.put(DefaultAccountRole.SALES, Pattern.compile("venta"));
        patterns.put(DefaultAccountRole.OTHER_INCOME, Pattern.compile("otro|vario|no operacional|diverso"));
        patterns.put(DefaultAccountRole.OFFICE_SUPPLIES, Pattern.compile("oficina|general|administracion"));
        patterns.put(DefaultAccountRole.SERVICES, Pattern.compile("servicio"));
        patterns.put(DefaultAccountRole.OTHER_EXPENSE, Pattern.compile("otro|vario|clasificar|general"));
        patterns.put(DefaultAccountRole.DEPRECIATION_EXPENSE, Pattern.compile("depreciacion"));
        patterns.put(DefaultAccountRole.ACCUMULATED_DEPRECIATION, Pattern.compile("depreciacion|acumulada"));
        this.namePatterns = Map.copyOf(patterns);
    }

    /**
     * Resuelve la cuenta para un rol en la empresa indicada.
     *
     * @param companyId empresa
     * @param role      rol contable
     * @return código de cuenta activo
     * @throws IllegalStateException si el plan de cuentas no tiene una cuenta
     *                               compatible
     */
    public String resolve(CompanyId companyId, DefaultAccountRole role) {
        String configured = configuredCodes.get(role);
        if (isActive(companyId, configured)) {
            return configured;
        }

        for (String candidate : fallbackCodes.getOrDefault(role, List.of())) {
            if (isActive(companyId, candidate)) {
                return candidate;
            }
        }

        return findByTypeAndName(companyId, role);
    }

    /**
     * Resuelve la contrapartida de un movimiento de caja según su categoría.
     *
     * @param companyId empresa
     * @param category  categoría del movimiento (SALES, OFFICE_SUPPLIES, SERVICES u otra)
     * @param isExpense true si es egreso
     * @return código de cuenta activo
     */
    public String resolveCategory(CompanyId companyId, String category, boolean isExpense) {
        if ("SALES".equalsIgnoreCase(category)) {
            return resolve(companyId, DefaultAccountRole.SALES);
        }
        if ("OFFICE_SUPPLIES".equalsIgnoreCase(category)) {
            return resolve(companyId, DefaultAccountRole.OFFICE_SUPPLIES);
        }
        if ("SERVICES".equalsIgnoreCase(category)) {
            return resolve(companyId, DefaultAccountRole.SERVICES);
        }
        return resolve(companyId, isExpense ? DefaultAccountRole.OTHER_EXPENSE : DefaultAccountRole.OTHER_INCOME);
    }

    private boolean isActive(CompanyId companyId, String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        return accountRepository.findByCode(companyId, code)
                .map(Account::isActive)
                .orElse(false);
    }

    private String findByTypeAndName(CompanyId companyId, DefaultAccountRole role) {
        List<Account> accounts = accountRepository.findAll(companyId).stream()
                .filter(Account::isActive)
                .filter(account -> account.getType() == role.getExpectedType())
                .toList();

        Pattern pattern = namePatterns.get(role);
        String byName = accounts.stream()
                .filter(account -> pattern != null && pattern.matcher(normalize(account.getName())).find())
                .max(Comparator.comparingInt(account -> account.getCode().length()))
                .map(Account::getCode)
                .orElse(null);
        if (byName != null) {
            return byName;
        }

        return accounts.stream()
                .max(Comparator.comparingInt(account -> account.getCode().length()))
                .map(Account::getCode)
                .orElseThrow(() -> new IllegalStateException(
                        "No hay cuenta contable activa para el rol " + role + " en la empresa " + companyId.value()));
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}
