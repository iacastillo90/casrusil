package com.casrusil.b2b.infrastructure.persistence.repository;
import com.casrusil.b2b.infrastructure.persistence.entity.B2bOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface B2bOrderJpaRepository extends JpaRepository<B2bOrderEntity, UUID> {
    List<B2bOrderEntity> findByCompanyIdAndCustomerId(String companyId, String customerId);
}
