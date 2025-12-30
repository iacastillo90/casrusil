<div align="center">
  <img src="https://raw.githubusercontent.com/lucide-icons/lucide/main/icons/bar-chart-4.svg" width="100" height="100" alt="SII-ERP Logo"/>
  <h1>SII-ERP AI 🇨🇱</h1>
  <p><strong>El primer ERP Contable y Financiero potenciado por Inteligencia Artificial</strong></p>
  
  [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?logo=springboot&logoColor=white)](https://spring.io/)
  [![Next.js](https://img.shields.io/badge/Next.js-15.0-000000?logo=next.js&logoColor=white)](https://nextjs.org/)
  [![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16.0-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
  [![LangChain4j](https://img.shields.io/badge/AI-LangChain4j-FF9900?logo=openai&logoColor=white)](https://github.com/langchain4j/langchain4j)
</div>

---

## 🚀 ¿Qué es SII-ERP AI?

**SII-ERP** no es solo un sistema contable, es tu **Gerente Financiero Virtual**. Construido con una robusta Arquitectura Hexagonal y micro-módulos integrados por Eventos de Dominio, este ERP gestiona integralmente Inventario, Remuneraciones e Impuestos, auditando automáticamente las finanzas con Modelos de Lenguaje Grandes (LLMs).

## ✨ Características Principales

*   🧠 **Asistente IA Nativo (Gerente Financiero):** Auditoría automática de Cierres de Mes y chat interactivo conectado a la Contabilidad Central usando **OpenAI** o **Google Gemini**.
*   📦 **Inventario Inteligente (PMP):** Cálculo de Precio Medio Ponderado en tiempo real con bloqueo pesimista de concurrencia.
*   👥 **Remuneraciones y Previred:** Motor de cálculo de sueldos líquidos, descuentos legales chilenos (AFP, Fonasa) y generación de liquidaciones en formato PDF.
*   ⚖️ **Libro Mayor Event-Driven:** Cero acoplamiento. Movimientos de stock y sueldos se registran automáticamente en la contabilidad central mediante Spring Events.
*   📊 **Operación Renta F22:** Motor fiscal chileno. Calculadora de RLI e IDPC adaptado a tu Régimen Tributario.
*   🔒 **Multi-Tenant y Seguro:** Autenticación por **JWT**, preparado para Auth0, en un entorno seguro y aislado por `CompanyId`.

---

## 🏗️ Arquitectura del Sistema

El ecosistema está dividido en dos grandes proyectos aislados, orquestados bajo un mismo entorno productivo:

| Componente | Tecnología Principal | Descripción |
| :--- | :--- | :--- |
| **Backend** | Spring Boot, Java 21, JPA | Arquitectura Limpia/Hexagonal. Expone una API REST blindada con JWT. |
| **Frontend** | Next.js, Tailwind, Shadcn UI | Dashboard premium React. Consumo de API asíncrono y descarga de reportes. |
| **Base de Datos** | PostgreSQL + PgVector | Almacenamiento relacional y memoria vectorial para la Inteligencia Artificial. |
| **Caché** | Redis | Gestión rápida de sesiones y caché de tokens distribuidos. |

---

## 🛠️ Levantando el Entorno (Quickstart)

¡Poner a correr el Ferrari es cuestión de 2 pasos gracias a Docker!

### 1. Variables de Entorno
Asegúrate de configurar tus credenciales en el archivo `.env` en la raíz del proyecto.
```env
# Ejemplo de .env
POSTGRES_USER=postgres
POSTGRES_PASSWORD=tu_password_seguro
JWT_SECRET=tu_jwt_secret
OPENAI_API_KEY=sk-proj-...
```

### 2. Levantar la Infraestructura
En la raíz del proyecto, ejecuta Docker Compose para levantar la base de datos (con PgVector) y Redis:

```bash
docker-compose up -d sii-db sii-redis
```
*(Opcional: Si deseas que Docker también compile y levante el Backend, ejecuta `docker-compose up -d`).*

### 3. Iniciar el Desarrollo Local
**Backend (Spring Boot):**
```bash
cd SII-ERP-AI-BACK
./gradlew bootRun
```
*La API estará disponible en: `http://localhost:8080/api/v1`*

**Frontend (Next.js):**
```bash
cd SII-ERP-AI-FRONT
npm install
npm run dev
```
*El Dashboard interactivo estará en: `http://localhost:3000`*

---

## 📚 Documentación Técnica

Los logs históricos, archivos de volcado, certificados P12 base y diagramas arquitectónicos antiguos han sido archivados de forma segura en la carpeta `/Doc`. (Nota: Esta carpeta está explícitamente ignorada por Git por motivos de seguridad corporativa).

---
<div align="center">
  <i>Desarrollado con ❤️ y Arquitectura Limpia para revolucionar la contabilidad.</i>
</div>
