package com.casrusil.siierpai.modules.crm.infrastructure.adapter.out.persistence;

import com.casrusil.siierpai.modules.crm.domain.model.Lead;
import com.casrusil.siierpai.modules.crm.domain.model.LeadStatus;
import com.casrusil.siierpai.modules.crm.domain.port.out.LeadRepository;
import com.casrusil.siierpai.modules.crm.infrastructure.persistence.entity.LeadEntity;
import com.casrusil.siierpai.modules.crm.infrastructure.persistence.repository.LeadJpaRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class LeadRepositoryAdapter implements LeadRepository {

    private final LeadJpaRepository jpaRepository;

    public LeadRepositoryAdapter(LeadJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Lead save(Lead lead) {
        LeadEntity saved = jpaRepository.save(toEntity(lead));
        return toDomain(saved);
    }

    @Override
    public Optional<Lead> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Lead> findByCompanyId(CompanyId companyId) {
        return jpaRepository.findByCompanyId(companyId.value()).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private LeadEntity toEntity(Lead lead) {
        return new LeadEntity(
                lead.getId(),
                lead.getCompanyId().value(),
                lead.getContactName(),
                lead.getCompanyName(),
                lead.getEmail(),
                lead.getPhone(),
                lead.getStatus().name(),
                lead.getCreatedAt()
        );
    }

    private Lead toDomain(LeadEntity entity) {
        return new Lead(
                entity.getId(),
                CompanyId.of(entity.getCompanyId()),
                entity.getContactName(),
                entity.getCompanyName(),
                entity.getEmail(),
                entity.getPhone(),
                LeadStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt()
        );
    }
}
