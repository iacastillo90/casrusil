# 📋 PENDIENTE.md — Auditoría Técnica SII-ERP

> Documento de hallazgos, vulnerabilidades, deuda técnica y refactorizaciones pendientes.
> Generado a partir de un análisis estático del monorepo (backend Spring Boot, frontend Next.js y microservicio B2B).
>
> **Fecha del análisis:** 2026-08-31
> **Alcance:** `SII-ERP-AI-BACK` (772 archivos Java), `SII-ERP-AI-FRONT` (Next.js 15), `SII-ERP-B2B-SERVICE`.
>
> **Convención de severidad:**
> - 🔴 **CRÍTICO** — Riesgo de seguridad real o corrupción de datos financieros. Atender de inmediato.
> - 🟠 **ALTO** — Impacto importante en seguridad, correctitud o mantenibilidad.
> - 🟡 **MEDIO** — Deuda técnica relevante, mocks o funcionalidad incompleta.
> - 🔵 **BAJO** — Mejoras, limpieza y buenas prácticas.

---

## 🔴 CRÍTICO — Seguridad

### C-1. Claves de API y credenciales reales en archivos `.env` (texto plano)
Aunque `.env` está en `.gitignore` (no se sube a git), los archivos existen en disco con **secretos reales activos** que deben rotarse:

- `SII-ERP-AI-BACK/.env`
  - `GEMINI_API_KEY=AIzaSyBoX8FzhyGa7Ni5zftKwZZMUsYmaSBZDYU` → **clave real de Google, debe revocarse y rotarse**.
  - `MAIL_PASSWORD=AIzaSyBsgH9gMUVI5nrh4o_0MvRnP0jCn3Mu0AQ` → aparenta ser **otra API key de Google usada erróneamente como contraseña de correo**. Configuración incorrecta + secreto expuesto.
  - `POSTGRES_PASSWORD=admin` → contraseña trivial.
- `SII-ERP-AI-FRONT/.env`
  - `GOOGLE_GENERATIVE_AI_API_KEY=AIzaSyBoX8FzhyGa7Ni5zftKwZZMUsYmaSBZDYU` → **la misma clave Gemini repetida** en el frontend.
- Raíz `.env`
  - `POSTGRES_PASSWORD=EsteEsUnPasswordSeguroParaProd123!` y `REDIS_PASSWORD=...` hardcodeados como si fueran de producción.

**Acción:** Revocar y rotar TODAS las claves expuestas (Gemini, correo). Mover secretos a un gestor (Vault, AWS Secrets Manager, SSM). Nunca reutilizar la misma clave en front y back. Separar credenciales por entorno.

### C-2. `MAIL_PASSWORD` mal configurada con una API key
En `SII-ERP-AI-BACK/.env` el valor de `MAIL_PASSWORD` es una cadena tipo `AIza...` (formato de Google API key), no una App Password de Gmail. El envío de correo no funcionará y hay un secreto filtrado en el campo equivocado.

### C-3. Endpoints de Actuator expuestos sin autenticación
En `SecurityConfig.java` (línea ~60): `.requestMatchers("/api/v1/auth/**", "/error", "/actuator/**").permitAll()`.
Combinado con:
- `application.properties`: `management.endpoints.web.exposure.include=health,info,prometheus,metrics` + `management.endpoint.health.show-details=always`.
- `application-dev.properties`: `management.endpoints.web.exposure.include=*` (expone **todos** los endpoints, incluido `env`, `beans`, `heapdump`, Chaos Monkey).

**Acción:** Restringir `/actuator/**` a rol de administrador. No exponer `prometheus`/`metrics`/`env` públicamente. `show-details=when_authorized`. En dev evitar `*`.

### C-4. Autorización a nivel de método casi inexistente (Broken Access Control — OWASP A01)
Solo **4 usos de `@PreAuthorize`** en **52 controladores REST**. La seguridad se apoya únicamente en "estar autenticado" + aislamiento por `companyId`, sin control de roles por endpoint.
- Ejemplo explícito: `AuditController.java:95` tiene `// @PreAuthorize("hasAuthority('AUDIT_READ')")` **comentado**.
Cualquier usuario autenticado (incluso rol bajo) puede invocar operaciones sensibles (contabilidad, cierres, nómina, usuarios).

