# Auditoría funcional del front (v3): ¿lo que muestra es correcto?

- **Agente:** I. Rama `docs/auditoria-front-3`, desde `origin/integracion` en el commit `355ea03`.
- **Fecha:** 2026-11-28.
- **Tipo:** solo lectura. No se modificó código. Este archivo y las capturas de `coordinacion/auditoria-front-3/` son lo único que se agrega.
- **Diferencia con las auditorías v1 y v2:** aquellas medían si la pantalla está **conectada** al backend. Esta mide si lo que muestra es **correcto** contra un cálculo hecho a mano, si cada botón hace lo que dice y si una empresa ve datos de otra.

## Pila usada

| Pieza | Valor |
|---|---|
| Backend | `bootJar` de `integracion`, `java --enable-preview`, perfil `dev`, puerto **8101**, `app.cors.allowed-origins=http://localhost:3011` |
| Base | PostgreSQL 16 local (no había 15 ni Docker), puerto 55461, base **vacía** `siierp_audit_i` creada por Flyway |
| Front | `next dev -p 3011` con `BACKEND_INTERNAL_URL=http://localhost:8101` |
| Navegador | Chrome for Testing 158 controlado con Playwright (script propio, fuera del repo) |
| Fuera de la pila | Redis, MinIO, SMTP, certificado SII real, CAF real y `GEMINI_API_KEY` válida. Lo que depende de ellos se marca ❔ |

> **Hallazgo de arranque (backend, P6/infra):** sobre una base vacía el backend **no arranca**. `V2__datos_maestros.sql:53-63` inserta `roles.permissions` como objeto JSON (`{"users":["read"]}`) y `RoleRepositoryAdapter.java:99` espera un arreglo de `Permission`: `IllegalStateException: Permisos del rol con formato inválido` al construir `RoleService`. Para seguir, en **mi** base puse `update roles set permissions='[]'`. Además `V2` siembra datos demo (organizaciones, usuarios `admin@demo.cl`, 3 vehículos), así que la base "vacía" no lo es.

---

## ⚠️ CRÍTICO: fugas entre empresas y exposición de datos

Probado con la sesión del admin de B contra datos de A (y al revés). En las pantallas, **ninguna lista mostró datos de la otra empresa** (13 páginas recorridas como B; `/invoices/{id de B}` abierto como A responde 400 sin datos). Las fugas están en la API y en tablas sin empresa:

| # | Fuga | Evidencia (request → response) | Archivo:línea | Riesgo P0 que lo cubre |
|---|---|---|---|---|
| C1 | **B escribe facturas en A** por la importación CSV. El modal del front envía el `companyId` que toma del store, y el backend acepta cualquiera | Sesión B: `POST /invoices/import` con `companyId=<A>`, `bookType=VENTA`, folio 9001 → `{"successCount":1}`. Luego `GET /invoices` como A contiene "INYECTADA POR B" (borrada después de mi base) | `InvoiceImportController.java:59-60`; front `CsvImportModal.tsx:58` | **R1** |
| C2 | **B abre períodos de nómina en A** con `X-Company-Id` | Sesión B: `POST /payroll/periods` + `X-Company-Id: <A>`, `{"year":2026,"month":10}` → 200 `{"id":2,"period":"2026-10"}`; en la base `pay_periods.company_id = A` | `PayrollController.java:33-36` | **R2** |
| C3 | **B lee órdenes de compra de A** por UUID | Sesión B: `GET /procurement/orders/ef80d373-…` → 200 con `companyId` de A, proveedor y líneas | `PurchaseOrderController.java:37-40` | **R5** |
| C4 | **La flota es global:** la tabla no tiene empresa y todas las empresas ven los mismos 3 vehículos sembrados por `V2` | A y B: `GET /fleet/metrics` → `{"activeVehicles":3,"totalVehicles":3,…}`. `fleet.vehicles` no tiene `company_id` | `fleet.vehicles` (esquema), `FleetController` | **No está en R1–R5**: proponer a P6 |
| C5 | **El "score verde" mezcla empresas:** suma la huella de carbono de todas | `sumCarbonFootprintBetween(from, to)` sin empresa; A y B reciben el mismo `greenScore 85` | `GreenCreditScoringService.java:29` | **No está en R1–R5**: P6 |
| C6 | **Se exponen hashes de contraseña:** `GET /company/users` devuelve la entidad `User` con `passwordHash` (bcrypt) de todos los usuarios de la empresa | Respuesta real: `{"email":"admin.a@audit.test","passwordHash":"$2a$10$bs0E…"…}` | `CompanyController.java:72-79`; `User.java:49` | No es una fuga entre empresas, pero sí crítica: P6 |
| C7 | **Tablas de negocio sin columna de empresa** (análisis estático de la base): `public.charging_sessions`, `energy_alerts`, `energy_measurements`, `maintenance_records`, `fs_green_suppliers`, `sustainability.sustainability_records`, `fleet.vehicles` | `information_schema` de la base creada por Flyway | migraciones `V1`/`V2` | No está en R1–R5: P6 |

