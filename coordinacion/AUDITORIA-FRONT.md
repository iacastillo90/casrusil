# Auditoría de datos no provenientes del backend — SII-ERP-AI-FRONT

- **Rama:** `docs/auditoria-front` (desde `origin/integracion`, commit `31e3e43`)
- **Alcance:** `SII-ERP-AI-FRONT/src/**` contrastado contra los controllers reales de `SII-ERP-AI-BACK/src/main/java`.
- **Tipo:** auditoría de solo lectura. No se modificó código de aplicación.
- **Fecha:** 2026-11-20
- **Método:** lectura de todos los `page.tsx`, hooks, services y componentes; extracción de `@RequestMapping`/`@GetMapping`/`@PostMapping` de los 59 controllers del backend; verificación de contratos (parámetros, headers y campos) en los controllers/DTOs.

## Leyenda de prioridad

- **P0:** un contador (o tesorero) lo ve en el uso diario y hoy no funciona o muestra datos falsos.
- **P1:** afecta un flujo contable/tributario relevante pero no diario, o muestra un dato de negocio incorrecto.
- **P2:** deuda de UI, botón sin acción, texto/catálogo fijo o componente no ruteado.

> Criterio de “página 100% dinámica”: todos los datos de negocio visibles provienen de una API real del backend. No se cuentan labels, textos de UI, catálogos legales (tipos de DTE) ni placeholders de formularios.

---

## 1. Hallazgos por módulo

### 1.1 Autenticación y onboarding

| Archivo:línea | Página / ruta | Tipo | Endpoint real que debería usar (o “no existe”) | Prioridad |
|---|---|---|---|---|
| `app/(auth)/onboarding/page.tsx:21-32` | `/onboarding` | simulación | `POST /company/onboarding` (existe) y `POST /company/onboarding/certificate` (existe); hoy solo hace `toast` y redirige | P1 |
| `app/(auth)/onboarding/page.tsx:79-111` | `/onboarding` | hardcode | Inputs RUT/razón social/certificado sin `register` ni estado: nunca se envían al backend | P1 |
| `lib/formatters.ts:50-69` | global | hardcode | `validateRut()` retorna `true` siempre (“Validación deshabilitada para facilitar pruebas”) | P1 |

### 1.2 Dashboard

| Archivo:línea | Página / ruta | Tipo | Endpoint real que debería usar (o “no existe”) | Prioridad |
|---|---|---|---|---|
| `features/invoicing/services/invoice.service.ts:184-186` | `/invoices/[id]`, `/invoices` | hardcode | `GET /invoices/{id}` (existe) pero `recipientName` se fuerza a `''`; el backend no persiste razón social del receptor | P1 |
| `app/(dashboard)/invoices/[id]/page.tsx:272-275` | `/invoices/[id]` | hardcode | No existe endpoint de sostenibilidad por documento; solo `GET /sustainability/score` global | P2 |
| `features/dashboard/components/KPIGrid.tsx:8,46,62,76,90` | `/dashboard` | — | Dinámico (`GET /dashboard/finance/metrics`); sin hallazgos | — |

### 1.3 Facturación y CRM

| Archivo:línea | Página / ruta | Tipo | Endpoint real que debería usar (o “no existe”) | Prioridad |
|---|---|---|---|---|
| `features/invoicing/components/CsvImportModal.tsx:39-45` | `/invoices` | simulación | `POST /invoices/import` (existe, requiere `file`, `companyId` y `bookType`); hoy muestra éxito sin llamar | P1 |
| `features/invoicing/components/InvoicePDF.tsx:183-185` | `/invoices/[id]` | hardcode | Recalcula neto e IVA 19% en cliente; debería usar `netAmount`/`taxAmount` de `GET /invoices/{id}` | P2 |
| `app/(dashboard)/crm/leads/page.tsx:52-55` | `/crm/leads` | botón muerto | `POST /crm/leads` (existe); “Nuevo Lead” no tiene handler | P2 |
| `app/(dashboard)/crm/quotes/page.tsx:26-29` | `/crm/quotes` | botón muerto | `POST /crm/quotes` (existe); “Nueva Cotización” no tiene handler | P2 |

