package com.casrusil.siierpai.modules.crm.application.service;

import com.casrusil.siierpai.shared.infrastructure.context.TenantGuard;
import com.casrusil.siierpai.modules.crm.domain.model.Lead;
import com.casrusil.siierpai.modules.crm.domain.model.LeadStatus;
import com.casrusil.siierpai.modules.crm.domain.model.Quote;
import com.casrusil.siierpai.modules.crm.domain.model.QuoteLine;
import com.casrusil.siierpai.modules.crm.domain.port.in.CrmUseCase;
import com.casrusil.siierpai.modules.crm.domain.port.out.LeadRepository;
import com.casrusil.siierpai.modules.crm.domain.port.out.QuoteRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class CrmServiceImpl implements CrmUseCase {

    private final LeadRepository leadRepository;
    private final QuoteRepository quoteRepository;

    public CrmServiceImpl(LeadRepository leadRepository, QuoteRepository quoteRepository) {
        this.leadRepository = leadRepository;
        this.quoteRepository = quoteRepository;
    }

    @Override
    @Transactional
    public Lead createLead(CompanyId companyId, String contactName, String companyName, String email, String phone) {
        Lead lead = Lead.create(companyId, contactName, companyName, email, phone);
        return leadRepository.save(lead);
    }

    @Override
    @Transactional
    public Lead updateLeadStatus(UUID leadId, LeadStatus status) {
        Lead lead = TenantGuard.requireOwned(leadRepository.findById(leadId), Lead::getCompanyId, "Lead");
        lead.updateStatus(status);
        return leadRepository.save(lead);
    }

    @Override
    public List<Lead> getLeadsByCompany(CompanyId companyId) {
        return leadRepository.findByCompanyId(companyId);
    }

    @Override
    @Transactional
    public Quote createQuote(CompanyId companyId, UUID leadId, String clientRut, String clientName, LocalDate expirationDate, List<QuoteLine> lines) {
        if (leadId != null) {
            // El lead referenciado debe ser de la misma empresa.
            TenantGuard.requireOwned(leadRepository.findById(leadId), Lead::getCompanyId, "Lead");
        }
        Quote quote = Quote.create(companyId, leadId, clientRut, clientName, expirationDate, lines);
        return quoteRepository.save(quote);
    }

    @Override
    @Transactional
    public Quote acceptQuote(UUID quoteId) {
        Quote quote = TenantGuard.requireOwned(quoteRepository.findById(quoteId), Quote::getCompanyId, "Cotización");
        quote.markAsAccepted();
        
        // TODO: In a real system, emit an event here to trigger the Invoice Generation in Invoicing Module
        
        return quoteRepository.save(quote);
    }

    @Override
    @Transactional
    public Quote rejectQuote(UUID quoteId) {
        Quote quote = TenantGuard.requireOwned(quoteRepository.findById(quoteId), Quote::getCompanyId, "Cotización");
        quote.markAsRejected();
        return quoteRepository.save(quote);
    }

    @Override
    public List<Quote> getQuotesByCompany(CompanyId companyId) {
        return quoteRepository.findByCompanyId(companyId);
    }
}