**Acción:** Definir matriz de roles/permisos y aplicar `@PreAuthorize` en todos los endpoints sensibles. Añadir tests de autorización.

### C-5. Next.js 15.0.3 vulnerable a bypass de middleware (CVE-2025-29927)
`SII-ERP-AI-FRONT/package.json`: `"next": "15.0.3"`. Esta versión es vulnerable al bypass de middleware vía el header `x-middleware-subrequest`. El archivo `src/middleware.ts` usa el middleware como puerta de autenticación de rutas, por lo que un atacante podría saltárselo.

**Acción:** Actualizar Next.js a `>= 15.2.3` (idealmente última 15.x estable). La autorización real siempre debe validarse en el backend, no solo en el middleware del front.

### C-6. Microservicio B2B sin ninguna capa de seguridad
`SII-ERP-B2B-SERVICE/build.gradle` **no incluye `spring-boot-starter-security`** ni autenticación. Sus endpoints (si expone controladores) quedarían totalmente abiertos. Además usa **Spring Boot 3.2.3** mientras el backend principal usa **3.4.0** (desalineación de versiones y parches de seguridad).

**Acción:** Añadir seguridad/JWT al B2B (o exponerlo solo tras un gateway interno), y alinear versiones de Spring Boot.

---

## 🟠 ALTO — Seguridad y Correctitud

### A-1. Sin revocación de tokens JWT / logout no invalida el token
`JwtTokenProvider.java` no implementa blacklist ni refresh tokens. `RedisConfig.java:35` **menciona en un comentario** "Blacklist de tokens JWT revocados", pero **no está implementado**. El `logout` del frontend solo borra la cookie; el token sigue siendo válido hasta expirar (24h).

**Acción:** Implementar blacklist de JWT en Redis (ya está la dependencia) y/o refresh tokens de vida corta.

### A-2. Sin rate limiting / protección de fuerza bruta en login
`build.gradle` anuncia "Redis: Caché, rate limiting e invalidación de tokens JWT", pero **no hay rate limiting implementado** en `AuthService.login`. No hay bloqueo tras N intentos fallidos.

**Acción:** Implementar rate limiting (por IP/usuario) en `/auth/login` y `/auth/register`.

### A-3. Enumeración de usuarios en el login
`AuthService.java`: el login lanza mensajes distintos para "Usuario no encontrado" vs "Contraseña incorrecta", y además los registra en log a nivel `error` con el email. Esto permite enumerar cuentas válidas.

**Acción:** Devolver un mensaje genérico ("Credenciales inválidas") en ambos casos. Evitar loggear emails/credenciales.

### A-4. Token JWT accesible por JavaScript (cookie no httpOnly)
`auth.service.ts`: la cookie `token` se establece desde el cliente con `js-cookie` (`secure` + `sameSite:'strict'`), pero **no es `httpOnly`** (imposible desde el cliente). Sigue siendo accesible por JS y por tanto vulnerable a robo vía XSS. El propio comentario reconoce la limitación.

**Acción:** Emitir la cookie desde el backend con `Set-Cookie: HttpOnly; Secure; SameSite=Strict`.

### A-5. Rol de usuario hardcodeado a `ADMIN` en el frontend
`auth.service.ts` (`handleAuthSuccess`): `role: 'ADMIN' as const` para **todos** los usuarios tras login, ignorando el rol real. El gating de UI por rol es inefectivo/engañoso.

**Acción:** Derivar el rol real desde la respuesta del backend / claims del token.

### A-6. `spring.jpa.hibernate.ddl-auto=update` en configuración por defecto y dev
`application.properties` y `application-dev.properties` usan `ddl-auto=update`. Si el perfil `prod` no se activa correctamente, Hibernate modifica el esquema en caliente (riesgo de corrupción). Ya hay carpeta `resources/db/migration` (Flyway) pero conviven ambos enfoques.

**Acción:** Usar `validate` + migraciones (Flyway/Liquibase) en todos los entornos salvo desarrollo local puntual. Verificar que Flyway esté realmente activo.

### A-7. Certificado `dummy.p12` versionado en el repositorio
`git ls-files` confirma que `SII-ERP-AI-BACK/src/main/resources/dummy.p12` **está trackeado**. Aunque sea de prueba, versionar `.p12` es mala práctica; el `.gitignore` de front ignora `*.pem` pero no `*.p12` del back.

