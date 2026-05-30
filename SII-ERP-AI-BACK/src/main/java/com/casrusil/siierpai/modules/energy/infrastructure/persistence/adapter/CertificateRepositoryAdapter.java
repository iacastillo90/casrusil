package com.casrusil.siierpai.modules.energy.infrastructure.persistence.adapter;

import com.casrusil.siierpai.modules.energy.domain.model.StationId;
import com.casrusil.siierpai.modules.energy.domain.model.security.Certificate;
import com.casrusil.siierpai.modules.energy.domain.model.security.CertificateId;
import com.casrusil.siierpai.modules.energy.domain.model.security.CertificateStatus;
import com.casrusil.siierpai.modules.energy.domain.model.security.CertificateType;
import com.casrusil.siierpai.modules.energy.domain.port.out.CertificateRepository;
import com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity.CertificateEntity;
import com.casrusil.siierpai.modules.energy.infrastructure.persistence.repository.CertificateJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JPA Adapter for CertificateRepository.
 * 
 * <p>
 * Implements the hexagonal architecture port by adapting between
 * the domain model and JPA entities.
 * 
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class CertificateRepositoryAdapter implements CertificateRepository {

    private final CertificateJpaRepository jpaRepository;

    @Override
    public Certificate save(Certificate certificate) {
        CertificateEntity entity = toEntity(certificate);
        CertificateEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Certificate> findById(CertificateId id) {
        return jpaRepository.findById(id.getValue())
                .map(this::toDomain);
    }

    @Override
    public List<Certificate> findByStationId(StationId stationId) {
        return jpaRepository.findByStationId(stationId.getValue()).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Certificate> findByType(CertificateType type) {
        return jpaRepository.findByType(type.name()).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Certificate> findByStationIdAndType(StationId stationId, CertificateType type) {
        return jpaRepository.findByStationIdAndType(
                stationId.getValue(),
                type.name()).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Certificate> findValidByStationId(StationId stationId) {
        return jpaRepository.findValidByStationId(
                stationId.getValue(),
                LocalDateTime.now()).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(CertificateId id) {
        jpaRepository.deleteById(id.getValue());
    }

    @Override
    public void deleteByStationId(StationId stationId) {
        jpaRepository.deleteByStationId(stationId.getValue());
    }

    // Mapping methods
    private CertificateEntity toEntity(Certificate cert) {
        return CertificateEntity.builder()
                .id(cert.getId().getValue())
                .stationId(cert.getStationId().getValue())
                .type(cert.getType().name())
                .certificateData(cert.getCertificateData())
                .serialNumber(cert.getSerialNumber())
                .issuer(cert.getIssuer())
                .validFrom(cert.getValidFrom())
                .validTo(cert.getValidTo())
                .status(cert.getStatus().name())
                .createdAt(cert.getCreatedAt())
                .build();
    }

    private Certificate toDomain(CertificateEntity entity) {
        return Certificate.builder()
                .id(CertificateId.of(entity.getId()))
                .stationId(StationId.of(entity.getStationId()))
                .type(CertificateType.valueOf(entity.getType()))
                .certificateData(entity.getCertificateData())
                .serialNumber(entity.getSerialNumber())
                .issuer(entity.getIssuer())
                .validFrom(entity.getValidFrom())
                .validTo(entity.getValidTo())
                .status(CertificateStatus.valueOf(entity.getStatus()))
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
