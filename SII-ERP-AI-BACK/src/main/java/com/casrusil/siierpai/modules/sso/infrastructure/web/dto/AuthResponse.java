package com.casrusil.siierpai.modules.sso.infrastructure.web.dto;

import java.util.UUID;

/**
 * DTO de respuesta para autenticación exitosa (Login/Register).
 * 
 * <p>
 * Devuelve el contexto mínimo para que el frontend inicialice su estado sin
 * llamar a /me. El JWT NO va en el body: se entrega solo en la cookie httpOnly
 * (Set-Cookie), para que JavaScript no pueda leerlo (R10).
 * 
 * @param userId      ID del usuario autenticado
 * @param userName    Nombre del usuario (o email si no tiene nombre)
 * @param companyId   ID de la empresa (contexto actual)
 * @param companyName Nombre de la empresa
 * @param role        Rol del usuario (para el gating de UI en el frontend)
 */
public record AuthResponse(
        UUID userId,
        String userName,
        UUID companyId,
        String companyName,
        String role) {
}
