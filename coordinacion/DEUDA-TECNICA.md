# Deuda técnica — inventario y estado (agente G)

Rama `chore/deuda-tecnica` desde `origin/integracion` (`4b99ddb`). Inventario hecho antes de tocar código; la columna **estado** refleja el cierre de esta rama.

Archivos bloqueados (presentes en PRs abiertos #32, #33, #34, #35 y #36 al empezar): 47. No se modificó ninguno.

## Números antes → después

| Métrica | Antes | Después | Cómo se mide |
|---|---|---|---|
| Tests backend | 180 (0 fallos) | 188 (0 fallos) | `./gradlew clean compileJava compileTestJava test` |
| TODO/FIXME/Ideally/stubs en `SII-ERP-AI-BACK/src/main` | 44 | 38 | `grep -rnE "TODO\|FIXME\|Ideally\|\(Stub\)\|Simulat(e\|es)\b"` (sin el falso positivo "TODO el día") |
| Usos de `@MockBean`/`@SpyBean` | 16 | 6 (bloqueados por PR #36) | `grep -rnE "@(MockBean\|SpyBean)\b" src/test` |
| Warnings `-Xlint` (deprecation, removal, unchecked) | 106 | 45 | init script con `-Xlint:deprecation,removal,unchecked` |
| Warnings de lint del front | 26 (0 errores) | 9 (0 errores) | `npx eslint .` |
| Vulnerabilidades npm (crít./altas/total) | 3 / 26 / 45 | 2 / 10 / 21 | `npm audit` |
| CVEs altos/críticos backend corregibles con patch/minor | 4 confirmados (ver h) | 0 de esos 4 | versión resuelta en `./gradlew dependencies` |

## Tabla de deuda

| id | archivo:línea | tipo | descripción | riesgo | esfuerzo | estado |
|---|---|---|---|---|---|---|
| A01 | `accounting/application/listener/ChargingEventListener.java:63` | a) TODO/stub | Costo kWh fijo en código (ver f) | medio | M | requiere decisión |
| A02 | `accounting/application/listener/ChargingEventListener.java:67` | a) TODO/stub | CompanyId por defecto en asientos de carga: debe venir del evento/contexto | alto | M | requiere decisión |
| A03 | `accounting/application/listener/PartnerLedgerListener.java:74` | a) TODO/stub | Listener no actualiza cache/BD del libro de socios | medio | M | pendiente |
| A04 | `accounting/application/service/CashFlowProjectionService.java:29` | a) TODO/stub | TODO: NUEVO: Obtiene el flujo de caja diario REAL para un mes específico. | bajo | M | pendiente |
| A05 | `accounting/application/service/WeeklyReportService.java:53` | a) TODO/stub | Comentario "Stub" desactualizado (el PDF ya es real con OpenPDF) | bajo | S | pendiente |
| A06 | `accounting/domain/service/F29SenderService.java:17` | a) TODO/stub | Envío de F29 al SII simulado: respondía SUBMITTED con folio inventado | alto | S | corregida |
| A07 | `accounting/domain/service/F29SenderService.java:30` | a) TODO/stub | Envío de F29 al SII simulado: respondía SUBMITTED con folio inventado | alto | S | corregida |
| A08 | `accounting/domain/service/F29SenderService.java:34` | a) TODO/stub | Envío de F29 al SII simulado: respondía SUBMITTED con folio inventado | alto | S | corregida |
| A09 | `accounting/domain/service/F29SenderService.java:36` | a) TODO/stub | Envío de F29 al SII simulado: respondía SUBMITTED con folio inventado | alto | S | corregida |
| A10 | `accounting/domain/service/F29SenderService.java:41` | a) TODO/stub | Envío de F29 al SII simulado: respondía SUBMITTED con folio inventado | alto | S | corregida |
| A11 | `accounting/infrastructure/web/AuditController.java:97` | a) TODO/stub | @PreAuthorize AUDIT_READ comentado; hoy rige el del controlador | medio | S | requiere decisión |
| A12 | `accounting/infrastructure/web/ReportingController.java:81` | a) TODO/stub | Email del reporte semanal no sale del usuario autenticado | bajo | S | pendiente |
| A13 | `ai_assistant/application/tools/GridStatusTool.java:118` | a) TODO/stub | TODO: Implementación real | bajo | L | pendiente |
| A14 | `ai_assistant/application/tools/GridStatusTool.java:132` | a) TODO/stub | TODO: Implementación real | bajo | L | pendiente |
| A15 | `crm/application/service/CrmServiceImpl.java:64` | a) TODO/stub | Cerrar oportunidad no emite evento hacia facturación | medio | M | pendiente |
| A16 | `energy/application/listener/OcppStatusEventListener.java:31` | a) TODO/stub | TODO: Persistir en tabla ocpp_diagnostics_audit cuando esté el repositorio JPA. | bajo | L | pendiente |
| A17 | `energy/application/listener/OcppStatusEventListener.java:49` | a) TODO/stub | TODO: Persistir firmwareVersion en la entidad ChargingStation via repositorio JPA. | bajo | L | pendiente |
| A18 | `energy/application/listener/SmartLoadBalancingListener.java:93` | a) TODO/stub | TODO: Implementación real | bajo | L | pendiente |
| A19 | `energy/application/service/OcppCommandExtensionService.java:174` | a) TODO/stub | TODO: Integrate with baseCommandService.sendCommand() once available | bajo | L | pendiente |
| A20 | `energy/application/service/monitoring/AlertService.java:29` | a) TODO/stub | TODO: replace with repository) | bajo | L | pendiente |
| A21 | `energy/application/service/reporting/EnergyReportService.java:154` | a) TODO/stub | TODO: Implement actual hourly analysis | bajo | L | pendiente |
| A22 | `energy/application/service/reporting/EnergyReportService.java:166` | a) TODO/stub | TODO: Group by station and calculate totals | bajo | L | pendiente |
| A23 | `energy/application/service/reporting/PdfExportService.java:61` | a) TODO/stub | TODO: Replace with proper PDF library (iText, PDFBox, etc.) | bajo | L | pendiente |
| A24 | `energy/infrastructure/adapter/in/ocpp/mapping/OcppStationMappingService.java:44` | a) TODO/stub | TODO: implementar cuando exista el | bajo | L | pendiente |
| A25 | `energy/infrastructure/adapter/in/rest/ChargerMetadataController.java:231` | a) TODO/stub | TODO: Add activate/deactivate methods to ChargerMetadataService | bajo | L | pendiente |
| A26 | `energy/infrastructure/adapter/in/rest/EnergyReportingController.java:247` | a) TODO/stub | TODO: Implement scheduled report functionality | bajo | L | pendiente |
| A27 | `energy/infrastructure/adapter/out/OcppChargingStationControlAdapter.java:52` | a) TODO/stub | TODO: Implementación real con OCPP client | bajo | L | pendiente |
| A28 | `energy/infrastructure/adapter/out/OcppChargingStationControlAdapter.java:70` | a) TODO/stub | TODO: Implementación real | bajo | L | pendiente |
| A29 | `energy/infrastructure/adapter/out/OcppChargingStationControlAdapter.java:83` | a) TODO/stub | TODO: Implementación real | bajo | L | pendiente |
| A30 | `energy/infrastructure/adapter/out/OcppChargingStationControlAdapter.java:95` | a) TODO/stub | TODO: Consultar repositorio de sesiones activas | bajo | L | pendiente |
| A31 | `financial_shield/application/service/FinancialShieldService.java:85` | a) TODO/stub | Factores de riesgo vacíos (sin API Poder Judicial/Dicom) | bajo | L | pendiente |
| A32 | `financial_shield/domain/service/GreenCreditScoringService.java:43` | a) TODO/stub | TODO: En el futuro, cruzar con facturas de "Energía Renovable" para sumar más | bajo | M | pendiente |
| A33 | `fleet/application/listener/VehicleChargingListener.java:43` | a) TODO/stub | TODO: Necesitamos mapear userId → Vehicle | bajo | L | pendiente |
| A34 | `fleet/application/listener/VehicleChargingListener.java:73` | a) TODO/stub | TODO: Similar al método anterior, necesitamos mapear el evento al vehículo | bajo | L | pendiente |
| A35 | `fleet/application/listener/VehicleChargingListener.java:95` | a) TODO/stub | TODO: Implementar lógica real basada en capacidad de batería del vehículo. | bajo | L | pendiente |
| A36 | `integration_sii/infrastructure/scheduler/TokenRefreshScheduler.java:124` | a) TODO/stub | Certificado global, no por empresa | medio | M | pendiente |
| A37 | `invoicing/application/listener/DtesDownloadedListener.java:61` | a) TODO/stub | Tipo de libro (compra/venta) inferido; el evento no lo trae | medio | M | pendiente |
| A38 | `invoicing/domain/port/out/InvoiceRepository.java:140` | a) TODO/stub | Búsqueda en memoria en vez de query | bajo | S | pendiente |
| A39 | `pos/application/service/PosServiceImpl.java:40` | a) TODO/stub | Venta POS no emite boleta electrónica en el SII | alto | L | requiere decisión |
| A40 | `procurement/application/service/PurchaseOrderServiceImpl.java:56` | a) TODO/stub | Recepción de OC no emite evento a inventario/CxP | medio | M | pendiente |
| A41 | `sso/application/service/UserInvitationService.java:41` | a) TODO/stub | No valida usuario ya existente o ya invitado | medio | S | pendiente |
| A42 | `sso/application/service/UserInvitationService.java:52` | a) TODO/stub | Busca datos del invitador por id en vez de UserContext | bajo | S | pendiente |
| A43 | `sustainability/infrastructure/adapter/in/rest/SustainabilityMetricsController.java:78` | a) TODO/stub | Salud financiera sin calcular | bajo | M | pendiente |
| A44 | `user/infrastructure/persistence/adapter/RoleRepositoryAdapter.java:30` | a) TODO/stub | Permisos del rol se guardaban siempre como "[]" y no se leían | alto | S | corregida |
| B01 | `integration_sii/infrastructure/adapter/in/rest/SiiIntegrationController.java:71` | b) catch que oculta error | Sin contexto de empresa usaba un `CompanyId` aleatorio y publicaba el RCV para un tenant inexistente | alto | S | corregida |
| B02 | `invoicing/infrastructure/adapter/in/rest/InvoiceImportController.java:204` | b) catch que oculta error | Monto mal formado en el CSV del SII → `ZERO` silencioso; la factura se importaba con montos en cero | alto | S | corregida |
| B03 | `accounting/domain/service/AnomalyDetectionService.java:76` | b) catch vacío | Error de repositorio ignorado al promediar IVA débito; cae a un promedio fijo de $500.000 | medio | S | pendiente |
| B04 | `sso/infrastructure/security/TokenBlacklistService.java:59` | b) catch solo log | Si Redis cae, el logout no invalida el token (fail-open deliberado; Redis puede estar caído en local) | medio | M | requiere decisión |
| B05 | `sso/infrastructure/security/LoginRateLimiter.java:51-74` | b) catch solo log | Rate limiter de login fail-open sin Redis | medio | M | requiere decisión |
| B06 | `invoicing/application/listener/DtesDownloadedListener.java:47` | b) catch solo log | Error por folio al procesar RCV solo queda en log, no llega al usuario | medio | M | pendiente |
| B07 | `invoicing/application/listener/PosTransactionListener.java:51` | b) catch solo log | Falla al generar boleta de una venta POS solo queda en log | alto | M | pendiente |
| B08 | `accounting/application/listener/ChargingEventListener.java:123` | b) catch con DLQ | Error de contabilidad de carga va a DLQ (aceptable) | bajo | S | pendiente |
| B09 | `integration_sii/application/service/DteStatusService.java:86` | b) catch solo log | Consulta de estado DTE fallida queda en log; el estado no cambia | bajo | S | pendiente |
| B10 | 176 bloques `catch` en `src/main` | b) | 59 retornan un valor por defecto, 46 solo registran en log, 4 vacíos. Revisados los de dinero, impuestos y autenticación (B01–B09); el resto son energía/IA/notificaciones | — | L | pendiente |
| C01 | `invoicing/infrastructure/adapter/in/rest/InvoiceControllerTest.java` | c) API obsoleta | 10 × `@MockBean` | bajo | S | corregida (`@MockitoBean`) |
| C02 | `sso/SsoIntegrationTest.java:31-46` | c) API obsoleta | 6 × `@MockBean` | bajo | S | bloqueada por PR #36 |
| C03 | `SuspiciousAmountDetector.java:31`, `BalanceSheetParser.java:24`, `InvoicePdfGenerator.java:31` | c) API obsoleta | `new Locale(String,String)` | bajo | S | corregida (`Locale.of`) |
| C04 | `ComplianceController.java:157-158`, `ElectricalSpecification.java:43`, `SustainabilityClassifier.java:75` | c) API obsoleta | `divide(BigDecimal,int,int)` + `BigDecimal.ROUND_HALF_UP` | bajo | S | corregida (`RoundingMode.HALF_UP`) |
| C05 | `DocumentIndexingServiceTest.java:78,80` | c) API obsoleta | `Metadata.add` | bajo | S | corregida (`Metadata.put`) |
| C06 | `ai_assistant/application/service/DocumentIndexingService.java:140` (+ test:87) | c) API obsoleta | `EmbeddingStore.findRelevant` → `search(EmbeddingSearchRequest)` | bajo | S | pendiente (cambia el mock del test) |
| C07 | `energy/application/service/CertificateManagementService.java:61` | c) API obsoleta | `getIssuerDN()`; el reemplazo `getIssuerX500Principal()` formatea el DN distinto | bajo | S | requiere decisión |
| C08 | `SiiAuthSoapClientTest.java:46,68,79`, `SiiRcvSoapClientTest.java:38,49` | c) unchecked | Mock genérico de `HttpClient.send` | bajo | S | pendiente |
| D01 | `SII-ERP-AI-FRONT/src/features/period_close/components/AiCloseDashboard.tsx` | d) código muerto | Componente sin importaciones | bajo | S | corregida (eliminado) |
| D02 | `SII-ERP-AI-FRONT/src/features/dashboard/components/RecentActivityFeed.tsx` | d) código muerto | Componente sin importaciones | bajo | S | corregida (eliminado) |
| D03 | `SII-ERP-AI-FRONT/src/features/fixed_assets/**` | d) código muerto / e) duplicación | Hook y servicio duplicados de `features/accounting/hooks/useAccounting` (la página de activos usa el de accounting) | bajo | S | corregida (eliminado) |
| D04 | `SII-ERP-AI-FRONT/src/hooks/useCompanyContext.ts`, `src/lib/validators.ts`, `src/types/api-error.types.ts` | d) código muerto | Sin importaciones en src ni e2e | bajo | S | corregida (eliminado) |
| D05 | `SII-ERP-AI-FRONT/src/features/inventory/components/InventoryDashboard.tsx` | d) código muerto | No ruteado, endpoints falsos (AUDITORIA-FRONT) | medio | S | bloqueada por PR #32 |
| D06 | `SII-ERP-AI-FRONT/src/features/payroll/components/PayrollDashboard.tsx` | d) código muerto | No ruteado, `periodId: 1` fijo (AUDITORIA-FRONT) | medio | S | bloqueada por PR #32 |
| D07 | `SII-ERP-AI-FRONT/src/components/shared/CelebrationOverlay.tsx` | d) código muerto | Sin uso; su retiro es la decisión D-15 del plan de rediseño | bajo | S | requiere decisión |
| D08 | `SII-ERP-AI-FRONT/src/components/shared/{DateRangePicker,PeriodSelector}.tsx` | d) código muerto | Sin uso; el plan de rediseño los unifica en un `PeriodPicker` | bajo | S | requiere decisión |
| D09 | `SII-ERP-AI-FRONT/src/components/ui/{command,separator}.tsx` | d) código muerto | Primitivas shadcn sin uso; se conservan como parte del kit | bajo | S | pendiente |
| D10 | `SII-ERP-AI-FRONT/src/test/setup.ts` | d) código muerto | `vitest.config.ts` tiene `setupFiles: []` | bajo | S | bloqueada por PR #32 (vitest.config.ts) |
| E01 | `treasury/infrastructure/adapter/in/rest/TreasuryController.java:71` | e) endpoint duplicado | `POST /treasury/cash-entry` duplica `POST /treasury/cash-entries` (el que usa el front) | bajo | S | corregida (`@Deprecated`, sin borrar) |
| E02 | `accounting/domain/service/F29CalculatorService.java:222,235` | e) duplicación | Métodos `@Deprecated` que conviven con `calculateF29()` | bajo | S | pendiente |
| F01 | `payroll/application/service/PayrollServiceImpl.java:41-43` | f) valor de negocio fijo | Tasas AFP 11,44 %, salud 7 %, AFC 0,6 % fijas en código | alto | M | requiere decisión |
| F02 | `payroll/application/service/PayrollServiceImpl.java:184` | f) valor fijo | Gratificación 25 % sin tope legal | medio | S | requiere decisión |
| F03 | `payroll/application/service/PayrollServiceImpl.java:227-236` | f) valor fijo | Tramos del impuesto único en UTM fijos en código | medio | M | requiere decisión |
| F04 | `annual_close/application/service/IDPCCalculatorService.java:19-28` | f) valor fijo | Tasas IDPC 27 % / 25 % fijas | medio | M | requiere decisión |
| F05 | `fees/domain/util/RetentionRateCalculator.java:11-18` | f) valor fijo | Tasas de retención de honorarios por año (Ley 21.133) en código | bajo | S | requiere decisión |
| F06 | `accounting/domain/service/AnomalyDetectionService.java:78` | f) valor fijo | Promedio de IVA débito por defecto $500.000 | medio | S | pendiente |
| F07 | `accounting/infrastructure/web/AuditController.java:99` | f) valor fijo | `period` por defecto `"2025-10"` en `/audit/reconciliation` | medio | S | pendiente |
| F08 | `invoicing/infrastructure/adapter/in/rest/InvoiceImportController.java:120,123` | f) valor fijo | RUT propio guardado como literal `"EMPRESA_PROPIA"` al importar CSV | alto | M | requiere decisión |
| F09 | `dms/infrastructure/adapter/out/ai/GeminiApiClient.java:30` | f) URL fija | URL y modelo de Gemini fijos | bajo | S | pendiente |
| F10 | `bankingsync/infrastructure/adapter/out/rest/FintocOpenBankingClient.java:47` | f) URL fija | URL de Fintoc fija | bajo | S | pendiente |
| F11 | `integration_chile_data/infrastructure/adapter/MindicadorAdapter.java:37` | f) URL fija | `https://mindicador.cl/api` fijo | bajo | S | pendiente |
| F12 | `shared/infrastructure/config/MinioConfig.java:49` | f) URL fija | Fallback a `http://localhost:9000` con credenciales `none` | medio | S | pendiente |
| F13 | `accounting/application/service/DefaultAccountResolver.java:73-80` | f) códigos de cuenta | Códigos de cuenta de respaldo fijos | medio | M | bloqueada por PR #36 |
| F14 | `dms/application/service/AutomatedAccountingService.java:35` | f) códigos de cuenta | Cuenta `210101` fija en asiento automático | medio | S | pendiente |
| G01 | `SII-ERP-AI-FRONT/src/app/**`, `src/features/{settings,sii,sustainability}/**` | g) lint | 17 × `no-unused-vars` | bajo | S | corregida |
| G02 | `banking/page.tsx`, `LoginForm.tsx`, `DTEStatusTracker.tsx`, `InvoicePDF.tsx`, `lib/formatters.ts` | g) lint | 8 × `no-unused-vars` | bajo | S | bloqueada por PR #32 / #34 |
| G03 | `SII-ERP-AI-FRONT/src/components/shared/LogoUpload.tsx:87` | g) lint | `@next/next/no-img-element`; pasar a `next/image` cambia el render | bajo | S | pendiente |
| H01 | `SII-ERP-AI-FRONT/package.json` | h) dependencia | `next` 15.5.25: GHSA-2464-fhr4-w9xw (RCE en Image Optimization), GHSA-4jqv-mc3x-m676, GHSA-mcj8-r9mp-w47p y otros | alto | S | corregida (15.5.29) |
| H02 | `SII-ERP-AI-FRONT/package-lock.json` | h) dependencia | `npm audit fix` sin `--force`: axios, form-data, lodash, js-cookie, minimatch, brace-expansion, picomatch, rollup, sharp, ws, flatted, devalue, etc. | alto | S | corregida |
| H03 | `SII-ERP-AI-FRONT` `ai@3.4.33` | h) dependencia | GHSA-rwvc-j5jr-mgvh y transitivas (`@ai-sdk/*`, jsondiffpatch GHSA-33vc-wfww-vjfv, undici, @fastify/busboy); el fix es `ai@7` (mayor) | alto | L | requiere decisión |
| H04 | `SII-ERP-AI-FRONT` `vitest@2` | h) dependencia (dev) | tinypool GHSA-5gmw-xhrv-c9v3 (crítica), vitest GHSA-5xrq-8626-4rwp, vite, esbuild GHSA-67mh-4wv8-2f99; fix en vitest 5 (mayor) | medio (solo dev) | M | requiere decisión |
| H05 | `SII-ERP-AI-FRONT` `zod@3` | h) dependencia | GHSA-mrhx-c7xc-m3w5; fix en zod 4 (mayor) | medio | M | requiere decisión |
| H06 | `SII-ERP-AI-FRONT` `next@15` | h) dependencia | Moderadas restantes (postcss interno) se cierran en next 16 (mayor) | medio | L | requiere decisión |
| H07 | `SII-ERP-AI-BACK/build.gradle` | h) dependencia | Spring Boot 3.4.0 → Tomcat 10.1.33 (CVE-2025-24813, crítica), Spring Security 6.4.1 (CVE-2025-22228), pgjdbc 42.7.4 (CVE-2025-49146), Netty 4.1.115 (CVE-2025-24970) | alto | S | corregida (Boot 3.4.13: Tomcat 10.1.50, Security 6.4.13, pgjdbc 42.7.8, Netty 4.1.130) |
| H08 | `SII-ERP-AI-BACK` Apache Tika 2.9.1 (vía `langchain4j-document-parser-apache-tika:0.35.0`) | h) dependencia | CVE-2025-66516 y CVE-2025-54988 (XXE, críticas); el fix es Tika 3.2.2 (mayor) | alto | M | requiere decisión |
| H09 | `SII-ERP-AI-BACK` `bcprov-jdk18on:1.76` (transitiva de Tika) | h) dependencia | CVE-2024-30172 (alta) y CVE-2024-29857; fix en 1.78 (minor) | medio | S | requiere decisión |
| H10 | `SII-ERP-AI-BACK` `springdoc-openapi 2.3.0`, `langchain4j 0.35.0`, `minio 8.5.7`, `poi 5.2.5` | h) dependencia | Sin herramienta de CVEs configurada (no hay OWASP dependency-check ni Dependabot; osv.dev no es accesible desde el entorno del agente). Se listan versiones | — | — | pendiente |

