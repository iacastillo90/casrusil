# Auditoría de datos no provenientes del backend — SII-ERP-AI-FRONT (v2)

- **Rama:** `docs/auditoria-front-2` (desde `origin/integracion`, commit `a9c8732`)
- **Alcance:** `SII-ERP-AI-FRONT/src/**` contrastado contra los controllers reales de `SII-ERP-AI-BACK/src/main/java`.
- **Tipo:** segunda auditoría de solo lectura. No se modificó código de aplicación.
- **Fecha:** 2026-11-20
- **Método:** mismo que la v1: lectura de todos los `page.tsx`, hooks, services y componentes; extracción de mappings de controllers; verificación de contratos y de endpoints existentes/inexistentes. Se contrastó además cada hallazgo de la v1 contra el código actual.

## Leyenda de prioridad

- **P0:** un contador (o tesorero) lo ve en el uso diario y hoy no funciona o muestra datos falsos.
- **P1:** afecta un flujo contable/tributario relevante pero no diario, o muestra un dato de negocio incorrecto.
- **P2:** deuda de UI, botón sin acción, texto/catálogo fijo o componente no ruteado.

> Criterio de “página 100% dinámica”: todos los datos de negocio visibles provienen de una API real del backend. No se cuentan labels, textos de UI, catálogos legales ni valores de configuración. Los pendientes de definición del dueño se listan aparte y no se cuentan como datos fijos salvo que la página muestre un valor fijo (ej. terminal POS).

---

## 1. Resumen ejecutivo v2

- **43 `page.tsx`** en total; **42 páginas con datos** + `app/page.tsx` (redirect a `/login`).
- **40 páginas 100% dinámicas (95,2%)**.
- **2 páginas con datos fijos (4,8%):** `/invoices/[id]` y `/pos`.
- **0 páginas sin backend**.
- **Sin bloqueantes P0** de la v1: libro diario, F29, honorarios, caja, conciliación y cartola quedaron conectados o explícitamente marcados como pendientes de dueño.
- **Quedan 5 defectos P1** y varios P2 de botones/endpoints/deuda no ruteada (sección 3).

**Delta vs v1:** 57,1% → **95,2%** de páginas 100% dinámicas. Se cerraron 5 de 5 páginas “sin backend” y 11 de 13 páginas “con datos fijos”.

---

## 2. Qué se cerró desde la primera auditoría

