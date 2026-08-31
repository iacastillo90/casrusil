package com.casrusil.siierpai.modules.projects.domain.port.out;

import com.casrusil.siierpai.modules.projects.domain.model.Project;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository {
    Project save(Project project);
    Optional<Project> findById(UUID id);
    List<Project> findByCompanyId(CompanyId companyId);
}
