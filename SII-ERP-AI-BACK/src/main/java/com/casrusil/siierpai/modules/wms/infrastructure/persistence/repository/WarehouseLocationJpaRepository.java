package com.casrusil.siierpai.modules.wms.infrastructure.persistence.repository;
import com.casrusil.siierpai.modules.wms.infrastructure.persistence.entity.WarehouseLocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface WarehouseLocationJpaRepository extends JpaRepository<WarehouseLocationEntity, UUID> {
    List<WarehouseLocationEntity> findByCompanyId(String companyId);
}