### 1.4 Contabilidad

| Archivo:línea | Página / ruta | Tipo | Endpoint real que debería usar (o “no existe”) | Prioridad |
|---|---|---|---|---|
| `features/accounting/hooks/useAccounting.ts:26-28` | `/accounting/ledger` | simulación (stub) | `GET /accounting/entries` (existe); hoy `useLedger` devuelve `{ data: [], isLoading: false }` | **P0** |
| `features/accounting/hooks/useAccounting.ts:30-32` | `/accounting/ledger` | simulación | `POST /accounting/cleanup/november-rcv` (existe); `useCleanupNovember` es un no-op | P1 |
| `features/accounting/hooks/useAccounting.ts:33-35` | `/accounting/f29` | simulación (stub) | `GET /accounting/f29?period=` (existe); `useF29` devuelve `null` | **P0** |
| `app/(dashboard)/accounting/f29/page.tsx:26` | `/accounting/f29` | contrato roto | `GET /accounting/f29` (existe); pasa `report` a `F29Preview`, que declara `report?: never` y espera `period`/`precalculatedData` | **P0** |
| `features/accounting/components/BalanceSheetTree.tsx:24-95` | `/accounting/balance` | mock | `GET /accounting/accounts/tree` (existe); hay árbol mock hardcodeado (hoy no se usa porque la página pasa `data`, pero el código sigue vivo) | P2 |
| `app/(dashboard)/accounting/close/page.tsx:95,101,148,156` | `/accounting/close` | hardcode | `POST /period-close/audit` y `POST /period-close/execute` (existen); período fijo “Noviembre 2026”, año/mes `2026/11` y utilidad fija `$4.220.000` | P1 |
| `app/(dashboard)/accounting/close/page.tsx:106-119` | `/accounting/close` | simulación | `POST /period-close/audit` (existe); marca los 4 pasos como “Validado correctamente” aunque el backend solo devuelve `issuesFound` agregado | P1 |
| `app/(dashboard)/accounting/annual-close/page.tsx:100,107,151-158,180` | `/accounting/annual-close` | hardcode + simulación | `POST /annual-close/f22/calculate/{fiscalYear}` (existe); `handleClose` no llama API, año fijo 2026 y saldos 2027 | P1 |
| `app/(dashboard)/accounting/annual-close/page.tsx:284-289` | `/accounting/annual-close` | botón muerto | `POST /annual-close/f22/calculate/{fiscalYear}` (existe); “Ver Borrador F22” y “Exportar Papeles” sin handler | P2 |
| `features/accounting/components/F29Preview.tsx:28-41` | `/accounting/f29` | hardcode | `GET /accounting/f29` (existe); selector de períodos solo 2024 | P1 |
| `features/accounting/components/F29Preview.tsx:268-275` | `/accounting/f29` | botón muerto | `GET /accounting/f29` / `POST /reports/f29/submit` (existen); “Exportar PDF” sin handler | P2 |
| `features/accounting/components/TaxReconciliationTable.tsx:27-40` | `/accounting/audit`, `/sii/audit` | hardcode | `GET /accounting/f29?period=` (existe); selector de períodos solo 2024 | P1 |
| `features/accounting/components/TaxReconciliationTable.tsx:268-274` | `/accounting/audit` | botón muerto | `POST /reports/f29/submit` (existe); “Exportar a Excel” y “Enviar Declaración” sin handler | P2 |
| `features/accounting/components/JournalEntryTable.tsx:56-63` | `/accounting/ledger` | simulación/TODO | `GET /accounting/entries` (existe, sin filtros de rango); con rango de fechas devuelve `[]` | P1 |
| `features/accounting/components/DepreciationCalculator.tsx:85-95` | no ruteado | simulación | `POST /fixed-assets/run-depreciation` (existe); “Aplicar Depreciación” solo hace `console.log` + `alert()` | P2 |