**Acción:** Quitar el `.p12` del control de versiones y cargarlo por configuración externa. Añadir `*.p12` al `.gitignore`.

### A-8. Extracción de RUT del certificado SII no implementada (placeholder)
`Pkcs12Handler.java` → `extractRutFromCert()` **siempre retorna `"UNKNOWN-RUT"`**. El RUT del titular del certificado es esencial para la autenticación y firma ante el SII. La integración fiscal está funcionalmente rota en este punto.

**Acción:** Implementar la extracción real del RUT desde el DN/SerialNumber según el formato de la CA (Acepta, E-Sign, etc.).

### A-9. Uso masivo de `throw new RuntimeException(...)` en lógica de negocio y crypto
~30+ ocurrencias, especialmente en el módulo `integration_sii` (crypto, SOAP, REST) y en servicios de negocio (ej. `InventoryServiceImpl` "Insufficient stock", `PayrollServiceImpl` "period already closed"). Esto impide un manejo de errores tipado, dificulta las respuestas HTTP correctas y puede filtrar mensajes internos.

**Acción:** Sustituir por excepciones de dominio/aplicación específicas y mapearlas en el `GlobalExceptionHandler`.

### A-10. Dos `GlobalExceptionHandler` duplicados
Existen dos clases:
- `shared/infrastructure/web/GlobalExceptionHandler.java`
- `shared/infrastructure/web/exception/GlobalExceptionHandler.java`

Dos `@RestControllerAdvice` con potenciales solapamientos de `@ExceptionHandler` generan comportamiento ambiguo.

**Acción:** Consolidar en un único manejador global de excepciones.

---

## 🟡 MEDIO — Mocks, funcionalidad incompleta y deuda técnica

### M-1. Módulos de negocio con lógica mock / placeholder (backend)
Funcionalidades anunciadas como completas pero implementadas con datos falsos o valores fijos:

| Archivo | Problema |
| :--- | :--- |
| `treasury/.../CashflowPredictorService.java:22-25` | `currentBalance`, `pendingReceivables`, `pendingPayables`, `fixedMonthlyCosts` todos fijados a `0` con comentario "Placeholder: leer desde BD". Predicción de flujo de caja **no real**. |
| `payroll/.../PayrollServiceImpl.java:77,134` | Generación Previred (archivo plano 105 columnas) marcada como "Mock"; cálculo "Dummy 4%". Nómina/Previred **no confiable**. |
| `period_close/.../DepreciationServiceImpl.java:15` | "Mock logic" en lugar de calcular depreciación real desde Activos Fijos. |
| `annual_close/.../F22DraftService.java:50` | "Mock PPM logic" en Operación Renta F22. |
| `sustainability/.../EsgReportService.java:13` | "Mock data for ESG Report (Scope 1, 2, 3)". |
| `sustainability/.../SustainabilityController.java` (varios) | `financialHealth = new BigDecimal("75.0")` hardcodeado (líneas 76/77/105). |
| `bankingsync/.../BankingSyncServiceImpl.java:46` | "Mock API Sync from Open Banking provider". |
| `dms/.../DmsServiceImpl.java:46` | "Mock AI Extraction Logic". |
| `dms/.../GeminiApiClient.java:22-27` | Modo `MOCK` por defecto (`@Value("${gemini.api.key:MOCK}")`). |
| `accounting/.../WeeklyReportService.java:54` | Genera "dummy bytes" en vez de PDF real (OpenPDF). |
| `energy/.../OcppCommandExtensionService.java:175` | Retorna "mock response". |
| `accounting/.../PeriodClosingService.java:148,155` | Cuentas `4101`/`5101` hardcodeadas con comentario "Placeholder: should be all". |

**Acción:** Priorizar la implementación real de Treasury, Payroll/Previred, Depreciación y F22 (impacto contable/legal directo).

### M-2. Componentes Mock auto-generados en el frontend
Existe el script `SII-ERP-AI-FRONT/create_mocks.py` que **genera automáticamente** componentes/servicios placeholder (`export const X = () => <div>...(Mock)</div>`) para imports faltantes. Actualmente hay **9 archivos con marcador `(Mock)`** aún presentes, concentrados en Tesorería:

