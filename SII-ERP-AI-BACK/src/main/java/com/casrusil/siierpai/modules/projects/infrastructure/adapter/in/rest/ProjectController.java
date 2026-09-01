package com.casrusil.siierpai.modules.projects.infrastructure.adapter.in.rest;

import jakarta.validation.Valid;

import com.casrusil.siierpai.modules.projects.domain.model.Project;
import com.casrusil.siierpai.modules.projects.domain.port.in.ProjectUseCase;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@PreAuthorize("hasAnyRole('ADMIN','OPERATIONS')")
public class ProjectController {

    private final ProjectUseCase projectUseCase;

    public ProjectController(ProjectUseCase projectUseCase) {
        this.projectUseCase = projectUseCase;
    }

    @GetMapping
    public ResponseEntity<List<Project>> getProjects() {
        CompanyId companyId = CompanyContext.getCompanyId();
        return ResponseEntity.ok(projectUseCase.getProjectsByCompany(companyId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProject(@PathVariable UUID id) {
        return ResponseEntity.ok(projectUseCase.getProjectById(id));
    }

    @PostMapping
    public ResponseEntity<Project> createProject(@Valid @RequestBody CreateProjectRequest request) {
        CompanyId companyId = CompanyContext.getCompanyId();
        Project project = projectUseCase.createProject(companyId, request.name(), request.description(), request.budget());
        return ResponseEntity.ok(project);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Project> updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateProjectStatusRequest request) {
        Project project = projectUseCase.updateStatus(id, request.status());
        return ResponseEntity.ok(project);
    }

    public record CreateProjectRequest(String name, String description, BigDecimal budget) {}
    public record UpdateProjectStatusRequest(String status) {}
}
