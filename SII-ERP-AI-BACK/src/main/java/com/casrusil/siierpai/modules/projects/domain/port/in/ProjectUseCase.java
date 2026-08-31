package com.casrusil.siierpai.modules.projects.domain.port.in;

import com.casrusil.siierpai.modules.projects.domain.model.Project;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ProjectUseCase {
    Project createProject(CompanyId companyId, String name, String description, BigDecimal budget);
    Project updateStatus(UUID projectId, String status);
    List<Project> getProjectsByCompany(CompanyId companyId);
    Project getProjectById(UUID id);
}