| Hallazgo v1 | Estado v2 | Evidencia |
|---|---|---|
| `/onboarding` simulaba el alta | **Cerrado** | `features/onboarding/services/onboarding.service.ts` usa `POST /company/onboarding` y `/company/onboarding/certificate` |
| `useLedger` stub devolvía `[]` | **Cerrado** | `useAccounting.ts` usa `GET /accounting/entries`; `JournalEntryTable` es presentacional real |
| `useF29` stub devolvía `null` y `f29/page.tsx` pasaba mal el prop | **Cerrado** | `useF29` usa `GET /accounting/f29`; `F29Preview` recibe `report` real |
| `BalanceSheetTree` tenía árbol mock | **Cerrado** | El componente usa solo `BalanceSheet` del backend, con secciones reales |
| `/accounting/close` fijaba Noviembre 2026 y utilidad $4.220.000 | **Cerrado** | Selector de período + `usePeriodStatus` + `/period-close/audit|execute` |
| `/accounting/annual-close` simulaba el cierre y fijaba 2026/2027 | **Cerrado** | `POST /annual-close/f22/calculate/{year}` con año seleccionable |
| `F29Preview` y `TaxReconciliationTable` solo permitían 2024 | **Cerrado** | Inputs `type="month"`; datos reales por período |
| `TaxReconciliationTable` usaba F29 como si fuera RCV | **Cerrado** | Usa `GET /accounting/audit/sii-mirror` documento por documento |
| `JournalEntryTable` no consultaba asientos | **Cerrado** | Recibe `GET /accounting/entries` desde la página |
| `DepreciationCalculator` aplicaba con `alert()` | **Cerrado** | Archivo eliminado; activos fijos usan `/fixed-assets/run-depreciation` |
| `CashEntryModal` simulaba el alta | **Cerrado** | `POST /treasury/cash-entries` real + refresh de lista |
| `ReconciliationInterface` operaba sobre estado local | **Cerrado** | `GET /banking/transactions` + `POST reconcile/ignore` reales |
| `BankStatementUpload` simulaba la carga | **Cerrado como defecto** | Ahora sincroniza con `POST /banking/sync`; import de cartolas queda pendiente de dueño |
| `GET /fees` no existía y la importación era simulada | **Cerrado** | Backend `GET /fees`; `POST /fees/import` real y toma la empresa del JWT |
| `/payroll` usaba 22% fijo y “Noviembre 2026” | **Cerrado** | Cálculo real `POST /payroll/calculate`; descuentos y líquido vienen del backend |
| `/employee` no enviaba `X-Employee-Id` | **Cerrado** | El backend resuelve el empleado desde el JWT; el front ya no necesita header |
| `/inventory` tenía “+2.4%”, `minStock=5` y botones muertos | **Cerrado** | Movimientos/alta reales; `minStock` 0 si falta; sin variación fija |
| `/projects` mostraba Gastos/Ingresos `$0` | **Cerrado como defecto** | Ya no inventa P&L; avisa que el backend no lo expone |
| `/fleet` tenía “+2 desde ayer” y “+2.1%” fijos | **Cerrado** | Métricas reales de `GET /fleet/metrics` |
| `/pos` usaba “Cajero Principal” fijo | **Parcial** | Cajero sale de la sesión; terminal sigue pendiente de dueño |
| `/energy` tenía estaciones fijas | **Cerrado** | `GET /energy/stations`, métricas y alertas reales |
| `/reports` generaba compras/balance con `[]` | **Cerrado** | Ventas, compras y balance usan backend; Excel solo ventas queda pendiente de dueño |
| `/settings/company` arrancaba vacío | **Cerrado** | Reutiliza `CompanyProfile` real (`GET/PUT /company/profile`) |
| `usePushNotifications` usaba `localhost:8080` y `userId=1` | **Cerrado** | Same-origin y `userId` desde el auth store |
| `app/actions.tsx` usaba `localhost:8080` fijo | **Cerrado** | Usa `BACKEND_INTERNAL_URL` |
| `CAFUpload` simulaba progreso | **Abierto** | Sigue con `setInterval`; la carga real sí ocurre |
| `lib/formatters.ts` `validateRut()` siempre `true` | **Abierto** | Sin cambios |
| `invoice.service` fuerza `recipientName: ''` | **Abierto** | Sin cambios; el backend no persiste razón social del receptor |
| `CsvImportModal` muestra éxito sin importar | **Abierto** | Sigue comentado `POST /invoices/import` |
| `useAuditAlerts` stub devuelve `[]` | **Abierto** | `/audit/alerts` existe y devuelve alertas; el front no lo consume |
| `DELETE /sso/users/{id}` y `PUT /notifications/preferences` | **Abierto** | Siguen sin existir en backend |
| `banking/page.tsx` “Conectar Banco” sin handler | **Abierto** | Sin cambios |
| `crm` “Nuevo Lead”/“Nueva Cotización” sin handler | **Abierto** | Sin cambios |
| `wms` “Escanear QR” sin handler | **Abierto** | Sin cambios |
| `employee` botones de vacaciones/PDF sin handler | **Abierto** | PDF queda pendiente de dueño |
| `Header` “Sincronizar” sin handler | **Abierto** | Sin cambios |
| `lib/constants.ts` endpoints muertos | **Abierto** | `API_ROUTES` sigue sin importarse |
| `InventoryDashboard`/`PayrollDashboard` no ruteados con endpoints falsos | **Abierto** | Siguen en el repo |
| `sustainability.service` devuelve ceros ante error | **Abierto** | Sin cambios |

---

## 3. Defectos reales que quedan (v2)

### 3.1 Autenticación / global

| Archivo:línea | Página / ruta | Tipo | Endpoint real que debería usar (o “no existe”) | Prioridad |
|---|---|---|---|---|
| `lib/formatters.ts:68` | global | hardcode | `validateRut()` retorna `true` siempre | P1 |
| `components/layout/Header.tsx:49-52` | layout global | botón muerto | `POST /sii/sync` (existe); “Sincronizar” sin handler | P2 |
| `lib/constants.ts:18-37` | global | endpoints obsoletos (no usados) | `/sii/dte/send`, `/bank/upload`, `/ai/chat`, `/accounting/entries/correct` no existen; `API_ROUTES` no se importa | P2 |