R3 (cierre cruzado con `X-Company-Id`) también responde 200 desde B, pero hoy la auditoría de cierre devuelve texto fijo (ver `/accounting/close`), así que no se puede distinguir la fuga del simulacro.

---

## Resumen

| Estado | Páginas | % |
|---|---|---|
| ✅ conectada y correcta | 14 | 32,6% |
| 🟡 conectada pero con resultados incorrectos o acciones que fallan | 17 | 39,5% |
| ❌ datos fijos, éxito falso o inutilizable | 4 | 9,3% |
| ❔ no verificable en esta pila | 8 | 18,6% |
| **Total** | **43** | |

**Comparación con la v2:** la v2 midió 95,2% de páginas **conectadas** (40 de 42). Esta v3 confirma que la conexión es real (solo 2 respuestas de backend con datos fijos), pero solo **32,6% muestra resultados correctos**. La diferencia está en el backend: las facturas no generan asientos, así que el libro diario, el balance, el estado de resultados y el F29 salen mal aunque la pantalla esté bien conectada.

**El ✅ vale solo para el rol ADMIN.** Con cualquier otro rol (contador, tesorero, RR.HH., ventas, operaciones, auditor, usuario) el front **cierra la sesión al entrar al panel** (ver F1). Para esos 7 roles las 40 páginas autenticadas son ❌ hoy.

---

## Datos de prueba y resultados esperados (calculados a mano antes de mirar la pantalla)

### Empresas y usuarios

| Empresa | RUT | Usuarios |
|---|---|---|
| A: Comercial Andes SpA | 76.111.111-6 | `admin.a@audit.test` (ADMIN) y uno por rol: `accountant.a`, `treasurer.a`, `hr.a`, `sales.a`, `operations.a`, `auditor.a`, `user.a` (`@audit.test`) |
| B: Servicios Bio SpA | 77.222.222-K | `admin.b@audit.test` (ADMIN). Datos marcados "SECRETO DE B" para detectar fugas |

Las contraseñas de prueba no se anotan aquí: son locales y se descartan con la base.

### Facturas de A (noviembre 2026), cargadas con la importación RCV (`POST /invoices/import`)

No hay CAF de prueba, así que la emisión desde `/invoices/new` no puede asignar folio. Las facturas se cargaron como RCV, el flujo real de una pyme que trae su registro del SII.

| Tipo | Folio | Fecha | Contraparte | Neto | IVA | Total |
|---|---|---|---|---|---|---|
| Venta 33 | 101 | 03-11 | Cliente Uno Ltda 78.333.333-3 | 1.000.000 | 190.000 | 1.190.000 |
| Venta 33 | 102 | 07-11 | Cliente Uno Ltda | 2.500.000 | 475.000 | 2.975.000 |
| Venta 33 | 103 | 12-11 | Cliente Dos SpA 78.555.555-4 | 350.000 | 66.500 | 416.500 |
| Venta 33 | 104 | 18-11 | Cliente Dos SpA | 4.200.000 | 798.000 | 4.998.000 |
| Venta 33 | 105 | 24-11 | Cliente Uno Ltda | 780.000 | 148.200 | 928.200 |
| **Ventas** | | | | **8.830.000** | **1.677.700** | **10.507.700** |
| Compra 33 | 5001 | 05-11 | Proveedor Sur SA 79.444.444-7 | 1.200.000 | 228.000 | 1.428.000 |
| Compra 33 | 5002 | 10-11 | Proveedor Sur SA | 600.000 | 114.000 | 714.000 |
| Compra 33 | 5003 | 20-11 | Proveedor Sur SA | 2.000.000 | 380.000 | 2.380.000 |
| **Compras** | | | | **3.800.000** | **722.000** | **4.522.000** |

