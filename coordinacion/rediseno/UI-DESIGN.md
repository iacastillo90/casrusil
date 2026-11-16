# Documento de Diseño Visual - SII-ERP-AI Frontend

*Documento para diseñador/a, no técnico. Extraído del código fuente.*

> **Versión actualizada al commit `9593ef8` (15-11-2026).** Reemplaza a `Doc/UI-DESIGN.md`. Cambios: referencias de línea corregidas, conteo de colores sueltos rehecho, menú actual (§8), componentes que dejaron de ser placeholders (§7, §11) y estado de las pantallas según `Doc/rediseno/MAPA-FUNCIONAL.md`. Los valores de tokens, tipografía, espaciado y componentes base no cambiaron.

---

## 1. Identidad

### Nombre del Producto
**SII-ERP-AI** – Sistema ERP contable chileno con IA integrada.

### Logo
- **Logo principal (Dashboard):** `Calculator` de lucide-react, mostrado junto con el texto "SII-ERP-AI" en el sidebar.
- **Logo de empresa configurable:** Se usa el componente [`LogoUpload.tsx`](src/components/shared/LogoUpload.tsx) para subir logos de empresas desde la configuración.
- **Archivos estáticos:** No hay logo estático en `public/`. Los logos se suben vía Upload.

### Tono Visual

> **Punto de partida para el diseño: hoy el producto no tiene identidad visual propia.**
> Todos los tokens de color, el radio y los colores de gráficos son los **valores por defecto de Shadcn (tema "slate")**. Los únicos tokens propios son los verdes `--sustainability-*`. Tampoco existe un logo: el sidebar muestra el ícono genérico `Calculator` junto al texto.

- **Color primario real:** azul pizarra casi negro, `#0f172a` (`--primary`). **No es índigo.** Botones principales, estados activos.
- **Índigo, el acento "de facto":** no es un token. Aparece con clases sueltas (`text-indigo-600` en 16 lugares, `bg-indigo-100` en 6), sobre todo en las partes de IA. Visualmente la app mezcla un primario gris-negro con un acento índigo que el sistema no define.
- **Verde:** éxito, montos positivos y sostenibilidad, con tonos distintos según el lugar (`green-500`, `green-600`, `emerald-600`).
- **Estilo:** limpio y neutro, con algunos toques lúdicos (emoji animado en el título del dashboard, `CelebrationOverlay`, animaciones con framer-motion en el onboarding).

---

## 2. Colores

### Paleta de tokens (CSS Variables)

> Los HEX están calculados desde los valores HSL de `globals.css`. Los HSL coinciden con los defaults de Shadcn "slate".

#### Modo Claro (`:root`, `globals.css:6-35`)

| Token | HEX | HSL | Uso |
|-------|-----|-----|-----|
| `--background` | #ffffff | 0 0% 100% | Fondo principal |
| `--foreground` | #020817 | 222.2 84% 4.9% | Texto principal |
| `--card` / `--popover` | #ffffff | 0 0% 100% | Fondo de tarjetas y popovers |
| `--card-foreground` / `--popover-foreground` | #020817 | 222.2 84% 4.9% | Texto en tarjetas y popovers |
| `--primary` | #0f172a | 222.2 47.4% 11.2% | Botón primario, estados activos |
| `--primary-foreground` | #f8fafc | 210 40% 98% | Texto sobre primary |
| `--secondary` / `--muted` / `--accent` | #f1f5f9 | 210 40% 96.1% | Fondos secundarios, hover |
| `--secondary-foreground` / `--accent-foreground` | #0f172a | 222.2 47.4% 11.2% | Texto sobre esos fondos |
| `--muted-foreground` | #64748b | 215.4 16.3% 46.9% | Texto secundario, descripciones |
| `--destructive` | #ef4444 | 0 84.2% 60.2% | Error, eliminar |
| `--destructive-foreground` | #f8fafc | 210 40% 98% | Texto sobre destructive |
| `--border` / `--input` | #e2e8f0 | 214.3 31.8% 91.4% | Bordes, bordes de inputs |
| `--ring` | #020817 | 222.2 84% 4.9% | Anillo de foco |
| `--radius` | 0.5rem (8px) | — | Radio base |

#### Modo Oscuro (`.dark`, `globals.css:37-65`)

| Token | HEX | HSL | Uso |
|-------|-----|-----|-----|
| `--background` / `--card` / `--popover` | #020817 | 222.2 84% 4.9% | Fondos |
| `--foreground` y textos `*-foreground` | #f8fafc | 210 40% 98% | Texto principal |
| `--primary` | #f8fafc | 210 40% 98% | **Se invierte:** el botón primario es casi blanco |
| `--primary-foreground` | #0f172a | 222.2 47.4% 11.2% | Texto sobre primary |
| `--secondary` / `--muted` / `--accent` / `--border` / `--input` | #1e293b | 217.2 32.6% 17.5% | Fondos secundarios y bordes |
| `--muted-foreground` | #94a3b8 | 215 20.2% 65.1% | Texto secundario |
| `--destructive` | #7f1d1d | 0 62.8% 30.6% | Error (rojo oscuro) |
| `--ring` | #cbd5e1 | 212.7 26.8% 83.9% | Anillo de foco |

