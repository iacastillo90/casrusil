package com.casrusil.siierpai.modules.payroll.infrastructure.persistence.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.YearMonth;

/** Persiste YearMonth como varchar(7) 'yyyy-MM' en Postgres. */
@Converter
public class YearMonthStringConverter implements AttributeConverter<YearMonth, String> {

    @Override
    public String convertToDatabaseColumn(YearMonth attribute) {
        return attribute != null ? attribute.toString() : null;
    }

    @Override
    public YearMonth convertToEntityAttribute(String dbData) {
        return dbData != null && !dbData.isBlank() ? YearMonth.parse(dbData) : null;
    }
}
