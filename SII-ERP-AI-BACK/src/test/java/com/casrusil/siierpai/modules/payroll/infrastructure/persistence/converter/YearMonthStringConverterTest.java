package com.casrusil.siierpai.modules.payroll.infrastructure.persistence.converter;

import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;

class YearMonthStringConverterTest {

    private final YearMonthStringConverter converter = new YearMonthStringConverter();

    @Test
    void shouldRoundTripYearMonthAsVarchar() {
        YearMonth period = YearMonth.of(2026, 11);

        String dbValue = converter.convertToDatabaseColumn(period);

        assertEquals("2026-11", dbValue);
        assertEquals(period, converter.convertToEntityAttribute(dbValue));
    }
}
