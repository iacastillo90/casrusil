package com.casrusil.siierpai.modules.b2b.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.b2b.infrastructure.persistence.entity.CustomerOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerOrderJpaRepository extends JpaRepository<CustomerOrderEntity, UUID> {

    Optional<CustomerOrderEntity> findByIdAndCustomerId(UUID id, UUID customerId);

    List<CustomerOrderEntity> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
}
