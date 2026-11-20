# E2E — SII-ERP (API + Web)

Suites de QA de la ronda H. No forma parte del build de producción.

## Requisitos

- Backend en `http://localhost:8092` con Postgres 15 (Testcontainers no aplica aquí; ver `api/matrix.mjs`).
- Frontend en `http://localhost:3000` con proxy a `BACKEND_INTERNAL_URL=http://localhost:8092`.
- Node 22+.

## 1. Matriz de permisos por API

```bash
cd e2e
npm ci
npm run api
```

El script:
- Registra una empresa/admin vía `POST /api/v1/auth/register`.
- Crea un usuario por rol vía `POST /api/v1/sso/users` y hace login.
- Intenta crear `B2B_CLIENT` y documenta que el enum `UserRole` no lo soporta.
- Prueba 9 endpoints clave y escribe:
  - `api/report.md` — matriz rol × endpoint, esperado vs obtenido.
  - `api/results.json` — detalle para CI.
  - `.generated/users.json` — credenciales para Playwright.

## 2. E2E web (Playwright)

```bash
cd e2e
npm ci
npm run web
```

- `web/auth.spec.ts` — login ADMIN.
- `web/sidebar.spec.ts` — grupos del Sidebar para ADMIN y SALES.
- `web/navigation.spec.ts` — 5 páginas existentes sin errores de consola.
- `web/dark-mode.spec.ts` — `html.dark` y fondo oscuro.
- `web/pending.spec.ts` — `invoices/new`, `invoices/[id]` y `sii/caf` marcados `test.fixme` hasta que C integre sus pantallas.

## Hallazgos de la ronda H

- `SII-ERP-AI-FRONT/src/middleware.ts:10` redirige `/api/v1/**` a `/login` porque el matcher no excluye `/api`; el helper de Playwright usa una cookie dummy para poder iniciar sesión. Es un bug del front (ruta de C/E).
- `usePushNotifications.ts:14` y `app/actions.tsx:41` hardcodean `http://localhost:8080`; en local/CI eso produce errores CORS y SSE. Se evita `/settings` en la navegación de 5 páginas y se reporta.
- `PayrollController.java:59` permite USER en `/payroll/employee/payslips`, aunque `MAPA-FUNCIONAL.md` dice que USER no tiene áreas.
- `/actuator/metrics` no está expuesto (`application.properties:120`); ADMIN recibe 500 en vez de 200.

Playwright usa `channel: 'chrome'` (Chrome del sistema). Si no está instalado:

```bash
npm run web:install
npx playwright install-deps chromium
```

## Variables

- `API_URL` (default `http://localhost:8092/api/v1`)
- `API_ROOT` (default `http://localhost:8092`)
- `WEB_BASE_URL` (default `http://localhost:3000`; en local se usó `http://localhost:3004` para evitar conflictos de puerto)

## CI

El job E2E es opcional y no corre en PR: se dispara con `workflow_dispatch` y en push a `integracion`, porque levanta Postgres + backend + front (varios minutos). Ver `.github/workflows/ci.yml`.
