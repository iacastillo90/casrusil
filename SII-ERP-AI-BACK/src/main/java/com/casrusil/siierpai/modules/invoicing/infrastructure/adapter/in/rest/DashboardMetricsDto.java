package com.casrusil.siierpai.modules.invoicing.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.model.PaymentStatus;
import com.casrusil.siierpai.modules.invoicing.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * DTO para métricas del dashboard financiero.
 * Contiene KPIs pre-calculados para evitar múltiples llamadas al backend
 * y reducir el tráfico de red.
 */
public record DashboardMetricsDto(
        double currentMonthSales,
        double currentMonthVat,
        int currentMonthCount,
        double previousMonthSales,
        Double salesDeltaPct,
        int pendingCount,
        double pendingTotal,
        double averageTicket,
        List<MonthlySalesPoint> monthlySales
) {
    public static DashboardMetricsDto from(List<Invoice> invoices) {
        LocalDate now = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(now);
        YearMonth previousMonth = currentMonth.minusMonths(1);

        // Filter sales only (transactionType == SALE)
        List<Invoice> sales = invoices.stream()
                .filter(i -> i.getTransactionType() == TransactionType.SALE)
                .collect(Collectors.toList());

        // Current month metrics
        List<Invoice> currentMonthInvoices = sales.stream()
                .filter(i -> YearMonth.from(i.getDate()).equals(currentMonth))
                .collect(Collectors.toList());

        BigDecimal currentMonthSales = currentMonthInvoices.stream()
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal currentMonthVat = currentMonthInvoices.stream()
                .map(Invoice::getTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int currentMonthCount = currentMonthInvoices.size();

        // Previous month metrics
        List<Invoice> previousMonthInvoices = sales.stream()
                .filter(i -> YearMonth.from(i.getDate()).equals(previousMonth))
                .collect(Collectors.toList());

        BigDecimal previousMonthSales = previousMonthInvoices.stream()
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Sales delta percentage
        Double salesDeltaPct = previousMonthSales.compareTo(BigDecimal.ZERO) > 0
                ? currentMonthSales.subtract(previousMonthSales)
                        .divide(previousMonthSales, 4, java.math.RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue()
                : null;

        // Pending invoices (PENDING or OVERDUE status)
        List<Invoice> pending = invoices.stream()
                .filter(i -> i.getStatus() == PaymentStatus.PENDING || i.getStatus() == PaymentStatus.OVERDUE)
                .collect(Collectors.toList());

        int pendingCount = pending.size();
        BigDecimal pendingTotal = pending.stream()
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Average ticket
        BigDecimal averageTicket = currentMonthCount > 0
                ? currentMonthSales.divide(BigDecimal.valueOf(currentMonthCount), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Monthly sales (last 12 months)
        List<MonthlySalesPoint> monthlySales = calculateMonthlySales(sales, now);

        return new DashboardMetricsDto(
                currentMonthSales.doubleValue(),
                currentMonthVat.doubleValue(),
                currentMonthCount,
                previousMonthSales.doubleValue(),
                salesDeltaPct,
                pendingCount,
                pendingTotal.doubleValue(),
                averageTicket.doubleValue(),
                monthlySales
        );
    }

    private static List<MonthlySalesPoint> calculateMonthlySales(List<Invoice> sales, LocalDate now) {
        List<MonthlySalesPoint> points = new java.util.ArrayList<>();
        String[] monthNames = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
        for (int idx = 11; idx >= 0; idx--) {
            YearMonth month = YearMonth.from(now).minusMonths(idx);
            BigDecimal total = sales.stream()
                    .filter(inv -> YearMonth.from(inv.getDate()).equals(month))
                    .map(Invoice::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String monthLabel = monthNames[month.getMonthValue() - 1];

            points.add(new MonthlySalesPoint(monthLabel, total.doubleValue()));
        }
        return points;
    }

    public record MonthlySalesPoint(String name, double total) {}
}