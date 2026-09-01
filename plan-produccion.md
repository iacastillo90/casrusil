# 🚀 PLAN-PRODUCCION.md — De Demo a Producción (SII-ERP)

> Plan de remediación por fases para cerrar **todos** los hallazgos de [`pendiente.md`](./pendiente.md) y dejar el sistema **a prueba de balas, dinámico y listo para producción**.
>
> Cada tarea referencia su ID de hallazgo (ej. `C-1`, `A-4`, `M-3`). Cada fase incluye **objetivo**, **tareas**, **Definition of Done (DoD)** y **dependencias**.
>
> **Principio rector:** primero *detener el sangrado* (secretos y accesos), luego *cerrar puertas* (autz/auth), luego *hacerlo real* (quitar mocks), y por último *hacerlo robusto y observable* (resiliencia, tests, CI/CD, monitoreo).

---

## 🎯 Objetivos de "producción"

Definimos "listo para producción" con criterios medibles:

| Dimensión | Meta |
| :--- | :--- |
| **Seguridad** | 0 secretos en repo/disco; 100% de endpoints sensibles con autorización por rol; dependencias sin CVEs críticos/altos. |
| **Dinámico** | 0 valores hardcodeados de negocio; configuración por entorno (dev/staging/prod) vía variables/secret manager. |
| **Fuera de demo** | 0 mocks/placeholders en flujos de negocio; datos reales desde BD/servicios. |
| **A prueba de balas** | Manejo de errores tipado, idempotencia y reintentos (DLQ) en flujos financieros; cobertura de tests > 60% en módulos críticos. |
| **Operable** | CI con tests+lint+SCA en verde; observabilidad (logs, métricas, trazas); runbook de despliegue y rollback. |

---

## 📅 Fase 0 — Preparación y línea base (1 semana)

**Objetivo:** establecer las bases para trabajar de forma segura y medible antes de tocar código de negocio.

**Tareas:**
1. Crear entornos separados: `dev`, `staging`, `prod` con perfiles Spring (`application-{env}.properties`) y `.env.{env}` para el front.
2. Integrar un **gestor de secretos** (AWS Secrets Manager / SSM / HashiCorp Vault) y decidir el mecanismo de inyección (variables de entorno en runtime).
3. Congelar una rama `main` protegida; trabajar por PRs con revisión obligatoria.
4. Habilitar herramientas de calidad locales: `spotless`/`checkstyle` (back), `eslint`/`prettier` (front).
5. Generar reporte base de cobertura (JaCoCo back, Vitest coverage front) para medir progreso.

**DoD:** existen 3 perfiles funcionales; el equipo puede levantar el proyecto sin `.env` con secretos reales en disco; hay un número de cobertura inicial registrado.

---

## 🔴 Fase 1 — Detener el sangrado: Secretos (2-3 días) — CRÍTICA

**Objetivo:** que no exista ningún secreto real en el código, disco versionable ni en el frontend.

**Tareas:**
1. **Rotar/revocar** todas las claves expuestas: `GEMINI_API_KEY`, la API key mal usada en `MAIL_PASSWORD`, `POSTGRES_PASSWORD`, `REDIS_PASSWORD`, credenciales MinIO. → `C-1`, `C-2`, `B-3`
2. Mover todos los secretos al gestor de secretos; el código solo lee variables inyectadas en runtime. → `C-1`
3. Corregir `MAIL_PASSWORD` con una App Password válida de correo (no una API key). → `C-2`
4. Usar **claves Gemini distintas** para back y front, y llamar a Gemini **solo desde el backend** (el front no debe portar la clave). → `C-1`
5. Sacar `dummy.p12` del control de versiones (`git rm --cached`) y añadir `*.p12`, `*.jks`, `*.pem` al `.gitignore` global. Cargar el certificado por config externa. → `A-7`
6. Auditar `/Doc` y eliminar cualquier certificado/volcado con datos reales. → `B-7`
7. Añadir un **secret scanner** (gitleaks/trufflehog) como pre-commit hook y en CI para prevenir regresiones.

**DoD:** `gitleaks` en verde; ningún secreto real en repo ni en `.env` versionable; claves rotadas confirmadas; la app arranca leyendo secretos desde el gestor.

**Dependencias:** Fase 0 (gestor de secretos).

---

## 🔴 Fase 2 — Cerrar puertas: AuthN / AuthZ (1.5-2 semanas) — CRÍTICA

**Objetivo:** control de acceso robusto en back y front.

