package com.casrusil.siierpai.modules.general_ledger.domain.model;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class JournalLine {
    private String accountCode;
    private BigDecimal debit;
    private BigDecimal credit;
}