### 1.5 Tributario / SII

| Archivo:línea | Página / ruta | Tipo | Endpoint real que debería usar (o “no existe”) | Prioridad |
|---|---|---|---|---|
| `app/(dashboard)/accounting/f29/page.tsx:26` | `/accounting/f29` | contrato roto | `GET /accounting/f29` (existe); ver 1.4 | **P0** |
| `features/sii/components/CAFUpload.tsx:51-54` | `/sii` | simulación | `POST /sii/caf` (existe); progreso simulado con `setInterval` (la carga real sí ocurre) | P2 |
| `app/(dashboard)/sii/audit/page.tsx:21` | `/sii/audit` | hardcode | `GET /accounting/f29` (existe) vía `TaxReconciliationTable`; períodos fijos 2024 | P1 |

### 1.6 Tesorería y bancos

| Archivo:línea | Página / ruta | Tipo | Endpoint real que debería usar (o “no existe”) | Prioridad |
|---|---|---|---|---|
| `features/treasury/services/treasury.service.ts:18-22` | `/treasury` | endpoint inexistente + hardcode | `GET /treasury/exchange-rates` **no existe**; fallback fijo `USD_CLP 945.50`, `EUR_CLP 1020.30`, `UF_CLP 37500.20` (alternativa: `GET /integration/indicators/today`) | P1 |
| `features/treasury/components/BankStatementUpload.tsx:23-26` | `/treasury` | simulación | `POST /treasury/statements/upload` **no existe** (comentado); muestra éxito sin subir nada | **P0** |
| `features/treasury/components/CashEntryModal.tsx:44-47` | `/treasury` | simulación | `POST /treasury/cash-entries` (existe); comentado y muestra éxito sin registrar | **P0** |
| `features/treasury/components/ReconciliationInterface.tsx:19-29` | `/treasury` | sin backend | `GET /banking/transactions` + `POST /banking/transactions/{id}/reconcile`/`ignore` (existen); opera sobre estado local vacío | **P0** |
| `app/(dashboard)/banking/page.tsx:17-20` | `/banking` | botón muerto | `POST /banking/connections` (existe); “Conectar Banco” sin handler | P2 |

### 1.7 Personas / remuneraciones

| Archivo:línea | Página / ruta | Tipo | Endpoint real que debería usar (o “no existe”) | Prioridad |
|---|---|---|---|---|
| `app/(dashboard)/payroll/page.tsx:16-18` | `/payroll` | hardcode | `GET /payroll/employees` (existe); leyes sociales estimadas con `0.22` fijo (“for demo”) | P1 |
| `app/(dashboard)/payroll/page.tsx:117` | `/payroll` | hardcode | `GET /payroll/employees` (existe); “Período Noviembre 2026” fijo | P2 |
| `app/(dashboard)/payroll/page.tsx:44-46,148-150` | `/payroll` | botón muerto | `POST /payroll/calculate` / `GET /payroll/employee/payslips` (existen); “Emitir Liquidaciones” y “Ver Detalle” sin handler | P2 |
| `features/payroll/components/PayrollDashboard.tsx:25` | no ruteado | hardcode/contrato | `POST /payroll/calculate` (existe); envía `{ periodId: 1 }` cuando el backend espera `{ year, month }` | P2 |
| `features/payroll/components/PayrollDashboard.tsx:36` | no ruteado | endpoint inexistente | `GET /payroll/payslip/{id}/pdf` **no existe** | P2 |
| `features/employee/services/employee.service.ts:13` | `/employee` | contrato/tenancy | `GET /payroll/employee/payslips` (existe) pero requiere `X-Employee-Id`; el front no lo envía y el backend usa UUID cero | P1 |
| `app/(dashboard)/employee/page.tsx:24-27,52-54` | `/employee` | botón muerto | “Solicitar Vacaciones” y descarga de liquidación sin handler | P2 |