B: 1 venta, folio 101 (el mismo folio que A, a propósito), neto 9.999.000, total 11.898.810.

### Otros datos de A

- **Caja** (`POST /treasury/cash-entries`): 04-11 aporte de caja chica +500.000 · 11-11 útiles −45.990 · 19-11 movilización −12.500. Durante la prueba de la pantalla se agregó 26-11 venta mesón +25.000.
- **Productos** (`POST /inventory/products` + entradas): SKU-001 resma, 100 u a $3.000, luego salida de 25 · SKU-002 tóner, 12 u a $30.000 · SKU-003 silla, 5 u a $120.000.
- **Empleados** (no hay endpoint de alta: insertados por SQL en `pay_employees`): Pedro Rojas, base $600.000 · Ana Soto, base $1.200.000. Ambos AFP Capital (11,44%), Fonasa 7%, contrato indefinido (AFC 0,6%).

### Resultados esperados

| Concepto | Cálculo | Esperado |
|---|---|---|
| IVA débito (F29 cód. 538) | Σ IVA ventas | **1.677.700** |
| IVA crédito (cód. 520) | Σ IVA compras | **722.000** |
| IVA a pagar (cód. 89) | 1.677.700 − 722.000 | **955.700** (más PPM según perfil tributario; A no tiene perfil) |
| Ventas netas (cód. 503) | Σ neto ventas | **8.830.000** |
| Libro diario: asientos de facturas | 5 ventas (D Clientes / H Ventas + H IVA DF) + 3 compras (D Compras + D IVA CF / H Proveedores) | 8 asientos, debe = haber = **15.029.700** |
| Libro diario: asientos de caja | 3 asientos con **su fecha** (04, 11 y 19-11) | debe = haber = **558.490** |
| Total libro diario | | **15.588.190** (16.613.190 con la venta mesón) |
| Caja | 500.000 − 45.990 − 12.500 | **441.510** |
| Clientes por cobrar | Cliente Uno 5.093.200 · Cliente Dos 5.414.500 | **10.507.700** |
| Proveedores por pagar | Proveedor Sur | **4.522.000** |
| Stock y valorización | SKU-001 75 × 3.000 = 225.000 · SKU-002 12 × 30.000 = 360.000 · SKU-003 5 × 120.000 = 600.000 | **1.185.000** |
| Nómina, Pedro | gratificación 25% = 150.000 (bajo el tope 4,75 IMM/12 = 213.354 con IMM 539.000); imponible 750.000; AFP 85.800; salud 52.500; AFC 4.500; tributable 607.200 < 13,5 UTM → impuesto 0 | **líquido 607.200** |
| Nómina, Ana | gratificación topada 213.354; imponible 1.413.354; AFP 161.688; salud 98.935; AFC 8.480; tributable 1.144.251 → tramo 4%: 1.144.251 × 0,04 − 0,54 × 69.751 = 8.104 | **líquido 1.136.147** |
| Nómina, total | | **descuentos 420.007 · líquido 1.743.347** |

UTM 69.751 y UF 39.731,79 son los valores 2026 que trae `tax_parameters` en el backend.

---

## Tabla por página

Rondas: P1 documentos electrónicos · P2 cadena contable y F29 · P3 remuneraciones · P4 operación · P5 IA · P6 multiempresa · "front".