- `treasury/components/CashFlowChart.tsx`, `ReconciliationInterface.tsx`, `BankStatementUpload.tsx`, `LiquidityKPIs.tsx`, `CashEntryModal.tsx`
- `accounting/components/AuditAlertsList.tsx`, `partner-ledger.tsx`
- `sustainability/components/TopGreenProviders.tsx`
- `ai-assistant/stores/chat.store.tsx`

**Acción:** Implementar los componentes reales y eliminar `create_mocks.py` del repo (es un parche que oculta imports rotos en vez de resolverlos).

### M-3. El build del frontend ignora errores de TypeScript y ESLint
`next.config.mjs`:
```js
eslint: { ignoreDuringBuilds: true },
typescript: { ignoreBuildErrors: true }
```
Se compila y despliega aunque haya errores de tipos y lint. Oculta bugs.

**Acción:** Poner ambos en `false`, corregir los errores existentes y mantener el gate activo.

### M-4. Dos archivos de configuración de Next.js coexistiendo
Existen `next.config.mjs` **y** `next.config.ts` en la raíz del front. Ambigüedad de cuál se aplica.

**Acción:** Dejar uno solo (preferiblemente `.ts`) y borrar el otro.

### M-5. Import roto en `Header.tsx`
`src/components/layout/Header.tsx:12`: `import Cookies from "../../../node_modules/@types/js-cookie";` importa desde una **ruta de node_modules/@types** (declaración de tipos), no desde el paquete. Es incorrecto y frágil.

**Acción:** `import Cookies from 'js-cookie';`

### M-6. CI no ejecuta tests ni escaneo de seguridad
- `backend-ci.yml`: `./gradlew build -x test` → **omite los tests** en ambos servicios.
- `frontend-ci.yml`: solo `npm run build` (que además ignora TS/ESLint). No corre `vitest` ni `eslint`.
- No hay análisis de dependencias (SCA) ni SAST en los pipelines.

**Acción:** Ejecutar tests en CI, añadir lint y un escáner de dependencias (Dependabot/Trivy/OWASP Dependency-Check).

### M-7. Cobertura de tests muy baja
**33 archivos de test** para **772 archivos de producción** en el backend (~4%). Módulos financieros críticos sin pruebas.

**Acción:** Priorizar tests en contabilidad, nómina, impuestos (F29/F22) e integración SII.

### M-8. Chaos Monkey habilitado por defecto
`SII-ERP-AI-BACK/.env`: `CHAOS_MONKEY_ENABLED=true`. Si este `.env` se usa en un entorno no-dev, inyecta latencia/fallos.

**Acción:** `false` por defecto; habilitar solo explícitamente en entornos de prueba.

### M-9. TODOs pendientes de riesgo funcional
- `accounting/.../ChargingEventListener.java:62,66,123` → costo de energía por kWh hardcodeado, `companyId` por defecto hardcodeado, y `// TODO: Enviar a Dead Letter Queue ... para no perder dinero` (pérdida potencial de asientos contables).
- `user/.../RoleRepositoryAdapter.java:30` → `entity.setPermissions("[]")` con `// TODO: map permissions properly`. Los permisos de roles **no se persisten**, reforzando el problema de autorización (C-4).
- `integration_sii/.../TokenRefreshScheduler.java:124` → `// TODO: load company-specific certificate from database` (multi-tenant de certificados incompleto).
- `accounting/.../PartnerLedgerListener.java:74` → `// TODO: Implementar actualización de cache/BD`.

---

## 🔵 BAJO — Mejoras y limpieza

### B-1. Anti-patrón: subclases anónimas de `DomainException`
17 ocurrencias del tipo `throw new DomainException("...") {}` (instanciar una clase abstracta con subclase anónima vacía). Ruido y mala práctica.
**Acción:** Hacer `DomainException` concreta o crear excepciones específicas (`UserNotFoundException`, `InvalidCredentialsException`, etc.).

### B-2. `docker-compose.yml` con credenciales y detalles menores
- Volumen duplicado en `postgres-vector` (`- postgres_data:/var/lib/postgresql/data` repetido dos veces).
- `MINIO_ROOT_USER: admin` / `MINIO_ROOT_PASSWORD: password123` como defaults.
- Grafana con `GF_AUTH_ANONYMOUS_ENABLED=true` + rol `Admin` y login deshabilitado (aceptable solo en local).
- Colisión de puerto `3000`: Grafana y el frontend Next.js usan el mismo puerto host.

