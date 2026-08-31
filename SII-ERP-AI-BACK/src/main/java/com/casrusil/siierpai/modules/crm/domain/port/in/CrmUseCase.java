package com.casrusil.siierpai.modules.crm.domain.port.in;

import com.casrusil.siierpai.modules.crm.domain.model.Lead;
import com.casrusil.siierpai.modules.crm.domain.model.LeadStatus;
import com.casrusil.siierpai.modules.crm.domain.model.Quote;
import com.casrusil.siierpai.modules.crm.domain.model.QuoteLine;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface CrmUseCase {
    Lead createLead(CompanyId companyId, String contactName, String companyName, String email, String phone);
    Lead updateLeadStatus(UUID leadId, LeadStatus status);
    List<Lead> getLeadsByCompany(CompanyId companyId);

    Quote createQuote(CompanyId companyId, UUID leadId, String clientRut, String clientName, LocalDate expirationDate, List<QuoteLine> lines);
    Quote acceptQuote(UUID quoteId);
    Quote rejectQuote(UUID quoteId);
    List<Quote> getQuotesByCompany(CompanyId companyId);
}