## c) Resumen de `-Xlint` (antes)

```
80 removal     | MockBean has been deprecated and marked for removal
 5 unchecked   | unchecked method invocation: HttpClient.send
 5 unchecked   | unchecked conversion
 4 deprecation | divide(BigDecimal,int,int)
 4 deprecation | ROUND_HALF_UP
 3 deprecation | Locale(String,String)
 2 deprecation | findRelevant(Embedding,int)
 2 deprecation | Metadata.add(String,String)
 1 deprecation | getIssuerDN()
106 en total → 45 después (30 de MockBean en SsoIntegrationTest, bloqueado; 10 unchecked; 5 deprecation pendientes)
```

## g) Lint del front (antes, 0 errores / 26 warnings)

| Regla | Cantidad |
|---|---|
| `@typescript-eslint/no-unused-vars` | 25 |
| `@next/next/no-img-element` | 1 |

| Carpeta | Warnings |
|---|---|
| `src/features/sii` | 7 |
| `src/lib/formatters.ts` | 4 (bloqueado) |
| `src/app/(dashboard)` | 3 |
| `src/features/settings` | 3 |
| `src/features/sustainability` | 3 |
| `src/app/actions.tsx` | 2 |
| `src/features/invoicing` | 2 (bloqueado) |
| `src/components/shared` | 1 |
| `src/features/auth` | 1 (bloqueado) |

