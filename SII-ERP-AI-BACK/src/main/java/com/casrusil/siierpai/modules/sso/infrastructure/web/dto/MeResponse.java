package com.casrusil.siierpai.modules.sso.infrastructure.web.dto;

import java.util.List;
import java.util.UUID;

/**
 * Respuesta de {@code GET /api/v1/auth/me}.
 *
 * <p>
 * Solo expone datos de identidad y contexto necesarios para el frontend. Nunca
 * incluye el token JWT, el hash de la contraseña ni preferencias internas.
 * </p>
 *
 * @param id        ID del usuario autenticado
 * @param email     email del usuario
 * @param name      nombre completo
 * @param role      rol del usuario
 * @param companyId ID de la empresa activa
 * @param companies empresas visibles para el usuario (hoy una, por el modelo
 *                  usuario→empresa)
 */
public record MeResponse(
        UUID id,
        String email,
        String name,
        String role,
        UUID companyId,
        List<CompanySummary> companies) {

    /**
     * Resumen de empresa seguro para el frontend.
     *
     * @param id   ID de la empresa
     * @param name razón social
     * @param rut  RUT
     */
    public record CompanySummary(UUID id, String name, String rut) {
    }
}
