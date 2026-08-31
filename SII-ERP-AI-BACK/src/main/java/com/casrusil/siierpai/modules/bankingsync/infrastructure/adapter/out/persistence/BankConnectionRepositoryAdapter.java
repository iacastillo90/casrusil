package com.casrusil.siierpai.modules.bankingsync.infrastructure.adapter.out.persistence;

import com.casrusil.siierpai.modules.bankingsync.domain.model.BankConnection;
import com.casrusil.siierpai.modules.bankingsync.domain.port.out.BankConnectionRepository;
import com.casrusil.siierpai.modules.bankingsync.infrastructure.persistence.entity.BankConnectionEntity;
import com.casrusil.siierpai.modules.bankingsync.infrastructure.persistence.repository.BankConnectionJpaRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class BankConnectionRepositoryAdapter implements BankConnectionRepository {

    private final BankConnectionJpaRepository jpaRepository;

    public BankConnectionRepositoryAdapter(BankConnectionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public BankConnection save(BankConnection connection) {
        BankConnectionEntity saved = jpaRepository.save(toEntity(connection));
        return toDomain(saved);
    }

    @Override
    public Optional<BankConnection> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<BankConnection> findByCompanyId(CompanyId companyId) {
        return jpaRepository.findByCompanyId(companyId.value()).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private BankConnectionEntity toEntity(BankConnection domain) {
        return new BankConnectionEntity(
                domain.getId(),
                domain.getCompanyId().value(),
                domain.getBankName(),
                domain.getAccountNumber(),
                domain.getStatus(),
                domain.getLastSync()
        );
    }

    private BankConnection toDomain(BankConnectionEntity entity) {
        return new BankConnection(
                entity.getId(),
                CompanyId.of(entity.getCompanyId()),
                entity.getBankName(),
                entity.getAccountNumber(),
                entity.getStatus(),
                entity.getLastSync()
        );
    }
}