### 1.8 Operaciones

| Archivo:línea | Página / ruta | Tipo | Endpoint real que debería usar (o “no existe”) | Prioridad |
|---|---|---|---|---|
| `app/(dashboard)/inventory/page.tsx:67` | `/inventory` | hardcode | `GET /inventory/products` (existe); “+2.4% vs mes anterior” fijo | P2 |
| `app/(dashboard)/inventory/page.tsx:46-54` | `/inventory` | botón muerto | `POST /inventory/products`, `POST /inventory/products/{id}/movements` (existen); Entrada/Salida/Nuevo sin handler | P2 |
| `features/inventory/hooks/useInventory.ts:15,35` | `/inventory` | hardcode (fallback) | `GET /inventory/products` (existe); `minStock` cae a `5` si falta | P2 |
| `features/inventory/components/InventoryDashboard.tsx:42` | no ruteado | endpoint inexistente | `GET /inventory/export/excel` **no existe** | P2 |
| `features/inventory/components/InventoryDashboard.tsx:31-32` | no ruteado | contrato | `GET /inventory/products` (existe); espera `quantity/unitCost/totalValue` pero el DTO devuelve `currentStock/salePrice/averageCost` | P2 |
| `app/(dashboard)/projects/page.tsx:59,63` | `/projects` | hardcode | No existe endpoint de P&L por proyecto; “Gastos a la fecha” e “Ingresos” siempre `$0` | P1 |
| `app/(dashboard)/fleet/page.tsx:39,49` | `/fleet` | hardcode | `GET /fleet/metrics` (existe); variaciones “+2 desde ayer” y “+2.1% esta semana” fijas | P2 |
| `app/(dashboard)/fleet/page.tsx:71-77` | `/fleet` | placeholder | `GET /fleet/metrics` (existe); mapa de telemetría “próximamente” | P2 |
| `app/(dashboard)/pos/page.tsx:49-50` | `/pos` | hardcode | `POST /pos/transactions` (existe); `posTerminalId: 'CAJA-01'` y `cashierName: 'Cajero Principal'` fijos | P1 |
| `app/(dashboard)/pos/page.tsx:86` | `/pos` | TODO | No existe endpoint de “quick items” por categoría | P2 |
| `app/(dashboard)/wms/page.tsx:28` | `/wms` | botón muerto | `POST /wms/...` (según flujo); “Escanear QR” sin handler | P2 |

### 1.9 Compras, documentos y reportes

| Archivo:línea | Página / ruta | Tipo | Endpoint real que debería usar (o “no existe”) | Prioridad |
|---|---|---|---|---|
| `features/fees/services/fee.service.ts:27` | `/fees` | endpoint inexistente | `GET /fees` **no existe**; el backend solo expone `POST /fees/import` | **P0** |
| `features/fees/components/FeeImportModal.tsx:30-33` | `/fees` | simulación | `POST /fees/import` (existe; requiere `file` y `companyId`); comentado y muestra éxito sin importar | **P0** |
| `app/(dashboard)/fees/page.tsx:13-17` | `/fees` | sin backend | `GET /fees` no existe; el listado siempre queda vacío/error | **P0** |
| `app/(dashboard)/procurement/page.tsx:28-31` | `/procurement` | botón muerto | `POST /procurement/orders` (existe); “Nueva Orden” sin handler | P2 |
| `app/(dashboard)/documents/page.tsx:30` | `/documents` | botón muerto | `POST /dms` (existe, multipart); “Subir Documento” sin handler | P2 |
| `app/(dashboard)/reports/page.tsx:22-23` | `/reports` | hardcode/empty | `GET /reports/income-statement` y `GET /reports/balance-sheet` (existen); compras/balance se generan con `[]` | P1 |
| `features/reports/hooks/useReports.ts:24` | `/reports` | hardcode (fallback) | `GET /invoices` (existe); si falta fecha usa la fecha de hoy | P2 |

