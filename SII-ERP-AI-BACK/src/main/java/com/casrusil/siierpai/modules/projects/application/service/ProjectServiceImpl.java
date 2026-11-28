package com.casrusil.siierpai.modules.projects.application.service;

import com.casrusil.siierpai.shared.infrastructure.context.TenantGuard;
import com.casrusil.siierpai.modules.projects.domain.model.Project;
import com.casrusil.siierpai.modules.projects.domain.port.in.ProjectUseCase;
import com.casrusil.siierpai.modules.projects.domain.port.out.ProjectRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class ProjectServiceImpl implements ProjectUseCase {

    private final ProjectRepository projectRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    @Transactional
    public Project createProject(CompanyId companyId, String name, String description, BigDecimal budget) {
        Project project = Project.create(companyId, name, description, budget);
        return projectRepository.save(project);
    }

    @Override
    @Transactional
    public Project updateStatus(UUID projectId, String status) {
        Project project = TenantGuard.requireOwned(projectRepository.findById(projectId),
                Project::getCompanyId, "Proyecto");
        project.setStatus(status);
        return projectRepository.save(project);
    }

    @Override
    public List<Project> getProjectsByCompany(CompanyId companyId) {
        return projectRepository.findByCompanyId(companyId);
    }

    @Override
    public Project getProjectById(UUID id) {
        return TenantGuard.requireOwned(projectRepository.findById(id), Project::getCompanyId, "Proyecto");
    }
}
