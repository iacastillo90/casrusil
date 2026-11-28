package com.casrusil.siierpai.modules.user.infrastructure.persistence.adapter;

import com.casrusil.siierpai.modules.user.domain.model.*;
import com.casrusil.siierpai.modules.user.domain.port.out.RoleRepository;
import com.casrusil.siierpai.modules.user.infrastructure.persistence.entity.RoleEntity;
import com.casrusil.siierpai.modules.user.infrastructure.persistence.repository.RoleJpaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RoleRepositoryAdapter implements RoleRepository {

    private static final Logger log = LoggerFactory.getLogger(RoleRepositoryAdapter.class);
    private static final String WILDCARD = "*";
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final TypeReference<Set<Permission>> PERMISSION_SET = new TypeReference<>() {
    };


    private final RoleJpaRepository jpaRepository;

    public RoleRepositoryAdapter(RoleJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Role save(Role role) {
        RoleEntity entity = new RoleEntity();
        entity.setId(role.getId().value());
        entity.setOrganizationId(role.getOrganizationId() != null ? role.getOrganizationId().value() : null);
        entity.setName(role.getName());
        entity.setDescription(role.getDescription());
        entity.setSystemRole(role.isSystemRole());
        entity.setPermissions(writePermissions(role.getPermissions()));
        entity.setCreatedAt(role.getCreatedAt());
        entity.setUpdatedAt(role.getUpdatedAt());
        
        RoleEntity saved = jpaRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<Role> findById(RoleId id) {
        return jpaRepository.findById(id.value()).map(this::mapToDomain);
    }

    @Override
    public List<Role> findAll() {
        return jpaRepository.findAll().stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    @Override
    public List<Role> findSystemRoles() {
        return jpaRepository.findAllSystemRoles().stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    @Override
    public List<Role> findByOrganizationId(OrganizationId organizationId) {
        return jpaRepository.findByOrganizationId(organizationId.value()).stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    private Role mapToDomain(RoleEntity entity) {
        return Role.builder()
                .id(RoleId.of(entity.getId()))
                .organizationId(entity.getOrganizationId() != null ? OrganizationId.of(entity.getOrganizationId()) : null)
                .name(entity.getName())
                .description(entity.getDescription())
                .systemRole(entity.isSystemRole())
                .permissions(readPermissions(entity.getPermissions()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Serializa los permisos a JSON para la columna {@code roles.permissions}
     * (jsonb). Antes siempre se guardaba {@code "[]"} y los permisos del rol se
     * perdían al persistir.
     */
    static String writePermissions(Set<Permission> permissions) {
        try {
            return JSON.writeValueAsString(permissions == null ? Set.of() : permissions);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudieron serializar los permisos del rol", e);
        }
    }

    /**
     * Lee la columna {@code roles.permissions} aceptando los dos formatos que
     * conviven en la base:
     * <ul>
     *   <li><b>Canónico</b> (el que escribe {@link #writePermissions}, desde el PR #37):
     *   arreglo de {@link Permission},
     *   p. ej. {@code [{"resource":"users","actions":["READ"],"scope":"ORGANIZATION"}]}.</li>
     *   <li><b>Legado</b> (seed {@code V2__datos_maestros.sql}): objeto
     *   recurso → acciones en minúscula, p. ej. {@code {"users":["read","write"]}}.
     *   No trae alcance: se asume {@code ORGANIZATION}, salvo {@code {"*":["*"]}}
     *   (super administrador) que se lee como {@code ALL}.</li>
     * </ul>
     * Hasta que una migración normalice los datos, un JSON con forma distinta
     * sigue siendo un error de datos y se informa con {@link IllegalStateException}.
     */
    static Set<Permission> readPermissions(String json) {
        if (json == null || json.isBlank()) {
            return new HashSet<>();
        }
        try {
            JsonNode root = JSON.readTree(json);
            if (root == null || root.isNull()) {
                return new HashSet<>();
            }
            if (root.isArray()) {
                return new HashSet<>(JSON.convertValue(root, PERMISSION_SET));
            }
            if (root.isObject()) {
                return readLegacyPermissions(root);
            }
            throw new IllegalStateException("Permisos del rol con formato inválido: " + json);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            throw new IllegalStateException("Permisos del rol con formato inválido", e);
        }
    }

    /** Formato legado de la seed V2: {@code {"recurso": ["read", "write", ...]}}. */
    private static Set<Permission> readLegacyPermissions(JsonNode root) {
        Set<Permission> permissions = new HashSet<>();
        Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String resource = field.getKey();
            JsonNode values = field.getValue();
            if (!values.isArray()) {
                throw new IllegalStateException(
                        "Permisos legados inválidos para '" + resource + "': se esperaba un arreglo de acciones");
            }
            Set<PermissionAction> actions = EnumSet.noneOf(PermissionAction.class);
            boolean allActions = false;
            for (JsonNode value : values) {
                String action = value.asText();
                if (WILDCARD.equals(action)) {
                    allActions = true;
                }
                actions.addAll(mapLegacyAction(resource, action));
            }
            if (actions.isEmpty()) {
                continue;
            }
            PermissionScope scope = WILDCARD.equals(resource) && allActions
                    ? PermissionScope.ALL
                    : PermissionScope.ORGANIZATION;
            permissions.add(new Permission(resource, Set.copyOf(actions), scope));
        }
        return permissions;
    }

    private static Set<PermissionAction> mapLegacyAction(String resource, String action) {
        switch (action.trim().toLowerCase(Locale.ROOT)) {
            case WILDCARD:
                return EnumSet.allOf(PermissionAction.class);
            case "read":
                return EnumSet.of(PermissionAction.READ);
            case "write":
                return EnumSet.of(PermissionAction.CREATE, PermissionAction.UPDATE);
            case "create":
                return EnumSet.of(PermissionAction.CREATE);
            case "update":
                return EnumSet.of(PermissionAction.UPDATE);
            case "delete":
                return EnumSet.of(PermissionAction.DELETE);
            case "execute":
                return EnumSet.of(PermissionAction.EXECUTE);
            default:
                log.warn("Acción de permiso desconocida '{}' en recurso '{}': se ignora", action, resource);
                return EnumSet.noneOf(PermissionAction.class);
        }
    }
}
