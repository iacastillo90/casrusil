package com.casrusil.siierpai.modules.user.infrastructure.persistence.adapter;

import com.casrusil.siierpai.modules.user.domain.model.Permission;
import com.casrusil.siierpai.modules.user.domain.model.PermissionAction;
import com.casrusil.siierpai.modules.user.domain.model.PermissionScope;
import com.casrusil.siierpai.modules.user.domain.model.Role;
import com.casrusil.siierpai.modules.user.infrastructure.persistence.entity.RoleEntity;
import com.casrusil.siierpai.modules.user.infrastructure.persistence.repository.RoleJpaRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Deuda técnica: el adaptador guardaba siempre {@code permissions = "[]"} y al
 * leer no mapeaba los permisos, así que un rol perdía sus permisos al
 * persistirse.
 */
class RoleRepositoryAdapterTest {

    @Test
    void saveConservaLosPermisosDelRol() {
        RoleJpaRepository jpa = mock(RoleJpaRepository.class);
        when(jpa.save(any(RoleEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        RoleRepositoryAdapter adapter = new RoleRepositoryAdapter(jpa);

        Set<Permission> permissions = Set.of(
                Permission.fullAccess("invoices", PermissionScope.values()[0]),
                new Permission("reports", Set.of(PermissionAction.READ), PermissionScope.values()[0]));
        Role role = Role.createSystem("CONTADOR", "Rol de prueba", permissions);

        Role saved = adapter.save(role);

        ArgumentCaptor<RoleEntity> captor = ArgumentCaptor.forClass(RoleEntity.class);
        verify(jpa).save(captor.capture());
        assertThat(captor.getValue().getPermissions()).isNotEqualTo("[]").contains("invoices", "reports");
        assertThat(saved.getPermissions()).containsExactlyInAnyOrderElementsOf(permissions);
    }

    @Test
    void readPermissionsToleraJsonVacio() {
        assertThat(RoleRepositoryAdapter.readPermissions("[]")).isEmpty();
        assertThat(RoleRepositoryAdapter.readPermissions(null)).isEmpty();
    }

    @Test
    void writeYReadSonSimetricos() {
        Set<Permission> permissions = Set.of(Permission.readOnly("treasury", PermissionScope.values()[0]));
        assertThat(RoleRepositoryAdapter.readPermissions(RoleRepositoryAdapter.writePermissions(permissions)))
                .isEqualTo(permissions);
    }
}
