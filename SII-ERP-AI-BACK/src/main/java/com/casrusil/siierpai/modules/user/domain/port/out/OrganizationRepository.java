package com.casrusil.siierpai.modules.user.domain.port.out;

import com.casrusil.siierpai.modules.user.domain.model.Organization;
import com.casrusil.siierpai.modules.user.domain.model.OrganizationId;
import java.util.List;
import java.util.Optional;

public interface OrganizationRepository {
    Organization save(Organization organization);
    Optional<Organization> findById(OrganizationId id);
    List<Organization> findAll();
    List<Organization> findActive();
    List<Organization> findByParentId(OrganizationId parentId);
}
