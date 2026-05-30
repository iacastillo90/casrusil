package com.casrusil.siierpai.modules.general_ledger.application.service;

import com.casrusil.siierpai.shared.domain.event.PayrollCalculatedEvent;
import com.casrusil.siierpai.shared.domain.event.StockMovedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class JournalEntryService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JournalEntryService.class);

    @EventListener
    public void handleStockMovedEvent(StockMovedEvent event) {
        log.info("Received StockMovedEvent for company {}. Creating Journal Entry...", event.companyId());
        
        // Logic to create a double-entry journal (e.g. Debit Inventory, Credit Bank/Accounts Payable)
        // based on event.movementType()
    }

    @EventListener
    public void handlePayrollCalculatedEvent(PayrollCalculatedEvent event) {
        log.info("Received PayrollCalculatedEvent for company {}. Period: {}. Creating Journal Entry...", 
                event.companyId(), event.period());
                
        // Logic to create a double-entry journal (Debit Salary Expense, Credit Bank & Social Security Payables)
    }
}