### 3.2 Facturación / ventas

| Archivo:línea | Página / ruta | Tipo | Endpoint real que debería usar (o “no existe”) | Prioridad |
|---|---|---|---|---|
| `features/invoicing/services/invoice.service.ts:190` | `/invoices/[id]` | hardcode | `GET /invoices/{id}` (existe) pero `recipientName` se fuerza a `''`; el backend no persiste ese campo | P1 |
| `features/invoicing/components/CsvImportModal.tsx:39-45` | `/invoices` | simulación | `POST /invoices/import` (existe; requiere `file`, `companyId` y `bookType`); muestra éxito sin llamar | P1 |
| `features/invoicing/components/InvoicePDF.tsx:183-185` | `/invoices/[id]` | hardcode | Recalcula neto e IVA 19%; debería usar `netAmount`/`taxAmount` del backend | P2 |
| `app/(dashboard)/invoices/[id]/page.tsx:272-275` | `/invoices/[id]` | hardcode | No existe endpoint de sostenibilidad por documento; se pasa `data={null}` | P2 |
| `app/(dashboard)/crm/leads/page.tsx:52-55` | `/crm/leads` | botón muerto | `POST /crm/leads` (existe); “Nuevo Lead” sin handler | P2 |
| `app/(dashboard)/crm/quotes/page.tsx:26-29` | `/crm/quotes` | botón muerto | `POST /crm/quotes` (existe); “Nueva Cotización” sin handler | P2 |

### 3.3 Contabilidad / SII

| Archivo:línea | Página / ruta | Tipo | Endpoint real que debería usar (o “no existe”) | Prioridad |
|---|---|---|---|---|
| `features/accounting/hooks/useAccounting.ts:91-92` | layout global (campana) | simulación (stub) | `GET /audit/alerts` (existe y devuelve alertas); el hook devuelve `[]` | P1 |
| `features/sii/components/CAFUpload.tsx:48-54` | `/sii` | simulación | `POST /sii/caf` (existe); progreso simulado con `setInterval` (la carga real sí ocurre) | P2 |

### 3.4 Tesorería / bancos

| Archivo:línea | Página / ruta | Tipo | Endpoint real que debería usar (o “no existe”) | Prioridad |
|---|---|---|---|---|
| `app/(dashboard)/banking/page.tsx:17-20` | `/banking` | botón muerto | `POST /banking/connections` (existe); “Conectar Banco” sin handler (sí funciona el formulario de `/banking/reconciliation`) | P2 |

### 3.5 Personas

| Archivo:línea | Página / ruta | Tipo | Endpoint real que debería usar (o “no existe”) | Prioridad |
|---|---|---|---|---|
| `app/(dashboard)/employee/page.tsx:24-27` | `/employee` | botón muerto | No existe endpoint de solicitud de vacaciones | P2 |
| `features/payroll/components/PayrollDashboard.tsx:25,36` | no ruteado | contrato + endpoint inexistente | `POST /payroll/calculate` (existe; espera `{year,month}`, no `periodId`) y `GET /payroll/payslip/{id}/pdf` **no existe** | P2 |

### 3.6 Operaciones

| Archivo:línea | Página / ruta | Tipo | Endpoint real que debería usar (o “no existe”) | Prioridad |
|---|---|---|---|---|
| `app/(dashboard)/wms/page.tsx:28` | `/wms` | botón muerto | “Escanear QR” sin handler; no hay endpoint de escaneo | P2 |
| `features/inventory/components/InventoryDashboard.tsx:31-42` | no ruteado | contrato + endpoint inexistente | `GET /inventory/products` (existe) pero espera `quantity/unitCost/totalValue`; usa `GET /inventory/export/excel` **que no existe** | P2 |

### 3.7 Documentos / compras / reportes

| Archivo:línea | Página / ruta | Tipo | Endpoint real que debería usar (o “no existe”) | Prioridad |
|---|---|---|---|---|
| — | — | — | Sin defectos de datos abiertos; la limitación de Excel/PDF está en “pendiente de dueño” | — |

