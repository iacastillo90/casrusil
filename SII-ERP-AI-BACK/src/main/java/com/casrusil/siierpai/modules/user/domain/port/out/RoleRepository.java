package com.casrusil.siierpai.modules.user.domain.port.out;

import com.casrusil.siierpai.modules.user.domain.model.Role;
import com.casrusil.siierpai.modules.user.domain.model.RoleId;
import com.casrusil.siierpai.modules.user.domain.model.OrganizationId;
import java.util.List;
import java.util.Optional;

public interface RoleRepository {
    Role save(Role role);
    Optional<Role> findById(RoleId id);
    List<Role> findAll();
    List<Role> findSystemRoles();
    List<Role> findByOrganizationId(OrganizationId organizationId);
}