### 1.10 Energía y sostenibilidad

| Archivo:línea | Página / ruta | Tipo | Endpoint real que debería usar (o “no existe”) | Prioridad |
|---|---|---|---|---|
| `app/(dashboard)/energy/page.tsx:95-122` | `/energy` | mock/hardcode | `GET /energy/stations` (existe); bloque “Estado de Cargadores” con “Estación Norte/Sur/Centro Logístico/Este”, “Ocupado (80%)”, “Libre”, “Mantenimiento” fijos | P1 |
| `app/(dashboard)/energy/page.tsx:78-84` | `/energy` | placeholder | `GET /energy/metrics` (existe); gráfico “próximamente” | P2 |
| `features/sustainability/services/sustainability.service.ts:59-80` | `/sustainability`, `/dashboard` | hardcode (fallback) | `GET /sustainability/score` (existe); ante error devuelve ceros (`ecoScore 0`, compras 0) en vez de propagar el error | P2 |

### 1.11 Configuración y layout global

| Archivo:línea | Página / ruta | Tipo | Endpoint real que debería usar (o “no existe”) | Prioridad |
|---|---|---|---|---|
| `features/settings/services/settings.service.ts:93` | `/settings` | endpoint inexistente | `DELETE /sso/users/{id}` **no existe** en `SsoUserController` | P1 |
| `features/settings/services/settings.service.ts:103` | `/settings` | endpoint inexistente | `PUT /notifications/preferences` **no existe**; `NotificationController` solo expone `GET /preferences` | P2 |
| `app/(dashboard)/settings/company/page.tsx:23-31` | `/settings/company` | sin backend (no carga) | `GET /company/profile` (existe); el formulario arranca vacío y nunca lee el perfil | P2 |
| `features/settings/components/InviteUserModal.tsx:111` | `/settings` | hardcode | Remitente fijo `invitaciones@sii-erp-ai.com` | P2 |
| `features/accounting/hooks/useAccounting.ts:37-40` | layout global (campana) | simulación (stub) | `GET /audit/alerts` (existe); `useAuditAlerts` devuelve `[]`, la campana nunca muestra alertas | P1 |
| `hooks/usePushNotifications.ts:14` | layout global | hardcode + fetch fijo | `GET /notifications/subscribe?userId=` (existe); usa `http://localhost:8080/...` y `userId=1` fijos | P1 |
| `app/actions.tsx:41` | asistente IA global | hardcode (fallback) | `POST /ai/conversations` y `POST /ai/conversations/{id}/messages` (existen); fallback `http://localhost:8080/api/v1` | P2 |
| `components/layout/Header.tsx:49-52` | layout global | botón muerto | `POST /sii/sync` (existe); “Sincronizar” sin handler | P2 |
| `lib/constants.ts:18-37` | global | endpoints obsoletos (no usados) | `/sii/dte/send`, `/bank/upload`, `/ai/chat`, `/accounting/entries/correct` no existen; `API_ROUTES` no se importa en ningún archivo | P2 |

---

## 2. Endpoints que el front llama y no existen (consolidado)

