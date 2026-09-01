package com.casrusil.siierpai.modules.user.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.user.application.service.OrganizationService;
import com.casrusil.siierpai.modules.user.domain.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for organization management.
 */
@RestController
@RequestMapping("/api/v1/organizations")
@Tag(name = "Organizations", description = "Multi-tenant organization management")
@PreAuthorize("hasRole('ADMIN')")
public class OrganizationController {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationController.class);

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @PostMapping
    @Operation(summary = "Create organization")
    public ResponseEntity<OrganizationDto> createOrganization(
            @RequestBody @Valid CreateOrganizationRequest request) {

        logger.info("Creating organization: {}", request.name());

        Organization organization = organizationService.createOrganization(
                request.name(),
                request.type());

        return ResponseEntity.ok(OrganizationDto.from(organization));
    }

    @PostMapping("/{parentId}/children")
    @Operation(summary = "Create child organization")
    public ResponseEntity<OrganizationDto> createChildOrganization(
            @PathVariable UUID parentId,
            @RequestBody @Valid CreateOrganizationRequest request) {

        logger.info("Creating child organization under: {}", parentId);

        Organization organization = organizationService.createChildOrganization(
                request.name(),
                request.type(),
                OrganizationId.of(parentId));

        return ResponseEntity.ok(OrganizationDto.from(organization));
    }

    @GetMapping
    @Operation(summary = "List all organizations")
    public ResponseEntity<List<OrganizationDto>> getAllOrganizations() {
        List<Organization> organizations = organizationService.getAllOrganizations();
        return ResponseEntity.ok(organizations.stream()
                .map(OrganizationDto::from)
                .toList());
    }

    @GetMapping("/active")
    @Operation(summary = "List active organizations")
    public ResponseEntity<List<OrganizationDto>> getActiveOrganizations() {
        List<Organization> organizations = organizationService.getActiveOrganizations();
        return ResponseEntity.ok(organizations.stream()
                .map(OrganizationDto::from)
                .toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get organization by ID")
    public ResponseEntity<OrganizationDto> getOrganization(@PathVariable UUID id) {
        try {
            Organization organization = organizationService.getOrganization(OrganizationId.of(id));
            return ResponseEntity.ok(OrganizationDto.from(organization));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/hierarchy")
    @Operation(summary = "Get organization hierarchy")
    public ResponseEntity<List<OrganizationDto>> getOrganizationHierarchy(@PathVariable UUID id) {
        try {
            List<Organization> hierarchy = organizationService.getOrganizationHierarchy(
                    OrganizationId.of(id));
            return ResponseEntity.ok(hierarchy.stream()
                    .map(OrganizationDto::from)
                    .toList());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update organization")
    public ResponseEntity<OrganizationDto> updateOrganization(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateOrganizationRequest request) {

        try {
            Organization organization = organizationService.updateOrganization(
                    OrganizationId.of(id),
                    request.name(),
                    request.type());
            return ResponseEntity.ok(OrganizationDto.from(organization));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/suspend")
    @Operation(summary = "Suspend organization")
    public ResponseEntity<OrganizationDto> suspendOrganization(@PathVariable UUID id) {
        try {
            Organization organization = organizationService.suspendOrganization(OrganizationId.of(id));
            return ResponseEntity.ok(OrganizationDto.from(organization));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Activate organization")
    public ResponseEntity<OrganizationDto> activateOrganization(@PathVariable UUID id) {
        try {
            Organization organization = organizationService.activateOrganization(OrganizationId.of(id));
            return ResponseEntity.ok(OrganizationDto.from(organization));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DTOs
    public record OrganizationDto(
            UUID id,
            String name,
            String type,
            String status,
            UUID parentId,
            Map<String, Object> settings,
            Map<String, Object> branding,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        public static OrganizationDto from(Organization org) {
            return new OrganizationDto(
                    org.getId().value(),
                    org.getName(),
                    org.getType().name(),
                    org.getStatus().name(),
                    org.getParentId() != null ? org.getParentId().value() : null,
                    org.getSettings(),
                    org.getBranding(),
                    org.getCreatedAt(),
                    org.getUpdatedAt());
        }
    }

    public record CreateOrganizationRequest(
            @NotBlank String name,
            @NotNull OrganizationType type) {
    }

    public record UpdateOrganizationRequest(
            @NotBlank String name,
            @NotNull OrganizationType type) {
    }
}
