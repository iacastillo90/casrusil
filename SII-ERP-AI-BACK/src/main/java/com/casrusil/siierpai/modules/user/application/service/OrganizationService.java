package com.casrusil.siierpai.modules.user.application.service;

import com.casrusil.siierpai.modules.user.domain.model.*;
import com.casrusil.siierpai.modules.user.domain.port.out.OrganizationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Application service for organization management.
 */
@Service
@Transactional
public class OrganizationService {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationService.class);

    private final OrganizationRepository organizationRepository;

    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    /**
     * Creates a new organization.
     */
    public Organization createOrganization(String name, OrganizationType type) {
        logger.info("Creating organization: {}", name);

        Organization organization = Organization.create(name, type);
        organization = organizationRepository.save(organization);

        logger.info("Organization created: {}", organization.getId());
        return organization;
    }

    /**
     * Creates a child organization.
     */
    public Organization createChildOrganization(
            String name,
            OrganizationType type,
            OrganizationId parentId) {

        logger.info("Creating child organization: {} under parent: {}", name, parentId);

        if (organizationRepository.findById(parentId).isEmpty()) {
            throw new IllegalArgumentException("Parent organization not found: " + parentId);
        }

        Organization organization = Organization.createChild(name, type, parentId);
        organization = organizationRepository.save(organization);

        logger.info("Child organization created: {}", organization.getId());
        return organization;
    }

    /**
     * Gets organization by ID.
     */
    public Organization getOrganization(OrganizationId id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found: " + id));
    }

    /**
     * Lists all organizations.
     */
    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
    }

    /**
     * Lists active organizations.
     */
    public List<Organization> getActiveOrganizations() {
        return organizationRepository.findActive();
    }

    /**
     * Updates organization.
     */
    public Organization updateOrganization(
            OrganizationId id,
            String name,
            OrganizationType type) {

        logger.info("Updating organization: {}", id);

        Organization organization = getOrganization(id);
        organization.update(name, type);
        return organizationRepository.save(organization);
    }

    /**
     * Suspends organization.
     */
    public Organization suspendOrganization(OrganizationId id) {
        logger.info("Suspending organization: {}", id);

        Organization organization = getOrganization(id);
        organization.suspend();
        return organizationRepository.save(organization);
    }

    /**
     * Activates organization.
     */
    public Organization activateOrganization(OrganizationId id) {
        logger.info("Activating organization: {}", id);

        Organization organization = getOrganization(id);
        organization.activate();
        return organizationRepository.save(organization);
    }

    /**
     * Gets organization hierarchy.
     */
    public List<Organization> getOrganizationHierarchy(OrganizationId rootId) {
        List<Organization> hierarchy = new ArrayList<>();
        Organization root = getOrganization(rootId);
        hierarchy.add(root);

        // Find all children
        hierarchy.addAll(organizationRepository.findByParentId(rootId));

        return hierarchy;
    }
}