#### Colores de gráficos (`globals.css:27-31` claro, `57-61` oscuro)

| Token | Claro (HEX / HSL) | Oscuro (HEX / HSL) |
|-------|-------------------|--------------------|
| `--chart-1` | #e76e50 coral / 12 76% 61% | #2662d9 azul / 220 70% 50% |
| `--chart-2` | #2a9d90 verde azulado / 173 58% 39% | #2eb88a verde / 160 60% 45% |
| `--chart-3` | #274754 petróleo oscuro / 197 37% 24% | #e88c30 naranja / 30 80% 55% |
| `--chart-4` | #e8c468 mostaza / 43 74% 66% | #af57db violeta / 280 65% 60% |
| `--chart-5` | #f4a462 naranja suave / 27 87% 67% | #e23670 rosado / 340 75% 55% |

⚠️ **Las dos paletas no se corresponden:** al cambiar de tema, cada serie cambia de color por completo (la serie 1 pasa de coral a azul, la 3 de petróleo a naranja). Un usuario que alterna entre modos ve los mismos datos con colores distintos. Son los defaults de Shadcn.

### Tokens propios: sostenibilidad (`globals.css:32-34`, `62-64`)

| Token | Claro (HEX / HSL) | Oscuro (HEX / HSL) | Uso |
|-------|-------------------|--------------------|-----|
| `--sustainability-primary` | #16a249 / 142 76% 36% | #1cca5b / 142 76% 45% | Color principal |
| `--sustainability-light` | #d2f9e0 / 142 76% 90% | #09431e / 142 76% 15% | Fondos suaves (en oscuro es verde oscuro) |
| `--sustainability-border` | #1fe066 / 142 76% 50% | #18b451 / 142 76% 40% | Bordes |

### No existen tokens de éxito, advertencia ni información

Shadcn no los trae y el proyecto no los agregó. Por eso esos estados se pintan con clases sueltas de Tailwind (tabla siguiente).

### Colores sueltos usados directamente (sin token)

Conteo de ocurrencias en `src/` (commit `9593ef8`), sin contar las que llevan prefijo `hover:` o `dark:`.

| Clase | Cantidad | Uso específico |
|-------|----------|----------------|
| `bg-green-500` | 6 | Badges de estado exitoso, botones de confirmación |
| `text-green-600` | 43 | Montos positivos, indicadores de éxito |
| `text-red-500` / `text-red-600` | 23 / 36 | Errores, estado rechazado, montos negativos |
| `bg-red-50` | 11 | Fondos de alerta de error |
| `bg-amber-50` | 7 | Fondos de advertencia |
| `text-amber-600` / `text-amber-700` | 9 / 8 | Texto de advertencia |
| `text-blue-500` / `text-blue-600` | 10 / 14 | Información, IA |
| `bg-blue-50` | 10 | Fondos de información |
| `text-indigo-600` | 16 | Texto de acento primario, IA |
| `bg-indigo-100` | 6 | Fondos de IA |
| `text-emerald-700` / `text-emerald-600` | 6 / 6 | Éxito, sostenibilidad |
| `bg-emerald-50` | 7 | Fondos de éxito |
| `bg-emerald-600` | 4 | Botones de acción positiva |
| `border-green-200` | 7 | Bordes de estado exitoso |

`bg-blue-500` y `bg-indigo-500`, que figuraban en la versión anterior, ya no aparecen.

### Mapeo de estados de negocio

| Estado | Color usado | Componente que lo usa |
|--------|-------------|----------------------|
| **Éxito** | `bg-green-500` / `bg-emerald-600` | Badge (variant "success"), CurrencyDisplay (variant "positive") |
| **Error** | `bg-destructive` / `text-red-500` | Alert (destructive), StatusBadge (variant "destructive"), FormMessage |
| **Advertencia** | `bg-amber-50` / `text-amber-600` | No hay token dedicado; se usa `text-amber-*` y `bg-amber-50` directamente |
| **Información** | `bg-blue-50` / `text-blue-600` | Cards informativas, iconos de info |
| **Primario** | `bg-primary` (`--primary`) | Botones default, estados activos |
| **Secundario** | `bg-secondary` (`--secondary`) | Botones outline, fondos secundarios |

---

## 3. Tipografía

