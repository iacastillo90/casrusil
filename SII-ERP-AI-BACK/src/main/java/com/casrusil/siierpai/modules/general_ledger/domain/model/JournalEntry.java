package com.casrusil.siierpai.modules.general_ledger.domain.model;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class JournalEntry {
    private String id;
    private String companyId;
    private LocalDateTime date;
    private String description;
    private List<JournalLine> lines;
}
