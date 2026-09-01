package com.casrusil.siierpai.modules.sso.domain.model;

/**
 * Roles de usuario en el sistema ERP (modelo de roles simples).
 *
 * <p>
 * Define los niveles de acceso por dominio funcional. Se usan como authorities
 * de Spring Security con el prefijo {@code ROLE_} (ver {@code SecurityFilter}),
 * por lo que habilitan {@code @PreAuthorize("hasRole('ADMIN')")} y
 * {@code hasAnyRole(...)} en los controladores.
 *
 * <h2>Roles disponibles:</h2>
 * <ul>
 * <li><b>ADMIN</b> — Acceso completo: gestión de usuarios, empresa y todos los módulos.</li>
 * <li><b>ACCOUNTANT</b> — Contabilidad, impuestos, SII, cierres, facturación y reportes.</li>
 * <li><b>TREASURER</b> — Tesorería, banca y conciliaciones.</li>
 * <li><b>HR</b> — Nómina y recursos humanos.</li>
 * <li><b>SALES</b> — CRM, ventas, facturación y punto de venta (POS).</li>
 * <li><b>OPERATIONS</b> — Inventario, WMS, MRP, compras, activos fijos, proyectos y energía.</li>
 * <li><b>AUDITOR</b> — Solo lectura de auditoría, cumplimiento, reportes y sostenibilidad.</li>
 * <li><b>USER</b> — Acceso básico / lectura general.</li>
 * </ul>
 *
 * <p><b>Nota de compatibilidad:</b> Los valores {@code ADMIN}, {@code ACCOUNTANT} y
 * {@code USER} preexistentes se mantienen para no romper datos ni tokens ya emitidos.
 *
 * @see com.casrusil.siierpai.modules.sso.domain.model.User
 * @since 1.0
 */
public enum UserRole {
    ADMIN,
    ACCOUNTANT,
    TREASURER,
    HR,
    SALES,
    OPERATIONS,
    AUDITOR,
    USER
}
