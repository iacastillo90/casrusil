# 🔐 SECURITY.md — Gestión de secretos y rotación

Este documento cubre la **Fase 1** de [`plan-produccion.md`](./plan-produccion.md): externalización y rotación de secretos.

## ⚠️ Acción inmediata requerida: rotar claves comprometidas

Las siguientes credenciales estuvieron en archivos `.env` en texto plano y deben considerarse **comprometidas**. Deben rotarse **cuanto antes** (esto solo puede hacerlo el propietario de cada cuenta):

| Secreto | Dónde estaba | Acción |
| :--- | :--- | :--- |
| **Google Gemini API Key** (`AIzaSy...ZDYU`) | `SII-ERP-AI-BACK/.env` y `SII-ERP-AI-FRONT/.env` | Revocar en [Google AI Studio](https://aistudio.google.com/app/apikey) / Google Cloud y generar una nueva. |
| **API key usada como `MAIL_PASSWORD`** (`AIzaSy...Mu0AQ`) | `SII-ERP-AI-BACK/.env` | Revocarla. Configurar una **App Password** real del proveedor de correo (no una API key). |
| **`POSTGRES_PASSWORD=admin`** | `SII-ERP-AI-BACK/.env` | Cambiar por una contraseña fuerte y única. |
| **`POSTGRES_PASSWORD` / `REDIS_PASSWORD` "de prod"** | raíz `.env` | Rotar; nunca reutilizar entre entornos. |
| **`JWT_SECRET`** | raíz `.env` (placeholder) | Generar uno nuevo: `openssl rand -hex 64`. |

> Rotar una clave la **invalida**, por lo que las copias filtradas dejan de servir. Hazlo aunque el `.env` no esté en git.

## ✅ Cambios ya aplicados en el repositorio

- **`.gitignore`** endurecido: se ignoran `*.p12`, `*.pfx`, `*.jks`, `*.keystore`, `*.key`, `*.b64` (con excepción del `dummy.p12` de tests) y se preservan los `.env.example`.
- **Plantillas `.env.example`** creadas para raíz, backend, frontend y B2B (sin secretos, con placeholders).
- **Escaneo de secretos** en CI con **gitleaks** (`.github/workflows/security-scan.yml` + `.gitleaks.toml`). El pipeline falla si detecta un secreto.
- **`.env` del frontend** deduplicado (`NEXT_PUBLIC_API_URL` estaba dos veces).

## 📋 Buenas prácticas a partir de ahora

1. **Nunca** poner secretos reales en el repo ni en `.env` versionables. Usar `.env.example` como plantilla.
2. En **producción**, inyectar secretos desde un **gestor** (AWS Secrets Manager / SSM / HashiCorp Vault) como variables de entorno en runtime.
3. **Una clave por entorno** (dev/staging/prod) y por servicio. No reutilizar la misma clave en frontend y backend.
4. Las llamadas a IA (Gemini/OpenAI) deben ir **desde el backend**; el frontend no debe portar claves de IA.
5. Los **certificados SII** (`.p12`) se cargan por ruta externa (`SII_CERTIFICATE_PATH=file:/...`), nunca desde `classpath` ni versionados.

## 🧪 Ejecutar el escáner localmente (opcional)

```bash
# Requiere gitleaks instalado (https://github.com/gitleaks/gitleaks)
gitleaks detect --config .gitleaks.toml --verbose
```

## 📣 Reporte de vulnerabilidades

Si encuentras una vulnerabilidad, no abras un issue público: contacta al equipo de seguridad por el canal interno definido por la organización.