| Ruta | Estado | Qué se probó | Esperado vs obtenido | Causa | Archivo:línea | Ronda |
|---|---|---|---|---|---|---|
| `/` | ✅ | Carga sin sesión | Redirige a `/login` ✓ | n/a | `app/page.tsx` | n/a |
| `/login` | ✅ | Login de los 8 usuarios | Los 8 entran; JWT en cookie y **también en el body** (R10) | backend | `AuthController.java:191-197,232-240` | P6 |
| `/register` | ❔ | No se recorrió en navegador; el mismo endpoint se usó por API para crear A y B | `POST /auth/register` → 200 | n/a | n/a | n/a |
| `/onboarding` | ❔ | Carga datos del perfil; el paso de certificado necesita un .p12 real | n/a | n/a | n/a | P1 |
| `/dashboard` | 🟡 | Tarjetas vs cálculo | Ventas del mes 10.507.700 ✓; IVA débito 1.677.700 ✓; ticket promedio 2.101.540 ✓. **"Facturas pendientes 8 por $15.029.700"** suma compras con ventas (esperado 5 por $10.507.700). **Eco Score 85** sale de un score global (C5) y contradice `/sustainability` (0). "Saldo proyectado $0". `/integration/indicators/today` → 500 sin Redis | backend | `DashboardMetricsDto.java:77`; `GreenCreditScoringService.java:29-41`; indicadores sin degradar ante Redis caído | P2 / P6 |
| `/invoices` | 🟡 | Lista, importar CSV | Lista los 8 documentos ✓ pero **mezcla compras con ventas sin distinguirlas**, y las compras salen con receptor y RUT vacíos. El modal de importación funciona, pero envía `companyId` (C1) | backend (receptor `EMPRESA_PROPIA`) + front (sin filtro de libro) | `InvoiceImportController.java:120-122`; `CsvImportModal.tsx:58` | P1 / P6 |
| `/invoices/[id]` | 🟡 | Folio 101 de A; folio de B abierto como A | Montos 1.000.000 / 190.000 / 1.190.000 ✓. **Emisor = "Cliente Uno Ltda"** (es el cliente) y RUT emisor vacío. "Enviar al SII" → 500 genérico. "Descargar PDF" no produjo descarga visible. Pestaña Contabilidad: "el asiento se genera cuando el SII acepta" (no hay asiento). Factura de B → 400, mensaje "revisa tu conexión" (debería decir 404 / no existe) | front (mapeo) + backend (`businessName` es la contraparte en importación, el emisor en creación; `issuerRut="EMPRESA_PROPIA"`) | `invoice.service.ts:189-194`; `page.tsx:182-190`; `InvoiceImportController.java:122,152,166` | P1 |
| `/invoices/new` | ❔ | Asistente de 4 pasos | Bloquea correctamente: "No hay folios para este tipo de documento" (no hay CAF). Sin CAF real no se puede emitir de punta a punta | n/a | n/a | P1 |
| `/accounting/ledger` | 🟡 | Asientos vs cálculo | **3 asientos (solo caja), 558.490**; esperado 11 asientos, 15.588.190. Las facturas no generan asiento: solo `InvoiceSiiAcceptedEvent` contabiliza, y un RCV importado nunca se "acepta". **Los asientos de caja salen con fecha 28-11** (hoy) en vez de 04, 11 y 19-11. Exportar CSV ✓ descarga `libro-diario.csv` | backend | `InvoiceAccountingListener.java:57`; `CashManagementService.java:98-109` (no pasa `transaction.getDate()`) | P2 |
| `/accounting/f29` | 🟡 | F29 de 2026-11 | Esperado débito 1.677.700, crédito 722.000, a pagar 955.700. **Obtenido: débito 0, crédito 0, a pagar 0; "503 Ventas Netas $500.000"** (es el aporte de caja chica). Tras importar una boleta de honorarios: cód. 151 = 15.250 ✓ | backend (F29 calculado desde asientos que no existen; ingreso de caja clasificado como venta) | `F29CalculatorService.java:72-171` | P2 |
| `/accounting/balance` | 🟡 | Balance al 30-11 | Activos 441.510 (solo caja) ✓ contra caja; faltan clientes 10.507.700, IVA CF 722.000, proveedores 4.522.000, IVA DF 1.677.700 | backend (mismo origen que el libro diario) | ídem | P2 |
| `/accounting/income` | 🟡 | Estado de resultados noviembre | **Ingresos operacionales 500.000** (aporte de caja) y gastos 58.490; esperado ventas 8.830.000 y compras 3.800.000 + 58.490. Margen 88,3% falso | backend | `CashManagementService.java:86-88` (INGRESO → "Otros Ingresos" mostrado como operacional) + ausencia de asientos de facturas | P2 |
| `/accounting/partners` | 🟡 | Cuentas corrientes | Clientes: Cliente Uno 5.093.200 ✓, Cliente Dos 5.414.500 ✓. **Falta Proveedor Sur 4.522.000.** "Deuda vencida" = total porque `dueDate` = fecha de emisión | backend | `PartnerLedgerListener.java:59`; `Invoice.create` (vencimiento = emisión) | P2 |
| `/accounting/audit` | ❔ | Espejo RCV | 409 "SII Token not found": necesita autenticación SII real | n/a | n/a | P1 |
| `/accounting/close` | ❌ | "Auditar período" | **Observaciones fijas del backend**: "Missing bank reconciliation for transaction: $50,000 on 15th" y "Uncentralized purchase invoice #99432" (no existen). Además muestra el error crudo de la IA ("API key not valid") | backend (mock) | `PeriodCloseAuditorService.java:29-36` | P2 / P5 |
| `/accounting/annual-close` | 🟡 | "Calcular F22" | 422 "No existe perfil tributario… Créalo en Configuración > Empresa > Perfil tributario": **esa pantalla no existe** (Configuración solo tiene Empresa, Usuarios, Personal). El texto de ayuda del front dice "el backend responde 500" y respondió 422 | front (texto y falta de pantalla) + backend (sin perfil por defecto) | `annual-close/page.tsx` (texto de error) | front / P2 |
| `/accounting/assets` | ✅ | Lista y "Ejecutar depreciación mensual" | Sin activos: lista vacía ✓; depreciación → 200 y aviso correcto. No hay botón de alta de activos | n/a | n/a | n/a |
| `/banking` | ✅ | Conexiones y movimientos | Vacíos ✓; "Conectar Banco" lleva a conciliación ✓ (en la v2 no hacía nada) | n/a | n/a | n/a |
| `/banking/reconciliation` | ❔ | "Sincronizar Bancos" | `POST /banking/sync` → 200 vacío y **sin aviso en pantalla**. Sin Fintoc no hay datos que contrastar | front (sin feedback) | `banking/reconciliation/page.tsx:52-58` | P4 |
| `/compliance` | 🟡 | Salud tributaria | `GET /compliance/dashboard` → 500. El front lo explica bien | backend | `ComplianceController` | P1 |
| `/crm/leads` | ✅ | "Nuevo lead" | `POST /crm/leads` → 200 con los datos enviados ✓ (en la v2 era un botón muerto) | n/a | n/a | n/a |
| `/crm/quotes` | ✅ | "Nueva cotización" 2 × 150.000 | `POST /crm/quotes` → 200 en borrador ✓ | n/a | n/a | n/a |
| `/documents` | ❔ | "Subir documento" | 400 "Failed to connect to localhost:9000": MinIO no está en la pila. El error se muestra bien | n/a | n/a | P4 |
| `/employee` | ❌ | Como ADMIN y como USER | ADMIN: `GET /payroll/employee/payslips` → 404 y la pantalla dice "No tienes liquidaciones" (oculta el error). USER, su rol objetivo: **expulsado** por F1. No hay forma de vincular un empleado a un usuario | front (oculta 404) + backend (sin alta ni vínculo de empleado) | `employee/page.tsx`; `PayrollServiceImpl.java:166-170` | P3 / front |
| `/energy` | ✅ | Métricas, estaciones, alertas | Todo vacío y coherente para A | n/a | n/a | n/a |
| `/fees` | 🟡 | "Importar Boletas" | 1 boleta: bruto 100.000, retención 15.250 (15,25%) ✓, líquido 84.750 ✓. Con un encabezado de folio no reconocido la fila se guarda con **folio 0** en vez de rechazarse | backend | `FeeReceiptParser.java:59` | P2 |
| `/fleet` | ❌ | Métricas | **3 vehículos que no son de la empresa** (los mismos para A y B: C4) | backend | `fleet.vehicles` sin `company_id` | P6 |
| `/inventory` | ✅ | Stock, valorización, alta, salida | Stock 75 / 12 / 5 ✓; valorización 1.185.000 ✓; "Nuevo producto" → 201 ✓; "Salida" 2 u de tóner → 201, stock 10 ✓ | n/a | n/a | n/a |
| `/mrp` | ✅ | Órdenes y BOM | Vacíos y coherentes | n/a | n/a | n/a |
| `/payroll` | 🟡 | Abrir período, calcular, Previred | Abrir ✓ (UF/UTM correctas). **Calcular: Pedro líquido 381.577 (esperado 607.200), Ana 1.037.291 (esperado 1.136.147); descuentos 831.132 (esperado 420.007).** Causas: salud = `health_percentage × UF` (7 × UF = 278.123), gratificación sin tope (Ana 300.000 vs 213.354). Previred descarga el archivo con los mismos errores | backend | `PayrollServiceImpl.java:184-192` (R7) | P3 |
| `/pos` | 🟡 | Venta de SKU-001 en efectivo (2 veces) | `POST /pos/transactions` → 200 "Venta registrada" ✓, pero **el stock no baja** (sigue 75), no se emite boleta ni asiento; queda `PENDING_SYNC`. Terminal "CAJA-01" fijo | backend | `PosController` / listener de inventario ausente | P4 / P1 |
| `/procurement` | ✅ | "Nueva orden" 50 × 2.800 | `POST /procurement/orders` → 200 en borrador ✓ | n/a | n/a | n/a |
| `/projects` | ✅ | Lista | Vacía; avisa que el P&L no existe | n/a | n/a | n/a |
| `/reports` | ✅ | Libro de ventas | 5 ventas con total correcto ✓. No se probó cambiar a compras ni balance | n/a | n/a | n/a |
| `/settings` | 🟡 | Pestaña Usuarios | Tesorero, RR.HH., Ventas, Operaciones, Auditor y Usuario aparecen todos como **"Visualizador"**. La respuesta trae `passwordHash` (C6) | front (mapeo de roles) + backend | `UserManagement.tsx:31-38`; `CompanyController.java:72-79` | front / P6 |
| `/settings/company` | 🟡 | Editar y guardar | `PUT /company/profile` ✓ y el backend queda `profileComplete:true`, pero el aviso **"Perfil Incompleto" sigue visible** (no se refresca). El formulario no tiene giro, acteco ni comuna, que el SII exige | front | `settings/company` (sin invalidar la query) | front |
| `/sii` | 🟡 | "Forzar Sincronización" | `POST /sii/sync` → 200 `{"success":false,"message":"Failed to load PKCS12…"}` y la pantalla **no muestra nada** | front | `SyncStatusDashboard.tsx:57-66` (ignora `success:false`) | front |
| `/sii/audit` | ❔ | Espejo RCV | 409 sin token SII | n/a | n/a | P1 |
| `/sii/caf` | ❔ | Carga de CAF | Necesita un XML CAF real firmado por el SII | n/a | n/a | P1 |
| `/sustainability` | 🟡 | Score, ESG, CORFO | Eco-Score **0** mientras el backend envía `environmentalHealth 50` y el panel muestra 85. "Silver, 5% de rebaja fiscal" sin ningún dato ambiental | front (mapeo del score) + backend (heurística fija) | `sustainability.service`; `corfo-simulator` | P5 / front |
| `/treasury` | ❌ | Movimientos, registrar, proyección | Movimientos con fechas correctas ✓; "Registrar Movimiento" ✓. **"Liquidez actual $15.000.000" y "Proyección $9.500.000" fijas** (también en B); la alerta "salida F29 el 2026-12-20" es inventada. El front no indica que es una simulación | backend | `TreasuryAiPredictorServiceImpl.java:20` ("Base simulada") | P4 / P5 |
| `/wms` | ✅ | Picking y ubicaciones | Vacíos; "Escanear QR" deshabilitado con motivo (en la v2 era un botón muerto) | n/a | n/a | n/a |
| `/portal` | ✅ | Pedidos B2B | Lista vacía; el modal "Nuevo Pedido" abre (no se envió) | n/a | n/a | n/a |

