# Prototipo del rediseño — SII-ERP AI

Prototipo navegable del rediseño del frontend (Fase 3 de `Doc/rediseno/PLAN-REDISENO.md`). Muestra todas las pantallas del producto, las que funcionan, las incompletas y las planificadas, con el sistema de diseño v2 (marca verde petróleo).

**Todos los datos son ficticios**: empresas, personas, RUT (con dígito verificador válido) y montos.

## Cómo abrirlo

Haz doble clic en `index.html`. Funciona sin internet y sin instalar nada (HTML, CSS y JavaScript sin dependencias; la fuente Inter va incluida en `fonts/`).

Probado en Chrome. Debería funcionar en cualquier navegador actual.

## Demo en un solo archivo

`demo/siierp-v2-demo.html` es el mismo prototipo empaquetado en un único HTML (estilos, scripts y fuente incluidos), pensado para presentar o enviar por correo. La navegación ocurre en memoria, sin cambiar la URL, y el botón **Guion de la demo** de la barra inferior guía un recorrido de 12 pasos con el rol adecuado en cada uno.

Para regenerarlo después de cambiar el prototipo, desde la raíz del repo: `python3 prototipo/build_demo.py prototipo prototipo/demo/siierp-v2-demo.html`.

## Cómo usarlo

- **Mapa del prototipo** (`#/mapa`, enlace en la barra inferior): lista todas las pantallas por área, con su estado real y un enlace a cada una.
- **Barra de control** (abajo):
  - **Rol**: los 9 roles del backend más dos personas de portal (Empleado y Cliente B2B). El menú y el acceso a cada pantalla cambian según la matriz rol × área del plan (§3.2).
  - **Módulos**: activa o desactiva Punto de venta, Pedidos B2B, Bodega, Producción, Flota, Energía y Sostenibilidad.
  - **Estado de pantalla**: con datos, vacío, cargando o error.
  - **Modo claro u oscuro**.
- **Etiqueta de estado** junto al título de cada pantalla: Disponible, Parcial, En desarrollo o Planificado. Al pasar el cursor (o el foco del teclado) se ve qué falta y en qué documento está la evidencia.
- **Ctrl + K** abre el buscador de pantallas. **Esc** cierra diálogos y paneles.
- Los ajustes de la barra de control se recuerdan en el navegador. Los datos que crees (facturas, pedidos, etc.) se reinician al recargar.

## Flujos completos

| Flujo | Dónde empieza |
|-------|---------------|
| Emitir factura: receptor → detalle con IVA 19% → vista previa → firmar → enviar al SII → seguimiento (aceptado, con reparos, rechazado, corrección y nota de crédito que anula) | `#/invoices/new` |
| Cargar CAF y ver folios disponibles | `#/sii/caf` |
| F29: revisar → confirmar → declarado | `#/accounting/f29` |
| Conectar banco → conciliación | `#/banking` |
| Cierre mensual (5 pasos con auditoría IA) y cierre anual (F22) | `#/accounting/close` |
| Onboarding: empresa → certificado → preferencias | `#/onboarding` |
| Portal del empleado (layout propio) | Rol "Empleado (portal)" o `#/employee` |
| Portal del cliente B2B (layout propio) | Rol "Cliente B2B (portal)" o `#/portal` |

Como el backend aún no consulta el estado de un documento en el SII (PLAN B-02), el detalle de un documento enviado tiene un panel **Simular respuesta del SII**, marcado como exclusivo del prototipo.

## Cobertura

- Las **43 pantallas** de `Doc/rediseno/MAPA-FUNCIONAL.md` (ninguna falta).
- **5 funcionalidades del backend sin pantalla** (§6), como pantallas "Planificado": aceptar invitación, plan de cuentas y libro mayor, reglas de clasificación, base de conocimiento IA y notificaciones. Las demás de §6 aparecen como pestañas o acciones dentro de otras pantallas; el mapa del prototipo indica dónde.
- **2 propuestas nuevas**: documentos recibidos (acuse y reclamo) y la vista interna de pedidos B2B.
- Componentes fuera de una ruta: asistente IA (panel lateral) y buscador de pantallas.

Por estado real: 19 Disponible (incluye la raíz, que redirige), 17 Parcial, 7 En desarrollo, 7 Planificado.

## Estructura

```
prototipo/
├── index.html
├── css/tokens.css     Tokens v2 claro y oscuro (PLAN §4)
├── css/app.css        Estilos de la aplicación
├── fonts/             Inter (OFL) en woff2
└── js/
    ├── util.js        Formatos chilenos (CLP, RUT, fechas) e íconos SVG estilo Lucide
    ├── data.js        Datos de demostración
    ├── components.js  Componentes (tabla de datos, estados del DTE, stepper, campos RUT y monto…)
    ├── registry.js    Pantallas, roles, módulos y menú
    ├── app.js         Router por hash, shell, barra de control, diálogos
    └── screens-*.js   Vistas por área
```

## Verificación realizada

- 75 rutas y 73 enlaces internos recorridos con Chrome sin interfaz: todas cargan, ninguna da "página no encontrada".
- 0 errores y 0 advertencias en la consola.
- 17 flujos recorridos haciendo clic de principio a fin.
- axe-core (WCAG 2.2 A y AA) sobre todas las rutas, en claro y oscuro, y con diálogos, paleta y paneles abiertos: 0 infracciones, incluido el contraste.
- Teclado: Ctrl + K, flechas, Enter, Esc, trampa de foco en diálogos y retorno del foco al cerrar.

## Pendiente

- No se probó con lectores de pantalla reales (NVDA, VoiceOver); solo con axe-core y el teclado.
- Solo se probó en Chrome; faltan Firefox y Safari.
- La lectura de archivos (CAF, certificado, CSV, cartolas) es simulada: los botones "usar archivo de ejemplo" reemplazan el procesamiento real.
- Las pantallas de Energía que no son Cargadores ni Control remoto muestran solo el alcance (decisión D-05 pendiente).
- Los textos no se validaron con usuarios (PLAN PEN-08).