| Endpoint llamado por el front | Archivo:línea | ¿Existe en backend? | Reemplazo real | Prioridad |
|---|---|---|---|---|
| `GET /treasury/exchange-rates` | `features/treasury/services/treasury.service.ts:18` | No | `GET /integration/indicators/today` (UF/USD) | P1 |
| `POST /treasury/statements/upload` | `features/treasury/components/BankStatementUpload.tsx:23` | No | Sin equivalente; usar `POST /banking/sync` + `GET /banking/transactions` | **P0** |
| `GET /fees` | `features/fees/services/fee.service.ts:27` | No | Sin equivalente; solo `POST /fees/import` | **P0** |
| `DELETE /sso/users/{id}` | `features/settings/services/settings.service.ts:93` | No | Sin equivalente; `PUT /sso/users/{id}/role` solo cambia rol | P1 |
| `PUT /notifications/preferences` | `features/settings/services/settings.service.ts:103` | No | Solo `GET /notifications/preferences` | P2 |
| `GET /inventory/export/excel` | `features/inventory/components/InventoryDashboard.tsx:42` | No | `GET /reports/sales/excel` no aplica; no hay export de inventario | P2 |
| `GET /payroll/payslip/{id}/pdf` | `features/payroll/components/PayrollDashboard.tsx:36` | No | Sin equivalente | P2 |
| `/sii/dte/send`, `/bank/upload`, `/ai/chat`, `/accounting/entries/correct` | `lib/constants.ts:20,32,37,29` | No | Constantes muertas (no importadas) | P2 |

### Endpoints que existen pero el front no usa (falsos “mocks”)

| Endpoint real | Dónde debería usarse | Hoy |
|---|---|---|
| `GET /accounting/entries` | `/accounting/ledger` | `useLedger` stub devuelve `[]` |
| `GET /accounting/f29` | `/accounting/f29` | `useF29` stub devuelve `null` |
| `POST /accounting/cleanup/november-rcv` | `/accounting/ledger` | `useCleanupNovember` no-op |
| `GET /accounting/accounts/tree` | `/accounting/balance` (árbol) | `BalanceSheetTree` tiene mock |
| `GET /audit/alerts` | campana de notificaciones | `useAuditAlerts` stub `[]` |
| `POST /company/onboarding` | `/onboarding` | no se llama |
| `POST /invoices/import` | `/invoices` (CSV) | comentado |
| `POST /fees/import` | `/fees` (importar) | comentado |
| `POST /treasury/cash-entries` | `/treasury` (movimiento) | comentado |
| `POST /annual-close/f22/calculate/{year}` | `/accounting/annual-close` | no se llama |
| `POST /dms` | `/documents` | botón sin handler |
| `POST /fixed-assets/run-depreciation` | `DepreciationCalculator` | `alert()` |

---

## 3. Componentes/hooks no ruteados con mocks o contratos viejos

Estos archivos no se importan desde ningún `page.tsx` (verificado por búsqueda). No afectan la UI actual, pero son deuda que puede revivir un mock:

| Archivo | Qué contiene | Acción sugerida |
|---|---|---|
| `features/accounting/components/BalanceSheetTree.tsx:24-95` | Árbol de balance mock con montos fijos | eliminar mock y usar `accountingService.getBalanceSheetTree()` |
| `features/accounting/components/DepreciationCalculator.tsx:85-95` | Cálculo local + `alert()` | conectar a `POST /fixed-assets/run-depreciation` o eliminar |
| `features/payroll/components/PayrollDashboard.tsx:25,36` | `periodId: 1` y `/payroll/payslip/{id}/pdf` inexistente | alinear a `{ year, month }` y quitar PDF |
| `features/inventory/components/InventoryDashboard.tsx:31-42` | Campos y export inexistentes | alinear a `ProductResponse` y eliminar export |
| `features/invoicing/components/CsvImportModal.tsx` | Import simulado | conectar `POST /invoices/import` |

---

## 4. Resumen por módulo

Se cuentan los 43 `page.tsx` del proyecto. `app/page.tsx` es solo un redirect a `/login`, por lo que **no entra en el denominador de datos** (42 páginas con datos).