Global (todas las páginas): el botón **"Sincronizar"** del encabezado muestra **"Sincronización SII solicitada"** aunque el backend respondió `success:false` (éxito falso, captura 11).

**IA:** el chat del asistente (`AiChat.tsx`), la recomendación de cierre, el pronóstico de tesorería y el análisis de documentos son ❔: no hay `GEMINI_API_KEY` válida. Donde la IA falla, `/accounting/close` muestra el error crudo del proveedor (R12).

---

## Errores de FRONT (para corregir de inmediato)

| # | Prioridad | Error | Archivo:línea |
|---|---|---|---|
| F1 | **P0** | **Toda sesión que no sea ADMIN se cierra al entrar.** `SessionGuard` valida la sesión con `GET /company/profile`, que es solo para ADMIN (`@PreAuthorize("hasRole('ADMIN')")`); cualquier error, incluido 403, llama a `expireSession()` y redirige a `/login?expired=1`. Probado con los 7 roles no admin. Arreglo: validar con `GET /auth/me` (ya existe) y expirar solo ante 401 | `components/layout/SessionGuard.tsx:60-70` |
| F2 | P1 | Éxito falso al sincronizar SII desde el encabezado: no mira `success` | `components/layout/Header.tsx:27-31` |
| F3 | P1 | "Forzar Sincronización" ignora `success:false` y no avisa nada | `features/sii/components/SyncStatusDashboard.tsx:57-66` |
| F4 | P1 | Detalle de factura: el emisor muestra la razón social de la contraparte (`issuerName = dto.businessName`); para documentos propios debe usar los datos de la empresa | `features/invoicing/services/invoice.service.ts:189-194` |
| F5 | P1 | La lista de facturas mezcla compras y ventas sin columna ni filtro de libro | `app/(dashboard)/invoices/page.tsx` |
| F6 | P1 | El modal de importación CSV envía `companyId` desde el store (habilita C1 si el backend no lo ignora) | `features/invoicing/components/CsvImportModal.tsx:58` |
| F7 | P2 | Roles TREASURER, HR, SALES, OPERATIONS, AUDITOR y USER se muestran como "Visualizador" | `features/settings/components/UserManagement.tsx:31-38` |
| F8 | P2 | `/employee` convierte el 404 en "No tienes liquidaciones" | `app/(dashboard)/employee/page.tsx` |
| F9 | P2 | El aviso "Perfil Incompleto" no se refresca tras guardar; el formulario no pide giro, acteco ni comuna | `features/settings` (perfil de empresa) |
| F10 | P2 | El F22 remite a "Configuración > Empresa > Perfil tributario", que no existe; el texto dice 500 cuando el backend responde 422 | `app/(dashboard)/accounting/annual-close/page.tsx` |
| F11 | P2 | Una factura de otra empresa responde 400 y la pantalla dice "revisa tu conexión" | `app/(dashboard)/invoices/[id]/page.tsx` |
| F12 | P2 | Eco-Score 0 cuando el backend envía `environmentalHealth 50` | `features/sustainability` |
| F13 | P2 | "Sincronizar Bancos" no da ningún aviso | `app/(dashboard)/banking/reconciliation/page.tsx:52-58` |
| F14 | P2 | La proyección de tesorería no indica que es simulada | `features/treasury` |
| F15 | P2 | Rate limit de `/auth/me`: al navegar rápido aparecen 429 (`/crm/leads`, `/procurement`, `/projects`) | `SessionGuard.tsx:42` / limitador del backend |

