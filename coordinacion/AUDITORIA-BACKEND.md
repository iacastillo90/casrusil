# Auditoría funcional del backend — SII-ERP AI

**Rama:** `docs/auditoria-backend` (desde `origin/integracion`, commit `4b99ddb`)
**Fecha:** 2026-11-21
**Naturaleza:** auditoría de **solo lectura**. No se modificó código. Los únicos archivos creados son este
informe y `coordinacion/evals-ia/casos.md`.

---

## 0. Método, entorno y límites

**Qué se hizo**

1. Relevamiento estático de 956 archivos Java, 60 controladores y 72 tests del backend
   (`SII-ERP-AI-BACK`), más el microservicio `SII-ERP-B2B-SERVICE`.
2. **Prueba real de runtime**: backend levantado en el puerto **8098** con una base PostgreSQL **vacía y
   propia** (`siierp_audit`, puerto 55432). Flyway aplicó V1→V4. Se registraron dos empresas reales, se
   importaron facturas reales por CSV y se ejecutaron llamadas HTTP contra los endpoints.
3. Verificación de normativa chilena en fuentes oficiales (sii.cl, dt.gob.cl, spensiones.cl) y de la
   oferta pública de ERP competidores (Nubox, Defontana, Bsale, Chipax, Rex+, Softland).

**Entorno de prueba**