### 3.8 Configuración / layout

| Archivo:línea | Página / ruta | Tipo | Endpoint real que debería usar (o “no existe”) | Prioridad |
|---|---|---|---|---|
| `features/settings/services/settings.service.ts:93` | `/settings` | endpoint inexistente | `DELETE /sso/users/{id}` **no existe** en `SsoUserController` | P1 |
| `features/settings/services/settings.service.ts:103` | `/settings` | endpoint inexistente | `PUT /notifications/preferences` **no existe**; `NotificationController` solo expone `GET /preferences` | P2 |
| `features/settings/components/InviteUserModal.tsx:111` | `/settings` | hardcode (texto) | Remitente fijo `invitaciones@sii-erp-ai.com` | P2 |
| `features/sustainability/services/sustainability.service.ts:71-80` | `/sustainability`, `/dashboard` | hardcode (fallback) | `GET /sustainability/score` (existe); ante error devuelve ceros en vez de propagar el error | P2 |

### 3.9 Componentes/hooks no ruteados con deuda

| Archivo | Qué contiene | Acción sugerida |
|---|---|---|
| `features/payroll/components/PayrollDashboard.tsx:25,36` | `periodId:1` y PDF inexistente | eliminar o alinear al flujo real de `/payroll` |
| `features/inventory/components/InventoryDashboard.tsx:31-42` | Campos y export inexistentes | eliminar o alinear a `ProductResponse` |

---

## 4. Pendiente de definición del dueño (no son defectos del front)

Estos puntos están explícitamente marcados en la UI o dependen de una decisión de negocio/proveedor. No se cuentan como mocks ni como defectos de integración.

| Tema | Dónde | Estado actual | Qué falta definir |
|---|---|---|---|
| **PDF de liquidación** | `app/(dashboard)/employee/page.tsx:52-54` y `features/payroll/components/PayrollDashboard.tsx:36` | Botón de descarga sin handler; no existe `GET /payroll/payslip/{id}/pdf` | Formato, numeración y endpoint de generación de PDF |
| **Tipo de cambio** | `features/treasury/components/MultiCurrencyPanel.tsx:17-25` y `features/treasury/services/treasury.service.ts:38-46` | Panel muestra “pendiente de definición”; el método muerto conserva fallback fijo pero no se usa | Proveedor/fuente de USD, EUR y UF; contrato del endpoint |
| **Cartolas bancarias** | `features/treasury/components/BankStatementUpload.tsx:40-45` | La UI avisa que la importación CSV/Excel está pendiente; se usa `POST /banking/sync` real | Formato de archivo, parsing y endpoint de carga |
| **Export Excel** | `app/(dashboard)/reports/page.tsx:102-106` y `features/inventory/components/InventoryDashboard.tsx:42` | Solo ventas exporta Excel; compras/balance e inventario quedan deshabilitados o con endpoint inexistente | Alcance de exportables y endpoint de inventario |
| **Terminales POS** | `app/(dashboard)/pos/page.tsx:17` | `TERMINAL_ID` sale de env con default `CAJA-01` | Registro de terminales, asignación por caja y contrato backend |

**Otros pendientes de backend (no bloquean, no son dueño):**

- **P&L por proyecto** — `app/(dashboard)/projects/page.tsx:48`; el backend no expone endpoint.
- **Telemetría de flota** — `app/(dashboard)/fleet/page.tsx:81`; no hay endpoint de posiciones.
- **Sostenibilidad por documento** — `app/(dashboard)/invoices/[id]/page.tsx:273`; solo existe el score global.
- **Resumen semanal manual** — `features/settings/components/PersonalSettings.tsx:66-77`; botón deshabilitado, sin endpoint.

---

## 5. Endpoints que el front llama y no existen (v2)

