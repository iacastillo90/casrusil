package com.casrusil.siierpai.modules.projects.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.projects.infrastructure.persistence.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectJpaRepository extends JpaRepository<ProjectEntity, UUID> {
    List<ProjectEntity> findByCompanyId(UUID companyId);
}
