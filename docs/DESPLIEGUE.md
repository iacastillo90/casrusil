# Despliegue en producción — variables de entorno

Inventario de **todo** lo que necesita el sistema para arrancar y operar, extraído
de `SII-ERP-AI-BACK/src/main/resources/application*.properties`, `next.config.mjs`
y `.env.example`.

Convención de esta tabla:

- **Obligatoria**: si falta, el arranque falla (fail-fast) o la funcionalidad queda inoperante.
- **Default**: existe un valor por defecto en el código; el sistema arranca igual.

> Regla de oro: en `application-prod.properties` las variables del SII y
> `APP_CRYPTO_KEY` están declaradas **sin** valor por defecto. Si no se definen, el
> backend no arranca. Es intencional: evita emitir contra el SII con configuración
> de certificación o cifrar certificados con una clave de desarrollo.

---

## 1. Backend — base de datos y caché

| Variable | Obligatoria | Para qué | Ejemplo | Si falta |
|---|---|---|---|---|
| `POSTGRES_HOST` | **Sí** | Host de PostgreSQL | `db.internal` | **No arranca** (prod sin default) |
| `POSTGRES_PORT` | Sí | Puerto de PostgreSQL | `5432` | Default `5432` en prod |
| `POSTGRES_DB` | Sí | Nombre de la base | `sii_erp` | Default `sii_erp_ai` en prod |
| `POSTGRES_USER` | **Sí** | Usuario | `sii_app` | **No arranca** (prod sin default) |
| `POSTGRES_PASSWORD` | **Sí** | Contraseña | `__CAMBIAR__` | **No arranca** (prod sin default) |
| `REDIS_PASSWORD` | Según infraestructura | Contraseña de Redis (compose/staging) | `__CAMBIAR__` | Redis sin auth o conexión rechazada |

## 2. Backend — seguridad y sesión

| Variable | Obligatoria | Para qué | Ejemplo | Si falta |
|---|---|---|---|---|
| `JWT_SECRET` | **Sí** | Firma de los JWT de sesión | `openssl rand -base64 48` | **No arranca** |
| `JWT_EXPIRATION` | No | Vida del token (ms) | `86400000` | Default 86400000 (24 h) |
| `SECURITY_COOKIE_SECURE` | No | Cookie sólo por HTTPS | `true` (prod) | Default `false` en base, `true` en prod |
| `SECURITY_COOKIE_SAME_SITE` | No | Política SameSite | `Strict` | Default `Strict` |
| `SECURITY_LOGIN_MAX_ATTEMPTS` | No | Intentos de login antes de bloquear | `5` | Default 5 |
| `SECURITY_LOGIN_WINDOW_MINUTES` | No | Ventana del rate limit | `15` | Default 15 |
| `SECURITY_REDIS_FAIL_CLOSED` | No | Sin Redis, logout y login responden 503 en vez de seguir sin blacklist ni rate limit (B04/B05) | `true` | `true` en prod, `false` en dev |
| `APP_FEATURES_ENERGY_ADMIN_ENABLED` | No | Endpoints de energía sin `company_id` (reportes, dashboard, mantención, precios, catálogo) | `false` | Apagados (404) en todos los perfiles hasta la ronda P6 |
| `APP_FEATURES_LEGACY_USER_ADMIN_ENABLED` | No | Módulo `/users`, `/roles`, `/organizations` (esquema `public`, sin `company_id`) | `false` | Apagados (404) en todos los perfiles; el login no depende de él |
| `APP_CRYPTO_KEY` | **Sí** | Clave AES-GCM para cifrar certificados `.p12` y sus contraseñas | `openssl rand -base64 32` | **No arranca** (`SecretCipher` falla si el perfil es `prod` y la clave es la de desarrollo) |

### Generar `APP_CRYPTO_KEY`

```bash
openssl rand -base64 32
```

- Guárdala en el gestor de secretos; **nunca** en el repositorio ni en logs.
- **Rotarla obliga a recargar los certificados**: los `.p12` y sus contraseñas están
  cifrados con la clave anterior. Si cambias la clave sin volver a subir los
  certificados, `CompanyCertificateService` no podrá descifrarlos y la emisión
  fallará hasta que cada empresa cargue de nuevo su certificado por onboarding.

## 3. Backend — SII

