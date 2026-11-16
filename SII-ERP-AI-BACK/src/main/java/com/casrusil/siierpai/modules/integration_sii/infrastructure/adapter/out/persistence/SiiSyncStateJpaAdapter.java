package com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.out.persistence;

import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiSyncState;
import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiSyncStatusValue;
import com.casrusil.siierpai.modules.integration_sii.domain.port.out.SiiSyncStateRepository;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.out.persistence.entity.SiiSyncStateEntity;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.out.persistence.repository.SiiSyncStateJpaRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Persistence adapter for {@link SiiSyncState}.
 */
@Component
public class SiiSyncStateJpaAdapter implements SiiSyncStateRepository {

    private static final String DEFAULT_STATUS = SiiSyncStatusValue.PENDING.name();

    private final SiiSyncStateJpaRepository jpaRepository;

    public SiiSyncStateJpaAdapter(SiiSyncStateJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<SiiSyncState> findByCompanyId(CompanyId companyId) {
        return jpaRepository.findById(companyId.value()).map(this::toDomain);
    }

    @Override
    public SiiSyncState save(SiiSyncState state) {
        SiiSyncStateEntity entity = jpaRepository.findById(state.companyId().value())
                .orElseGet(SiiSyncStateEntity::new);
        entity.setCompanyId(state.companyId().value());
        entity.setLastSyncAt(state.lastSyncAt());
        entity.setNextSyncAt(state.nextSyncAt());
        entity.setSiiStatus(statusName(state.siiStatus()));
        entity.setCafStatus(statusName(state.cafStatus()));
        entity.setDteStatus(statusName(state.dteStatus()));
        entity.setLastError(state.lastError());
        entity.setUpdatedAt(state.updatedAt());
        return toDomain(jpaRepository.save(entity));
    }

    private SiiSyncState toDomain(SiiSyncStateEntity entity) {
        return new SiiSyncState(
                new CompanyId(entity.getCompanyId()),
                entity.getLastSyncAt(),
                entity.getNextSyncAt(),
                parseStatus(entity.getSiiStatus()),
                parseStatus(entity.getCafStatus()),
                parseStatus(entity.getDteStatus()),
                entity.getLastError(),
                entity.getUpdatedAt());
    }

    private String statusName(SiiSyncStatusValue value) {
        return value != null ? value.name() : DEFAULT_STATUS;
    }

    private SiiSyncStatusValue parseStatus(String value) {
        if (value == null || value.isBlank()) {
            return SiiSyncStatusValue.PENDING;
        }
        try {
            return SiiSyncStatusValue.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return SiiSyncStatusValue.PENDING;
        }
    }
}
