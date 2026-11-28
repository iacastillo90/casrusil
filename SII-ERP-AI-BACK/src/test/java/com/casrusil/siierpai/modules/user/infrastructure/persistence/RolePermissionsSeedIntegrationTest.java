package com.casrusil.siierpai.modules.user.infrastructure.persistence;

import com.casrusil.siierpai.modules.user.application.service.RoleService;
import com.casrusil.siierpai.modules.user.domain.model.Permission;
import com.casrusil.siierpai.modules.user.domain.model.PermissionAction;
import com.casrusil.siierpai.modules.user.domain.model.PermissionScope;
import com.casrusil.siierpai.modules.user.domain.model.Role;
import com.casrusil.siierpai.modules.user.domain.model.RoleId;
import com.casrusil.siierpai.modules.user.domain.port.out.RoleRepository;
import com.casrusil.siierpai.modules.user.infrastructure.persistence.entity.RoleEntity;
import com.casrusil.siierpai.modules.user.infrastructure.persistence.entity.UserEntity;
import com.casrusil.siierpai.modules.user.infrastructure.persistence.repository.UserJpaRepository;
import com.casrusil.siierpai.shared.test.BaseIntegrationTest;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Hotfix: desde el PR #37 el lector de permisos solo aceptaba el arreglo de
 * {@link Permission} y la seed V2 guarda objetos {@code {"recurso":["read",...]}}.
 * El constructor de {@link RoleService} lee los roles de sistema al arrancar, así
 * que una base vacía migrada con Flyway V1→V4 no levantaba el contexto.
 *
 * <p>Este test arranca contra el Postgres vacío de Testcontainers (Flyway aplica
 * las migraciones reales), comprueba que el contexto sube y que cada usuario de
 * la seed obtiene los permisos de sus roles.</p>
 */
@Transactional
class RolePermissionsSeedIntegrationTest extends BaseIntegrationTest {

    private static final Set<PermissionAction> READ_WRITE =
            Set.of(PermissionAction.READ, PermissionAction.CREATE, PermissionAction.UPDATE);
    private static final Set<PermissionAction> READ_CREATE = Set.of(PermissionAction.READ, PermissionAction.CREATE);
    private static final PermissionScope ORG = PermissionScope.ORGANIZATION;

    @Autowired
    private Flyway flyway;
    @Autowired
    private RoleService roleService;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserJpaRepository userJpaRepository;

    @Test
    void flywayAplicoV1aV4SobreBaseVaciaYElContextoLevanto() {
        Set<String> applied = Arrays.stream(flyway.info().applied())
                .map(MigrationInfo::getVersion)
                .map(Object::toString)
                .collect(Collectors.toSet());
        assertThat(applied).contains("1", "2", "3", "4");
        // RoleService se construyó (lee los roles de sistema de la seed en el constructor)
        assertThat(roleService.getSystemRoles()).extracting(Role::getName)
                .contains("SUPER_ADMIN", "ADMIN", "ACCOUNTANT", "USER");
    }

    @Test
    void superAdminDeLaSeedTieneAccesoTotalGlobal() {
        Role superAdmin = roleRepository.findById(RoleId.of(UUID.fromString("20000000-0000-0000-0000-000000000001")))
                .orElseThrow();
        assertThat(superAdmin.getPermissions()).containsExactly(
                new Permission("*", Set.of(PermissionAction.values()), PermissionScope.ALL));
    }

    @Test
    void cadaUsuarioDeLaSeedObtieneLosPermisosDeSusRoles() {
        // ADMIN + GERENTE (Demo)
        assertThat(permissionsOf("admin@demo.cl")).containsExactlyInAnyOrder(
                new Permission("users", Set.of(PermissionAction.READ, PermissionAction.CREATE,
                        PermissionAction.UPDATE, PermissionAction.DELETE), ORG),
                new Permission("roles", READ_WRITE, ORG),
                new Permission("settings", READ_WRITE, ORG),
                new Permission("*", READ_WRITE, ORG));

        // ACCOUNTANT
        assertThat(permissionsOf("contador@demo.cl")).containsExactlyInAnyOrder(
                new Permission("accounting", READ_WRITE, ORG),
                new Permission("invoicing", READ_WRITE, ORG),
                new Permission("tax", READ_WRITE, ORG));

        // USER + VENDEDOR
        assertThat(permissionsOf("vendedor@demo.cl")).containsExactlyInAnyOrder(
                new Permission("profile", READ_WRITE, ORG),
                new Permission("invoicing", READ_CREATE, ORG),
                new Permission("customers", READ_WRITE, ORG));

        // ADMIN + GERENTE (Los Andes)
        assertThat(permissionsOf("admin@losandes.cl"))
                .contains(new Permission("*", READ_WRITE, ORG))
                .anySatisfy(p -> assertThat(p.resource()).isEqualTo("users"));

        // Cada rol asignado en la seed se lee con al menos un permiso
        Map<String, Set<Permission>> porRol = userJpaRepository.findAll().stream()
                .flatMap(u -> u.getRoles().stream())
                .distinct()
                .collect(Collectors.toMap(r -> r.getName() + "@" + r.getId(), this::permissionsOfRole, (a, b) -> a));
        assertThat(porRol).isNotEmpty().allSatisfy((rol, permisos) -> assertThat(permisos).as(rol).isNotEmpty());
        assertThat(permissionsOf("contador@demo.cl")).noneMatch(p -> p.allows(PermissionAction.DELETE));
    }

    private Set<Permission> permissionsOf(String email) {
        UserEntity user = userJpaRepository.findByEmail(email).orElseThrow();
        assertThat(user.getRoles()).as("roles de " + email).isNotEmpty();
        return user.getRoles().stream()
                .flatMap(r -> permissionsOfRole(r).stream())
                .collect(Collectors.toSet());
    }

    private Set<Permission> permissionsOfRole(RoleEntity role) {
        return roleService.getRole(RoleId.of(role.getId())).getPermissions();
    }
}