| Elemento | Valor |
|---|---|
| Commit auditado | `4b99ddb` (Merge PR #30, `origin/integracion`) |
| Java | OpenJDK 21.0.12 (`--enable-preview` **obligatorio**: el jar no arranca sin él) |
| Comando | `java --enable-preview -jar build/libs/SII-ERP-AI-0.0.1-SNAPSHOT.jar --server.port=8098 --spring.profiles.active=dev` |
| Base | `siierp_audit` en `localhost:55432`, creada vacía; Flyway V1→V4 en 4,7 s |
| Arranque | ~255–298 s (Javers + carga de embeddings locales) |
| Empresas creadas | Alfa `009616f1…` (RUT 76543210-3) y Beta `6af7d54c…` (RUT 77123456-9) |
| Datos | 4 facturas de venta + 2 de compra, noviembre 2026 |

**Límites honestos de esta auditoría**

- **No hay `GEMINI_API_KEY`** en el entorno. El arranque se forzó con una clave *dummy* para observar el
  comportamiento ante fallo del proveedor. **Toda la Parte 2.3 (evaluación con clave real) queda ❔**, con
  los 24 casos escritos y sus respuestas correctas en `coordinacion/evals-ia/casos.md`.
- **No se envió nada al SII real** (requiere certificado digital y CAF vigente). El envío/consulta se
  verificó en código y en su cadena de tests, no contra `maullin.sii.cl`.
- El perfil `dev` usa almacén de embeddings en memoria y Redis puede estar caído; los errores derivados se
  reportan como tales, no como fallo del producto en producción.

**Escala aplicada**

- ✅ **Completo**: existe + expuesto por API + test + probado con llamada real.
- 🟡 **Parcial**: existe en parte, o existe sin test, o sin prueba real.
- ❌ **No existe**.
- ❔ **No verificable aquí** (se indica qué falta).

> **Resultado de fondo:** **ningún ítem alcanza ✅**. Varios están cerca (F29, estado de resultados, RCV),
> pero ninguno cumple las cuatro condiciones simultáneamente. El sistema tiene una arquitectura razonable y
> piezas reales; lo que falta es la **cadena que las conecta** y el **aislamiento entre empresas**.

---

## 1. Resumen ejecutivo por bloque

| Bloque | ✅ | 🟡 | ❌ | ❔ | Total | Lectura |
|---|---|---|---|---|---|---|
| **A. DTE emitidos** | 0 | 10 | 3 | 0 | 13 | Pipeline XML/TED/firma/envío existe; faltan boletas 39/41, referencia en NC/ND, traslado en guía y anulación |
| **B. Documentos recibidos** | 0 | 1 | 3 | 0 | 4 | RCV parcial; no hay descarga de XML, ni acuse/reclamo de 8 días |
| **C. Libros y declaraciones** | 0 | 11 | 3 | 0 | 14 | F29 calcula pero sobre asientos que casi nunca se crean; F22 parcial; sin corrección monetaria |
| **D. Remuneraciones** | 0 | 2 | 7 | 0 | 9 | Cálculo simplificado y hardcodeado; sin finiquitos, LRE, vacaciones ni certificados |
| **E. Operación** | 0 | 9 | 2 | 0 | 11 | Inventario y tesorería con lógica real; conciliación bancaria y centros de costo no existen |
| **F. Plataforma** | 0 | 5 | 4 | 0 | 9 | **1 usuario = 1 empresa**; sin rol por empresa, sin respaldo, sin API keys; B2B es esqueleto |
| **G. Calidad** | 0 | 1 | 3 | 0 | 4 | 60 controladores / 72 tests; stubs, hardcodes legales y catches silenciosos |
| **IA** | 0 | 5 | 6 | 1 | 12 | Asistente real de solo lectura + OCR; predicción y anomalías **no son IA**; aislamiento roto en escritura |
| **TOTAL** | **0 (0%)** | **44 (58%)** | **31 (41%)** | **1 (1%)** | **76** | |

**Conclusión del resumen:** el producto está en una etapa **“núcleo contable-tributario a medio camino”**.
Lo que un contador independiente necesita para operar un mes completo (emitir boletas, aceptar facturas
recibidas, generar asientos desde documentos, declarar F29/F22, liquidar sueldos legales y conciliar el
banco) **no está cerrado end-to-end**. Lo que sí funciona bien como base es: plan de cuentas chileno
sembrado, cálculo F29 sobre asientos, estado de resultados/balance sobre asientos, motor de inventario PMP,
tesorería con asientos, y un pipeline DTE con XML/TED/firma/envío.

---

## 2. PARTE 1 — Obligaciones y funciones esperadas en Chile

### 2.1 Verificación normativa (fuentes oficiales)

Solo se listan obligaciones confirmadas con fuente oficial. Lo no confirmable va como **NO CONFIRMADO**.

| Obligación | Vigente | Norma | Dato clave verificado | Fuente |
|---|---|---|---|---|
| Factura electrónica obligatoria | Sí | DL 825; Ley 20.727; Res. Ex. SII 45/2003 | Obligatorio emitir electrónicamente factura, factura exenta, factura de compra, NC y ND | https://www.sii.cl/destacados/factura_electronica/ |
| Acuse de recibo / reclamo de facturas | Sí | Ley 19.983 arts. 3-4; Circ. SII 56/2013 | **8 días corridos** desde la recepción en el SII; sin acción hay acuse ficto | https://www.sii.cl/preguntas_frecuentes/factura_electronica/001_003_6603.htm |
| Libros de compras/ventas electrónicos | Sí | DL 825 art. 59; Res. 45/2003 | Envío **mensual obligatorio** de IEC/IEV al SII | https://www.sii.cl/destacados/factura_electronica/factura_etapas_8.html |
| F29 (IVA mensual) | Sí | DL 825 art. 64 | Día **12**; día 20 con pago online; hasta día 28 sin pago | https://www.sii.cl/preguntas_frecuentes/impuestos_mensuales/001_130_1060.htm |
| F22 (renta anual) | Sí | DL 824 art. 65 | Declaración con pago: **8 al 30 de abril** | https://www.sii.cl/preguntas_frecuentes/declaracion_renta/001_140_8953.htm |
| Regímenes 14 A / 14 D N°3 / N°8 | Sí | DL 824 arts. 14 A, 14 D | 14 A **27%**; 14 D N°3 **25%** (transitoria **12,5% en 2025**, 2026 **NO CONFIRMADO**); N°8 liberada de IDPC | https://www.sii.cl/destacados/renta/2025/regimenes_renta2025.html |
| PPM | Sí | DL 824 art. 84 | Obligatorios en renta efectiva; Pro Pyme N°3 **0,25%** (≤50.000 UF); N°8 **0,2%** | https://www.sii.cl/destacados/modernizacion/tipos_regimenes_mt.html |
| Corrección monetaria | Sí | DL 824 arts. 32-33 | Aplica al 14 A (contabilidad completa); **no** a Pro Pyme N°3/N°8 | https://www.sii.cl/preguntas_frecuentes/declaracion_renta/001_140_2655.htm |
| Cotizaciones previsionales | Sí | DL 3500 art. 17; Ley 19.728; Ley 21.735 | AFP **10%** + comisión, tope **90 UF**; cesantía **0,6%** trabajador, **2,4%** indefinido / **3,0%** plazo fijo; horas extra **+50%**; gratificación **25%** con tope **4,75 IMM** | https://www.spensiones.cl/portal/institucional/594/w3-propertyvalue-9908.html |
| Nuevo aporte empleador (reforma) | Sí | Ley 21.735 | **1% inicial → 7% en 9 años**; **3,5% desde ago-2026** | https://www.spensiones.cl/portal/institucional/594/w3-propertyvalue-9902.html |
| Finiquito e indemnización | Sí | Código del Trabajo arts. 159-163 y 172 | **30 días** por año; fracción >6 meses = año; tope **11 años**; tope **90 UF** sobre remuneración base | https://www.dt.gob.cl/legislacion/1624/w3-article-95516.html |
| Previred | Operativo | DL 3500; Ley 17.322 | Declaración y pago de cotizaciones; plazo día **10** (13 si electrónico) | https://www.previred.com/ |
| Retención de honorarios (tasa) | Parcial | DL 824 art. 74 N°2 | **Tasa 2025-2026: NO CONFIRMADO** en fuente oficial consultada | — |
| Códigos DTE y TED por tipo | Parcial | — | **NO CONFIRMADO** en fuente oficial consultada; el código usa 33/34/46/52/56/61/110/111/112 y **no** 39/41 | — |
| LRE (Libro de Remuneraciones Electrónico) | Sí (habilita) | Ley 21.361 | Obligatoriedad y plazos específicos: **NO CONFIRMADO** | https://www.dt.gob.cl/legislacion/1624/w3-article-95516.html |

> **Advertencia de implementación:** el código hardcodea **IVA 19%** y **tasas IDPC 25/27%**, pero la tasa
> Pro Pyme N°3 de 2025 es **transitoria (12,5%)**. Codificar 25% como permanente produce un F22 incorrecto
> en 2025. Ver `IDPCCalculatorService.java:19,22,28`.

### 2.2 Bloque A — Documentos tributarios electrónicos

Pipeline común existente: XML (`DteXmlBuilder.java:84-113`), TED (`TedGenerator.java:29-50`), firma
xmlsec (`XmlDsigSigner.java:60-103`), envío (`DteSenderService.java:83-138`, `SiiUploadClient.java:25-52`),
consulta de estado (`DteStatusService.java:116-127`), PDF417 (`Pdf417Generator.java:34-57`), PDF
(`InvoicePdfGenerator.java:33-156`).

| Ítem | Estado | Evidencia | Qué falta | Esf. | Impacto |
|---|---|---|---|---|---|
| Factura afecta 33 | 🟡 | `InvoiceType.java:36`; `DteXmlBuilder.java:115-178`; tests `DteXmlBuilderTest`, `DteEmissionChainTest`; runtime `GET /api/v1/sii/dte/33/1001` → 200 | Sin anulación/NC automática; sin endpoint de descarga PDF; no probado contra SII real | M | Obligatorio legal |
| Factura exenta 34 | 🟡 | `InvoiceType.java:38`; `DteXmlBuilder.java:152-163` | El XML emite `MntNeto`, **no `MntExe`** → documento incorrecto; sin test por tipo | S | Obligatorio legal |
| Boleta afecta 39 | ❌ | No está en `InvoiceType.java:35-54`; `PosTransactionListener.java:31-38` dice 39 pero usa código **58** | Todo el flujo (XML, TED, envío, PDF, código 39 real) | L | Obligatorio legal (emprendedor/POS) |
| Boleta exenta 41 | ❌ | No existe en el modelo | Todo el flujo | M | Obligatorio legal |
| Factura de compra 46 | 🟡 | `InvoiceType.java:42` | Sin bloques específicos de factura de compra; sin test | M | Esperado por el mercado |
| Guía de despacho 52 | 🟡 | `InvoiceType.java:44` | No emite `IndTraslado` ni destino; sin test | M | Esperado por el mercado |
| Nota de débito 56 | 🟡 | `InvoiceType.java:46` | **No emite bloque `Referencia`** → ND inválida tributariamente; sin test | S | Obligatorio legal |
| Nota de crédito 61 | 🟡 | `InvoiceType.java:48` | **No emite `Referencia`**; sin anulación automática; sin test | S | Obligatorio legal |
| Factura exportación 110 | 🟡 | `InvoiceType.java:50` | Sin campos de exportación (moneda, aduana, país); sin test | M | Esperado por el mercado |
| ND exportación 111 | 🟡 | `InvoiceType.java:52` | Sin `Referencia` ni campos de exportación | M | Esperado por el mercado |
| NC exportación 112 | 🟡 | `InvoiceType.java:54` | Ídem | M | Esperado por el mercado |
| PDF con PDF417 descargable | 🟡 | `Pdf417Generator.java:34-57`; `InvoicePdfGenerator.java:33-156`; solo se usa al enviar por email (`InvoiceDispatchService.java:96-107`) | Endpoint de descarga de PDF; datos de empresa demo hardcodeados (`InvoicePdfGenerator.java:47-50`) | S | Obligatorio legal |
| Anulación / corrección de DTE | ❌ | No existe; solo `PUT /api/v1/invoices/{id}` bloqueado si hay asientos (`InvoiceManagementService.java:91-145`) | Flujo de anulación, NC de referencia y acuse al SII | L | Obligatorio legal |

**Hallazgo transversal A:** el sobre de envío hardcodea `TpoDTE=33` y `NroDTE=1`
(`EnvioDteBuilder.java:51-52`). Aunque cada tipo tenga XML, **el envío no es correcto para tipos distintos
de 33**. Firma con `RSA-SHA1` (`XmlDsigSigner.java:60-103`), algoritmo obsoleto.

### 2.3 Bloque B — Documentos recibidos

| Ítem | Estado | Evidencia | Qué falta | Esf. | Impacto |
|---|---|---|---|---|---|
| RCV (compras/ventas) | 🟡 | `POST /api/v1/sii/ops/fetch-rcv` (`SiiIntegrationController.java:60-97`); `POST /api/v1/sii/sync`; `SiiRcvSoapClient.java:36-79` | Cliente marcado como “implementación simplificada/simulada” (`:37-40`); sin prueba real | L | Obligatorio legal |
| Descarga de XML de DTE recibidos | ❌ | No hay cliente de descarga XML; solo RCV | Descarga, almacenamiento y parseo de recibidos | M | Esperado por el mercado |
| Aceptación/reclamo de facturas (8 días) | ❌ | Solo `AcuseReciboScheduler.java:35-40` genera una alerta con folio/proveedor **hardcodeados** | Entidad, endpoints, cómputo legal de 8 días corridos, envío al SII | M | **Obligatorio legal** |
| Acuse de recibo de mercaderías | ❌ | No encontrado | Flujo completo | M | Esperado por el mercado |

**Bug funcional:** el listener de importación RCV crea **todas** las facturas como `PURCHASE`
(`DtesDownloadedListener.java:56-62`, TODO en `:61`), aunque el libro sea de ventas. Además
`SiiSyncService.java:144` afirma que el RCV “no se importa”, contradiciendo al listener.

### 2.4 Bloque C — Libros y declaraciones

| Ítem | Estado | Evidencia | Qué falta | Esf. | Impacto |
|---|---|---|---|---|---|
| Libro diario | 🟡 | `GET /api/v1/accounting/entries` (`AccountingController.java:132-136`); `AccountingEntryService.java:38-54`; test `AccountingEntryServiceTest` | **No hay endpoint para crear un asiento manual**; sin exportación oficial | M | Obligatorio legal |
| Libro mayor | 🟡 | `GET /api/v1/accounting/ledger/{accountCode}` (`AccountingController.java:191-200`) | Sin test directo del endpoint; sin exportación | S | Obligatorio legal |
| Balance de 8 columnas | ❌ | No encontrado | Implementación y exportación | M | Esperado por el mercado |
| Estado de resultados | 🟡 | `GET /api/v1/accounting/reports/income-statement`; `ReportingService.java:41-104`; test `ReportingServiceTest` | Devuelve 0 en prueba real porque **no hay asientos**; llama a Gemini en cada request | S | Esperado por el mercado |
| F29 — cálculo | 🟡 | `F29CalculatorService.java:62-205`; `GET /api/v1/accounting/f29?period=YYYY-MM`; test `F29CalculatorServiceTest` | Calcula sobre `AccountingEntry`; en prueba real con 6 facturas devolvió **0** (no hay asientos). Códigos F29 incompletos | M | Obligatorio legal |
| F29 — declaración | 🟡 | `POST /api/v1/reports/f29/submit`; `F29SenderService.java:25-46` | **Simula** el envío (`Thread.sleep`, UUID); sin firma ni SII | M | Obligatorio legal |
| F22 por régimen | 🟡 | `POST /api/v1/annual-close/f22/calculate/{year}`; `F22DraftService.java:29-72`; `IDPCCalculatorService.java:11-32` | Sin formulario F22 ni envío; 3 regímenes incompletos; renta presunta; test solo de perfil faltante | L | Obligatorio legal |
| Declaraciones juradas | 🟡 | `Dj1879GeneratorService.java:33-96`; **ningún controller lo expone** | Exponerlo, formato oficial y otras DJ (1948/1947/1926/1847/1943) | M | Obligatorio legal |
| PPM | 🟡 | Solo estimación anual dentro de F22 (`F22DraftService.java:85-99`) | Declaración mensual y tasas por régimen | M | Obligatorio legal |
| BHE — recepción | 🟡 | `POST /api/v1/fees/import`, `GET /api/v1/fees`; `FeeReceiptService.java:49-108`; tests | Obtención desde SII; validación estricta; tasas hardcodeadas (`RetentionRateCalculator.java:11-18`) | M | Obligatorio legal |
| BHE — emisión | ❌ | No encontrado | Emisión completa | M | Obligatorio legal (si emite honorarios) |
| Cierre mensual | 🟡 | `POST /api/v1/accounting/periods/close` (`AccountingController.java:241-250`); `PeriodClosingService.java:57-73`; test | El módulo `period_close` es mock (`PeriodCloseAuditorService.java:32-35`, `:73`) | M | Esperado por el mercado |
| Cierre anual | 🟡 | Solo cálculo F22 | Asientos de cierre anual | M | Esperado por el mercado |
| Corrección monetaria | ❌ | Solo IPC=1.0 en DJ 1879 (`Dj1879GeneratorService.java:54-58`) | Implementación real (requerida por 14 A) | L | Obligatorio legal (régimen general) |

**Hallazgo crítico de la cadena contable (runtime):** las facturas importadas **no generan asientos**.
El asiento solo se crea cuando el SII **acepta** el DTE (`InvoiceAccountingListener.java:56-70`). Como
nadie envía al SII en el flujo normal, `entries=[]`, `F29=0`, `ER=0`, `balance=0`. **El sistema no puede
producir contabilidad a partir de sus propios documentos.**

### 2.5 Bloque D — Remuneraciones

| Ítem | Estado | Evidencia | Qué falta | Esf. | Impacto |
|---|---|---|---|---|---|
| Contratos de trabajo | ❌ | Solo tabla `pay_employees` (`V1__esquema_base.sql:722`); sin entidad ni servicio | Alta, cláusulas, jornada, vigencia, anexos | L | Obligatorio legal |
| Liquidaciones (proceso) | 🟡 | `POST /api/v1/payroll/calculate` (`PayrollController.java:40`); `PayrollServiceImpl.java:180-217`; test `PayrollServiceImplTest` (solo no-null/neto) | Topes imponibles, rentas no imponibles, descuentos, préstamos | M | Obligatorio legal |
| Parámetros legales (AFP por administradora, salud, AFC empleador, gratificación topada, IUSC anual) | ❌ | Tasas **fijas** `PayrollServiceImpl.java:41-43` (`0.1144`, `7%`, `0.6%`); ignora `Employee.afp`; gratificación 25% **sin tope** (`:184`); tabla IUSC hardcodeada (`:227-237`) | Tablas por AFP y por año, topes 90 UF / 135,2 UF, AFC 2,4%/3,0% empleador, tope 4,75 IMM, actualización anual | L | **Obligatorio legal** |
| Horas extra y asignaciones | ❌ | Sin campos ni cálculo | Recargo 50%, tope legal; movilización, colación, asignación familiar | M | Obligatorio legal |
| Archivo Previred | 🟡 | `POST /api/v1/payroll/previred/export`; `PreviredFileGenerator.java:37,51-98`; 2 tests | Campos en `0`, sexo vacío (`:66`), sin RUT empresa ni licencias; sin cotización empleador | M | Obligatorio legal |
| LRE (DT) | ❌ | No encontrado | Libro y envío a DT | L | Obligatorio legal |
| Vacaciones y licencias | ❌ | No encontrado (solo cuenta contable de provisión) | Solicitud, saldo, fuero, subsidio, integración Previred | L | Obligatorio legal |
| Finiquitos e indemnizaciones | ❌ | No encontrado | Años de servicio, aviso previo, causal, finiquito electrónico | M | **Obligatorio legal** |
| Certificados de remuneraciones | ❌ | `PayslipPdfExporter` existe pero **ningún controller lo usa** | Certificado anual/renta y endpoint de PDF | S | Esperado por el mercado |

**Bug concreto:** salud Isapre multiplica **UF por porcentaje** en vez de aplicar el plan en UF sobre la
renta (`PayrollServiceImpl.java:190-192`). Produce liquidaciones incorrectas cuando `healthPercentage`
está seteado.

### 2.6 Bloque E — Operación

| Ítem | Estado | Evidencia | Qué falta | Esf. | Impacto |
|---|---|---|---|---|---|
| Inventario con valorización | 🟡 | `/api/v1/inventory/products`, `/{id}/movements` (`InventoryController.java:26-76`); `InventoryServiceImpl.java:115-127`; test | Solo **PMP**; FIFO no existe; Excel y forecast no expuestos | M | Esperado por el mercado |
| Compras | 🟡 | `/api/v1/procurement/orders` (`PurchaseOrderController.java:21`) | Recibir **no aumenta stock ni CxP** (`PurchaseOrderServiceImpl.java:56`) | M | Esperado por el mercado |
| Ventas | 🟡 | `/api/v1/invoices`; runtime OK | Montos e IVA vienen del request (`InvoiceController.java:86-88`), **no se calculan en servidor** | S | Esperado por el mercado |
| Cotizaciones | 🟡 | `/api/v1/crm/quotes`; `Quote.java:40-45` | Aceptar **no genera factura** (`CrmServiceImpl.java:64`) | S | Esperado por el mercado |
| Tesorería | 🟡 | `/api/v1/treasury/*`; `CashManagementService.java:78-112`; runtime `cash-entries` 200 | Pronóstico “IA” simulado (`TreasuryAiPredictorServiceImpl.java:18-44`) | M | Esperado por el mercado |
| Conciliación bancaria | ❌ | `/api/v1/banking/*`; `POST /sync` inserta **movimientos mock** (`BankingSyncServiceImpl.java:46-51`); `reconcile` solo cambia estado (`:65-68`) | Integración bancaria real (Fintoc existe sin usar) y matching | L | Esperado por el mercado |
| Activos fijos y depreciación | 🟡 | `/api/v1/fixed-assets`, `/run-depreciation`; `FixedAssetServiceImpl.java:55` | Vidas útiles hardcodeadas 84/120 meses; **no genera asiento** ni control por período | M | Esperado por el mercado |
| Centros de costo | ❌ | No encontrado | Modelo, imputación y reportes | L | Esperado por el mercado |
| Proyectos | 🟡 | `/api/v1/projects`; `ProjectServiceImpl.java:27-46` | CRUD sin imputación de costos ni presupuesto; **IDOR** (`ProjectController.java:35-36`) | M | Diferenciador |
| Multimoneda | 🟡 | Tesorería acepta `currency`/`exchangeRate` (`TreasuryController.java:104-105`) | Facturas guardan moneda sin conversión (`Invoice.java:126`); sin revaluación | L | Esperado por el mercado |
| POS | 🟡 | `/api/v1/pos/*`; `PosServiceImpl.java:40` | No descuenta stock ni emite boleta; evento sin publicador | M | Esperado por el mercado |

**Módulos cascarón adicionales** (existen endpoints, lógica incompleta): `mrp` (`MrpServiceImpl.java:60-83`),
`wms` (`WmsServiceImpl.java:75-80`), `fleet` (solo `GET /metrics`), `procurement`, `b2b`
(`CustomerOrder.pay` solo cambia estado), `sustainability` (factores fijos,
`SustainabilityMetricsController.java:78-79` salud financiera placeholder 75.0).

### 2.7 Bloque F — Plataforma

| Ítem | Estado | Evidencia | Qué falta | Esf. | Impacto |
|---|---|---|---|---|---|
| Un usuario con varias empresas | ❌ | `sso.users` con `company_id` y `role` únicos (`V1__esquema_base.sql:1049-1058`); `/me` devuelve **una** empresa (runtime) | Tabla puente usuario-empresa, empresa activa, cambio de contexto | L | **Esperado (contador independiente)** |
| Rol por empresa | ❌ | Rol único en `sso.users` (`User.java:50-51`) | Membresía con rol por empresa | M | Esperado (contador) |
| Invitación de usuarios | 🟡 | `POST /api/v1/sso/users/invite`; token UUID, 7 días (`UserInvitation.java:44-45`) | `validate` y `register-invite` quedan bajo `hasRole('ADMIN')` (`SsoUserController.java:36,75-103`) → **el invitado no puede aceptar**; sin revocación | S | Esperado por el mercado |
| Auditoría de acciones | 🟡 | Javers instalado con tablas `jv_*` (`V1__esquema_base.sql:591-638`) pero **sin `@Audited` ni commits** | Anotar entidades y registrar actor real; `UserContext` **nunca se puebla** (`SecurityFilter.java:100-115`) → actor siempre “Sistema” | M | Esperado por el mercado |
| Exportación de datos del cliente | 🟡 | Excel de ventas OK (`ReportingController.java:114-146`); Previred OK | Export completo por empresa; CSV de energía **sin filtro de empresa** (`EnergyReportService.java:93-98`) | M | Esperado por el mercado |
| Respaldo | ❌ | No existe en código (solo mención documental) | Backup/restore, export completo | L | Esperado por el mercado |
| Notificaciones | 🟡 | `/api/v1/notifications/*`; `NotificationService.java:14-38` | SSE en memoria, `HashMap` no concurrente, sin persistencia ni scoping | M | Esperado por el mercado |
| API para integraciones | 🟡 | Solo JWT Bearer; springdoc en dependencias (`build.gradle:27`) | Sin API keys, sin OAuth, Swagger exige JWT y no está configurado (`SecurityConfig.java:59-71`) | M | Diferenciador |
| Microservicio B2B | ❌ | `SII-ERP-B2B-SERVICE`: **cero `@RestController`**; no usa `erp.core-url`; entidad no coincide con el esquema | Implementarlo o retirarlo | M | Diferenciador |

### 2.8 Bloque G — Calidad

| Ítem | Estado | Evidencia | Qué falta | Esf. | Impacto |
|---|---|---|---|---|---|
| Cobertura de tests de endpoints | 🟡 | 60 controllers, 72 tests; `energy` tiene 58 endpoints y **1 test**; sin tests de `partners`, `procurement`, `projects`, `crm`, `banking`, `wms`, `mrp` | Tests de contrato por endpoint crítico | L | Esperado por el mercado |
| Servicios con TODO / stubs | ❌ | `F29SenderService.java:17-45`; `SiiRcvSoapClient.java:37-40`; `TreasuryAiPredictorServiceImpl.java:18-44`; `BankingSyncServiceImpl.java:46-51`; `SiiAuditController.java:30-35` (no hace nada); `TaxAuditController.java:31-33`; `JournalEntryService.java:14-27` (handlers solo loguean) | Implementar o eliminar del API | L | Esperado por el mercado |
| Catch que traga errores | ❌ | `AnomalyDetectionService.java:76` `catch (Exception ignored)`; `FeeReceiptService.java:105-107`; `FeeReceiptParser.java:116-117`; `DtesDownloadedListener.java:47-50`; `PayslipRepositoryAdapter.java:41-43` | Propagar o registrar con contexto | M | Esperado por el mercado |
| Valores de negocio fijos | ❌ | IVA 19% (`DteXmlBuilder.java:172`, `InvoicePdfGenerator.java:143`, `PosTransactionListener.java:33`); IDPC (`IDPCCalculatorService.java:19,22,28`); BHE (`RetentionRateCalculator.java:11-18`); tasas AFP/salud/AFC (`PayrollServiceImpl.java:41-43`); IUSC (`:227-237`); cuentas (`AutomatedAccountingService.java:31-35`); umbral 500.000 (`AnomalyDetectionService.java:78,105`); RUT fallback `66666666-6` (`InvoiceController.java:82`) | Parametrizar por año y por régimen en `tax_parameters`; eliminar fallbacks de RUT | L | **Obligatorio legal** (tasas cambian por ley) |

**Bug de contrato HTTP:** el `GlobalExceptionHandler` captura `Exception` (`GlobalExceptionHandler.java:177-188`)
y convierte **rutas inexistentes y parámetros faltantes en 500** en vez de 404/400 (verificado en runtime:
`GET /api/v1/spaces` → 500; `financial-ratios` sin `year` → 500).

---

## 3. PARTE 2 — Capacidades de IA

### 3.1 Inventario

| # | Capacidad | Archivo:línea | Modelo | Datos enviados | Caída del proveedor |
|---|---|---|---|---|---|
| 1 | Chat + 3 herramientas | `ConversationService.java:47-133` | Gemini (`GeminiConfig.java:34-42`; default `gemini-1.5-flash`) | Prompt + `companyId` + historial + salida de tools | `catch` devuelve **el error crudo del proveedor al usuario** (`:122-124`) — verificado en runtime |
| 2 | `search_invoices` | `SearchInvoicesTool.java:53-67` | — | **Todas** las facturas serializadas (RUT, razón social, folio, montos) | Devuelve texto de error |
| 3 | `get_sales_report` | `GetSalesReportTool.java:54-110` | — | Totales del período | Devuelve texto de error |
| 4 | `calculate_f29` | `CalculateF29Tool.java:52-64` | — | Borrador F29 | Devuelve texto de error |
| 5 | OCR de facturas | `GeminiApiClient.java:64-66`, `InvoiceAiReaderService.java:31-52` | **`gemini-1.5-pro` hardcodeado** (`:30`) | PDF/imagen completa en Base64 | `RuntimeException` (`:118-124`), sin timeout |
| 6 | Resumen de documentos DMS | `DmsServiceImpl.java:161-176` | `gemini-1.5-pro` | Bytes del documento | Propaga excepción |
| 7 | Resumen ejecutivo de reportes | `AiFinancialAnalyst.java:24-29` | `gemini-1.5-pro` | Estado de resultados | `catch` → texto “no disponible” (runtime) |
| 8 | Asesor financiero diario por email | `FinancialAdvisorScheduler.java:49-148` | Gemini | Razón social, F29, vencidos, caja | `try/catch` por empresa |
| 9 | Análisis de cierre de período | `AiIntegrationService.java:22-52` | Gemini | Discrepancias (mock) | `catch` → texto de error |
| 10 | Asiento automático desde factura | `AutomatedAccountingService.java:23-56` | **No invoca LLM** | Salida del OCR | Excepción → 500 |
| 11 | RAG / base de conocimiento | `KnowledgeBaseController.java:52-99`; `DocumentIndexingService.java:103-156` | Embeddings locales MiniLM + pgvector | Documentos internos | `search` → lista vacía |
| 12 | Predicción de flujo de caja | `TreasuryAiPredictorServiceImpl.java:16-58` | **No es IA**: valores fijos | — | N/A (runtime: 15.000.000 fijo) |
| 13 | Detección de anomalías | `AnomalyDetectionService.java:84-114`; `DuplicateInvoiceDetector.java:37-57` | **No es IA**: reglas/estadística | — | — |
| 14 | Clasificación/aprendizaje de cuentas | `LearningService.java:75-114` | **No es IA**: reglas por evento | — | — |
| 15 | Inventario OCPP/energía IA | `GridStatusTool.java:125,136` | — | — | **Inventa datos con `Math.random`**; tool no cableada |

**Herramientas muertas:** `EfficiencyAdvisorTool`, `GridStatusTool`, `SustainabilityReportTool` tienen
`@Tool` pero **no se inyectan** en ningún agente.

**OpenAI / xAI:** solo variables (`application.properties:11-13,32`); **no hay cliente implementado**.

### 3.2 Privacidad — qué sale hacia el proveedor

| Dato | Evidencia | Severidad |
|---|---|---|
| RUT emisor/receptor, razón social, folio, montos, ítems completos | `SearchInvoicesTool.java:57,63-64`; `Invoice.java:65-83` | **Alta** |
| Razón social, F29, facturas vencidas, caja, alertas | `FinancialContextBuilder.java:96-185` | **Alta** |
| Documentos completos (PDF/imágenes) en Base64 | `GeminiApiClient.java:91-103` | **Alta** |
| Redacción / anonimización | **No existe** (búsqueda `anonim|redact|mask` sin resultados) | **Alta** |
| API key en query string | `GeminiApiClient.java:113` (riesgo de fuga en logs) | Media |

No hay DPA, retención declarada ni opción de desactivar el envío de PII por empresa. Para datos
tributarios de terceros (RUT de clientes/proveedores) esto es un riesgo de cumplimiento (Ley 19.628/21.719).

### 3.3 Aislamiento entre empresas en la IA

**Lo correcto:** las 3 tools del chat **no** reciben `companyId` como parámetro; usan
`CompanyContext.requireCompanyId()` (`SearchInvoicesTool.java:57`, `GetSalesReportTool.java:67`,
`CalculateF29Tool.java:56`). El LLM no puede elegir empresa. La búsqueda RAG sí filtra por empresa
(`DocumentIndexingService.java:143-148`).

**Lo roto (crítico):**

| Hallazgo | Evidencia | Verificación |
|---|---|---|
| `X-Tenant-ID` del request define la empresa del **asiento creado por IA** | `AiInvoicingController.java:29-38` | Estático |
| `X-Company-Id` del request define la empresa del análisis IA de cierre | `PeriodCloseController.java:20-31` | **Runtime: HTTP 200 con header de otra empresa** |
| `sendMessage` no valida que la conversación sea del usuario/empresa | `ConversationService.java:47-51`; `AiAssistantController.java:57-61` | Estático |
| Reglas de clasificación por ID sin filtro de empresa | `LearningController.java:51-57,64-74,81-85` | Estático |

### 3.4 Acciones que la IA puede disparar

| Acción | Evidencia | ¿Aprobación humana? |
|---|---|---|
| **Crear asiento contable** desde factura leída por IA, en la misma request | `AiInvoicingController.java:35-38`; `AutomatedAccountingService.java:24-56` | ❌ **No**. Queda en `DRAFT` pero **se persiste** (`:50,55`) |
| Enviar email masivo con análisis IA a todas las empresas (cron) | `FinancialAdvisorScheduler.java:49,146` | ❌ No |
| Crear/modificar reglas de clasificación | `LearningController.java:92-103` | Manual, no IA |
| Emitir DTE / crear factura / enviar al SII vía LLM | — | **No existe** (bien) |

Las 3 tools del chat son de **solo lectura**. El único camino de escritura es el endpoint de OCR
(`/dms/ai-invoicing/process`), que **no pide confirmación**.

### 3.5 Validación de salidas y robustez

| Control | Evidencia | Evaluación |
|---|---|---|
| Partida doble (Debe = Haber) | `AccountingEntry.java:89-99` | Único control real; si el OCR devuelve montos incoherentes → excepción → HTTP 500 |
| Comparación monto IA vs. documento/fuente | **No existe** | **Alta**: no se revalida neto+IVA=total contra la factura ni contra el SII |
| Validación de RUT / folio / fecha del OCR | **No existe** (`InvoiceAiReaderService.java:38-51`) | **Alta** |
| Umbral de confianza / revisión | **No existe** | Alta |
| Timeouts | `GeminiApiClient.java:48` `new RestTemplate()` sin timeouts | **Alta** |
| Reintentos / circuit breaker | Solo instancia `sii` (`application.properties:34-41`); **no aplica a IA** | **Alta** |
| Esquema de parámetros de tools | `ToolSpecification` sin `parameters` (`ConversationService.java:72-77`) | **Alta (funcional)**: el LLM no puede entregar `YYYY-MM`; `get_sales_report` y `calculate_f29` reciben argumento vacío |
| Iteración de tool-calling | Un solo ciclo (`ConversationService.java:84-120`) | Media |
| Prompt injection | Hay instrucción defensiva (`GeminiApiClient.java:31-35`) | Parcial, sin validación posterior |

**Qué pasa si la IA responde un monto incorrecto:** nada lo detecta antes de mostrar el texto. En el caso
del OCR, el asiento se crea y solo falla si Debe≠Haber; un IVA mal calculado que cuadre la partida pasa sin
objeción. **No hay verificación contra los datos reales.**

### 3.6 Evaluación con clave real — ❔

**No ejecutada:** `GEMINI_API_KEY` no está definida en el entorno. Los **24 casos** con preguntas reales de
contador/emprendedor, la respuesta correcta calculada desde el dataset sembrado, los criterios
exacto/parcial/error/alucinación y los 4 casos de seguridad están en
**`coordinacion/evals-ia/casos.md`** para ejecutarlos cuando exista clave.

Lo que **sí** se verificó con la clave *dummy* (comportamiento ante caída del proveedor):

- Chat: `HTTP 200` con `"Error processing request: INVALID_ARGUMENT (code 400) API key not valid…"`
  → **filtra el error del proveedor al usuario final** y no hay mensaje de degradación controlado.
- Estado de resultados: `aiAnalysis` incluye el JSON de error completo de Google (400 líneas de detalle).
- Cierre de período: `aiRecommendation: "Error conectando con la IA: … API key not valid"`.

---

### 3.7 Resumen de capacidades de IA (base del cuadro general)

| Ítem | Estado | Evidencia | Qué falta | Esf. | Impacto |
|---|---|---|---|---|---|
| Chat con herramientas de solo lectura | 🟡 | `ConversationService.java:47-133`; runtime 200 con error del proveedor | Manejo de errores, persistencia, ownership de conversación | M | Diferenciador |
| Herramientas con parámetros (rango de fechas) | ❌ | `ToolSpecification` sin `parameters` (`ConversationService.java:72-77`) | Declarar parámetros; hoy el LLM no puede pasar `YYYY-MM` | S | Diferenciador |
| OCR / lectura de documentos | 🟡 | `GeminiApiClient.java:64-66`; `InvoiceAiReaderService.java:31-52` | Validación de RUT/folio/montos, timeouts, confianza | M | Diferenciador |
| Asiento automático desde documento | 🟡 | `AutomatedAccountingService.java:23-56` | Aprobación humana y revalidación de montos | M | Diferenciador |
| Resúmenes de reportes y documentos | 🟡 | `AiFinancialAnalyst.java:24-29`; `DmsServiceImpl.java:161-176` | El resumen de ER llama al método OCR con esquema de factura (falla) | S | Diferenciador |
| RAG / base de conocimiento | 🟡 | `KnowledgeBaseController.java:52-99`; `DocumentIndexingService.java:103-156` | RAG apagado por defecto; `@Lazy` falla al indexar si está apagado | M | Diferenciador |
| Clasificación / aprendizaje de cuentas con IA | ❌ | `LearningService.java:75-114` (reglas, no IA) | Modelo o heurística entrenada; hoy es reglas por evento | L | Diferenciador |
| Detección de anomalías con IA | ❌ | `AnomalyDetectionService.java:84-114` (reglas; umbral fijo 500.000) | Modelo real y umbral por empresa | M | Diferenciador |
| Predicción de flujo de caja | ❌ | `TreasuryAiPredictorServiceImpl.java:16-58` (valores fijos) | Modelo real sobre movimientos | L | Diferenciador |
| Privacidad / anonimización de PII | ❌ | Sin `anonim|redact|mask`; envío completo a Gemini | Anonimización, retención, opt-out por empresa | M | Obligatorio legal (datos de terceros) |
| Aislamiento entre empresas en IA | ❌ | `AiInvoicingController.java:29-38`; `PeriodCloseController.java:20-31` | Derivar empresa del JWT y quitar headers | S | Obligatorio (seguridad) |
| Evaluación con clave real | ❔ | `coordinacion/evals-ia/casos.md` | Falta `GEMINI_API_KEY` | S | Diferenciador |

---

## 4. PARTE 3 — Brechas para competir

Comparativo basado **solo en información publicada** por cada proveedor (noviembre 2026). “No publicado”
significa que el sitio no lo declara; no prueba que no exista.

| Proveedor | DTE | Contabilidad | Remuneraciones | Inventario/POS | Conciliación bancaria | Multiempresa | API pública | IA publicada | Precio público |
|---|---|---|---|---|---|---|---|---|---|
| **Nubox** | Factura, boleta afecta/exenta, NC, ND, guía, exportación | Diario, mayor, balances, ER, DDJJ 1879/1847/1835/1926 | Liquidaciones, contratos, finiquitos, LRE, DT | Stock básico | Sí (Fintoc, “principales bancos”) | Contabilidad multiempresa + portal contador | “API de emisión” | **No publica IA**; habla de automatización | Desde 1,31 UF/mes |
| **Defontana** | Factura electrónica SII | Mayor, cierres, 36+ DDJJ, IFRS | Nómina, Previred, LRE, contratos/finiquitos | Sí + POS (Tivendo) | Automática (bancos no publicados) | 10/50/100 empresas según plan | Menciona API; doc no pública | **Sí, amplia**: “Ana IA”, agentes de compras, abastecimiento, reportería NL, RRHH | ERP 2,50–6,99 UF |
| **Bsale** | Boleta y factura electrónica | **No publicado** (sin módulo contable) | No ofrece | **Núcleo**: POS, inventario multi-sucursal | No publicado | Usuarios ilimitados; multiempresa no publicado | **Sí**: API REST, sandbox, webhooks | **No publica IA** | 1,3–2,9 UF |
| **Chipax** | **No emite DTE**; carga desde SII | Plan de cuentas, flujo de caja; libros no publicados | No ofrece | No ofrece | **Núcleo** (bancos/pasarelas) | 1/2/3 usuarios; multiempresa no publicado | **Sí**: API v2 + MCP | **Sí**: clasificación/conciliación, cobranza, MCP para Claude/ChatGPT | Gratis; 2–4 UF |
| **Rex+** | **No emite DTE** | “Centralización contable” | **Núcleo**: sueldos, finiquitos, Previred, LRE, DDJJ | No ofrece | Transferencias (conciliación no publicada) | **Sí**: múltiples empresas | APIs como add-on | **Sí**: SmartRex (RRHH) con aprobación previa | 0,039–0,09 UF/colaborador |
| **Softland** | Facturación electrónica SII (tipos no detallados) | Finanzas, tesorería, presupuestos | Nómina | Compras e inventario | “Robot de Conciliación” | No publicado | Plugins; sin API pública visible | “Softland Pedidos IA” (alcance no verificado) | Desde 2 UF |

**Fuentes:** nubox.com/software-factura, /software-contabilidad, /software-factura/conciliacion-bancaria ·
defontana.com/cl/erp-genesis, /software-contabilidad-booker, /software-rrhh-zenda, /agentes-ia ·
bsale.cl/sheet/precios, bsale.cl/sheet/plan-punto-venta, bsale.dev ·
chipax.com, chipax.com/integraciones, chipax.com/mcp-ia ·
rexmas.com/software-de-remuneraciones, /precios, /agente-inteligencia-artificial ·
softland.com/cl/, /recursos/apis-e-integraciones/, /productos/softland-erp/finanzas-y-contabilidad/

**Lectura competitiva honesta**

- **Ningún competidor publica** un “cierre contable autónomo” ni IA que emita DTE sin intervención. Ese
  espacio está libre, pero hoy el sistema tampoco lo ocupa: su IA es de solo lectura y su OCR escribe
  asientos sin validación.
- **Lo que este sistema tiene y ellos no publican:** OCR de facturas con asiento borrador, asistente de chat
  con herramientas sobre datos propios, y un motor OCPP/energía. Es un **diferenciador potencial**, no
  actual, porque el aislamiento y la validación están rotos.
- **Lo que ellos tienen y este sistema no:** multiempresa real con portal de contador (Nubox/Defontana/Rex+),
  conciliación bancaria real (Nubox/Chipax/Defontana), LRE y finiquitos (Nubox/Defontana/Rex+), y
  **API pública documentada** (Bsale/Chipax/Rex+).
- **Público objetivo del prompt (contador independiente con varias empresas):** es exactamente donde el
  sistema está más lejos: **1 usuario = 1 empresa** y sin rol por empresa.

---

## 5. Top 10 brechas (orden: obligatorio legal → impacto en el público → esfuerzo)

| # | Brecha | Por qué primero | Esf. | Impacto |
|---|---|---|---|---|
| 1 | **Boletas electrónicas 39/41** (con su XML/TED/envío/PDF) | Obligatorio legal para emprendedor y POS; hoy no existen y el listener usa el código 58 | L | Obligatorio legal |
| 2 | **Aceptación/reclamo de facturas recibidas en 8 días** | Plazo legal con consecuencia automática (acuse ficto); hoy es una alerta con datos falsos | M | Obligatorio legal |
| 3 | **Cadena DTE → asiento → F29** (hoy F29 = 0 con facturas cargadas) | Sin esto no hay contabilidad ni declaración correcta; es el corazón del producto | M | Obligatorio legal |
| 4 | **Remuneraciones legales completas** (AFP por administradora, topes, AFC empleador, gratificación topada, IUSC anual, horas extra, finiquitos, LRE) | Un error de nómina es una contingencia laboral real; hoy hay tasas fijas y bugs | L | Obligatorio legal |
| 5 | **F22 por régimen + declaraciones juradas expuestas + corrección monetaria** | Obligatorio para cerrar el año; el cálculo existe a medias y sin envío | L | Obligatorio legal |
| 6 | **BHE emisión y retención con tasa vigente** | Obligatorio si el cliente emite honorarios; la tasa está hardcodeada por año | M | Obligatorio legal |
| 7 | **Referencia en NC/ND (56/61), traslado en guía (52) y anulación de DTE** | Los documentos actuales son inválidos o no se pueden corregir | S/M | Obligatorio legal |
| 8 | **Multiempresa real con rol por empresa y portal de contador** | Es el público declarado (contador con varias empresas); hoy es 1:1 y además hay fugas entre empresas | L | Esperado por el mercado |
| 9 | **Conciliación bancaria real** | Función estándar en Nubox/Chipax/Defontana; hoy inserta datos mock | L | Esperado por el mercado |
| 10 | **IA confiable: tools con parámetros, validación contra datos, aislamiento y aprobación humana** | Es el diferenciador declarado; hoy puede crear asientos en otra empresa y no valida montos | M | Diferenciador |

---

## 6. Riesgos críticos encontrados

| # | Riesgo | Evidencia | Verificación |
|---|---|---|---|
| R1 | **Escritura cruzada entre empresas vía importación CSV**: un usuario de A importa facturas en la empresa B pasando `companyId=B` | `InvoiceImportController.java:59-60,80,170-172` | **Runtime: A importó folio 9001 en B; B lo ve con su `companyId`** |
| R2 | **Lectura/escritura cruzada en nómina** con `X-Company-Id` no validado | `PayrollController.java:27,35,42,49` | **Runtime: A abrió período de nómina 2026-11 en B (HTTP 200)** |
| R3 | **Cierre de período/año cruzado** con `X-Company-Id` | `PeriodCloseController.java:20,24,31`; `AnnualCloseController.java:17,22` | **Runtime: A auditó el cierre de B (HTTP 200)** |
| R4 | **Asiento contable creado por IA en empresa arbitraria** vía `X-Tenant-ID` | `AiInvoicingController.java:29-38` | Estático (requiere PDF + IA) |
| R5 | **IDOR** en proyectos, órdenes de compra, reglas de clasificación y conciliación (buscan por UUID sin empresa) | `ProjectController.java:35-36,48`; `PurchaseOrderController.java:38-39,62-68`; `LearningController.java:52-56,67-73,82-84`; `BankingSyncController.java:54-61` | Estático |
| R6 | **Fuga de PII fiscal a Gemini sin anonimización** (RUT, razones sociales, montos, PDFs completos) | `SearchInvoicesTool.java:57,63-64`; `FinancialContextBuilder.java:96-185`; `GeminiApiClient.java:91-103` | Estático |
| R7 | **Cálculo de nómina legalmente incorrecto** (AFP fija 11,44%, salud Isapre en UF mal aplicada, gratificación sin tope, AFC empleador ausente) | `PayrollServiceImpl.java:41-43,184,190-192` | Estático + tests débiles |
| R8 | **Tasas legales hardcodeadas por año** (IDPC 25/27%, BHE, IUSC) que no se actualizan | `IDPCCalculatorService.java:19,22,28`; `RetentionRateCalculator.java:11-18`; `PayrollServiceImpl.java:227-237` | Estático |
| R9 | **Asiento automático sin aprobación humana ni revalidación de montos** | `AutomatedAccountingService.java:50,55`; `AiInvoicingController.java:35-38` | Estático |
| R10 | **Secretos por defecto** (BD, MinIO `admin/password123`, JWT de compose) y **JWT devuelto en el body** (anula la cookie HttpOnly) | `application-prod.properties:4`; `application.properties:128-129`; `docker-compose.yml:35`; `AuthResponse.java:20-26` | Estático |
| R11 | **Invitaciones inutilizables**: `validate`/`register-invite` exigen ADMIN; el invitado no puede aceptar | `SsoUserController.java:36,75-103` | Estático |
| R12 | **Errores internos filtrados al usuario** (mensaje crudo del proveedor de IA) y **500 en rutas inexistentes** | `ConversationService.java:122-124`; `GlobalExceptionHandler.java:177-188` | **Runtime** |

> R1–R3 están **confirmados con llamadas reales** contra el backend levantado. Son bloqueantes de seguridad:
> cualquier usuario autenticado puede leer y escribir datos de otra empresa.

---

## 7. Propuesta de rondas de trabajo (agentes en paralelo sin pisarse)

Cada ronda toca paths disjuntos y arranca desde `origin/integracion` en worktree propio.

| Ronda | Foco | Paths (propiedad) | Entregable verificable |
|---|---|---|---|
| **P0 — Seguridad multi-tenant** | Eliminar `companyId`/`tenantId`/`X-Company-Id` del request; derivar siempre de `CompanyContext`; cerrar IDOR; cerrar actor de auditoría | `shared/**/security/**`, `shared/**/context/**`, `modules/{invoicing,payroll,period_close,annual_close,dms,projects,procurement,accounting,bankingsync}/**/rest|web/**` | Tests de aislamiento con 2 empresas (los 3 casos R1–R3 en verde) |
| **P1 — DTE emitidos** | Boletas 39/41; `Referencia` en 56/61; traslado en 52; campos de exportación; sobre por tipo; endpoint PDF; anulación | `modules/invoicing/{domain,application}/**`, `modules/integration_sii/**`, `modules/invoicing/infrastructure/pdf/**` | Un test por tipo DTE + golden XML + PDF descargable |
| **P2 — Cadena contable y declaraciones** | Asientos desde DTE (sin esperar aceptación SII para compras/RCV); asiento manual; F29 real; F22 por régimen; DDJJ expuestas; balance 8 columnas; corrección monetaria | `modules/accounting/**`, `modules/annual_close/**`, `modules/period_close/**` | F29 con el dataset de prueba = **30.400** (débito 76.000 − crédito 45.600); balance cuadrado |
| **P3 — Remuneraciones** | `tax_parameters` por AFP y año; topes; AFC empleador; gratificación topada; horas extra; finiquitos; LRE; Previred completo | `modules/payroll/**`, `modules/integration_chile_data/**`, `db/migration/**` | Liquidación de ejemplo validada contra cálculo manual; Previred con 105 campos completos |
| **P4 — Operación** | Conciliación bancaria real; compras→CxP+stock; POS→boleta+stock; compras de cotizaciones; centros de costo; multimoneda | `modules/{bankingsync,procurement,pos,crm,inventory,projects,treasury}/**` | Flujo compra→stock→CxP y venta POS→boleta con tests |
| **P5 — IA** | Tools con esquema de parámetros; `get_sales_report` filtrando ventas; validación de montos contra fuente; aislamiento; timeouts/retry/circuit breaker; aprobación humana; harness de evals | `modules/ai_assistant/**`, `modules/dms/**`, `modules/accounting/application/service/AiFinancialAnalyst.java` | `coordinacion/evals-ia/casos.md` ejecutado con ≥80% de acierto exacto y 0 alucinaciones |
| **P6 — Plataforma** | Membresía usuario-empresa con rol; invitaciones; auditoría real (Javers + actor); export/backup; API keys; retirar o implementar B2B | `modules/sso/**`, `modules/user/**`, `SII-ERP-B2B-SERVICE/**`, `shared/**/notification/**` | Contador con 3 empresas y roles distintos; `/me` con cambio de contexto |

**Orden recomendado:** P0 primero (bloquea todo lo demás por seguridad). Luego P1+P2 en paralelo (comparten
conocimiento del dominio pero no paths). P3 y P4 en paralelo. P5 depende de P2 (necesita F29 correcto para
evaluar). P6 al final o en paralelo con P3/P4 si hay equipo.

---

## 8. Informe (plantilla del plan)

```markdown
## Informe — Auditoría Backend — rama docs/auditoria-backend
**Commits**: <hash — docs(auditoria): auditoría funcional del backend y casos de evaluación IA>
**PR**: <url> → integracion
### Hecho (verificado)
- Relevamiento estático de 956 archivos Java, 60 controllers, 72 tests — evidencia archivo:línea.
- Backend levantado en 8098 con base vacía `siierp_audit` (Flyway V1→V4) — `GET /actuator/health` → UP.
- 2 empresas registradas y 6 facturas importadas — `GET /api/v1/invoices` con 6 registros.
- Aislamiento multi-tenant roto confirmado con 3 llamadas reales (R1, R2, R3).
- F29/ER/balance = 0 con facturas cargadas (cadena contable desconectada).
- IA sin clave: error crudo del proveedor filtrado al usuario (chat y reportes).
### Contratos API expuestos o consumidos
| Método | Ruta | Request | Response | Rol(es) |
|---|---|---|---|---|
| POST | /api/v1/auth/register | rut, razonSocial, adminEmail, adminPassword | token, companyId | público |
| POST | /api/v1/invoices/import | multipart file + companyId + bookType | ImportResult | ADMIN/SALES/ACCOUNTANT |
| GET | /api/v1/accounting/f29 | period=YYYY-MM | F29Report | autenticado |
| POST | /api/v1/payroll/periods | X-Company-Id + year/month | PayrollPeriodResponse | ADMIN/HR |
| POST | /api/v1/ai/conversations/{id}/messages | content | Message | autenticado |
### Gates
- compileJava: ✅ | bootJar: ✅ | tests: no ejecutados (auditoría de solo lectura)
### Contradicciones doc ↔ código
- `SiiSyncService.java:144` dice que el RCV no se importa; `DtesDownloadedListener` sí crea facturas.
- `/me` devuelve arreglo `companies`, pero el modelo tiene una sola empresa por usuario.
### Pendiente / a medias
- Evaluación de IA con clave real — ❔ falta GEMINI_API_KEY.
- Envío real al SII — ❔ falta certificado y CAF.
### Necesito de otros agentes
- P0 — seguridad multi-tenant — es prerrequisito de cualquier feature que toque datos por empresa.
### Riesgos/IDs cerrados
- R1, R2, R3 confirmados en runtime; R4–R12 estáticos. Ninguno cerrado.
```