### B-3. Defaults inseguros de MinIO en `application.properties`
`minio.access-key=${MINIO_ACCESS_KEY:admin}` / `minio.secret-key=${MINIO_SECRET_KEY:password123}`. Si no se inyectan variables, usa credenciales triviales.

### B-4. Inconsistencia de expiración JWT por defecto
`JwtTokenProvider` default `jwt.expiration:3600000` (1h) vs `application.properties` `86400000` (24h). Alinear y documentar la política de expiración (24h es larga para un ERP financiero).

### B-5. Doble declaración de `PasswordEncoder`
Coexisten el bean `BCryptPasswordEncoder` en `SecurityConfig`, más las clases `sso/infrastructure/security/PasswordEncoder.java` y `PasswordEncoderAdapter.java`. Consolidar para evitar confusión.

### B-6. `spring.main.allow-bean-definition-overriding=true`
Habilitar el override de beans globalmente suele ocultar conflictos de configuración. Revisar por qué es necesario y eliminarlo si se puede.

### B-7. Archivos históricos y certificados en `/Doc`
La carpeta `Doc/` (ignorada por git) contiene 156 archivos: 127 `.txt`, 25 `.log`, y **1 `.p12` + 1 `.b64`** (certificados/volcados). Aunque no se versiona, conviene depurar y asegurar que no contenga secretos reales de producción.

### B-8. Archivos grandes candidatos a refactor (God classes)
Archivos que superan las ~500-700 líneas y concentran demasiada responsabilidad:
- `energy/.../OcppMessageDispatcher.java` (~17.8 KB)
- `energy/.../OcppCommandService.java` (~17.4 KB)
- `invoicing/.../InvoiceImportController.java` (~16.1 KB)
- `energy/.../RevenueReportService.java`, `DemandForecastService.java`, `EnergyReportingController.java`
- `accounting/.../ChileanChartOfAccountsSeeder.java`, `ComplianceController.java`, `AccountingController.java`

**Acción:** Extraer responsabilidades (dispatcher → handlers, controllers gruesos → servicios).

### B-9. Módulo `energy` (OCPP/EV charging) desproporcionado
El módulo `energy` es, con diferencia, el más grande y complejo del backend (OCPP, smart charging, firmware, pricing) dentro de un ERP contable. Revisar si su alcance corresponde al producto o debería ser un servicio aparte (posible sobre-ingeniería / acoplamiento).

---

## ✅ Resumen priorizado (orden sugerido de atención)

1. **Rotar y externalizar todos los secretos** (C-1, C-2, B-3) y quitar `dummy.p12` de git (A-7).
2. **Actualizar Next.js** a >= 15.2.3 (C-5).
3. **Cerrar Actuator** y aplicar `@PreAuthorize` por rol en todos los controladores (C-3, C-4); persistir permisos de roles (M-9).
4. **Implementar revocación JWT + rate limiting + mensaje de login genérico + cookie httpOnly** (A-1, A-2, A-3, A-4, A-5).
5. **Añadir seguridad al B2B y alinear versiones de Spring Boot** (C-6).
6. **Sustituir mocks/placeholders de módulos financieros** (Treasury, Payroll/Previred, Depreciación, F22) y la extracción de RUT del certificado (M-1, A-8).
7. **Endurecer CI**: correr tests, lint y SCA; dejar de ignorar errores TS/ESLint (M-3, M-6, M-7).
8. **Refactor de errores** (RuntimeException → excepciones de dominio), consolidar `GlobalExceptionHandler` y `PasswordEncoder` (A-9, A-10, B-5).
9. **Eliminar mocks del frontend** y `create_mocks.py`; corregir configs duplicadas (M-2, M-4, M-5).
10. **Refactor de God classes** y revisión de alcance del módulo `energy` (B-8, B-9).

---

> _Nota: Este documento se basa en análisis estático del código y configuración. Se recomienda complementar con pruebas dinámicas (DAST), pentesting y revisión manual de los módulos financieros antes de producción._