## Errores de BACKEND (para las rondas P1–P6)

| # | Ronda | Error | Archivo:línea |
|---|---|---|---|
| B1 | P6 | Fugas C1–C7 (arriba) | ver tabla crítica |
| B2 | P6 / infra | El backend no arranca sobre una base vacía (formato de `roles.permissions` en `V2`); `V2` siembra datos demo | `V2__datos_maestros.sql:53-63`; `RoleRepositoryAdapter.java:99` |
| B3 | P2 | **Las facturas importadas (RCV) no generan asientos**: solo se contabiliza al recibir `InvoiceSiiAcceptedEvent`. Rompe el libro diario, el balance, el estado de resultados y el F29 | `InvoiceAccountingListener.java:57` |
| B4 | P2 | El F29 se calcula desde asientos: débito y crédito 0; un ingreso de caja aparece como "Ventas Netas" | `F29CalculatorService.java:72-171` |
| B5 | P2 | Los asientos de caja usan la fecha de hoy y no la del movimiento | `CashManagementService.java:98-109` |
| B6 | P2 | La auditoría de cierre devuelve observaciones fijas ("$50,000 on 15th", "#99432") | `PeriodCloseAuditorService.java:29-36` |
| B7 | P2 | Cuentas corrientes sin proveedores; vencimiento = emisión, así que todo aparece vencido | `PartnerLedgerListener.java:59`; `Invoice.create` |
| B8 | P2 | El panel suma compras en "Facturas pendientes" | `DashboardMetricsDto.java:77` |
| B9 | P2 | Una boleta de honorarios sin folio reconocible se guarda con folio 0 | `FeeReceiptParser.java:59` |
| B10 | P2 | El F22 exige un perfil tributario que no se crea al registrar la empresa ni tiene pantalla | `AnnualCloseController` / registro |
| B11 | P1 | La importación guarda `"EMPRESA_PROPIA"` como RUT emisor o receptor, y `businessName` cambia de significado según el origen | `InvoiceImportController.java:120-122,152,166` |
| B12 | P1 | `POST /sii/dte/{id}/send` → 500 genérico (sin CAF ni certificado debería ser 422 con motivo) | `SiiDteController` |
| B13 | P1 | `/compliance/dashboard` → 500 | `ComplianceController` |
| B14 | P3 | Nómina incorrecta: salud = `health_percentage × UF`, gratificación sin tope (R7) | `PayrollServiceImpl.java:184-192` |
| B15 | P3 | No hay endpoint de alta de empleados ni vínculo empleado-usuario | `PayrollController` |
| B16 | P4 | La venta POS no descuenta stock ni genera boleta o asiento | `PosController` / listeners |
| B17 | P4 / P5 | Liquidez de tesorería fija en $15.000.000 | `TreasuryAiPredictorServiceImpl.java:20` |
| B18 | P5 | Score verde y simulador CORFO con heurística fija y sin datos | `GreenCreditScoringService.java:36-41` |
| B19 | P5 | Error crudo del proveedor de IA devuelto al usuario (R12) | `PeriodCloseAuditorService.java` → `aiRecommendation` |
| B20 | P6 | `GET /company/users` expone `passwordHash` | `CompanyController.java:72-79` |
| B21 | P4 | `/integration/indicators/today` → 500 si Redis no está; debería degradar | `EconomicIndicatorsController` |
| B22 | P6 | JWT devuelto en el body del login (R10) | `AuthController.java:191-197,232-240` |