Después: 9 warnings (8 en archivos bloqueados + `LogoUpload.tsx`).

## h) Versiones principales del backend (resueltas tras el cambio)

Spring Boot 3.4.13 · Spring Framework 6.2.15 · Spring Security 6.4.13 · Tomcat 10.1.50 · Jackson 2.18.5 · pgjdbc 42.7.8 · Netty 4.1.130 · Logback 1.5.22 · langchain4j 0.35.0 · Apache Tika 2.9.1 · PDFBox 2.0.29 · POI 5.2.5 · xmlsec 3.0.4 · bcprov 1.76 · jjwt 0.11.5 · springdoc 2.3.0 · MinIO 8.5.7 · Testcontainers 1.20.x.

## Requiere decisión del dueño

| Deuda | Opciones | Recomendación |
|---|---|---|
| F01–F05 tasas de remuneraciones e impuestos fijas | (a) tabla `tax_parameters` por año (requiere migración); (b) `application.properties`; (c) dejar en código | (a), con este SQL de partida: `CREATE TABLE payroll_rate (year int, code varchar(32), rate numeric(7,4), PRIMARY KEY (year, code));` |
| F08 RUT propio literal `EMPRESA_PROPIA` | (a) leer el RUT de la empresa del contexto; (b) exigir columna en el CSV | (a): no requiere esquema |
| A02 `app.default-company-id` en asientos de carga | (a) propagar `CompanyId` en el evento OCPP; (b) mantener default | (a) |
| B04/B05 Redis caído deja logout y rate limiter en fail-open | (a) fail-closed en prod (`503`); (b) mantener fail-open | (a) solo con perfil `prod` |
| H03 `ai@3` → `ai@7` | (a) migrar el chat al SDK nuevo; (b) quitar `ai/rsc` si no se usa | Revisar uso real de `src/app/actions.tsx` primero |
| H04 `vitest@2` → `vitest@5` | Solo dev; impacto en CI | Subir junto con `vite` en un PR aparte |
| H08 Tika 2.9.1 (XXE crítica) | (a) subir langchain4j a una versión con Tika 3.2.2+; (b) forzar `tika-core`/`tika-parsers` 3.2.2 por constraint; (c) quitar el parser Tika si no se usa | (b) o (c) tras verificar el uso en `DocumentIndexingService` |
| H09 bcprov 1.76 | Forzar 1.78.1 por constraint (minor) | Sí, junto con H08 |
| C07 `getIssuerDN()` | Cambiar a `getIssuerX500Principal().getName()` (cambia el formato del DN guardado) | Decidir formato |
| D07/D08 componentes sin uso del rediseño | Borrar o esperar `PeriodPicker` / D-15 | Esperar el rediseño |
| Roles ya persistidos con `permissions = "[]"` | (a) re-sembrar roles de sistema; (b) `UPDATE roles SET permissions = ... WHERE system_role` | (a): `RoleService` solo siembra si no hay roles; borrar y re-sembrar en un entorno nuevo |
