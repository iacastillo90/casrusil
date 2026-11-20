# Matriz de permisos por API — SII-ERP

Generado: 2026-11-20T05:10:12.889Z
Backend: http://localhost:8092
Empresa: f01bb884-c811-453f-9bef-e3bc77a6a9f5

## Setup

- Empresa E2E: `QA E2E 1795151387828` / RUT `E2E-1795151387828`
- ADMIN: `qa-admin-1795151387828@example.com`
- ACCOUNTANT: `qa-accountant-1795151387828@example.com`
- TREASURER: `qa-treasurer-1795151387828@example.com`
- SALES: `qa-sales-1795151387828@example.com`
- OPERATIONS: `qa-operations-1795151387828@example.com`
- HR: `qa-hr-1795151387828@example.com`
- AUDITOR: `qa-auditor-1795151387828@example.com`
- USER: `qa-user-1795151387828@example.com`
- B2B_CLIENT: `qa-b2b_client-1795151387828@example.com`
- B2B_CLIENT: `qa-b2b_client-1795151387828@example.com`

## Matriz rol × endpoint

Celdas: ✅ status = coincide con lo esperado; ❌ status = discrepancia; — = rol no creado.

| Endpoint | ADMIN | ACCOUNTANT | TREASURER | SALES | OPERATIONS | HR | AUDITOR | USER | B2B_CLIENT |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| GET /invoices/{id} (UUID aleatorio) | ✅ 400 | ✅ 400 | ✅ 403 | ✅ 400 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 |
| GET /sii/caf | ✅ 200 | ✅ 200 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 |
| GET /sii/status | ✅ 200 | ✅ 200 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 |
| GET /accounting/entries | ✅ 200 | ✅ 200 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 |
| GET /payroll/employee/payslips | ✅ 404 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 404 | ✅ 403 | ✅ 404 | ✅ 403 |
| GET /actuator/metrics | ✅ 200 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 |
| POST /ai/conversations | ✅ 200 | ✅ 200 | ✅ 200 | ✅ 200 | ✅ 403 | ✅ 403 | ✅ 200 | ✅ 403 | ✅ 403 |
| GET /notifications/preferences | ✅ 200 | ✅ 200 | ✅ 200 | ✅ 200 | ✅ 200 | ✅ 200 | ✅ 200 | ✅ 200 | ✅ 200 |
| GET /b2b/orders/my-orders | ✅ 200 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 403 | ✅ 200 |

## Resultado

Sin discrepancias en los endpoints probados.

## Notas de la matriz

- **B2B_CLIENT** ya existe en UserRole y se crea por POST /sso/users; el endpoint B2B se prueba con su token.
- **payroll / USER**: el endpoint permite USER y ahora resuelve el empleado desde el usuario autenticado; solo devuelve sus liquidaciones (test backend PayrollPayslipIsolationTest). La fila USER de MAPA-FUNCIONAL.md estaba desactualizada.
- **actuator / ADMIN**: metrics está expuesto en application.properties y protegido por ADMIN; responde 200.
- **invoices / ALLOW (400)**: UUID inexistente; lo relevante es que no sea 403.
- **B2B aislamiento**: OK: cada cliente ve solo su orden (A=ca5507f0-6fd6-4fb8-afa9-5fcc9f25143e, B=bfaf851b-fdee-429f-94f4-ee9eb9d5aae2).

## B2B_CLIENT

UserRole incluye B2B_CLIENT. CustomerOrderService filtra por customerId y el test E2E crea dos clientes con una orden cada uno; cada GET /b2b/orders/my-orders devuelve solo la propia.
