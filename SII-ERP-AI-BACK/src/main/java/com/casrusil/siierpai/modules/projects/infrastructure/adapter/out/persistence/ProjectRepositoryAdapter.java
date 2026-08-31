package com.casrusil.siierpai.modules.projects.infrastructure.adapter.out.persistence;

import com.casrusil.siierpai.modules.projects.domain.model.Project;
import com.casrusil.siierpai.modules.projects.domain.port.out.ProjectRepository;
import com.casrusil.siierpai.modules.projects.infrastructure.persistence.entity.ProjectEntity;
import com.casrusil.siierpai.modules.projects.infrastructure.persistence.repository.ProjectJpaRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ProjectRepositoryAdapter implements ProjectRepository {

    private final ProjectJpaRepository jpaRepository;

    public ProjectRepositoryAdapter(ProjectJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Project save(Project project) {
        ProjectEntity saved = jpaRepository.save(toEntity(project));
        return toDomain(saved);
    }

    @Override
    public Optional<Project> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Project> findByCompanyId(CompanyId companyId) {
        return jpaRepository.findByCompanyId(companyId.value()).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private ProjectEntity toEntity(Project domain) {
        return new ProjectEntity(
                domain.getId(),
                domain.getCompanyId().value(),
                domain.getName(),
                domain.getDescription(),
                domain.getStartDate(),
                domain.getEndDate(),
                domain.getStatus(),
                domain.getBudget()
        );
    }

    private Project toDomain(ProjectEntity entity) {
        return new Project(
                entity.getId(),
                CompanyId.of(entity.getCompanyId()),
                entity.getName(),
                entity.getDescription(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getStatus(),
                entity.getBudget()
        );
    }
}
