package com.casrusil.siierpai.modules.bankingsync.infrastructure.adapter.out.persistence;

import com.casrusil.siierpai.modules.bankingsync.domain.model.BankTransaction;
import com.casrusil.siierpai.modules.bankingsync.domain.port.out.BankTransactionRepository;
import com.casrusil.siierpai.modules.bankingsync.infrastructure.persistence.entity.BankTransactionEntity;
import com.casrusil.siierpai.modules.bankingsync.infrastructure.persistence.repository.BankTransactionJpaRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class BankTransactionRepositoryAdapter implements BankTransactionRepository {

    private final BankTransactionJpaRepository jpaRepository;

    public BankTransactionRepositoryAdapter(BankTransactionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public BankTransaction save(BankTransaction transaction) {
        BankTransactionEntity saved = jpaRepository.save(toEntity(transaction));
        return toDomain(saved);
    }

    @Override
    public Optional<BankTransaction> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<BankTransaction> findByCompanyId(CompanyId companyId) {
        return jpaRepository.findByCompanyId(companyId.value()).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private BankTransactionEntity toEntity(BankTransaction domain) {
        return new BankTransactionEntity(
                domain.getId(),
                domain.getCompanyId().value(),
                domain.getBankName(),
                domain.getAccountNumber(),
                domain.getTransactionId(),
                domain.getTransactionDate(),
                domain.getDescription(),
                domain.getAmount(),
                domain.getStatus()
        );
    }

    private BankTransaction toDomain(BankTransactionEntity entity) {
        return new BankTransaction(
                entity.getId(),
                CompanyId.of(entity.getCompanyId()),
                entity.getBankName(),
                entity.getAccountNumber(),
                entity.getTransactionId(),
                entity.getTransactionDate(),
                entity.getDescription(),
                entity.getAmount(),
                entity.getStatus()
        );
    }
}
