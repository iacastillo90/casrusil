# Matriz de permisos por API — SII-ERP

Generado: 2026-11-20T02:49:31.580Z
Backend: http://localhost:8092
Empresa: 1866e3aa-d266-4df7-901c-b89bcb7c30dd

## Setup

- Empresa E2E: `QA E2E 1795142966892` / RUT `E2E-1795142966892`
- ADMIN: `qa-admin-1795142966892@example.com`
- ACCOUNTANT: `qa-accountant-1795142966892@example.com`
- TREASURER: `qa-treasurer-1795142966892@example.com`
- SALES: `qa-sales-1795142966892@example.com`
- OPERATIONS: `qa-operations-1795142966892@example.com`
- HR: `qa-hr-1795142966892@example.com`
- AUDITOR: `qa-auditor-1795142966892@example.com`
- USER: `qa-user-1795142966892@example.com`
- B2B_CLIENT: `qa-b2b-1795142966892@example.com` — alta vía `POST /sso/users` → HTTP 400 (rol inexistente en el enum)

## Matriz rol × endpoint

Celdas: `✅ status` = coincide con lo esperado; `❌ status` = discrepancia; `—` = rol no creado; `N/A` = no representable.

| Endpoint | ADMIN | ACCOUNTANT | TREASURER | SALES | OPERATIONS | HR | AUDITOR | USER | B2B_CLIENT |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| GET /invoices/{id} (UUID aleatorio) | ✅ 400 | ✅ 400 | ✅ 403 | ✅ 400 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | N/A |
| GET /sii/caf | ✅ 200 | ✅ 200 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | N/A |
| GET /sii/status | ✅ 200 | ✅ 200 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | N/A |
| GET /accounting/entries | ✅ 200 | ✅ 200 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | N/A |
| GET /payroll/employee/payslips | ✅ 200 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 200 | ✅ 403 | ❌ 200 | N/A |
| GET /actuator/metrics | ❌ 500 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | N/A |
| POST /ai/conversations | ✅ 200 | ✅ 200 | ✅ 200 | ✅ 200 | ✅ 403 | ✅ 403 | ✅ 200 | ✅ 403 | N/A |
| GET /notifications/preferences | ✅ 200 | ✅ 200 | ✅ 200 | ✅ 200 | ✅ 200 | ✅ 200 | ✅ 200 | ✅ 200 | N/A |
| GET /b2b/orders/my-orders | ✅ 200 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | N/A |

## Discrepancias

- **payroll** / USER: esperado DENY, obtenido 200 — `[]`
- **actuator** / ADMIN: esperado ALLOW, obtenido 500 — `{"timestamp":"2026-11-20T02:49:31.182141118Z","status":500,"error":"Internal Server Error","message":"An unexpected error occurred. Please try again later.","path":"/actuator/metri`

## Detalle de discrepancias

- **payroll / USER** — el método permite explícitamente al rol USER: `SII-ERP-AI-BACK/src/main/java/com/casrusil/siierpai/modules/payroll/infrastructure/web/PayrollController.java:59` (`@PreAuthorize("hasAnyRole('ADMIN','HR','USER')")`). La matriz de `MAPA-FUNCIONAL.md` dice que USER no tiene áreas propias. No se corrige: es código de E/C.
- **actuator / ADMIN (500)** — `SecurityConfig.java:66` protege `/actuator/**` con ADMIN, pero `application.properties:120` solo expone `health,info`; `/actuator/metrics` no está mapeado y el `GlobalExceptionHandler` lo convierte en 500 (`No static resource actuator/metrics`). No se tocó el backend.
- **invoices / ALLOW (400)** — el UUID aleatorio no existe; lo relevante es que no sea 403 (permiso concedido).

## B2B_CLIENT

`UserRole` no define `B2B_CLIENT` (`SII-ERP-AI-BACK/src/main/java/com/casrusil/siierpai/modules/sso/domain/model/UserRole.java:30`), aunque `CustomerOrderController` lo exige (`.../b2b/infrastructure/web/CustomerOrderController.java:30`). No es posible crear un usuario con ese rol por la API; la fila B2B_CLIENT queda sin token y el endpoint solo se valida con ADMIN.
