package com.casrusil.siierpai.shared.infrastructure.context;

import com.casrusil.siierpai.shared.domain.exception.ResourceNotFoundException;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Reglas de aislamiento entre empresas para la capa de aplicación.
 *
 * <p>La empresa activa sale siempre del JWT (vía {@link CompanyContext}); ningún
 * endpoint la recibe del cliente. Un registro de otra empresa se trata como
 * inexistente (404), para no revelar que el id existe.</p>
 */
public final class TenantGuard {

    private TenantGuard() {
    }

    /** Empresa del usuario autenticado. Falla si no hay contexto. */
    public static CompanyId currentCompany() {
        return CompanyContext.requireCompanyId();
    }

    /**
     * Devuelve el registro solo si pertenece a la empresa actual; si no existe o es
     * de otra empresa lanza {@link ResourceNotFoundException} (404).
     */
    public static <T> T requireOwned(Optional<T> found, Function<T, ?> companyOf, String resource) {
        CompanyId current = currentCompany();
        return found
                .filter(entity -> belongsTo(companyOf.apply(entity), current))
                .orElseThrow(() -> new ResourceNotFoundException(resource + " no encontrado"));
    }

    /** {@code true} si el valor (CompanyId, UUID o String) corresponde a la empresa dada. */
    public static boolean belongsTo(Object owner, CompanyId company) {
        if (owner == null || company == null) {
            return false;
        }
        if (owner instanceof CompanyId id) {
            return id.equals(company);
        }
        return Objects.equals(owner.toString(), company.value().toString());
    }
}
