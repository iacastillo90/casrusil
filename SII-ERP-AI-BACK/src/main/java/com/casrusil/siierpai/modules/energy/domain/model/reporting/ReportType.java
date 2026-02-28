package com.casrusil.siierpai.modules.energy.domain.model.reporting;

/**
 * Report types for energy reporting.
 */
public enum ReportType {
    /**
     * Daily report covering 24 hours.
     */
    DAILY,

    /**
     * Weekly report covering 7 days.
     */
    WEEKLY,

    /**
     * Monthly report covering a calendar month.
     */
    MONTHLY,

    /**
     * Quarterly report covering 3 months.
     */
    QUARTERLY,

    /**
     * Yearly report covering 12 months.
     */
    YEARLY,

    /**
     * Custom date range report.
     */
    CUSTOM
}