**Tareas:**
1. **Cerrar Actuator**: exponer solo `health`/`info` públicos; el resto tras rol `ADMIN`; `show-details=when_authorized`; en dev evitar `include=*`. → `C-3`
2. Definir **matriz de roles/permisos** (RBAC) por módulo y aplicar `@PreAuthorize` en todos los endpoints sensibles (52 controladores). Descomentar y activar el de `AuditController`. → `C-4`
3. **Persistir permisos de roles** de verdad en `RoleRepositoryAdapter` (hoy guarda `"[]"`). → `M-9`
4. Actualizar **Next.js a >= 15.2.3** (CVE-2025-29927) y validar que la autorización real vive en el backend, no solo en el middleware. → `C-5`
5. Implementar **revocación de JWT** (blacklist en Redis) + **refresh tokens** de vida corta; el logout invalida el token. → `A-1`
6. Añadir **rate limiting** (por IP/usuario) en `/auth/login` y `/auth/register` + bloqueo tras N intentos. → `A-2`
7. Login con **mensaje genérico** ("Credenciales inválidas") y sin loggear emails/credenciales. → `A-3`
8. Emitir la cookie de token desde el **backend** con `HttpOnly; Secure; SameSite=Strict`. → `A-4`
9. Derivar el **rol real** del usuario en el front desde el backend/claims (eliminar `role: 'ADMIN'` hardcodeado). → `A-5`
10. Añadir seguridad al **microservicio B2B** (JWT o gateway interno) y alinearlo a Spring Boot 3.4.0. → `C-6`

**DoD:** suite de tests de autorización pasa (usuario sin permiso → 403); Actuator inaccesible sin rol; `npm audit`/SCA sin CVE crítico de Next; logout invalida token verificado por test; B2B rechaza peticiones no autenticadas.

**Dependencias:** Fase 1.

---

## 🟡 Fase 3 — Salir de la demo: eliminar mocks y hardcodeo (3-4 semanas)

**Objetivo:** que todos los flujos de negocio usen datos y lógica reales.

**Tareas backend (prioridad por impacto contable/legal):**
1. **Tesorería** — `CashflowPredictorService`: leer balance, cuentas por cobrar/pagar y costos fijos desde BD. → `M-1`
2. **Nómina/Previred** — `PayrollServiceImpl`: implementar generación real del archivo de 105 columnas y cálculos legales (no "dummy 4%"). → `M-1`
3. **Depreciación** — `DepreciationServiceImpl`: cálculo real desde Activos Fijos. → `M-1`
4. **Operación Renta** — `F22DraftService`: lógica PPM real. → `M-1`
5. **Integración SII** — `Pkcs12Handler.extractRutFromCert`: extraer el RUT real del certificado. → `A-8`
6. **DMS/IA** — `GeminiApiClient`/`DmsServiceImpl`: extracción real (quitar modo `MOCK` por defecto). → `M-1`
7. **Reportes** — `WeeklyReportService`: generar PDF real (OpenPDF). → `M-1`
8. **Banking Sync / ESG / OCPP**: reemplazar respuestas mock por integraciones/datos reales. → `M-1`
9. Eliminar valores hardcodeados de negocio: `financialHealth=75.0`, costo kWh, `companyId` por defecto, cuentas `4101/5101`. → `M-1`, `M-9`

**Tareas frontend:**
10. Implementar los **9 componentes `(Mock)`** reales (Tesorería, contabilidad, sostenibilidad, chat IA). → `M-2`
11. **Eliminar `create_mocks.py`** del repo (oculta imports rotos). → `M-2`
12. Corregir el import roto en `Header.tsx`. → `M-5`
13. Unificar la config de Next (dejar un solo `next.config`). → `M-4`

**DoD:** búsqueda de `mock|dummy|placeholder|(Mock)` en código de negocio = 0; cada módulo reemplazado tiene test que valida el cálculo real; demo navegable con datos reales de punta a punta.

**Dependencias:** Fase 1 (claves reales de Gemini para DMS).

---

## 🟠 Fase 4 — A prueba de balas: robustez y correctitud (2-3 semanas)

**Objetivo:** manejo de errores tipado, resiliencia y consistencia de datos.

**Tareas:**
1. Sustituir `throw new RuntimeException(...)` por **excepciones de dominio/aplicación** específicas y mapearlas en el manejador global. → `A-9`
2. **Consolidar** los dos `GlobalExceptionHandler` en uno solo. → `A-10`
3. Consolidar el `PasswordEncoder` (bean + clases duplicadas). → `B-5`
4. Refactor de subclases anónimas de `DomainException` (x17) a excepciones concretas. → `B-1`
5. **Idempotencia + reintentos + DLQ** en flujos financieros que hoy tienen TODOs de "no perder dinero" (`ChargingEventListener`, listeners contables). → `M-9`
6. Cambiar `ddl-auto` a `validate` en todos los entornos y consolidar migraciones con **Flyway** (verificar que esté activo). → `A-6`
7. Certificados SII **por empresa** desde BD (`TokenRefreshScheduler` multi-tenant). → `M-9`
8. Revisar `spring.main.allow-bean-definition-overriding=true` y eliminarlo si es posible. → `B-6`
9. Alinear la **expiración de JWT** (política única, más corta que 24h). → `B-4`
10. Endurecer `docker-compose`: quitar volumen duplicado, resolver colisión de puerto 3000, credenciales por variables, Grafana sin anónimo en no-local. → `B-2`

**DoD:** no quedan `RuntimeException` en lógica de negocio/crypto; un único exception handler; migraciones Flyway aplican limpio en staging; test de idempotencia de eventos financieros en verde.

**Dependencias:** Fase 3 (lógica real ya presente).

---

