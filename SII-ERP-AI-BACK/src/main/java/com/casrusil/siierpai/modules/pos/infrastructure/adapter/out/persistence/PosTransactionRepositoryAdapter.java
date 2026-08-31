package com.casrusil.siierpai.modules.pos.infrastructure.adapter.out.persistence;

import com.casrusil.siierpai.modules.pos.domain.model.PosTransaction;
import com.casrusil.siierpai.modules.pos.domain.model.PosTransactionItem;
import com.casrusil.siierpai.modules.pos.domain.port.out.PosTransactionRepository;
import com.casrusil.siierpai.modules.pos.infrastructure.persistence.entity.PosTransactionEntity;
import com.casrusil.siierpai.modules.pos.infrastructure.persistence.entity.PosTransactionItemEntity;
import com.casrusil.siierpai.modules.pos.infrastructure.persistence.repository.PosTransactionJpaRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PosTransactionRepositoryAdapter implements PosTransactionRepository {

    private final PosTransactionJpaRepository jpaRepository;

    public PosTransactionRepositoryAdapter(PosTransactionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PosTransaction save(PosTransaction transaction) {
        PosTransactionEntity saved = jpaRepository.save(toEntity(transaction));
        return toDomain(saved);
    }

    @Override
    public Optional<PosTransaction> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<PosTransaction> findByCompanyId(CompanyId companyId) {
        return jpaRepository.findByCompanyId(companyId.value()).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PosTransaction> findPendingSync(CompanyId companyId) {
        return jpaRepository.findByCompanyIdAndStatus(companyId.value(), "PENDING_SYNC").stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private PosTransactionEntity toEntity(PosTransaction domain) {
        List<PosTransactionItemEntity> items = domain.getItems().stream()
                .map(i -> new PosTransactionItemEntity(
                        i.getId(), i.getProductId(), i.getProductName(),
                        i.getQuantity(), i.getUnitPrice(), i.getSubtotal()
                )).collect(Collectors.toList());

        return new PosTransactionEntity(
                domain.getId(),
                domain.getCompanyId().value(),
                domain.getPosTerminalId(),
                domain.getCashierName(),
                domain.getTransactionDate(),
                domain.getTotalAmount(),
                domain.getPaymentMethod(),
                domain.getStatus(),
                items
        );
    }

    private PosTransaction toDomain(PosTransactionEntity entity) {
        List<PosTransactionItem> items = entity.getItems().stream()
                .map(i -> new PosTransactionItem(
                        i.getId(), i.getProductId(), i.getProductName(),
                        i.getQuantity(), i.getUnitPrice()
                )).collect(Collectors.toList());

        return new PosTransaction(
                entity.getId(),
                CompanyId.of(entity.getCompanyId()),
                entity.getPosTerminalId(),
                entity.getCashierName(),
                entity.getTransactionDate(),
                entity.getPaymentMethod(),
                entity.getStatus(),
                items
        );
    }
}
