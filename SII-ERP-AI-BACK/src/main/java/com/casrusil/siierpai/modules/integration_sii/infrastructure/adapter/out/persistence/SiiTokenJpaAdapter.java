package com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.out.persistence;

import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiToken;
import com.casrusil.siierpai.modules.integration_sii.domain.port.out.SiiTokenRepository;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.out.persistence.entity.SiiTokenEntity;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.out.persistence.repository.SiiTokenJpaRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class SiiTokenJpaAdapter implements SiiTokenRepository {

    private final SiiTokenJpaRepository jpaRepo;

    public SiiTokenJpaAdapter(SiiTokenJpaRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public void save(CompanyId companyId, SiiToken token) {
        SiiTokenEntity e = jpaRepo.findByCompanyId(companyId.value().toString()).orElse(new SiiTokenEntity());
        if (e.getId() == null) e.setId(UUID.randomUUID());
        e.setCompanyId(companyId.value().toString());
        e.setToken(token.token());
        LocalDateTime expiration = LocalDateTime.ofInstant(token.expiresAt(), ZoneId.systemDefault());
        e.setExpiration(expiration);
        jpaRepo.save(e);
    }

    @Override
    public Optional<SiiToken> findByCompanyId(CompanyId companyId) {
        return jpaRepo.findByCompanyId(companyId.value().toString())
            .map(e -> {
                Instant expiresAt = e.getExpiration().atZone(ZoneId.systemDefault()).toInstant();
                return new SiiToken(e.getToken(), expiresAt);
            });
    }

    @Override
    public Map<CompanyId, SiiToken> findAll() {
        return jpaRepo.findAll().stream()
            .collect(Collectors.toMap(
                e -> CompanyId.fromString(e.getCompanyId()),
                e -> {
                    Instant expiresAt = e.getExpiration().atZone(ZoneId.systemDefault()).toInstant();
                    return new SiiToken(e.getToken(), expiresAt);
                }
            ));
    }

    @Override
    public void delete(CompanyId companyId) {
        jpaRepo.findByCompanyId(companyId.value().toString()).ifPresent(jpaRepo::delete);
    }
}