| Módulo | Páginas | 100% dinámicas | Con datos fijos | Sin backend | % 100% dinámicas |
|---|---:|---:|---:|---:|---:|
| Autenticación | 3 | 2 | 0 | 1 | 66,7% |
| Dashboard | 1 | 1 | 0 | 0 | 100,0% |
| Ventas / CRM | 5 | 4 | 1 | 0 | 80,0% |
| Contabilidad | 8 | 4 | 3 | 1 | 50,0% |
| Tributario / SII | 5 | 3 | 1 | 1 | 60,0% |
| Tesorería / Bancos | 3 | 2 | 1 | 0 | 66,7% |
| Personas | 2 | 1 | 1 | 0 | 50,0% |
| Operaciones | 6 | 2 | 4 | 0 | 33,3% |
| Compras | 2 | 1 | 0 | 1 | 50,0% |
| Documentos / Reportes | 2 | 1 | 1 | 0 | 50,0% |
| Energía / Sostenibilidad | 2 | 1 | 1 | 0 | 50,0% |
| Configuración | 2 | 1 | 0 | 1 | 50,0% |
| Portal B2B | 1 | 1 | 0 | 0 | 100,0% |
| **Total** | **42** | **24** | **13** | **5** | **57,1%** |

### Detalle de la clasificación

- **100% dinámicas (24):** `/login`, `/register`, `/dashboard`, `/invoices`, `/invoices/new`, `/crm/leads`, `/crm/quotes`, `/accounting/balance`, `/accounting/income`, `/accounting/partners`, `/accounting/assets`, `/compliance`, `/sii`, `/sii/caf`, `/banking`, `/banking/reconciliation`, `/employee`, `/wms`, `/mrp`, `/procurement`, `/documents`, `/sustainability`, `/settings`, `/portal`.
- **Con datos fijos (13):** `/invoices/[id]`, `/accounting/close`, `/accounting/annual-close`, `/accounting/audit`, `/sii/audit`, `/treasury`, `/payroll`, `/inventory`, `/projects`, `/fleet`, `/pos`, `/reports`, `/energy`.
- **Sin backend (5):** `/onboarding`, `/accounting/ledger`, `/accounting/f29`, `/fees`, `/settings/company`.

---

## 5. Top de bloqueantes P0 (uso diario del contador)

1. **Libro Diario siempre vacío** — `useLedger` es stub (`useAccounting.ts:26-28`); debería consumir `GET /accounting/entries`.
2. **F29 nunca muestra datos** — `useF29` es stub (`useAccounting.ts:33-35`) y la página pasa mal el prop a `F29Preview` (`f29/page.tsx:26`).
3. **Boletas de honorarios inoperativas** — `GET /fees` no existe (`fee.service.ts:27`) y la importación es simulada (`FeeImportModal.tsx:30-33`).
4. **Movimiento de caja no se guarda** — `CashEntryModal.tsx:44-47` muestra éxito sin llamar a `POST /treasury/cash-entries`.
5. **Conciliación de tesorería sin backend** — `ReconciliationInterface.tsx:19-29` opera sobre estado local vacío en vez de `GET /banking/transactions`.
6. **Carga de cartola bancaria simulada** — `BankStatementUpload.tsx:23-26` no sube nada (y el endpoint no existe).

---

## 6. Notas de alcance

- No se modificó ningún archivo de `SII-ERP-AI-FRONT/src` ni de `SII-ERP-AI-BACK`.
- El contraste de endpoints se hizo contra los 59 `*Controller.java` del backend y sus DTOs (`EmployeeResponse`, `ProductResponse`, `PayrollProcessRequest`, `CashEntryRequest`, `F29Report`, etc.).
- Los `page.tsx` de `(auth)` y `(dashboard)` se consideran páginas; los `layout.tsx` no.
- Los catálogos legales fijos (tipos de DTE 33/39/56/61, IVA 19%) no se listan como “datos fijos” porque son constantes tributarias, no datos de negocio que deban venir del backend. Sí se listan cuando se usan para recalcular montos que el backend ya entrega (ej. `InvoicePDF.tsx`).