## 🟡 Fase 5 — Calidad, tests y CI/CD (2-3 semanas)

**Objetivo:** red de seguridad automatizada que impida regresiones.

**Tareas:**
1. Poner `ignoreBuildErrors` e `ignoreDuringBuilds` en **`false`** y corregir todos los errores TS/ESLint. → `M-3`
2. Subir cobertura de tests: objetivo **> 60%** en módulos críticos (contabilidad, nómina, impuestos, SII). → `M-7`
3. **CI backend**: correr `./gradlew build` **con** tests (quitar `-x test`) + JaCoCo con umbral mínimo. → `M-6`
4. **CI frontend**: añadir `eslint`, `vitest` y `tsc --noEmit` como gates. → `M-6`
5. Añadir **SCA** (Dependabot/Trivy/OWASP Dependency-Check) y **SAST** en CI; secret scanner de Fase 1. → `M-6`
6. `CHAOS_MONKEY_ENABLED=false` por defecto; habilitar solo en pruebas de resiliencia controladas. → `M-8`

**DoD:** pipeline en verde bloquea merge si fallan tests/lint/cobertura/CVE crítico; badge de cobertura visible.

**Dependencias:** Fases 3 y 4 (código estable para testear).

---

## 🔵 Fase 6 — Arquitectura, escalabilidad y observabilidad (2-3 semanas)

**Objetivo:** mantenibilidad a largo plazo y operabilidad en producción.

**Tareas:**
1. Refactor de **God classes** (OCPP dispatcher/command, controllers de invoicing/energy) extrayendo responsabilidades. → `B-8`
2. Revisar el **alcance del módulo `energy`** (OCPP/EV): evaluar si debe ser un servicio independiente. → `B-9`
3. Completar la **observabilidad**: logs estructurados (sin PII), métricas Prometheus, trazas (Tempo ya está en config), dashboards Grafana con auth.
4. **Runbook** de despliegue: healthchecks, readiness/liveness, estrategia de rollback, backups de BD.
5. Pruebas de carga básicas en staging sobre endpoints críticos (login, emisión de documentos, cierres).

**DoD:** ninguna clase supera el umbral acordado de complejidad; dashboards operativos con alertas; despliegue en staging reproducible con rollback probado.

**Dependencias:** Fase 5.

---

## 🧭 Fase 7 — Endurecimiento final y salida a producción (1-2 semanas)

**Objetivo:** validación integral previa al go-live.

**Tareas:**
1. **Pentest / DAST** sobre staging (idealmente externo) y corrección de hallazgos.
2. Revisión manual de módulos financieros (contabilidad, nómina, F29/F22, SII) con un contador/experto de dominio.
3. Prueba de restore de backups y plan de continuidad.
4. Checklist OWASP ASVS nivel 2 para la app.
5. Go/No-Go con criterios de la tabla de "Objetivos de producción".

**DoD:** informe de pentest sin hallazgos críticos/altos abiertos; validación de dominio firmada; checklist de producción 100%.

---

## 🗺️ Cronograma orientativo

```
Fase 0  [#####]                                             ~1 sem
Fase 1     [###]                                            ~2-3 días  (CRÍTICA)
Fase 2        [##########]                                  ~1.5-2 sem (CRÍTICA)
Fase 3                  [####################]              ~3-4 sem
Fase 4                              [###############]       ~2-3 sem
Fase 5                                    [###############] ~2-3 sem  (parcialmente paralela a F4)
Fase 6                                            [########] ~2-3 sem
Fase 7                                                  [##] ~1-2 sem
```
**Estimación total:** ~13-18 semanas con un equipo pequeño. Las Fases 1 y 2 son bloqueantes para cualquier exposición pública.

---

## 📌 Matriz hallazgo → fase (trazabilidad)

| Fase | Hallazgos cubiertos |
| :--- | :--- |
| **1** | C-1, C-2, B-3, A-7, B-7 |
| **2** | C-3, C-4, C-5, C-6, A-1, A-2, A-3, A-4, A-5, M-9 (permisos) |
| **3** | M-1, M-2, M-4, M-5, A-8, M-9 (hardcodeo) |
| **4** | A-6, A-9, A-10, B-1, B-2, B-4, B-5, B-6, M-9 (DLQ/cert) |
| **5** | M-3, M-6, M-7, M-8 |
| **6** | B-8, B-9 |
| **7** | (validación integral de todo lo anterior) |

> Todos los IDs de `pendiente.md` quedan asignados a una fase. Se recomienda convertir cada tarea en un issue/ticket enlazado a este plan.

---

## ✅ Reglas para no volver a "modo demo"

1. **Definition of Done** obligatoria por PR: sin mocks, con tests, con autorización, sin secretos.
2. Gates de CI bloqueantes (tests, lint, cobertura, SCA, secret scan).
3. Prohibido `ignoreBuildErrors`/`-x test`/`permitAll` amplio sin justificación revisada.
4. Toda config de negocio proviene de BD o variables de entorno, nunca hardcodeada.
5. Revisión de seguridad en cada cambio que toque auth, crypto o datos financieros.
