package com.casrusil.siierpai.modules.user.domain.port.out;

import com.casrusil.siierpai.modules.user.domain.model.User;
import com.casrusil.siierpai.modules.user.domain.model.UserId;
import com.casrusil.siierpai.modules.user.domain.model.OrganizationId;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UserId id);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    List<User> findByOrganizationId(OrganizationId organizationId);
}
