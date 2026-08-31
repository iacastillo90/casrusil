package com.casrusil.siierpai.modules.wms.infrastructure.persistence.repository;
import com.casrusil.siierpai.modules.wms.infrastructure.persistence.entity.PickingTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface PickingTaskJpaRepository extends JpaRepository<PickingTaskEntity, UUID> {
    List<PickingTaskEntity> findByCompanyId(String companyId);
}