### Familia tipográfica
- **Fuente principal:** `Inter` (cargada vía `next/font/google`)
- **Ubicación:** [`src/app/layout.tsx:7`](src/app/layout.tsx#L7)
- **Subsets:** `["latin"]`
- **Peso:** La fuente ya incluye pesos automáticamente; los pesos usados son:
  - `font-medium` – Etiquetas y descripciones
  - `font-semibold` – Títulos de sección
  - `font-bold` / `font-black` – Títulos principales

### Escala real de tamaños y pesos usados

| Uso | Tamaño | Peso | Frecuencia |
|-----|--------|------|------------|
| Texto de cuerpo / labels | `text-sm` | `font-medium` | 126 usos |
| Texto detalle / descripción | `text-xs` | `font-medium` | 102 usos |
| Títulos de página principal | `text-3xl` | `font-bold` | 43 usos |
| Títulos secundarios / cards | `text-2xl` | `font-semibold` | 40 usos |
| Texto de sección / subtítulos | `text-lg` | `font-medium` | 28 usos |
| Texto base / párrafos | `text-base` | `font-normal` | 15 usos |
| Títulos muy grandes / hero | `text-4xl` | `font-bold` | 3 usos |
| Texto inline pequeño | `text-base` con peso normal | - | - |

### Jerarquía tipográfica
1. **Título de página:** `text-3xl font-bold` con emoji animado (ej: Dashboard)
2. **Título de sección:** `text-2xl font-semibold` en CardTitle
3. **Subtítulos / labels:** `text-lg font-medium` o `text-sm font-medium`
4. **Texto cuerpo:** `text-base` o `text-sm`
5. **Texto secundario:** `text-sm text-muted-foreground`
6. **Números / montos:** `text-2xl font-bold` o `text-3xl font-black` en KPIs
7. **Descripciones:** `text-sm text-muted-foreground`

---

## 4. Espaciado, radios y sombras

### Spacing
Escala de Tailwind por defecto: cada unidad = 4px (`p-4` = 16px, `p-6` = 24px). Los valores más comunes:
- Padding: `p-4` (16px), `p-6` (24px, padding de Card), `px-3` (12px), `py-2` (8px)
- Margin: `m-4`, `mt-6`, `mb-2`
- Gap: `gap-2`, `gap-4`, `gap-6`

### Radio de bordes
- **Valor base:** `var(--radius) = 0.5rem` (8px)
- **Derivados en tailwind.config.ts:**
  - `lg`: `var(--radius)` = 8px
  - `md`: `calc(var(--radius) - 2px)` = 6px
  - `sm`: `calc(var(--radius) - 4px)` = 4px

### Sombras
Usa clases de Tailwind:
- `shadow-sm` – Tarjetas, cards pequeñas
- `shadow-md` – Dialogos, modales
- `shadow-lg` – Dialogos grandes, contenidos destacados
- `border` – Borde explícito en cards

### Breakpoints
| Nombre | Ancho | Uso real |
|--------|-------|----------|
| `sm` | 640px | Cards en fila, tabla responsive |
| `md` | 768px | Grid 2 columnas, header responsive |
| `lg` | 1024px | Dashboard 3-4 columnas, sidebar colapsado |
| `xl` | 1280px | Grid 4 columnas, máximo de contenido |

---

## 5. Íconos

### Librería
- **Lucide React** (confirmado en `components.json:13`)
- Uso: `{import { IconName } from 'lucide-react';}`

### Tamaños usados
| Tamaño | Clase | Uso típico |
|--------|-------|------------|
| 16px | `h-4 w-4` | Botones, íconos inline (el más común) |
| 20px | `h-5 w-5` | Navegación, badges, stepper del onboarding |
| 24px | `h-6 w-6` | Header |
| 32px | `h-8 w-8` | Íconos destacados, bot de IA |

### Iconos más usados (por nombre descritivo)
- **Navegación:** `LayoutDashboard`, `FileText`, `Calculator`, `Cloud`, `Wallet`, `ShieldCheck`, `Settings`, `Users`, `Store`, `ShoppingBag`, `Boxes`, `ShoppingCart`, `Database`, `BarChart3`, `FileSignature`
- **Estado:** `CheckCircle2`, `XCircle`, `AlertTriangle`, `Clock`
- **Acciones:** `Plus`, `Edit`, `Trash2`, `Send`, `Download`, `UploadCloud`
- **Fecha/Hora:** `Calendar`, `Clock`, `RefreshCw`
- **Usuario:** `UserCircle2`, `LogOut`, `User`
- **IA:** `Bot`, `Sparkles`, `ShieldCheck`

---

## 6. Componentes UI

### Componentes en `src/components/ui/`

#### Button (`button.tsx:6-33`)
Base: `rounded-md` (6px), `text-sm font-medium`, ícono y texto centrados.

| Variante | Fondo | Texto | Hover |
|----------|-------|-------|-------|
| `default` | `--primary` (#0f172a; oscuro #f8fafc) | `--primary-foreground` | fondo al 90% |
| `destructive` | `--destructive` (#ef4444; oscuro #7f1d1d) | `--destructive-foreground` | fondo al 90% |
| `outline` | `--background` + borde `--input` | `--foreground` | fondo `--accent` |
| `secondary` | `--secondary` (#f1f5f9) | `--secondary-foreground` | fondo al 80% |
| `ghost` | transparente | `--foreground` | fondo `--accent` |
| `link` | transparente | `--primary` | subrayado |

| Tamaño | Alto | Padding horizontal |
|--------|------|--------------------|
| `sm` | 36px (h-9) | 12px |
| `default` | 40px (h-10) | 16px |
| `lg` | 44px (h-11) | 32px |
| `icon` | 40 × 40px | — |

- **Foco:** anillo de 2px color `--ring` con 2px de separación.
- **Deshabilitado:** opacidad 50%, sin interacción.
- **Cargando:** el componente **no tiene** estado de carga; cada pantalla lo arma a mano con `Loader2 animate-spin` y cambiando el texto.
- ⚠️ `destructive` en modo claro: texto #f8fafc sobre #ef4444 = **3.6:1**, no cumple AA para texto de 14px.

#### Input (`input.tsx`)
- 40px de alto (h-10), padding 12px × 8px, `text-sm`, `rounded-md` (6px), borde `--input`, fondo `--background`.
- Placeholder en `--muted-foreground`. Foco: anillo de 2px `--ring`. Deshabilitado: opacidad 50% y cursor bloqueado.
- **No tiene estado de error propio.** El borde rojo lo agrega cada formulario o componente por su cuenta (p. ej. `RutInput` usa `border-red-500` / `border-green-500`).

#### Card (`card.tsx`)
| Parte | Estilo |
|-------|--------|
| `Card` | `rounded-lg` (8px), borde `--border`, fondo `--card`, `shadow-sm` |
| `CardHeader` | padding 24px, 6px entre título y descripción |
| `CardTitle` | `text-2xl` (24px) `font-semibold`, interlineado 1, tracking ajustado |
| `CardDescription` | `text-sm` (14px), `--muted-foreground` |
| `CardContent` / `CardFooter` | padding 24px, sin padding superior |

#### Dialog (`dialog.tsx`)
- **Overlays:** bg-black/80 con animaciones fade/zoom
- **Contenido:** max-w-lg, border, shadow-lg, animaciones slide/fade
- **Estados:** open/closed con transiciones

#### Form (`form.tsx`)
- **Componentes:** FormItem, FormLabel, FormControl, FormDescription, FormMessage
- **Estados de error:** text-destructive en labels, border-red en inputs
- **Validación:** zod + react-hook-form

#### Table (`table.tsx`)
- **Wrapper:** overflow-auto
- **Estilos:** border-b, hover:bg-muted/50
- **Header:** h-12, align-middle, font-medium, text-muted-foreground

#### Badge (`badge.tsx:5-25`)
Base: píldora (`rounded-full`), padding 10px × 2px, `text-xs` (12px) `font-semibold`.

| Variante | Fondo | Texto |
|----------|-------|-------|
| `default` | `--primary` | `--primary-foreground` |
| `secondary` | `--secondary` | `--secondary-foreground` |
| `destructive` | `--destructive` | `--destructive-foreground` |
| `success` | `green-500` (#22c55e), **suelto, sin token** | blanco |
| `outline` | transparente + borde | `--foreground` |

#### StatusBadge — estados del documento tributario (`StatusBadge.tsx:5-11`)

| Estado (código) | Texto | Se ve como |
|-----------------|-------|------------|
| `DRAFT` | Borrador | Gris claro (`secondary`) |
| `SIGNED` | Firmado | Primario: casi negro (claro) / casi blanco (oscuro) |
| `SENT` | Enviado | **Idéntico a Firmado** |
| `ACCEPTED` | Aceptado | Verde `green-500` |
| `REJECTED` | Rechazado | Rojo `--destructive` |
| (otro) | El código tal cual | Contorno (`outline`) |

Problemas para el diseño:
- **Firmado y Enviado son visualmente iguales**: el usuario no distingue un documento firmado de uno ya enviado al SII.
- **No hay íconos**: el estado depende solo del color.
- **Aceptado no usa la variante `success` del Badge**: usa `default` con una clase verde encima (`StatusBadge.tsx:31-32`). Resultado: en modo claro el texto es blanco sobre verde (**2.2:1**, ilegible según AA) y en modo oscuro el texto pasa a ser oscuro (7.8:1). El mismo estado cambia de color de texto según el tema.
- **Faltan estados** que el negocio necesita: anulado, aceptado con reparos y pendiente/en proceso en el SII.

#### CurrencyDisplay — montos (`CurrencyDisplay.tsx`)
- Fuente monoespaciada (`font-mono`) para alinear cifras. Formato CLP con `formatCLP` (`$1.190.000`); el símbolo es opcional.
- Variantes: `default` (color de texto), `positive` → `text-green-600`, `negative` → `text-red-600`. Sin variante para modo oscuro.
- Contraste: positivo sobre blanco **3.3:1** ❌, sobre fondo oscuro 6.1:1 ✅. Negativo sobre blanco 4.8:1 ✅, sobre fondo oscuro **4.1:1** ❌.
- El signo de un monto negativo depende solo del color (además del signo `-` del formato).

#### Select (`select.tsx`)
- **Trigger:** h-10, border-input, bg-background
- **Contenido:** max-h-96, z-50, animaciones
- **Iconos:** ChevronDown/ChevronUp

#### Sheet (`sheet.tsx`)
- **Sides:** top, bottom, left, right (por defecto: right)
- **Ancho:** w-3/4 sm:max-w-sm
- **Overlays:** bg-black/80

#### Avatar (`avatar.tsx`)
- **Tamaño:** h-10 w-10 por defecto
- **Estilo:** rounded-full

#### Progress (`progress.tsx`)
- **Altura:** h-4
- **Fondo:** bg-secondary
- **Indicador:** bg-primary, transición

#### Switch (`switch.tsx`)
- **Tamaño:** h-6 w-11
- **Estados:** data-[state=checked]

#### Calendar (`calendar.tsx`)
- **Iconos:** ChevronLeft/ChevronRight
- **Cell selection:** bg-primary / bg-accent

#### ScrollArea (`scroll-area.tsx`)
- **Scrollbar:** thumb bg-border, w-2.5

#### Command (`command.tsx`)
- **Search:** icono de búsqueda
- **Item:** aria-selected=bg-accent

#### Tooltip (`tooltip.tsx`)
- **Posición:** sideOffset=4
- **Estilo:** bg-popover, shadow-md, animaciones

#### Alert (`alert.tsx`)
- **Variantes:** default, destructive
- **Iconos:** position absolute

#### Separator (`separator.tsx`)
- **Border:** bg-border, h-px

#### Skeleton (`skeleton.tsx`)
- **Animación:** animate-pulse
- **Fondo:** bg-muted

#### Popover (`popover.tsx`)
- **Tamaño:** w-72
- **Estilo:** bg-popover, shadow-md

#### Tabs (`tabs.tsx`)
- **List:** bg-muted, rounded-md p-1
- **Trigger activo:** bg-background, shadow-sm

#### Label (`label.tsx`)
- **Text:** text-sm font-medium

#### DatePicker (`DateRangePicker.tsx` - shared)
- **Popover:** Calendar con 2 meses
- **Formato:** dd/MM/yyyy (español)

---

## 7. Pantallas (src/app)

### Rutas principales y sus componentes

| Ruta | Objetivo | Componentes principales | Estados vacío/carga/error |
|------|----------|------------------------|---------------------------|
| `/` | Landing page | - | - |
| `/login` | Autenticación | LoginForm (Card) | - |
| `/register` | Registro | RegisterForm (Card) | - |
| `/onboarding` | Configuración inicial | Steppers con Card, animaciones framer-motion | - |
| `/dashboard` | Dashboard principal | KPIGrid, SalesChart, RecentInvoices, FinancialShieldWidget, EcoScoreCard | SkeletonLoader |
| `/invoices` | Lista de facturas | InvoiceTable, CsvImportModal, Dialog envío | InvoiceTable (empty state, skeleton) |
| `/invoices/[id]` | Detalle factura | InvoicePDF, DTEStatusTracker, AccountingEntryCard, InvoiceAuditTrail, SustainabilityCard | Carga con `isLoading`; **rota**: 3 endpoints inexistentes |
| `/invoices/new` | Crear factura | InvoiceForm (tipo, RUT, líneas, IVA 19%, totales) | Toast de error; **con fallas**: payload incompatible con el backend |
| `/accounting/ledger` | Libro diario | JournalEntryTable, botón de limpieza de noviembre | **Rota**: hook con datos vacíos fijos |
| `/accounting/balance` | Balance | SmartPeriodSelector, BalanceSheetTree (datos mock) | SkeletonLoader |
| `/accounting/f29` | F29 | F29Preview (vista previa y envío) | Carga, error y reintento |
| `/accounting/income` | Estado resultados | SmartPeriodSelector, tabla de resultados | SkeletonLoader |
| `/accounting/close` | Cierre período IA | PeriodClose con Progress, botones | - |
| `/accounting/annual-close` | Cierre anual | Stepper con AiChat | Usa la auditoría de período; no calcula F22 |
| `/accounting/assets` | Activos fijos | - | - |
| `/accounting/audit` | Auditoría tributaria | TaxReconciliationTable | Carga y error |
| `/accounting/partners` | Cuentas corrientes | - | - |
| `/compliance` | Centro de salud tributaria | ComplianceDashboard, F29Preview | Card con estado loading/error |
| `/fees` | Boletas de honorarios | FeeTable, FeeImportModal | **Rota** y sin acceso desde el menú |
| `/payroll` | Nómina | PayrollDashboard, tabla empleados | Loading skeleton |
| `/fleet` | Flota vehicular | - | - |
| `/documents` | Gestor documental IA | - | - |
| `/sii` | Sincronización SII | SyncStatusDashboard, CAFUpload | **Rota**: endpoints de estado inexistentes |
| `/sii/caf` | Gestión CAF | Carga de archivo CAF | Sin lista de CAF ni folios |
| `/banking/reconciliation` | Conciliación bancaria | Conexiones, movimientos, conciliar e ignorar | Carga |
| `/treasury` | Tesorería | LiquidityKPIs, AiPredictiveChart, CashFlowChart, ReconciliationInterface, BankStatementUpload, CashEntryModal, MultiCurrencyPanel | Pestañas: proyección IA, conciliación, flujo de caja |
| `/crm/leads` | Pipeline CRM | - | - |
| `/crm/quotes` | Cotizaciones | - | - |
| `/pos` | Punto de venta | - | - |
| `/portal` | Portal B2B | PortalLayout, page.tsx | - |
| `/employee` | Portal empleado | - | - |
| `/inventory` | Inventario | InventoryDashboard | - |
| `/procurement` | Adquisiciones | - | - |
| `/mrp` | Planificación | - | - |
| `/projects` | Centros de costos | - | - |
| `/reports` | Reportes | ReportTemplates | - |
| `/settings` | Configuración | CompanyProfile, UserManagement, InviteUserModal, PersonalSettings | - |
| `/sustainability` | Sostenibilidad | EcoScoreGauge, SustainablePurchasesChart, EsgReportPanel | Spinner, datos no disponibles |
| `/energy` | Energía | Métricas y alertas | **Rota**: endpoints inexistentes |
| `/wms` | Almacén | - | - |

### Layouts
- **`src/app/(dashboard)/layout.tsx`:** Sidebar + Header + AiDrawer
- **`src/app/(auth)/layout.tsx`:** Fondo muted/40, centrado
- **`src/app/layout.tsx`:** Providers (QueryClient, ThemeProvider, Toaster)

---

## 8. Layout y Navegación

### Estructura Dashboard

```
┌──────────────┬──────────────────────────────────────────────┐
│ Sidebar      │ Header (menú móvil, búsqueda, notificaciones, │
│ w-64 (256px) │ usuario)                                      │
│ colapsado:   ├──────────────────────────────────────────────┤
│ w-16 (64px)  │                                              │
│              │ Contenido (p-4, lg:px-6)                      │
│ Logo         │                                              │
│ Navegación   │                                              │
│ Colapsar     │                                   AiDrawer → │
└──────────────┴──────────────────────────────────────────────┘
En móvil (< md) el sidebar se abre como Sheet desde el botón de menú.
```

### Flow Auth/Onboarding

1. **Login** (`/login`) – Card con formulario, enlace a register
2. **Register** (`/register`) – Card con formulario RUT, empresa, email, password
3. **Onboarding** (`/onboarding`) – Stepper de 3 pasos:
   - Paso 1: Detalles empresa (RUT, razón social, industria)
   - Paso 2: Conexión SII (certificado, contraseña)
   - Paso 3: Preferencias (moneda, zona horaria, IA)
4. **Dashboard** – Redirección automática tras completar onboarding

### Navegación (Sidebar, `Sidebar.tsx:55-119`)

17 entradas de primer nivel y 29 enlaces en total, en este orden:

| # | Entrada | Ícono | Subítems |
|---|---------|-------|----------|
| 1 | Dashboard | LayoutDashboard | — |
| 2 | Facturas | FileText | — |
| 3 | Inventario | Boxes | — |
| 4 | Adquisiciones | ShoppingCart | — |
| 5 | Ventas CRM | Megaphone | Pipeline (Leads), Cotizaciones |
| 6 | Ventas | ShoppingCart | Punto de Venta (POS), Portal B2B |
| 7 | Remuneraciones | Users | — |
| 8 | Mi Portal (Empleado) | UserCircle2 | — |
| 9 | Contabilidad | Calculator | Libro Diario, Form 29, Balance, Activos Fijos, Estado Resultados, Centro de Costos, ✨ Cierre de Período IA, 🌟 Cierre Anual (F22) |
| 10 | SII | Cloud | Sincronizar, CAF |
| 11 | Conciliación y Control | ShieldCheck | Auditoría Tributaria, Conciliación Bancaria, Cuentas Corrientes |
| 12 | Producción (MRP) | Factory | — |
| 13 | Logística (WMS) | PackageSearch | — |
| 14 | Gestor Documental (IA) | FileText | — |
| 15 | Tesorería | Wallet | — |
| 16 | Reportes | FileBarChart | — |
| 17 | Configuración | Settings | — |

**Medidas y estados**
- Ancho 256px expandido / 64px colapsado, con transición de 300ms. Zona del logo de 64px de alto.
- Ítem: padding 12px × 8px, `text-sm`, `rounded-md`, ícono de 20px.
- Normal: texto `--muted-foreground`. Hover: fondo `--accent`. Activo: fondo `--primary` (casi negro en claro, casi blanco en oscuro).
- Los grupos (Contabilidad, SII, etc.) son títulos fijos, no se pueden plegar; los subítems van con sangría de 32px.

**Problemas de navegación**
- **Colapsado, los subítems se muestran como sus dos primeras letras** (`Sidebar.tsx:173`) ("Li", "Fo", "Ba", "Ac"…) y los títulos de grupo desaparecen: Contabilidad se convierte en una columna de 8 abreviaturas sin contexto.
- **Rutas en el código:** `BANKING_RECONCILIATION` apunta a `/banking/reconciliation` y existe `APP_ROUTES.TREASURY` (corregido). Tesorería conserva además una pestaña de conciliación.
- **Pantallas que existen y no están en el menú (8):** Honorarios (`/fees`), Compliance (`/compliance`, perdió su entrada), Conexiones bancarias (`/banking`), Auditoría SII (`/sii/audit`), Flota (`/fleet`), Sostenibilidad (`/sustainability`), Energía (`/energy`) y Perfil de empresa (`/settings/company`).
- **Íconos repetidos:** Facturas y Gestor Documental usan `FileText`; Adquisiciones y el grupo Ventas usan `ShoppingCart`.
- **Dos grupos de ventas:** "Ventas CRM" y "Ventas" están separados y en posiciones distintas del menú.
- **El estado activo solo funciona en la ruta exacta:** en `/invoices/new` o `/invoices/[id]`, "Facturas" no aparece marcado.
- **Mezcla de estilos en los textos:** emojis solo en dos ítems (✨, 🌟), inglés en "Pipeline (Leads)" y siglas entre paréntesis.
- **Todas las áreas se muestran a todas las empresas**, de cualquier rubro (POS, MRP, WMS, Portal B2B…).

---

## 9. Patrones de Datos

### Tablas
- Componente: `Table` de shadcn/ui
- Wrapper: `overflow-auto`
- Header: `h-12`, `text-left`, `font-medium`, `text-muted-foreground`
- Rows: `hover:bg-muted/50`, `border-b`
- Empty state: ícono `FileText`, texto centrado, padding 16px

### Filtros
- **Selects:** reutilizan componente `Select` de shadcn
- **Date Range:** `DateRangePicker` con popover Calendar (2 meses)
- **Text search:** Input con ícono Search en algunos casos

### Formularios
- Framework: React Hook Form + Zod validación
- Layout: `FormField` → `FormItem` → `FormLabel` → `FormControl` → `FormDescription`/`FormMessage`
- Botones submit en `CardFooter`
- Loading state: texto cambia + spinner `Loader2 animate-spin`

### Formato de datos chilenos

#### Montos CLP (`formatCLP` - `lib/formatters.ts`)
- Formato: Intl.NumberFormat('es-CL', { style: 'currency', currency: 'CLP' })
- Ejemplo: `$1.190.000`
- Símbolo: `$` (opcional)
- Monospace en UI: `font-mono`

#### RUT (`formatRut`, `validateRut`)
- Formato: `12.345.678-9`
- Validación con dígito verificador
- Campo: `placeholder="12.345.678-K"`

#### Fechas (`formatDate`)
- Formato: `dd de MMMM, yyyy` (ej: "15 de noviembre, 2026")
- Locale: español (`es`)
- Display: `text-muted-foreground` en labels

---

## 10. Responsive y Accesibilidad

### Responsive
- **Mobile first:** Grid columnas 1 por defecto
- **Tablet (md≥768px):** Grid 2 columnas, header visible
- **Desktop (lg≥1024px):** Grid 3-4 columnas, sidebar colapsado con animación
- **Mobile menu:** Sheet con sidebar (md:hidden, botón Menu)
- **Breakpoints reales:**
  - Cards: `grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4`
  - Dashboard: `flex flex-col md:flex-row`
  - Tabla: `overflow-x-auto` en mobile

### Accesibilidad
- **Dark mode:** `next-themes` con detección automática del sistema
- **Focus:** ring-2 ring-ring ring-offset-2 en todos los inputs/botones
- **Contraste** (WCAG AA exige 4.5:1 para texto normal y 3:1 para texto grande):

| Combinación | Ratio | AA texto normal |
|-------------|-------|-----------------|
| `--foreground` #020817 sobre blanco | ~20:1 | ✅ |
| `--muted-foreground` #64748b sobre blanco | 4.76:1 | ✅ (justo) |
| `--muted-foreground` #64748b sobre `--muted` #f1f5f9 | 4.34:1 | ❌ |
| `--muted-foreground` oscuro #94a3b8 sobre #020817 | 7.8:1 | ✅ |
| `text-red-500` #ef4444 sobre blanco (24 usos) | 3.76:1 | ❌ |
| `text-green-600` #16a34a sobre blanco | 3.3:1 | ❌ |
| `text-amber-600` #d97706 sobre `bg-amber-50` | 3.07:1 | ❌ |

Los textos de error, éxito y advertencia que usan colores sueltos no cumplen el contraste mínimo en tamaños chicos.
- **Screen reader:** `sr-only` para textos ocultos (ej: "Toggle navigation menu")
- **Labels:** siempre asociados a inputs vía `htmlFor` y `id`
- **ARIA:** `aria-selected`, `aria-invalid`, `role="alert"` en alerts

---

## 11. Vacíos / Inconsistencias

### Colores duplicados / no tokenizados

| Ubicación | Clase usada | Comentario |
|-----------|-------------|------------|
| `StatusBadge.tsx:17` | `bg-green-500` | Éxito - no hay token `--success` |
| `StatusBadge.tsx:32` | `hover:bg-green-600` | Éxito hover |
| `CurrencyDisplay.tsx:23` | `text-green-600` | Positivo |
| `CurrencyDisplay.tsx:24` | `text-red-600` | Negativo |
| `RutInput.tsx:41` | `border-red-500` | Error validación |
| `RutInput.tsx:42` | `border-green-500` | Validación OK |
| `Dashboard page.tsx:20` | `text-slate-900` | Título claro |
| `Dashboard page.tsx:23` | `text-slate-500` | Descripción |
| `Settings modal.tsx:109` | `bg-blue-100 dark:bg-blue-900` | Info icono |
| `Settings modal.tsx:109` | `text-blue-600 dark:text-blue-300` | Texto info |

### Componentes duplicados

| Función | Archivo 1 | Archivo 2 | Comentario |
|---------|-----------|-----------|------------|
| StatusBadge (custom) | `StatusBadge.tsx` | Badge de shadcn | StatusBadge agrega variante "success" |
| CurrencyDisplay | `CurrencyDisplay.tsx` | - | Formato CLP, usado en múltiples pantallas |
| DateRangePicker | `DateRangePicker.tsx` | - | Wrapper con Calendar shadcn |

### Colores hardcodeados sin dark mode

| Pantalla | Elemento | Clase clara | Missing dark |
|----------|----------|-------------|--------------|
| Sustainability page.tsx:47-50 | Gradient hero | `from-green-500/10` | No tiene `from-green-900/10` equivalente |
| Sustainability page.tsx:54 | Icono leaf | `text-green-600` | No cambia en dark |
| Payroll page.tsx:68 | Icono building | `text-blue-600` bg `bg-blue-100` | No tiene variante dark |
| PeriodClose page.tsx:193 | Icono RefreshCw | `text-indigo-600` | No tiene dark variant |
| Settings modal.tsx:109 | Info icon | `text-blue-600` con `dark:text-blue-300` | ✅ Sí tiene dark |

### Componentes sin estados definidos

| Componente | Estado vacío | Estado loading | Estado error |
|------------|---------------|---------------- |--------------|
| Dashboard KPIGrid | SkeletonLoader | `isLoading` muestra `—` | `isError` muestra texto |
| InvoiceTable | Card con ícono, mensaje "No hay facturas" | Skeleton columnas | - |
| FeeTable | - | - | - |
| PayrollDashboard | Texto "Cargando empleados..." | - | - |
| ComplianceDashboard | Texto "No se pudo cargar..." | "Consultando estado tributario SII..." | Card con border-destructive |
| SustainabilityDashboard | Spinner + "Datos no disponibles" | Spinner animado | - |

### Otros hallazgos

1. **InvoiceForm.tsx** (336 líneas) ya es un formulario real con tipo, RUT, líneas, IVA y totales, pero envía campos que el backend no acepta (`InvoiceForm.tsx:92-104` frente a `InvoiceController.java:105-125`). Usa `Label` y `Select` sueltos, no `RutInput` ni `CurrencyDisplay`.
2. **AiDrawer.tsx** es un `Sheet` lateral de 400–540px que abre `AiChat`; el disparador por defecto es un botón con el ícono `Bot`.
3. **F29Preview.tsx** (305 líneas) está conectado. **BalanceSheetTree.tsx** (313 líneas) tiene diseño completo pero usa datos mock (`BalanceSheetTree.tsx:24-94`).
4. **Alert component** usa `border-transparent` para variant default, `border-destructive/50` solo para destructivo
5. **Badge variant "success"** no está en shadcn original, se agrega manualmente con fallback a "default"
6. **PeriodClose page** tiene estados `pending`, `running`, `done`, `warning`, `error` con colores hardcodeados (`text-slate-400 bg-slate-100`, `text-blue-600 bg-blue-50`, etc.)

---

## 12. Resumen para el diseñador

### Lo que hay
- **Base:** Shadcn/ui sin personalizar (tema slate), Inter y íconos Lucide. Es un sistema coherente y funcional, pero **genérico**: no hay marca, logo ni color propio.
- **Primario real:** casi negro (#0f172a), invertido a casi blanco en modo oscuro. El índigo que se ve en la app (sobre todo en IA) **no es un token**.
- **Escala tipográfica:** xs · sm · base · lg · 2xl · 3xl · 4xl. Los títulos de página usan 3xl bold; los montos de KPI, 2xl–3xl bold/black.
- **Espaciado:** escala de 4px de Tailwind; cards con 24px de padding; radio de 8/6/4px.
- **Modo oscuro:** activo y sigue al sistema operativo por defecto, así que todo diseño necesita su versión oscura.

### Lo que falta definir (prioridad)
1. **Identidad:** logo, color de marca y si el índigo pasa a ser el primario oficial.
2. **Colores semánticos:** tokens de éxito, advertencia e información (claro y oscuro) que cumplan contraste AA. Hoy hay 3 verdes distintos y los rojos/verdes/ámbar no cumplen 4.5:1.
3. **Estados de documentos tributarios:** hoy Firmado y Enviado se ven iguales, Aceptado es ilegible en modo claro y faltan anulado, con reparos y en proceso. Definir color, ícono y texto para cada uno (`StatusBadge`).
4. **Navegación:** 17 entradas de primer nivel y 29 enlaces, con áreas muy distintas (contabilidad, POS, MRP, WMS) y 8 pantallas fuera del menú. Conviene agrupar, permitir plegar grupos, resolver el modo colapsado (hoy muestra abreviaturas de 2 letras) y mostrar módulos según el rol o el plan de la empresa.
5. **Estados vacío, carga y error:** solo 5–6 pantallas los tienen; la mayoría no tiene ninguno.
6. **Formato de fechas:** se usan dos (`15 de noviembre, 2026` en textos y `dd/MM/yyyy` en el selector). Definir uno.
7. **Componentes incompletos:** el botón no tiene estado de carga y el input no tiene estado de error; cada pantalla los resuelve a mano.
8. **Gráficos:** la paleta de modo oscuro no corresponde a la de modo claro (cada serie cambia de color al cambiar de tema).
9. **Pantallas a rediseñar:** crear factura (existe un formulario, pero sin pasos, sin vista previa ni folio visible), detalle de factura (rota), lista de CAF con folios (no existe), libro diario (sin datos), portal del empleado y gestión de usuarios. Ver `Doc/rediseno/PLAN-REDISENO.md` §7.
