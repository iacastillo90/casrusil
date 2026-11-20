package com.casrusil.siierpai.modules.payroll.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollProcessRequest {
    private int year;
    private int month;
}