## Capturas

`coordinacion/auditoria-front-3/`:

| Archivo | Muestra |
|---|---|
| `01-dashboard.png` | Panel de A: ventas e IVA correctos, pendientes mezclados |
| `02-f29.png` | F29 con IVA 0 y "Ventas Netas $500.000" |
| `03-libro-diario.png` | Libro diario solo con caja, fechas 28-11 |
| `04-facturas.png` | Lista que mezcla compras y ventas |
| `05-factura-detalle.png` | Emisor = cliente |
| `06-nomina.png` | Líquidos calculados por el backend |
| `07-cierre-periodo.png` | Observaciones fijas y error crudo de la IA |
| `08-tesoreria.png` | Liquidez fija $15.000.000 |
| `09-flota-empresa-B.png` | B ve los mismos 3 vehículos |
| `10-usuarios-roles.png` | Roles mostrados como "Visualizador" |
| `11-sincronizar-exito-falso.png` | Aviso de éxito con `success:false` |
| `12-rol-ventas-expulsado.png` | Usuario de ventas devuelto al login |
| `13-cuentas-corrientes.png` | Clientes correctos, sin proveedores |

## Límites de esta auditoría

- PostgreSQL 16 en vez de 15 (no había Docker ni PG15). Flyway corrió completo.
- Sin Redis, MinIO, SMTP, certificado SII real, CAF real ni clave de IA: las páginas que dependen de ellos son ❔.
- Los empleados se insertaron por SQL porque no existe un endpoint de alta.
- En mi base se ajustó `roles.permissions` para que el backend arrancara (B2). No se tocó código.
- Las acciones destructivas o de una sola vía (cerrar período, cerrar año) no se ejecutaron.
