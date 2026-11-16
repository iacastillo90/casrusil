# Plan de rediseño — SII-ERP-AI

> Fase 2 del rediseño del frontend. Base: `Doc/DESIGN.md`, `RIESGOS.md`, `UI-DESIGN.md`, `MAPA-FUNCIONAL.md`, más la revisión del código en el commit `9593ef8` (issue #1 del repositorio).
> Lo marcado **Propuesta nueva** no existe hoy ni en el front ni en el backend. Lo marcado **DECISIÓN PENDIENTE** necesita una respuesta antes de implementar.
> Fecha: 15-11-2026. Actualizado el mismo día con las decisiones D-01 (verde petróleo) y PEN-03 (documentos actualizados en `Doc/rediseno/`).

---

## 1. Diagnóstico

**Design system.** Shadcn UI tema "slate" sin personalizar, Inter e íconos Lucide. No hay marca: el primario es casi negro (`#0f172a`) y se invierte a casi blanco en modo oscuro; el índigo que se ve en la app (16 usos de `text-indigo-600`) no es un token. No existen tokens de éxito, advertencia ni información, así que esos estados se pintan con clases sueltas (tres verdes distintos, rojos, ámbar) que no cumplen AA: `text-green-600` 3.3:1, `text-red-500` 3.76:1, ámbar sobre ámbar claro 3.07:1, botón destructivo 3.6:1. Las paletas de gráficos de claro y oscuro no se corresponden. Button no tiene estado de carga e Input no tiene estado de error.

**Estados del documento tributario.** `StatusBadge` conoce 5 estados; Firmado y Enviado se ven idénticos; Aceptado tiene 2.2:1 en modo claro; no hay íconos; faltan Anulado, Aceptado con reparos y En proceso.

**Navegación.** Menú plano de 17 entradas y 29 enlaces, con dos grupos llamados "Ventas" y dos ítems que comparten ícono, igual para todos los roles (el front conoce 3 de 9). Colapsado muestra abreviaturas de dos letras. El estado activo solo se marca en la ruta exacta. 8 pantallas no tienen acceso desde la interfaz: `/fees`, `/banking`, `/sii/audit`, `/compliance`, `/fleet`, `/sustainability`, `/energy`, `/settings/company`.

**Pantallas (43).** Conectadas 18 · parciales 11 · con fallas 6 · rotas 6 · simulada 1 · redirección 1. El flujo central no funciona de punta a punta: crear factura envía un payload que el backend rechaza, el detalle de factura llama a tres endpoints inexistentes y no hay endpoint para listar CAF. El front hace 25 llamadas activas a endpoints que no existen.

**Riesgos que condicionan el diseño.** El folio lo escribe el usuario (R-02). No hay consulta de estado por TrackID (R-09), de modo que Aceptado, Reparos y Rechazado no pueden mostrarse con datos reales. El asiento se crea antes de la aceptación del SII (R-11). El ambiente SII apunta a producción por defecto (R-05). El F29 usa parámetros de 2024 (R-07). El portal del empleado exige ADMIN o HR. La IA no tiene restricción de rol y envía datos tributarios a un proveedor externo (R-13).

**Madurez.** Nivel 2 de 5 ("emergente"): hay estilos compartidos y una librería de componentes base, pero sin capa semántica, sin gobernanza y con adopción parcial. Meta de este plan: nivel 3.

---

## 2. Principios de diseño

Usuarios: contadores y personal administrativo de pymes chilenas. Trabajan varias horas seguidas en la aplicación, sobre tablas y montos, con plazos tributarios fijos. Son usuarios intermedios permanentes: conocen la materia, no necesariamente el software.

> Estos principios se apoyan en los documentos y en heurísticas de usabilidad, no en investigación con usuarios. Ver PEN-08.

| # | Principio | Qué significa en la práctica |
|---|-----------|------------------------------|
| 1 | **El estado tributario siempre a la vista** | Todo documento muestra su estado con color, ícono y texto. El ambiente SII (certificación o producción), los folios disponibles y el vencimiento del certificado son visibles sin buscarlos. Nunca se muestra "Aceptado" si el sistema no lo sabe. |
| 2 | **Densidad con orden** | Tablas compactas, montos alineados a la derecha con cifras tabulares, totales fijos, filtros persistentes. Es una aplicación de uso sostenido: se prioriza la lectura rápida sobre el espacio en blanco. |
| 3 | **Prevenir antes que corregir** | El sistema calcula lo que puede calcular (folio, IVA, totales, dígito verificador). Las acciones irreversibles (firmar, enviar al SII, cerrar período, declarar F29) piden confirmación con el efecto descrito en concreto. |
| 4 | **Cada rol ve su trabajo** | El menú muestra solo las áreas que el rol puede usar y los módulos que la empresa activó. Nadie llega a una pantalla para recibir un error de permiso. |
| 5 | **Lenguaje del contador chileno** | Términos del SII y de la práctica contable local: folio, CAF, DTE, F29, RCV, boleta de honorarios. RUT con puntos y guion, montos `$1.190.000`, fechas `dd-mm-aaaa`. Sin anglicismos ("Compliance Center") ni emojis en la navegación. |
| 6 | **Honestidad sobre lo que funciona** | Lo simulado o incompleto no se disfraza. Si un dato no está disponible se dice por qué y qué hacer. |
| 7 | **La IA asiste, no decide** | Las sugerencias de la IA se identifican como tales, muestran en qué se basan y requieren confirmación humana antes de afectar libros o declaraciones. |
| 8 | **Accesible por defecto** | Contraste AA, foco visible, navegación completa por teclado, estados que no dependen solo del color. |

---

## 3. Arquitectura de información

### 3.1 Menú agrupado

Diez grupos plegables con ícono y texto, más Inicio y Configuración. Colapsado muestra solo íconos de grupo con tooltip y un submenú flotante (se eliminan las abreviaturas de dos letras). El grupo y el ítem activos se marcan por prefijo de ruta, no por ruta exacta. Sobre el contenido van breadcrumbs.

| Grupo | Ítems | Rutas actuales | Notas |
|-------|-------|----------------|-------|
| **Inicio** | Panel | `/dashboard` | Contenido según rol |
| **Ventas** | Facturas y documentos · Cotizaciones · Oportunidades | `/invoices`, `/invoices/new`, `/invoices/[id]`, `/crm/quotes`, `/crm/leads` | "Pipeline (Leads)" pasa a "Oportunidades" |
| **Compras y gastos** | Órdenes de compra · Boletas de honorarios | `/procurement`, `/fees` | Honorarios deja de estar huérfana |
| **Tributario (SII)** | Salud tributaria · Formulario 29 · Folios (CAF) · Sincronización SII | `/compliance`, `/accounting/f29`, `/sii/caf`, `/sii` | El F29 se mueve aquí desde Contabilidad |
| **Contabilidad** | Libro diario · Libro mayor y plan de cuentas · Balance · Estado de resultados · Cuentas corrientes · Activos fijos · Cierres | `/accounting/ledger`, `/accounting/balance`, `/accounting/income`, `/accounting/partners`, `/accounting/assets`, `/accounting/close`, `/accounting/annual-close` | Libro mayor y plan de cuentas: backend sin pantalla (MAPA §6) |
| **Tesorería y bancos** | Flujo de caja · Bancos · Conciliación bancaria | `/treasury`, `/banking`, `/banking/reconciliation` | Bancos es el paso previo a conciliar |
| **Personas** | Remuneraciones | `/payroll` | "Mi portal" sale del menú interno (ver 3.5) |
| **Operaciones** | Inventario · Centros de costo | `/inventory`, `/projects` | Centros de costo sale de Contabilidad |
| **Auditoría y control** | Auditoría · Reglas de clasificación | `/accounting/audit`, `/sii/audit` | Unifica las tres auditorías (ver 3.4). Reglas: backend sin pantalla |
| **Documentos y reportes** | Gestor documental · Reportes | `/documents`, `/reports` | |
| **Módulos** (según rubro) | Punto de venta · Pedidos B2B · Bodega (WMS) · Producción (MRP) · Flota · Energía · Sostenibilidad | `/pos`, `/portal`, `/wms`, `/mrp`, `/fleet`, `/energy`, `/sustainability` | Solo los activados por la empresa |
| **Configuración** | Empresa · Usuarios y roles · Certificado digital · Módulos · Notificaciones | `/settings`, `/settings/company` | Una pantalla con pestañas |

Fuera del menú: el **Asistente IA** queda en el header, disponible en todas las pantallas. Login, registro y onboarding mantienen su layout propio.

Las 43 pantallas quedan cubiertas: 37 en el menú, en Configuración o en el menú de usuario, 2 por enlace desde Facturas, 4 de acceso y raíz.

### 3.2 Matriz rol × área

Verificada contra los `@PreAuthorize` de los 51 controllers del commit `9593ef8`. ● acceso completo · ◐ acceso parcial · — sin acceso.

| Área | ADMIN | ACCOUNTANT | TREASURER | SALES | OPERATIONS | HR | AUDITOR | USER | B2B_CLIENT |
|------|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|
| Inicio (panel) | ● | ● | ◐ salud financiera | ◐ ventas | — ¹ | — ¹ | — ¹ | — | — |
| Ventas: facturas | ● | ● | — | ● | — | — | — | — | — |
| Ventas: cotizaciones y oportunidades | ● | — | — | ● | — | — | — | — | — |
| Compras: órdenes de compra | ● | — | — | — | ● | — | — | — | — |
| Compras: honorarios | ● | ● | — | — | — | — | — | — | — |
| Tributario (SII) | ● | ● | — | — | — | — | — | — | — |
| Contabilidad | ● | ● | — | — | — | — | ◐ reportes | — | — |
| Tesorería (flujo de caja) | ● | ● | ● | — | — | — | — | — | — |
| Bancos y conciliación | ● | — ² | ● | — | — | — | — | — | — |
| Personas: remuneraciones | ● | — | — | — | — | ● | — | — | — |
| Operaciones: inventario, centros de costo | ● | — ³ | — | — | ● | — | — | — | — |
| Auditoría y control | ● | ◐ espejo SII | — | — | — | — | ● ⁶ | — | — |
| Documentos | ● | ● | — | — | — | — | — | — | — |
| Reportes | ● | ● | — | — | — | — | ● | — | — |
| Módulo POS | ● | — | — | ● | — | — | — | — | — |
| Módulos WMS, MRP, Flota | ● | — | — | — | ● | — | — | — | — |
| Módulo Energía | ● | — | — | — | ● | — | ◐ reportes | — | — |
| Módulo Sostenibilidad | ● | — | — | — | ● | — | ● | — | — |
| Configuración | ● | — | — | — | — | — | — | — | — |
| Portal del empleado | ● | — | — | — | — | ● | — | — ⁴ | — |
| Portal B2B | ○ ⁵ | ○ | ○ | ○ | ○ | ○ | ○ | ○ | ○ |
| Asistente IA | ○ ⁵ | ○ | ○ | ○ | ○ | ○ | ○ | ○ | ○ |

¹ El panel actual solo tiene datos para ADMIN, ACCOUNTANT, SALES y TREASURER. **Propuesta nueva:** un panel de inicio por rol (operaciones, personas, auditoría).
² El contador no puede conciliar (`BankingSyncController`: ADMIN, TREASURER). El mapa indica que debería poder. **DECISIÓN PENDIENTE D-06.**
³ El contador no accede a centros de costo (`ProjectController`: ADMIN, OPERATIONS). **DECISIÓN PENDIENTE D-06.**
⁴ `PayrollController` exige ADMIN o HR a nivel de clase: el empleado (USER) no puede ver sus liquidaciones. Bloqueo B-07.
⁶ Hoy la pantalla "Auditoría Tributaria" usa `GET /accounting/f29` (ADMIN, ACCOUNTANT), así que el auditor no puede usarla. Debe construirse sobre `/audit/*`, `/sii/audit/report` y `/accounting/audit/sii-mirror`, que sí lo admiten. El panel de salud tributaria (`ComplianceController.java:80`) también excluye al auditor a nivel de método.
⁵ ○ = sin restricción de rol en el backend (cualquier usuario autenticado). Debe restringirse: Portal B2B solo a B2B_CLIENT y ADMIN; IA según R-13. Bloqueo B-08.

Regla de diseño: el menú se construye con esta matriz. Un usuario con varios roles ve la unión. Si un rol no tiene ninguna área (hoy USER), entra directo a su portal.

### 3.3 Módulos activables por rubro

| Módulo | Rutas | Para quién | Estado real |
|--------|-------|-----------|-------------|
| Punto de venta | `/pos` | Comercio minorista | Con fallas |
| Pedidos B2B | `/portal` + vista interna de pedidos (**Propuesta nueva**) | Mayoristas, distribuidores | Portal del cliente conectado |
| Bodega (WMS) | `/wms` | Empresas con bodega | Parcial |
| Producción (MRP) | `/mrp` | Manufactura | Parcial |
| Flota | `/fleet` | Transporte, reparto | Conectada (un endpoint) |
| Energía | `/energy` | Redes de carga eléctrica | Rota; 70 endpoints sin uso |
| Sostenibilidad | `/sustainability` | Reportes ESG, Corfo | Conectada |

Comportamiento: se activan en **Configuración › Módulos** (solo ADMIN) y se sugieren en el onboarding según la industria declarada. Un módulo desactivado no aparece en el menú ni en el panel; su URL muestra una pantalla "Módulo no activado" con enlace a Configuración para el ADMIN. **Propuesta nueva:** no existe hoy un campo de módulos por empresa en el backend (bloqueo B-10); mientras tanto puede guardarse como preferencia en el front.

### 3.4 Duplicados

| Duplicado | Hoy | Propuesta |
|-----------|-----|-----------|
| **Conciliación** | Pestaña en `/treasury` y pantalla `/banking/reconciliation` | Una sola pantalla: `/banking/reconciliation`. En Tesorería queda un resumen con enlace. |
| **Auditorías** | `/accounting/audit` y `/sii/audit` (mismo componente), alertas `/audit/*` sin pantalla, espejo SII en Compliance | Un área **Auditoría** con pestañas: Conciliación SII–ERP · Alertas y duplicados · Reporte. `/sii/audit` redirige. |
| **Gestión de usuarios** | `/sso/users` (en uso, sin eliminar ni cambiar rol) y `/users` + `/roles` + `/organizations` (completo, sin pantalla) | Diseñar sobre `/users` + `/roles` por ser el más completo. **DECISIÓN PENDIENTE D-04.** |
| **Cierres** | `/accounting/close` (mensual) y `/accounting/annual-close` (anual) | Una entrada **Cierres** con pestañas Mensual y Anual (F22), mismo patrón de stepper. |
| **Perfil de empresa** | `/settings` y `/settings/company` | Pestaña Empresa dentro de Configuración. `/settings/company` redirige. |
| **Estado de resultados** | Dos endpoints en el backend | El front ya usa `/reports/income-statement`. Retirar el otro (decisión de backend). |

### 3.5 Portales con layout propio

- **Portal del empleado** (`/employee`): sin sidebar interno. Encabezado simple, liquidaciones, datos personales. Para el rol USER es su inicio. Para HR y ADMIN queda accesible desde el menú de usuario como "Ver mi portal".
- **Portal del cliente B2B** (`/portal`): ya tiene layout propio. Sale del menú interno; el personal interno gestiona los pedidos desde el módulo Pedidos B2B.

---

## 4. Design tokens v2

Arquitectura de tres niveles: **primitivos** (paleta), **semánticos** (propósito, con valor por tema) y **de componente** (solo donde un componente necesita desviarse). Los componentes nunca usan primitivos ni clases de color sueltas.

### 4.1 Tokens semánticos nuevos

Cada estado tiene cuatro tokens: `--{estado}` (texto e íconos sobre fondo de página), `--{estado}-solid` con `--{estado}-solid-foreground` (botones y badges llenos) y `--{estado}-subtle` con `--{estado}-subtle-foreground` (badges suaves, alertas, filas). Ratios calculados con la fórmula WCAG 2.x; mínimo 4.5:1.

**Modo claro** (fondo `#ffffff`)

| Token | Valor | Sobre | Ratio |
|-------|-------|-------|:-----:|
| `--success` | `#15803d` | `#ffffff` | 5.02 |
| `--success-solid` / `-foreground` | `#15803d` / `#ffffff` | — | 5.02 |
| `--success-subtle` / `-foreground` | `#dcfce7` / `#166534` | — | 6.49 |
| `--warning` | `#b45309` | `#ffffff` | 5.02 |
| `--warning-solid` / `-foreground` | `#b45309` / `#ffffff` | — | 5.02 |
| `--warning-subtle` / `-foreground` | `#fef3c7` / `#92400e` | — | 6.37 |
| `--info` | `#1d4ed8` | `#ffffff` | 6.70 |
| `--info-solid` / `-foreground` | `#1d4ed8` / `#ffffff` | — | 6.70 |
| `--info-subtle` / `-foreground` | `#dbeafe` / `#1e40af` | — | 7.15 |
| `--destructive` (texto) | `#b91c1c` | `#ffffff` | 6.47 |
| `--destructive-solid` / `-foreground` | `#dc2626` / `#ffffff` | — | 4.83 |
| `--destructive-subtle` / `-foreground` | `#fee2e2` / `#991b1b` | — | 6.80 |
| `--neutral-subtle` / `-foreground` | `#f1f5f9` / `#334155` | — | 9.45 |
| `--muted-foreground` (corregido) | `#475569` | `#f1f5f9` | 6.92 |

**Modo oscuro** (fondo `#020817`)

| Token | Valor | Sobre | Ratio |
|-------|-------|-------|:-----:|
| `--success` | `#4ade80` | `#020817` | 11.48 |
| `--success-solid` / `-foreground` | `#22c55e` / `#052e16` | — | 6.54 |
| `--success-subtle` / `-foreground` | `#052e16` / `#86efac` | — | 10.62 |
| `--warning` | `#fbbf24` | `#020817` | 11.98 |
| `--warning-solid` / `-foreground` | `#f59e0b` / `#451a03` | — | 6.97 |
| `--warning-subtle` / `-foreground` | `#451a03` / `#fcd34d` | — | 10.39 |
| `--info` | `#60a5fa` | `#020817` | 7.87 |
| `--info-solid` / `-foreground` | `#60a5fa` / `#172554` | — | 5.78 |
| `--info-subtle` / `-foreground` | `#172554` / `#93c5fd` | — | 8.15 |
| `--destructive` (texto) | `#f87171` | `#020817` | 7.23 |
| `--destructive-solid` / `-foreground` | `#f87171` / `#450a0a` | — | 5.84 |
| `--destructive-subtle` / `-foreground` | `#450a0a` / `#fca5a5` | — | 8.51 |
| `--neutral-subtle` / `-foreground` | `#1e293b` / `#cbd5e1` | — | 9.85 |
| `--muted-foreground` | `#94a3b8` | `#1e293b` | 5.71 |

Otros ajustes:
- **Borde de campos** (`--input`): hoy `#e2e8f0`, casi invisible. Pasa a `#64748b` en ambos temas: 4.75:1 sobre blanco y 4.18:1 sobre `#020817` (el mínimo para bordes de controles es 3:1). `--border` se mantiene para divisores decorativos.
- **Montos:** positivo usa `--success`, negativo usa `--destructive`, siempre con signo explícito.
- **Sostenibilidad:** los tres `--sustainability-*` se reemplazan por alias de `--success`.
- **Cifras:** se elimina `font-mono` para montos; se usa Inter con `font-variant-numeric: tabular-nums`, que alinea igual y no cambia de familia.

### 4.2 Color de marca — DECIDIDO: B, verde petróleo (D-01)

El primario deja de ser el gris-negro por defecto. Se evaluaron dos propuestas; **se eligió B (verde petróleo)** el 15-11-2026. La tabla queda como registro de la decisión.

| | **A — Índigo** (adopta el acento actual) | **B — Verde petróleo** (identidad nueva) |
|---|---|---|
| `--primary` claro / texto encima | `#4f46e5` / `#ffffff` · 6.29 | `#0f766e` / `#ffffff` · 5.47 |
| Texto de marca sobre blanco | `#4338ca` · 7.90 | `#0f766e` · 5.47 |
| `--primary-subtle` claro / texto | `#e0e7ff` / `#3730a3` · 8.06 | `#ccfbf1` / `#115e59` · 6.73 |
| `--primary` oscuro / texto encima | `#818cf8` / `#1e1b4b` · 5.36 | `#2dd4bf` / `#042f2e` · 7.77 |
| Texto de marca sobre `#020817` | `#a5b4fc` · 10.04 | `#5eead4` · 13.52 |
| A favor | Continuidad: ya se usa en 16 lugares y se asocia a la IA. Se distingue bien del azul informativo en badges. | Se diferencia de la competencia (casi todo el software contable usa azul). Evoca finanzas y confianza. |
| En contra | Cercano al azul de `--info`; exige cuidar que "Enviado al SII" no se confunda con marca. | Cercano al verde de `--success`; exige que "Aceptado" use siempre ícono además de color. |

Consecuencias de elegir B:
- `--primary` claro `#0f766e`, oscuro `#2dd4bf`; `--ring` (anillo de foco) usa los mismos valores (5.47:1 y 13.52:1 sobre el fondo).
- El riesgo señalado (marca cercana al verde de "Aceptado") se mitiga así: **Firmado deja de usar el color de marca y pasa a violeta** (sección 6), y Aceptado lleva siempre ícono y texto.
- El índigo actual (`text-indigo-600`, `bg-indigo-100`, 22 usos) se reemplaza por `--primary` o por el violeta de estado según el caso; la IA deja de tener un color propio y usa la marca.

| Token nuevo (estado Firmado) | Claro | Ratio | Oscuro | Ratio |
|---|---|:-:|---|:-:|
| `--signed-subtle` / `-foreground` | `#ede9fe` / `#5b21b6` | 7.57 | `#2e1065` / `#c4b5fd` | 8.25 |
| `--signed` (texto) | `#6d28d9` sobre blanco | 7.10 | `#c4b5fd` sobre `#020817` | 10.84 |

### 4.3 Paleta de gráficos

Mismos cinco tonos en ambos temas; solo cambia la luminosidad. Contraste contra el fondo ≥ 3:1 (mínimo para elementos gráficos).

| Serie | Tono | Claro | Ratio | Oscuro | Ratio |
|-------|------|-------|:-----:|--------|:-----:|
| `--chart-1` | Azul | `#2563eb` | 5.17 | `#60a5fa` | 7.87 |
| `--chart-2` | Turquesa | `#0d9488` | 3.74 | `#2dd4bf` | 10.75 |
| `--chart-3` | Ámbar | `#d97706` | 3.19 | `#fbbf24` | 11.98 |
| `--chart-4` | Violeta | `#7c3aed` | 5.70 | `#a78bfa` | 7.35 |
| `--chart-5` | Rosa | `#db2777` | 4.60 | `#f472b6` | 7.55 |

Reglas: ingresos y egresos usan `--success` y `--destructive`, no la paleta de series. Las series llevan etiqueta directa o leyenda; nunca se distinguen solo por color.

### 4.4 Tipografía, espaciado y fechas

- Se mantiene Inter y la escala actual. Se fija la jerarquía: título de página 24/32 semibold (baja desde 30 bold para ganar densidad), título de sección 18/28 semibold, cuerpo 14/20, detalle 12/16, KPI 24/32 bold con cifras tabulares.
- Espaciado: escala de 4px. Tablas con dos densidades: cómoda (fila de 48px) y compacta (36px), elegible por el usuario.
- **Fechas:** un solo formato, `dd-mm-aaaa`, en tablas, formularios y selector. Texto largo ("15 de noviembre de 2026") solo en encabezados de documentos.

---

## 5. Componentes

### 5.1 Corregir

| Componente | Problema | Cambio |
|------------|----------|--------|
| **Button** | Sin estado de carga; destructivo 3.6:1 | Prop `loading`: spinner, texto en gerundio ("Enviando…"), bloquea doble clic y conserva el ancho. Destructivo con `--destructive-solid`. Tamaño mínimo 36px. |
| **Input** | Sin estado de error; borde casi invisible | Estados error (borde y mensaje con `--destructive`, ícono, `aria-invalid`, `aria-describedby`) y éxito opcional. Borde `--input` nuevo. Texto de ayuda bajo el campo. |
| **StatusBadge** | 5 estados, dos iguales, sin íconos, contraste 2.2:1 | 7 estados con color, ícono y texto (sección 6). Variante suave por defecto. |
| **Badge** | Variante `success` con color suelto | Variantes semánticas: neutral, success, warning, info, destructive, brand; cada una en suave y sólida. |
| **CurrencyDisplay** | Colores sueltos, sin modo oscuro, fuente distinta | Tokens `--success` y `--destructive`, signo explícito, cifras tabulares en Inter. |
| **Sidebar** | Abreviaturas al colapsar, activo por ruta exacta, sin roles | Grupos plegables, colapsado con íconos y submenú flotante, activo por prefijo, filtrado por rol y módulos. |
| **Alert** | Solo default y destructive | Variantes info, success, warning, destructive con ícono y acción opcional. |
| **Table** | Solo estilos base | Se reemplaza por DataTable (5.2) en listados. |

### 5.2 Crear

| Componente | Para qué | Notas |
|------------|----------|-------|
| **DataTable** | Todos los listados | Orden por columna, filtros visibles, búsqueda, paginación con total, selección múltiple con acciones en lote, columnas numéricas a la derecha, fila de totales fija, densidad elegible, estados vacío, cargando y error integrados. |
| **EmptyState** | Listas sin datos | Ícono, título, explicación y acción principal. Distingue "aún no hay datos" de "ningún resultado para el filtro". |
| **ErrorState** | Fallas de carga | Qué pasó, botón Reintentar, detalle técnico plegable. Variante "sin permiso" con el rol requerido. |
| **Skeleton de página** | Cargas de 1 a 10 s | Por tipo de pantalla: tabla, formulario, panel. |
| **Stepper** | Emisión, cierres, onboarding, F29 | Horizontal en escritorio, vertical en móvil. Estados por paso: pendiente, actual, completo, con advertencia, con error. |
| **Breadcrumbs** | Orientación | Último nivel no clicable; se trunca en el medio. |
| **PeriodPicker** | F29, cierres, reportes | Unifica `PeriodSelector`, `SmartPeriodSelector` y `DateRangePicker`. Mes y año tributario; marca períodos cerrados. |
| **RutField** | Todo RUT | Formatea al escribir, valida dígito verificador, acepta con o sin puntos. Reemplaza `RutInput`. |
| **MoneyField** | Montos CLP | Separador de miles al escribir, sin decimales para CLP, alineado a la derecha, admite negativos solo si el campo lo permite. |
| **ConfirmDialog** | Acciones irreversibles | Título con la acción concreta ("Enviar la factura N° 1.024 al SII"), consecuencia y botón con el verbo. Nunca "¿Está seguro?". |
| **Timeline** | Historial de un documento | Eventos con fecha, actor y estado. |
| **StatCard** | KPIs | Valor, variación, período y enlace al detalle. |
| **EnvironmentBanner** | Ambiente SII | Franja permanente cuando la empresa opera en certificación; aviso destacado en producción al enviar. Depende de B-09. |
| **FolioMeter** | Folios disponibles | Barra con usados y disponibles por tipo de DTE, umbral de alerta. Depende de B-03. |
| **DevStatusTag** | Solo prototipo | Etiqueta Disponible, Parcial, En desarrollo o Planificado con tooltip de lo que falta. |

Cada componente se documenta con propósito, anatomía, variantes, estados, teclado, ARIA y tokens usados.

---

## 6. Estados del documento tributario

Siete estados. Cada uno se reconoce por color, ícono y texto; el color nunca va solo.

| Estado | Código | Token | Ícono (Lucide) | Texto | Descripción para el usuario | Acciones disponibles |
|--------|--------|-------|----------------|-------|-----------------------------|----------------------|
| **Borrador** | `DRAFT` | `neutral-subtle` | `FilePen` | Borrador | Aún no tiene validez tributaria. | Editar · Firmar · Eliminar |
| **Firmado** | `SIGNED` | `signed-subtle` (violeta) | `FileSignature` | Firmado | Firmado con el certificado. Falta enviarlo al SII. | Enviar al SII · Ver PDF |
| **Enviado al SII** | `SENT` | `info-subtle` | `Send` | Enviado al SII | En revisión. El SII aún no responde. | Consultar estado · Ver TrackID |
| **Aceptado** | `ACCEPTED` | `success-subtle` | `CircleCheck` | Aceptado | El SII aceptó el documento. | Enviar por email · Descargar PDF · Emitir nota de crédito |
| **Aceptado con reparos** | `ACCEPTED_WITH_OBJECTIONS` | `warning-subtle` | `TriangleAlert` | Aceptado con reparos | Válido, pero el SII informó observaciones. | Ver reparos · Enviar por email · Descargar PDF · Emitir nota de crédito |
| **Rechazado** | `REJECTED` | `destructive-subtle` | `CircleX` | Rechazado | El SII no lo aceptó. No tiene validez. | Ver motivo · Corregir y emitir de nuevo |
| **Anulado** | `CANCELLED` | `neutral-subtle` + texto tachado | `Ban` | Anulado | Anulado por una nota de crédito. | Ver nota de crédito |

Reglas:
- **Firmado y Enviado se distinguen** por color (violeta frente a azul) e ícono. Ningún estado usa el color de marca, para que la marca no se confunda con un estado tributario.
- Mientras no exista la consulta por TrackID (B-02), el sistema solo puede llegar a "Enviado al SII". En ese estado se muestra el TrackID y la hora de envío, y un aviso honesto: "La consulta automática de estado aún no está disponible".
- Emitir de nuevo un documento rechazado consume un folio nuevo; el diálogo lo dice.
- Los códigos `ACCEPTED_WITH_OBJECTIONS` y `CANCELLED` son **Propuesta nueva** (no existen en `DTE_STATUS` ni en el backend). **DECISIÓN PENDIENTE D-03.**

---

## 7. Pantalla por pantalla

P0 = bloquea el uso central del producto · P1 = necesario para operar un mes contable completo · P2 = módulos por rubro y mejoras.

### 7.1 P0

| Pantalla | Estado | Qué cambia | Dependencia de backend |
|----------|--------|-----------|------------------------|
| **App shell y menú por rol** | Menú plano | Sidebar agrupado y filtrado por rol y módulos, breadcrumbs, header con empresa, ambiente SII, buscador, notificaciones, asistente y usuario. | El front debe conocer los 9 roles. Sin bloqueo para filtrar el menú. |
| **Crear factura** `/invoices/new` | Con fallas | Flujo en pasos: datos del receptor (RutField con autocompletado desde clientes) → líneas con cálculo en vivo de neto, IVA 19% y total → vista previa del documento → firmar → enviar. El folio no se escribe: se muestra el próximo disponible. Guardado como borrador. | B-01 (payload y folio en servidor, R-02) · B-03 (folios disponibles) · MAPA §6 |
| **Detalle de factura** `/invoices/[id]` | Rota | Encabezado con estado y acciones según la tabla de la sección 6; pestañas Documento, Asiento contable e Historial; TrackID; reparos o motivo de rechazo. | B-02 (R-09) · B-04 (3 endpoints, MAPA §4) · R-08 · R-11 |
| **Lista de facturas** `/invoices` | Parcial | DataTable con filtros por estado, tipo, período y cliente; acciones en lote; importación CSV conectada. | `POST /invoices/import` ya existe (MAPA §5) |
| **Folios (CAF)** `/sii/caf` | Parcial | Lista de CAF por tipo de DTE con rango, usados, disponibles y fecha; FolioMeter; alerta de agotamiento; carga de CAF con validación. | B-03 (no hay endpoint para listar, MAPA §2.3) |
| **StatusBadge y tokens v2** | — | Base de todo lo anterior. | Ninguna |

### 7.2 P1

| Pantalla | Estado | Qué cambia | Dependencia de backend |
|----------|--------|-----------|------------------------|
| Panel `/dashboard` | Con fallas | Panel por rol: vencimientos tributarios, documentos por estado, folios, salud financiera. Sin emoji en el título. | `GET /dashboard/recent-activity` no existe (B-05) |
| Formulario 29 `/accounting/f29` | Conectada | Flujo revisar → confirmar con stepper, detalle por código, diferencia contra RCV, confirmación antes de declarar. | R-07 (parámetros 2024) |
| Sincronización SII `/sii` | Rota | Estado de la última descarga del RCV, botón de sincronizar, historial. | B-05: usar `POST /sii/ops/fetch-rcv`; faltan `/sii/status` y `/sii/sync` |
| Salud tributaria `/compliance` | Conectada, sin acceso | Vuelve al menú. Semáforo con texto, alertas accionables. | Acuse de recibo simulado (brecha, RIESGOS) |
| Boletas de honorarios `/fees` | Rota, sin acceso | Lista con retención, importación conectada. | B-05: `GET /fees` no existe; `POST /fees/import` sí |
| Libro diario `/accounting/ledger` | Rota | DataTable de asientos con filtro por período y cuenta, detalle debe y haber. Se retira el botón de limpieza de noviembre. | Conectar `GET /accounting/entries` (existe). Endpoint de limpieza: B-11 |
| Libro mayor y plan de cuentas | Planificado | Pantalla nueva sobre endpoints existentes. | `/accounting/accounts`, `/ledger/{accountCode}` (MAPA §6) |
| Balance `/accounting/balance` | Parcial | Árbol con datos reales, comparación con período anterior. | `GET /accounting/accounts/tree` no existe (B-05) |
| Estado de resultados `/accounting/income` | Conectada | Comparativo por período, exportación. | — |
| Cuentas corrientes `/accounting/partners` | Conectada | Detalle de movimientos por cliente o proveedor. | `/{rut}/movements` existe, sin uso |
| Activos fijos `/accounting/assets` | Parcial | Alta y edición de activos. | `POST /fixed-assets` existe; GET, PUT y DELETE por id no (B-05) |
| Cierres `/accounting/close`, `/annual-close` | Parcial | Una pantalla con pestañas; stepper con hallazgos de la IA identificados como sugerencias; cálculo real del F22. | `POST /annual-close/f22/calculate/{año}` existe; estado de períodos (MAPA §6) |
| Auditoría `/accounting/audit`, `/sii/audit` | Conectadas (duplicadas) | Área única con pestañas. | `/audit/*` existen sin uso (MAPA §6) |
| Reglas de clasificación | Planificado | CRUD de reglas. | R-18, R-19: el update no guarda |
| Flujo de caja `/treasury` | Con fallas | Pronóstico, movimientos de caja conectados, indicadores. | `POST /treasury/cash-entry` existe; tipos de cambio y cartola no (B-05) |
| Bancos `/banking` | Conectada, sin acceso | Entra al menú como paso previo a conciliar. | — |
| Conciliación `/banking/reconciliation` | Conectada | Vista de dos columnas (banco y libros), sugerencias de match, atajos de teclado. | Permiso del contador (D-06) |
| Remuneraciones `/payroll` | Con fallas | Lista de empleados, selector de período real, liquidaciones en PDF, archivo Previred. | B-05: `/payroll/employees` y PDF no existen; `periodId: 1` fijo |
| Portal del empleado `/employee` | Rota para su público | Layout propio; liquidaciones y datos. | B-07 (permiso USER) |
| Configuración `/settings` | Con fallas | Pestañas Empresa, Usuarios y roles, Certificado, Módulos, Notificaciones. Vencimiento del certificado visible. | D-04 · `DELETE /sso/users/{id}` y `PUT /notifications/preferences` no existen · R-01, R-10 |
| Onboarding `/onboarding` | Simulada | Empresa → certificado → preferencias y módulos, con guardado real y posibilidad de retomar. | No guarda nada hoy (B-06) |
| Login y registro | Conectadas | Mensajes de error claros, aviso de intentos. | R-17 (sin rate limiting) |
| Documentos `/documents` | Parcial | Subida de documentos, extracción con IA revisable. | `POST /dms` por verificar; `/dms/ai-invoicing/process` sin uso |
| Reportes `/reports` | Parcial | Catálogo de reportes con período y formato. | Reporte semanal y estados financieros existen sin uso (MAPA §6) |
| Asistente IA (header) | Disponible | Panel lateral con contexto de la pantalla, fuentes citadas y aviso de qué datos se envían. | R-12, R-13 |

### 7.3 P2

| Pantalla | Estado | Qué cambia | Dependencia de backend |
|----------|--------|-----------|------------------------|
| Cotizaciones `/crm/quotes` | Conectada | DataTable; "Convertir en factura" (**Propuesta nueva**). | Depende de B-01 |
| Oportunidades `/crm/leads` | Conectada | Tablero por etapa y lista. | — |
| Órdenes de compra `/procurement` | Conectada | DataTable, estados con StatusBadge genérico. | — |
| Inventario `/inventory` | Parcial | Alta, edición y movimientos de producto. | CRUD existe sin uso; exportar Excel no (B-05) |
| Centros de costo `/projects` | Conectada | Se mueve a Operaciones. | D-06 |
| Punto de venta `/pos` | Con fallas | Pantalla completa optimizada para caja. | `GET /pos/products/search` no existe; R-06 (boletas) |
| Pedidos B2B y portal `/portal` | Conectada | Vista interna de pedidos (**Propuesta nueva**); portal con layout propio. | B-08 · fuente de verdad de órdenes (DESIGN §13) |
| Bodega `/wms` | Parcial | Alta de ubicaciones y picking. | Endpoints de alta existen sin uso |
| Producción `/mrp` | Parcial | Alta de listas de materiales y órdenes. | Ídem |
| Flota `/fleet` | Conectada, sin acceso | Entra como módulo. | Un solo endpoint |
| Sostenibilidad `/sustainability` | Conectada, sin acceso | Entra como módulo; tokens de éxito. | — |
| Energía `/energy` | Rota, sin acceso | Módulo con cargadores, precios, mantenimiento, reportes y control remoto. | 70 endpoints sin uso; el front llama a 4 que no existen. **D-05.** |
| Notificaciones | Planificado | Centro de notificaciones y preferencias. | `/notifications/subscribe`, `/preferences` (MAPA §6); R-23 |
| Base de conocimiento IA | Planificado | Subir y buscar documentos de referencia. | `/ai/knowledge` (solo ADMIN) |
| Organizaciones | Planificado | Jerarquía de organizaciones. | `/organizations/*` (D-04) |
| Recepción de DTE de terceros y acuse | **Propuesta nueva** | Bandeja de documentos recibidos con aceptar o reclamar. | No existe (brecha, RIESGOS) |

---

## 8. Bloqueos de backend

Lo que el diseño no puede resolver solo. Sin estos cambios, la pantalla se construye pero no funciona con datos reales.

| ID | Bloqueo | Evidencia | Afecta a | Esf. |
|----|---------|-----------|----------|:----:|
| B-01 | El contrato de `POST /invoices` no calza con el front y el folio viene del request. Falta asignarlo en el servidor desde el CAF activo, con `UNIQUE(company_id, tipo_dte, folio)`. | `InvoiceController.java:105-125`, `InvoiceForm.tsx:92-104`, R-02 | Crear factura | M |
| B-02 | No existe consulta de estado por TrackID ni job que actualice estados. | `SiiUploadClient.java:27`, R-09 | Detalle, lista, estados | M |
| B-03 | No existe endpoint para listar CAF ni folios disponibles. | MAPA §2.3 | Folios, crear factura | S |
| B-04 | No existen `GET /invoices/{id}`, `PUT /invoices/{id}`, `GET /accounting/entries/by-invoice/{id}`, `GET /invoices/{id}/audit-trail`. | MAPA §4, issue #1 | Detalle de factura | M |
| B-05 | Otros 21 endpoints que el front llama y no existen (SII, honorarios, remuneraciones, tesorería, inventario, POS, activos, panel, energía, configuración). | Issue #1, tabla final | P1 y P2 | L |
| B-06 | El onboarding no persiste; no hay endpoint de carga de certificado en ese flujo. | `onboarding/page.tsx:24`, R-01, R-10 | Onboarding | M |
| B-07 | `PayrollController` exige ADMIN o HR: el empleado no ve sus liquidaciones. | `PayrollController.java:20` | Portal del empleado | S |
| B-08 | Controllers sin restricción de rol: `CustomerOrderController`, `AiAssistantController`, `NotificationController`. | R-23, R-13 | Portal B2B, IA | S |
| B-09 | Ambiente SII apunta a producción por defecto y el front no puede saber cuál está activo. | R-05 | EnvironmentBanner | S |
| B-10 | No hay campo de módulos activos por empresa. | Propuesta nueva | Módulos por rubro | S |
| B-11 | `POST /accounting/cleanup/november-rcv` sigue en producción. | RIESGOS, pendientes | Libro diario | S |
| B-12 | El despacho de factura no valida la empresa y acepta cualquier email. | R-08 | Enviar por email | S |
| B-13 | El asiento se crea antes de la aceptación del SII, sin reversa. | R-11 | Pestaña Asiento del detalle | M |
| B-14 | Parámetros tributarios fijos en 2024. | R-07 | F29 | S |
| B-15 | Estados `ACCEPTED_WITH_OBJECTIONS` y `CANCELLED` no existen. | Sección 6 | StatusBadge | S |

Permisos mal configurados respecto del uso esperado: contador sin conciliación ni centros de costo (D-06), empleado sin portal (B-07), portal B2B e IA abiertos (B-08).

---

## 9. Fases de implementación

Esfuerzo de front: **S** < 1 semana · **M** 1 a 3 semanas · **L** > 3 semanas. Supone una persona de front y no incluye el trabajo de backend de la sección 8.

| Fase | Contenido | Esf. | Depende de |
|------|-----------|:----:|-----------|
| **1. Fundamentos** | Tokens v2 en `globals.css` y Tailwind, reemplazo de clases sueltas, Button, Input, Badge, StatusBadge, Alert, CurrencyDisplay, formato de fecha único. | M | D-03 (D-01 ya decidido) |
| **2. Shell y navegación** | Sidebar agrupado, roles, módulos, breadcrumbs, header, estados vacío, error y sin permiso, redirecciones de duplicados. | M | Fase 1 |
| **3. Patrones de datos** | DataTable, PeriodPicker, RutField, MoneyField, Stepper, ConfirmDialog, Timeline, StatCard. | M | Fase 1 |
| **4. Flujo de emisión (P0)** | Crear factura, detalle, lista, folios. | L | Fases 2 y 3 · B-01 a B-04 |
| **5. Tributario y contabilidad (P1)** | F29, sincronización, salud tributaria, honorarios, libros, balance, resultados, cierres, auditoría. | L | Fase 3 · B-05, B-14 |
| **6. Finanzas, personas y configuración (P1)** | Tesorería, bancos, conciliación, remuneraciones, portal del empleado, configuración, onboarding, panel por rol. | L | Fase 3 · B-05 a B-07, D-04 |
| **7. Módulos por rubro (P2)** | CRM, compras, inventario, POS, B2B, WMS, MRP, flota, sostenibilidad, energía. | L | Fase 3 · B-10, D-05 |
| **8. Calidad** | Auditoría de accesibilidad WCAG 2.2 AA, revisión de textos, pruebas con usuarios, documentación de componentes. | M | En paralelo desde la fase 4 |

Las fases 1 a 3 no dependen del backend y pueden empezar de inmediato.

---

## 10. Decisiones pendientes

### Para ti (producto)

| ID | Decisión | Opciones | Recomendación |
|----|----------|----------|---------------|
| D-01 | Color de marca | A índigo · B verde petróleo | **Decidido: B, verde petróleo** (15-11-2026) |
| D-02 | Nombre y logo | Mantener "SII-ERP-AI" con un logo propio · renombrar | Definir antes de la fase 1; hoy se usa el ícono genérico `Calculator` |
| D-03 | Estados nuevos del documento | Agregar Aceptado con reparos y Anulado al backend · mantener 5 | Agregarlos (B-15) |
| D-04 | Sistema de usuarios | `/sso/users` · `/users` + `/roles` + `/organizations` | El segundo |
| D-05 | Módulo Energía | Parte del producto · producto aparte · retirar | Sin recomendación: es una decisión de negocio (DESIGN §13, pregunta 4) |
| D-06 | Permisos del contador | Dar acceso a conciliación y centros de costo · mantener | Dar acceso a conciliación; centros de costo en solo lectura |
| D-07 | Fuente de verdad de pedidos B2B | Microservicio · `modules/b2b` | Sin recomendación (DESIGN §13, pregunta 3) |
| D-08 | Plan de Figma | Mantener Starter (dos colecciones) · subir de plan (modos claro y oscuro) | Subir si un diseñador va a trabajar en el archivo |
| D-09 | Paneles de inicio por rol | Diseñar uno por rol · mostrar el panel actual solo a quien tiene datos | Uno por rol, en la fase 6 |

### Para el diseñador

| ID | Decisión |
|----|----------|
| D-10 | Logo, isotipo y uso del color de marca en superficies grandes |
| D-11 | Densidad por defecto de las tablas (cómoda o compacta) |
| D-12 | Iconografía de los 12 grupos del menú (hoy Facturas y Gestor Documental comparten ícono) |
| D-13 | Ilustraciones o solo íconos en los estados vacíos |
| D-14 | Tono de los textos: tratamiento de "tú" (propuesto) o "usted" |
| D-15 | Qué se conserva de los elementos lúdicos actuales (emoji animado, `CelebrationOverlay`); propuesta: retirarlos de las pantallas tributarias |
| D-16 | Diseño del documento impreso (PDF de la factura con timbre PDF417) |

---

## 11. Registro de pendientes

Todo lo que quedó abierto en las fases 0 a 2, con quién lo cierra y cuándo. El objetivo es llegar al final sin pendientes sin dueño.

| ID | Pendiente | Origen | Quién lo cierra | Cuándo | Estado |
|----|-----------|--------|-----------------|--------|--------|
| PEN-01 | Confirmar en ejecución (con backend levantado) las 18 pantallas "Conectada"; el inventario es análisis estático. | Fase 0, issue #1 | Desarrollo | Antes de la fase 4 de implementación | Abierto |
| PEN-02 | Verificar los permisos de todos los controllers. | Fase 0 | Claude | Fase 2 | **Cerrado**: 51 controllers revisados, matriz en 3.2 |
| PEN-03 | `MAPA-FUNCIONAL.md` y `UI-DESIGN.md` desfasados respecto del commit `9593ef8`. | Fase 0 | Claude | Fase 2 | **Cerrado**: versiones actualizadas en `Doc/rediseno/MAPA-FUNCIONAL.md` y `Doc/rediseno/UI-DESIGN.md` |
| PEN-04 | Figma: claro y oscuro en dos colecciones por el límite del plan Starter. | Fase 1 | Tú (D-08) | Antes de la fase 4 | Abierto |
| PEN-05 | Figma: faltan componentes actuales (Dialog, Table, Select, Tabs, Alert, Sheet y otros de `ui/`). | Fase 1 | Claude | Fase 4, junto con los componentes v2 | Abierto |
| PEN-06 | Figma: íconos genéricos en vez de Lucide; fuente de montos sustituida por Roboto Mono. | Fase 1 | Claude | Fase 4 (los montos pasan a Inter tabular, sección 4.1) | Abierto |
| PEN-07 | Auditoría de accesibilidad y revisión de textos de interfaz sobre pantallas reales. | Fase 2 | Claude | Fase 3, sobre el prototipo | Abierto |
| PEN-08 | Los principios y la arquitectura de información no se validaron con usuarios. | Fase 2 | Tú o el diseñador | Prueba de árbol del menú y 5 sesiones con contadores sobre el prototipo | Abierto |
| PEN-09 | 25 llamadas activas a endpoints inexistentes. | Issue #1 | Desarrollo | Según B-01 a B-05 | Abierto |
| PEN-10 | 15 decisiones abiertas (D-02 a D-16; D-01 decidida). | Sección 10 | Tú y el diseñador | D-02 a D-04 antes de la implementación | Abierto |
| PEN-11 | Crear factura parece funcionar en el código pero el backend rechazaría el payload; no se probó en ejecución. | Fase 0 | Desarrollo | Con B-01 | Abierto |
| PEN-13 | `DESIGN.md` §5 describe endpoints del microservicio B2B (`/api/v1/b2b/orders`), pero el microservicio **no tiene controllers**; el portal usa el módulo `b2b` del backend. Afecta a D-07. | Fase 2 | Tú (actualizar DESIGN.md) | Antes de la fase 7 | Abierto |
| PEN-14 | El issue #1 indicaba 231 endpoints; el conteo correcto es 226, todos en el backend principal. | Fase 2 | Claude | Fase 2 | **Cerrado**: corregido en el mapa y en un comentario del issue |
| PEN-12 | Preguntas abiertas de `DESIGN.md` §13 que afectan al diseño: endpoint real de envío al SII, aviso de CAF agotándose, energía, B2B. | DESIGN §13 | Tú | Antes de la fase 4 | Abierto |

**Estado para la fase 3:** D-01 y PEN-03 están resueltos. El prototipo usa las recomendaciones de este plan para el resto de decisiones abiertas y las marca como pendientes.
