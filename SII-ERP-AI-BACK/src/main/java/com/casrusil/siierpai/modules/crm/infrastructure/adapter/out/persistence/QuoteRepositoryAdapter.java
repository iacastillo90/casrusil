package com.casrusil.siierpai.modules.crm.infrastructure.adapter.out.persistence;

import com.casrusil.siierpai.modules.crm.domain.model.Quote;
import com.casrusil.siierpai.modules.crm.domain.model.QuoteLine;
import com.casrusil.siierpai.modules.crm.domain.port.out.QuoteRepository;
import com.casrusil.siierpai.modules.crm.infrastructure.persistence.entity.QuoteEntity;
import com.casrusil.siierpai.modules.crm.infrastructure.persistence.entity.QuoteLineEntity;
import com.casrusil.siierpai.modules.crm.infrastructure.persistence.repository.QuoteJpaRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class QuoteRepositoryAdapter implements QuoteRepository {

    private final QuoteJpaRepository jpaRepository;

    public QuoteRepositoryAdapter(QuoteJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Quote save(Quote quote) {
        QuoteEntity saved = jpaRepository.save(toEntity(quote));
        return toDomain(saved);
    }

    @Override
    public Optional<Quote> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Quote> findByCompanyId(CompanyId companyId) {
        return jpaRepository.findByCompanyId(companyId.value()).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private QuoteEntity toEntity(Quote domain) {
        List<QuoteLineEntity> lineEntities = domain.getLines().stream()
                .map(l -> new QuoteLineEntity(
                        l.getId(), l.getDescription(),
                        l.getQuantity(), l.getUnitPrice(), l.getTotalAmount()
                )).collect(Collectors.toList());

        return new QuoteEntity(
                domain.getId(),
                domain.getCompanyId().value(),
                domain.getLeadId(),
                domain.getClientRut(),
                domain.getClientName(),
                domain.getQuoteDate(),
                domain.getExpirationDate(),
                domain.getStatus(),
                domain.getTotalAmount(),
                lineEntities
        );
    }

    private Quote toDomain(QuoteEntity entity) {
        List<QuoteLine> lines = entity.getLines().stream()
                .map(l -> new QuoteLine(
                        l.getId(), l.getDescription(),
                        l.getQuantity(), l.getUnitPrice()
                )).collect(Collectors.toList());

        return new Quote(
                entity.getId(),
                CompanyId.of(entity.getCompanyId()),
                entity.getLeadId(),
                entity.getClientRut(),
                entity.getClientName(),
                entity.getQuoteDate(),
                entity.getExpirationDate(),
                entity.getStatus(),
                lines
        );
    }
}
