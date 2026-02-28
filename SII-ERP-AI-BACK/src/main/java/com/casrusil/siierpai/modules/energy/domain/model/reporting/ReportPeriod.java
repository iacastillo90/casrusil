package com.casrusil.siierpai.modules.energy.domain.model.reporting;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Value object representing a report period.
 */
public class ReportPeriod {
    private final LocalDate startDate;
    private final LocalDate endDate;

    private ReportPeriod(LocalDate startDate, LocalDate endDate) {
        this.startDate = Objects.requireNonNull(startDate, "Start date cannot be null");
        this.endDate = Objects.requireNonNull(endDate, "End date cannot be null");

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
    }

    public static ReportPeriod of(LocalDate startDate, LocalDate endDate) {
        return new ReportPeriod(startDate, endDate);
    }

    /**
     * Creates a daily period for the given date.
     */
    public static ReportPeriod daily(LocalDate date) {
        return new ReportPeriod(date, date);
    }

    /**
     * Creates a weekly period ending on the given date.
     */
    public static ReportPeriod weekly(LocalDate endDate) {
        return new ReportPeriod(endDate.minusDays(6), endDate);
    }

    /**
     * Creates a monthly period for the given year and month.
     */
    public static ReportPeriod monthly(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        return new ReportPeriod(startDate, endDate);
    }

    /**
     * Creates a yearly period for the given year.
     */
    public static ReportPeriod yearly(int year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        return new ReportPeriod(startDate, endDate);
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public long getDays() {
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ReportPeriod that = (ReportPeriod) o;
        return Objects.equals(startDate, that.startDate) &&
                Objects.equals(endDate, that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startDate, endDate);
    }

    @Override
    public String toString() {
        return startDate + " to " + endDate;
    }
}