| Variable | Obligatoria | Para qué | Ejemplo | Si falta |
|---|---|---|---|---|
| `SII_AMBIENTE` | **Sí** (prod) | Declara el ambiente (`certificacion`/`produccion`) | `produccion` | **No arranca** en prod |
| `SII_URL_AUTH` | **Sí** (prod) | Semilla de autenticación (CrSeed) | `https://palena.sii.cl/DTEWS` | **No arranca** en prod |
| `SII_URL_UPLOAD` | **Sí** (prod) | Subida de envíos DTE | `https://palena.sii.cl/cgi_dte/UPL/DTEUpload` | **No arranca** en prod |
| `SII_URL_RCV` | **Sí** (prod) | Consulta RCV | `https://www4.sii.cl/consulfaliinternetws/reporte/` | **No arranca** en prod |
| `SII_URL_QUERY_EST_UP` | **Sí** (prod) | Estado de envío por TrackID | `https://palena.sii.cl/DTEWS/QueryEstUp.jws` | **No arranca** en prod |
| `SII_URL_QUERY_EST_DTE` | **Sí** (prod) | Estado de un DTE | `https://palena.sii.cl/DTEWS/QueryEstDte.jws` | **No arranca** en prod |
| `SII_CERTIFICATE_PATH` | **Sí** | Certificado por defecto (fallback si la empresa no cargó uno) | `/etc/sii/certificado.p12` | **No arranca** en prod |
| `SII_CERTIFICATE_PASSWORD` | **Sí** | Contraseña de ese certificado | `__CAMBIAR__` | **No arranca** |
| `SII_CAF_UMBRAL_AGOTAMIENTO` | No | Folios restantes para marcar `POR_AGOTARSE` | `10` | Default 10 |
| `SII_STATUS_POLL_ENABLED` | No | Activa el job de estados | `true` | Default `true` |
| `SII_STATUS_POLL_INTERVAL_MS` | No | Intervalo del job | `1800000` (30 min) | Default 30 min |
| `SII_STATUS_POLL_INITIAL_DELAY_MS` | No | Espera inicial del job | `60000` | Default 1 min |
| `SII_STATUS_MAX_ATTEMPTS` | No | Reintentos por factura | `5` | Default 5 |
| `SII_ESTADO_ACEPTADOS` | **Sí** (prod) | Códigos SII que significan aceptado | `EPR,DOK` | **No arranca** en prod |
| `SII_ESTADO_ACEPTADOS_REPAROS` | No | Códigos de aceptado con reparos | vacío | Default vacío |
| `SII_ESTADO_RECHAZADOS` | **Sí** (prod) | Códigos que significan rechazo | `RCT,RFT,RSC,RCI,RCH,RFR,RTC,RCO,FAU` | **No arranca** en prod |
| `SII_ESTADO_ENVIO_PROCESADO` | **Sí** (prod) | Código de envío procesado | `EPR` | **No arranca** en prod |

> En certificación (maullin) el backend trae los defaults apuntando a
> `https://maullin.sii.cl/...`. En producción **hay que declarar explícitamente**
> los endpoints de palena/www4.

## 4. Backend — integraciones

| Variable | Obligatoria | Para qué | Ejemplo | Si falta |
|---|---|---|---|---|
| `MINIO_URL` | No | Endpoint S3/MinIO | `https://s3.internal` | Default `http://localhost:9000` |
| `MINIO_ACCESS_KEY` | **Sí** (prod) | Usuario S3 | `sii-app` | **No arranca** en prod; el compose de MinIO también exige el valor |
| `MINIO_SECRET_KEY` | **Sí** (prod) | Secreto S3 | `__CAMBIAR__` | **No arranca** en prod; el compose de MinIO también exige el valor |
| `MINIO_BUCKET` | No | Bucket de documentos | `sii-erp-documents` | Default `sii-erp-documents` |
| `MAIL_HOST` | No | SMTP | `smtp.gmail.com` | Default `smtp.gmail.com` |
| `MAIL_PORT` | No | Puerto SMTP | `587` | Default `587` |
| `MAIL_USERNAME` | **Sí** para enviar correo | Usuario SMTP | `notificaciones@empresa.cl` | Arranca, pero el despacho por email falla |
| `MAIL_PASSWORD` | **Sí** para enviar correo | Contraseña SMTP | `__CAMBIAR__` | Ídem |
| `OTLP_TRACING_ENDPOINT` | **Sí** | Endpoint OTLP de trazas | `https://otlp.internal/v1/traces` | **No arranca** |
| `TRACING_SAMPLING_PROBABILITY` | **Sí** | Muestreo de trazas (0–1) | `0.1` | **No arranca** |
| `OPENAI_API_KEY` | **Sí** | Asistente IA | `sk-...` | **No arranca** |
| `GEMINI_API_KEY` | **Sí** | Modelo Gemini | `__CAMBIAR__` | **No arranca** |
| `GEMINI_MODEL_NAME` | **Sí** | Nombre del modelo | `models/gemini-2.5-flash` | **No arranca** |
| `XAI_API_KEY` | **Sí** | Integración xAI | `__CAMBIAR__` | **No arranca** |
| `PGVECTOR_DIMENSION` | **Sí** | Dimensión de embeddings | `1536` | **No arranca** |
| `CHAOS_MONKEY_ENABLED` | **Sí** | Flag de caos | `false` | **No arranca** |
| `RESILIENCE4J_*` | No | Umbrales de circuit breaker | ver properties | Defaults del código |

