package com.casrusil.siierpai.modules.energy.domain.port.out;

import com.casrusil.siierpai.modules.energy.domain.model.StationId;
import com.casrusil.siierpai.modules.energy.domain.model.security.Certificate;
import com.casrusil.siierpai.modules.energy.domain.model.security.CertificateId;
import com.casrusil.siierpai.modules.energy.domain.model.security.CertificateType;

import java.util.List;
import java.util.Optional;

/**
 * Repository port for Certificate persistence.
 */
public interface CertificateRepository {

    /**
     * Saves a certificate.
     */
    Certificate save(Certificate certificate);

    /**
     * Finds a certificate by its ID.
     */
    Optional<Certificate> findById(CertificateId id);

    /**
     * Finds all certificates for a specific station.
     */
    List<Certificate> findByStationId(StationId stationId);

    /**
     * Finds all certificates of a specific type.
     */
    List<Certificate> findByType(CertificateType type);

    /**
     * Finds all certificates for a station by type.
     */
    List<Certificate> findByStationIdAndType(StationId stationId, CertificateType type);

    /**
     * Finds all valid (non-expired) certificates for a station.
     */
    List<Certificate> findValidByStationId(StationId stationId);

    /**
     * Deletes a certificate.
     */
    void delete(CertificateId id);

    /**
     * Deletes all certificates for a station.
     */
    void deleteByStationId(StationId stationId);
}
