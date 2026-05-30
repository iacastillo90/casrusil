package com.casrusil.siierpai.modules.user.application.service;

import com.casrusil.siierpai.modules.user.domain.model.*;
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

    // In-memory storage (TODO: replace with repository)
    private final Map<OrganizationId, Organization> organizations = new ConcurrentHashMap<>();

    /**
     * Creates a new organization.
     */
    public Organization createOrganization(String name, OrganizationType type) {
        logger.info("Creating organization: {}", name);

        Organization organization = Organization.create(name, type);
        organizations.put(organization.getId(), organization);

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

        if (!organizations.containsKey(parentId)) {
            throw new IllegalArgumentException("Parent organization not found: " + parentId);
        }

        Organization organization = Organization.createChild(name, type, parentId);
        organizations.put(organization.getId(), organization);

        logger.info("Child organization created: {}", organization.getId());
        return organization;
    }

    /**
     * Gets organization by ID.
     */
    public Organization getOrganization(OrganizationId id) {
        Organization org = organizations.get(id);
        if (org == null) {
            throw new IllegalArgumentException("Organization not found: " + id);
        }
        return org;
    }

    /**
     * Lists all organizations.
     */
    public List<Organization> getAllOrganizations() {
        return new ArrayList<>(organizations.values());
    }

    /**
     * Lists active organizations.
     */
    public List<Organization> getActiveOrganizations() {
        return organizations.values().stream()
                .filter(Organization::isActive)
                .collect(Collectors.toList());
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

        return organization;
    }

    /**
     * Suspends organization.
     */
    public Organization suspendOrganization(OrganizationId id) {
        logger.info("Suspending organization: {}", id);

        Organization organization = getOrganization(id);
        organization.suspend();

        return organization;
    }

    /**
     * Activates organization.
     */
    public Organization activateOrganization(OrganizationId id) {
        logger.info("Activating organization: {}", id);

        Organization organization = getOrganization(id);
        organization.activate();

        return organization;
    }

    /**
     * Gets organization hierarchy.
     */
    public List<Organization> getOrganizationHierarchy(OrganizationId rootId) {
        List<Organization> hierarchy = new ArrayList<>();
        Organization root = getOrganization(rootId);
        hierarchy.add(root);

        // Find all children
        organizations.values().stream()
                .filter(org -> rootId.equals(org.getParentId()))
                .forEach(hierarchy::add);

        return hierarchy;
    }
}