| Endpoint llamado por el front | Archivo:línea | ¿Existe en backend? | Reemplazo real | Prioridad |
|---|---|---|---|---|
| `DELETE /sso/users/{id}` | `features/settings/services/settings.service.ts:93` | No | Sin equivalente; `PUT /sso/users/{id}/role` solo cambia rol | P1 |
| `PUT /notifications/preferences` | `features/settings/services/settings.service.ts:103` | No | Solo `GET /notifications/preferences` | P2 |
| `GET /inventory/export/excel` | `features/inventory/components/InventoryDashboard.tsx:42` | No | No hay export de inventario | P2 |
| `GET /payroll/payslip/{id}/pdf` | `features/payroll/components/PayrollDashboard.tsx:36` | No | Sin equivalente; pendiente de dueño | P2 |
| `GET /treasury/exchange-rates` | `features/treasury/services/treasury.service.ts:44` | No | Método muerto; panel muestra pendiente de dueño | P2 |
| `/sii/dte/send`, `/bank/upload`, `/ai/chat`, `/accounting/entries/correct` | `lib/constants.ts:20,29,32,37` | No | Constantes muertas (no importadas) | P2 |

### Endpoints que existen y el front no usa (o usa mal)

| Endpoint real | Dónde debería usarse | Hoy |
|---|---|---|
| `GET /audit/alerts` | campana de notificaciones | `useAuditAlerts` devuelve `[]` |
| `POST /invoices/import` | `/invoices` (CSV) | comentado; éxito falso |
| `GET /company/profile` | `SessionGuard` | usado como chequeo de sesión; puede migrarse a `/auth/me` cuando E lo integre |
| `GET /fees` | `/fees` | usado correctamente |
| `POST /fees/import` | `/fees` | usado correctamente (empresa desde JWT) |

---

## 6. Resumen por módulo (v2)

Se cuentan los 43 `page.tsx`; `app/page.tsx` es redirect y no entra en el denominador (42 páginas con datos).

| Módulo | Páginas | 100% dinámicas | Con datos fijos | Sin backend | % 100% dinámicas |
|---|---:|---:|---:|---:|---:|
| Autenticación | 3 | 3 | 0 | 0 | 100,0% |
| Dashboard | 1 | 1 | 0 | 0 | 100,0% |
| Ventas / CRM | 5 | 4 | 1 | 0 | 80,0% |
| Contabilidad | 8 | 8 | 0 | 0 | 100,0% |
| Tributario / SII | 5 | 5 | 0 | 0 | 100,0% |
| Tesorería / Bancos | 3 | 3 | 0 | 0 | 100,0% |
| Personas | 2 | 2 | 0 | 0 | 100,0% |
| Operaciones | 6 | 5 | 1 | 0 | 83,3% |
| Compras | 2 | 2 | 0 | 0 | 100,0% |
| Documentos / Reportes | 2 | 2 | 0 | 0 | 100,0% |
| Energía / Sostenibilidad | 2 | 2 | 0 | 0 | 100,0% |
| Configuración | 2 | 2 | 0 | 0 | 100,0% |
| Portal B2B | 1 | 1 | 0 | 0 | 100,0% |
| **Total** | **42** | **40** | **2** | **0** | **95,2%** |

### Detalle de la clasificación

- **100% dinámicas (40):** todas las rutas con datos salvo las dos siguientes.
- **Con datos fijos (2):** `/invoices/[id]` (razón social del receptor forzada a vacío y sostenibilidad por documento `null`) y `/pos` (terminal por env/default `CAJA-01`, pendiente de dueño).
- **Sin backend (0).**

---

## 7. Notas de alcance

- No se modificó ningún archivo de `SII-ERP-AI-FRONT/src` ni de `SII-ERP-AI-BACK`.
- El contraste se hizo contra los controllers y DTOs actuales de `origin/integracion` (`a9c8732`).
- Los `page.tsx` de `(auth)` y `(dashboard)` se consideran páginas; los `layout.tsx` no.
- Los catálogos legales fijos (tipos de DTE, IVA 19%) no se listan como “datos fijos”.
- Los pendientes de la sección 4 no se cuentan como defectos ni como mocks: son decisiones de negocio/proveedor o capacidades de backend todavía no expuestas.
- La campana de notificaciones sigue sin consumir `GET /audit/alerts`; el backend ya devuelve alertas reales de duplicados/montos sospechosos.
- `SessionGuard` usa `GET /company/profile` como chequeo de sesión. Cuando se integre `GET /auth/me` (PR de auth), puede migrarse a ese contrato.
