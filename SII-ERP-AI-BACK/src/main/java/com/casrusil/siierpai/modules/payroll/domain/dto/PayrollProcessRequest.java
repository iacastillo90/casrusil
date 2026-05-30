package com.casrusil.siierpai.modules.payroll.domain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PayrollProcessRequest {
    private int year;
    private int month;
}