## 4.1 Migraciones de base de datos (Flyway)

El backend ejecuta Flyway al arrancar (`spring.flyway.enabled=true`). Las migraciones viven en `SII-ERP-AI-BACK/src/main/resources/db/migration`:

- `V1__esquema_base.sql`: DDL del esquema (sin datos).
- `V1.1__compatibilidad_bases_previas.sql`: tablas que faltaban en bases creadas antes de Flyway.
- `V2__datos_maestros.sql`: plan de cuentas detallado, parámetros tributarios y datos maestros.
- `V3__unificar_plan_cuentas.sql`: reasigna movimientos de cuentas canónicas al plan detallado.
- `V4__pay_periods_varchar.sql`: convierte `pay_periods.period` de `bytea` a `varchar(7)`.

El único plan de cuentas válido es el **detallado de 6 dígitos** (por ejemplo
`110101` Caja, `4201` Otros Ingresos, `5204` Servicios Básicos). V3 reasigna los
movimientos de los códigos canónicos con puntos (`1.1.01.001` → `110101`, etc.)
y elimina la cuenta canónica sólo si queda sin movimientos. Los defaults
`app.accounts.*` apuntan a los códigos detallados; el fallback al plan canónico
se conserva temporalmente para bases que aún no hayan migrado.

`spring.sql.init.mode=never` y `ddl-auto=validate` en main/dev/prod. En test se mantiene `ddl-auto=create-drop` y `spring.flyway.enabled=false` para que Hibernate cree el esquema efímero de Testcontainers.

### Base nueva

1. Crear una base PostgreSQL 15 vacía.
2. Configurar `POSTGRES_*` y arrancar el backend.
3. Flyway aplica V1, V1.1, V2, V3 y V4; verificar:

```sql
select version, description, success from public.flyway_schema_history order by installed_rank;
```

### Base existente (creada antes de Flyway)

1. Hacer backup (`pg_dump`) y clonar la base.
2. Arrancar el backend con `spring.flyway.baseline-on-migrate=true` y `baseline-version=1` (ya son los defaults).
3. Flyway crea `flyway_schema_history` con baseline 1 y aplica V1.1–V4; no recrea tablas existentes.
4. Verificar que la historia tenga baseline + migraciones y que `pay_periods.period` sea `varchar(7)`.

Si una base existente ya tenía datos maestros, V2 es idempotente (`ON CONFLICT DO NOTHING/UPDATE`). No ejecutar `flyway clean` en producción.

## 5. Frontend

| Variable | Obligatoria | Para qué | Ejemplo | Si falta |
|---|---|---|---|---|
| `BACKEND_INTERNAL_URL` | **Sí** (sólo servidor) | Destino del proxy same-origin `/api/v1` | `http://backend:8080` | Default `http://localhost:8092` (no sirve en prod) |
| `NEXT_PUBLIC_API_URL` | No | Base de las llamadas del navegador | `/api/v1` | Default `/api/v1` (recomendado: dejar relativa para la cookie httpOnly) |
| `NEXT_PUBLIC_SII_ENV` | No | Ambiente mostrado por `EnvironmentBanner` | `produccion` | Default `certificacion` (el banner se muestra) |

> `NEXT_PUBLIC_SII_ENV` es la fuente de respaldo del banner. El backend todavía **no
> expone** `ambiente` en `GET /api/v1/sii/status`; cuando lo haga, el banner usará ese
> valor y esta variable pasará a ser opcional.

## 6. Checklist de despliegue

1. Definir todas las variables marcadas **Sí** en el gestor de secretos.
2. Generar `APP_CRYPTO_KEY` y guardarla (ver advertencia de rotación).
3. Verificar que `SII_AMBIENTE=produccion` y los endpoints apunten a palena/www4.
4. `NEXT_PUBLIC_SII_ENV=produccion` para que el banner de certificación no aparezca.
5. `BACKEND_INTERNAL_URL` apuntando al servicio interno del backend.
6. Confirmar `SECURITY_COOKIE_SECURE=true` y HTTPS en el front.
7. Tras el despliegue, validar `GET /actuator/health` (público) y que
   `GET /actuator/metrics` responda **200 sólo a ADMIN**.
8. Los `docker-compose*.yml` ya no traen contraseñas por defecto: `DB_PASSWORD`,
   `JWT_SECRET`, `MINIO_ACCESS_KEY` y `MINIO_SECRET_KEY` deben venir del entorno o
   `docker compose` se niega a arrancar.
9. El login y el registro **no devuelven el JWT en el body**: viaja solo en la cookie
   `access_token` (HttpOnly). Clientes de API que necesiten Bearer deben leerlo del
   `Set-Cookie`.
10. Redis es obligatorio en producción: si cae, login, registro y logout responden 503
    hasta que vuelva (fail-closed).
