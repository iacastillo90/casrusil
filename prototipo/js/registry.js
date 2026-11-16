/* Registro de pantallas, roles, módulos y navegación.
   Fuentes: Doc/rediseno/MAPA-FUNCIONAL.md (estado real), PLAN-REDISENO.md §3 (arquitectura) y §7 (prioridades). */
(function () {
  'use strict';

  var ROLES = [
    ['ADMIN', 'Administrador'], ['ACCOUNTANT', 'Contador'], ['TREASURER', 'Tesorería'], ['SALES', 'Ventas'],
    ['OPERATIONS', 'Operaciones'], ['HR', 'Personas (RR.HH.)'], ['AUDITOR', 'Auditor'], ['USER', 'Usuario'], ['B2B_CLIENT', 'Cliente B2B']
  ];
  var PERSONAS = [['EMPLOYEE', 'Empleado (portal)'], ['B2B_PORTAL', 'Cliente B2B (portal)']];
  var ROLE_LABEL = {};
  ROLES.concat(PERSONAS).forEach(function (r) { ROLE_LABEL[r[0]] = r[1]; });
  // Persona → rol del backend
  var EFFECTIVE = { EMPLOYEE: 'USER', B2B_PORTAL: 'B2B_CLIENT' };
  var USERS_BY_ROLE = {
    ADMIN: ['Matías Fuentes', 'MF'], ACCOUNTANT: ['Valentina Rojas', 'VR'], TREASURER: ['Isidora Morales', 'IM'], SALES: ['Joaquín Herrera', 'JH'],
    OPERATIONS: ['Benjamín Salinas', 'BS'], HR: ['Carolina Pizarro', 'CP'], AUDITOR: ['Rosa Vergara', 'RV'], USER: ['Antonia Castro', 'AC'],
    B2B_CLIENT: ['Panadería La Espiga Dorada', 'PE'], EMPLOYEE: ['Benjamín Salinas', 'BS'], B2B_PORTAL: ['Panadería La Espiga Dorada', 'PE']
  };

  var MODULES = [
    ['pos', 'Punto de venta'], ['b2b', 'Pedidos B2B y portal'], ['wms', 'Bodega (WMS)'], ['mrp', 'Producción (MRP)'],
    ['fleet', 'Flota'], ['energy', 'Energía'], ['sustainability', 'Sostenibilidad']
  ];
  var MODULE_LABEL = {}; MODULES.forEach(function (m) { MODULE_LABEL[m[0]] = m[1]; });

  var INTERNAL = ['ADMIN', 'ACCOUNTANT', 'TREASURER', 'SALES', 'OPERATIONS', 'HR', 'AUDITOR'];
  var R = {
    all: INTERNAL,
    fact: ['ADMIN', 'ACCOUNTANT', 'SALES'],
    crm: ['ADMIN', 'SALES'],
    ops: ['ADMIN', 'OPERATIONS'],
    acc: ['ADMIN', 'ACCOUNTANT'],
    accAud: ['ADMIN', 'ACCOUNTANT', 'AUDITOR'],
    treas: ['ADMIN', 'TREASURER', 'ACCOUNTANT'],
    bank: ['ADMIN', 'TREASURER'],
    hr: ['ADMIN', 'HR'],
    audit: ['ADMIN', 'AUDITOR', 'ACCOUNTANT'],
    admin: ['ADMIN'],
    pos: ['ADMIN', 'SALES'],
    energy: ['ADMIN', 'OPERATIONS', 'AUDITOR'],
    sust: ['ADMIN', 'OPERATIONS', 'AUDITOR'],
    employee: ['ADMIN', 'HR', 'USER'],
    portal: ['ADMIN', 'B2B_CLIENT']
  };

  var M = 'MAPA-FUNCIONAL.md';
  // dev: disponible | parcial | desarrollo | planificado; real: estado en MAPA-FUNCIONAL; mapa: sección o 'backend' (§6) o 'propuesta'
  var SCREENS = [
    // Acceso
    { id: 'root', route: '/', title: 'Raíz', area: 'Acceso', layout: 'redirect', roles: null, dev: 'disponible', real: 'Redirección', mapa: '§2.9', code: '/', missing: 'Redirige según el rol: al panel, al portal del empleado o al portal B2B.', src: M + ' §2.9' },
    { id: 'login', route: '/login', title: 'Iniciar sesión', area: 'Acceso', layout: 'auth', roles: null, dev: 'disponible', real: 'Conectada', mapa: '§2.9', code: '/login', missing: 'Conectada. Falta limitar los intentos de inicio de sesión.', src: M + ' §2.9; RIESGOS R-17' },
    { id: 'register', route: '/register', title: 'Crear cuenta', area: 'Acceso', layout: 'auth', roles: null, dev: 'disponible', real: 'Conectada', mapa: '§2.9', code: '/register', missing: 'Conectada.', src: M + ' §2.9' },
    { id: 'onboarding', route: '/onboarding', title: 'Configuración inicial', area: 'Acceso', layout: 'auth', roles: null, dev: 'desarrollo', real: 'Simulada', mapa: '§2.9', code: '/onboarding', missing: 'No llama a la API: no guarda nada (onboarding/page.tsx:24). No hay carga de certificado en este flujo y el certificado se guardaría sin cifrar.', src: M + ' §2.9, §5; PLAN B-06; RIESGOS R-01, R-10' },
    { id: 'invite', route: '/invite', title: 'Aceptar invitación', area: 'Acceso', layout: 'auth', roles: null, dev: 'planificado', real: 'Solo backend', mapa: 'backend', missing: 'El backend tiene /sso/invite/validate y /sso/register-invite; no hay pantalla.', src: M + ' §6 (SSO)' },

    // Inicio
    { id: 'dashboard', route: '/dashboard', title: 'Panel', area: 'Inicio', icon: 'home', roles: R.all, dev: 'parcial', real: 'Con fallas', mapa: '§2.9', code: '/dashboard', missing: 'KPIs y salud financiera conectados. La actividad reciente llama a GET /dashboard/recent-activity, que no existe. Los paneles por rol son propuesta nueva.', src: M + ' §2.9, §4; PLAN §3.2, D-09' },

    // Ventas
    { id: 'invoices', route: '/invoices', title: 'Facturas y documentos', area: 'Ventas', roles: R.fact, dev: 'parcial', real: 'Parcial', mapa: '§2.1', code: '/invoices', missing: 'Lista y envío por email funcionan. Importar CSV es simulado aunque existe POST /invoices/import. Los filtros y los estados nuevos son del rediseño.', src: M + ' §2.1, §5; PLAN §7.1' },
    { id: 'invoice-new', route: '/invoices/new', title: 'Emitir documento', area: 'Ventas', navParent: 'invoices', roles: R.fact, dev: 'parcial', real: 'Con fallas', mapa: '§2.1', code: '/invoices/new', missing: 'El formulario existe y llama a POST /invoices, pero con campos que el backend rechaza; el folio todavía viene del request. Ver folios disponibles requiere un endpoint nuevo.', src: M + ' §2.1; PLAN B-01, B-03; RIESGOS R-02' },
    { id: 'invoice-detail', route: '/invoices/:id', title: 'Detalle del documento', area: 'Ventas', navParent: 'invoices', roles: R.fact, dev: 'desarrollo', real: 'Rota', mapa: '§2.1', code: '/invoices/[id]', missing: 'Llama a GET /invoices/{id}, al asiento por factura y al historial, que no existen. No hay consulta de estado por TrackID: hoy el sistema no sabe si el SII aceptó o rechazó.', src: M + ' §2.1, §4; PLAN B-02, B-04; RIESGOS R-09, R-11' },
    { id: 'quotes', route: '/crm/quotes', title: 'Cotizaciones', area: 'Ventas', roles: R.crm, dev: 'disponible', real: 'Conectada', mapa: '§2.1', code: '/crm/quotes', missing: 'Conectada. "Convertir en factura" es propuesta nueva y depende de corregir la emisión.', src: M + ' §2.1; PLAN §7.3' },
    { id: 'leads', route: '/crm/leads', title: 'Oportunidades', area: 'Ventas', roles: R.crm, dev: 'disponible', real: 'Conectada', mapa: '§2.1', code: '/crm/leads', missing: 'Conectada: listar, crear y cambiar etapa.', src: M + ' §2.1' },

    // Compras
    { id: 'procurement', route: '/procurement', title: 'Órdenes de compra', area: 'Compras y gastos', roles: R.ops, dev: 'disponible', real: 'Conectada', mapa: '§2.6', code: '/procurement', missing: 'Conectada: listar, crear, marcar enviada y recibida.', src: M + ' §2.6' },
    { id: 'fees', route: '/fees', title: 'Boletas de honorarios', area: 'Compras y gastos', roles: R.acc, dev: 'desarrollo', real: 'Rota', mapa: '§2.3', code: '/fees', missing: 'GET /fees no existe. Importar es simulado aunque existe POST /fees/import. Hoy no tiene acceso desde el menú. La retención usa parámetros de 2024.', src: M + ' §2.3, §3, §5; RIESGOS R-07' },

    // Tributario
    { id: 'compliance', route: '/compliance', title: 'Salud tributaria', area: 'Tributario (SII)', roles: R.acc, dev: 'disponible', real: 'Conectada', mapa: '§2.3', code: '/compliance', missing: 'Conectada, pero hoy sin acceso desde el menú. El acuse de recibo es simulado.', src: M + ' §2.3, §3; RIESGOS (brechas)' },
    { id: 'f29', route: '/accounting/f29', title: 'Formulario 29', area: 'Tributario (SII)', roles: R.acc, dev: 'disponible', real: 'Conectada', mapa: '§2.2', code: '/accounting/f29', missing: 'Conectada (vista previa y envío). Calcula con los parámetros tributarios de 2024.', src: M + ' §2.2; PLAN B-14; RIESGOS R-07' },
    { id: 'caf', route: '/sii/caf', title: 'Folios (CAF)', area: 'Tributario (SII)', roles: R.acc, dev: 'parcial', real: 'Parcial', mapa: '§2.3', code: '/sii/caf', missing: 'La carga de CAF funciona. No existe un endpoint para listar los CAF ni los folios disponibles: la lista y el medidor son del rediseño.', src: M + ' §2.3; PLAN B-03' },
    { id: 'sii', route: '/sii', title: 'Sincronización SII', area: 'Tributario (SII)', roles: R.acc, dev: 'desarrollo', real: 'Rota', mapa: '§2.3', code: '/sii', missing: 'Llama a /sii/status y /sii/sync, que no existen. Debe usar POST /sii/ops/fetch-rcv, que sí existe.', src: M + ' §2.3, §4; PLAN B-05' },
    { id: 'received', route: '/sii/received', title: 'Documentos recibidos', area: 'Tributario (SII)', roles: R.acc, dev: 'planificado', real: 'Propuesta nueva', mapa: 'propuesta', missing: 'La recepción de DTE de terceros y el acuse o reclamo (Ley 19.983) no existen en el backend.', src: 'RIESGOS (brechas funcionales); PLAN §7.3' },

    // Contabilidad
    { id: 'ledger', route: '/accounting/ledger', title: 'Libro diario', area: 'Contabilidad', roles: R.acc, dev: 'desarrollo', real: 'Rota', mapa: '§2.2', code: '/accounting/ledger', missing: 'El hook devuelve una lista vacía fija; GET /accounting/entries existe sin uso. El botón de limpieza de noviembre se retira.', src: M + ' §2.2, §6; PLAN B-11' },
    { id: 'accounts', route: '/accounting/accounts', title: 'Plan de cuentas y libro mayor', area: 'Contabilidad', roles: R.acc, dev: 'planificado', real: 'Solo backend', mapa: 'backend', missing: 'El backend tiene /accounting/accounts, /ledger/{accountCode} y /opening-balance; no hay pantalla.', src: M + ' §6 (Contabilidad)' },
    { id: 'balance', route: '/accounting/balance', title: 'Balance', area: 'Contabilidad', roles: R.acc, dev: 'parcial', real: 'Parcial', mapa: '§2.2', code: '/accounting/balance', missing: 'El balance está conectado; el árbol de cuentas usa datos mock y GET /accounting/accounts/tree no existe.', src: M + ' §2.2, §4' },
    { id: 'income', route: '/accounting/income', title: 'Estado de resultados', area: 'Contabilidad', roles: R.accAud, dev: 'disponible', real: 'Conectada', mapa: '§2.2', code: '/accounting/income', missing: 'Conectada a /reports/income-statement. El backend tiene un segundo endpoint duplicado.', src: M + ' §2.2' },
    { id: 'partners', route: '/accounting/partners', title: 'Cuentas corrientes', area: 'Contabilidad', roles: R.acc, dev: 'disponible', real: 'Conectada', mapa: '§2.2', code: '/accounting/partners', missing: 'Saldos conectados. El detalle de movimientos usa /{rut}/movements, que existe sin uso.', src: M + ' §2.2, §6' },
    { id: 'assets', route: '/accounting/assets', title: 'Activos fijos', area: 'Contabilidad', roles: R.acc, dev: 'parcial', real: 'Parcial', mapa: '§2.2', code: '/accounting/assets', missing: 'Lista y depreciación funcionan. Crear activos no está en la pantalla aunque existe POST /fixed-assets; editar y eliminar por id no existen.', src: M + ' §2.2, §4' },
    { id: 'close', route: '/accounting/close', title: 'Cierres', area: 'Contabilidad', roles: R.acc, dev: 'parcial', real: 'Parcial', mapa: '§2.2', code: '/accounting/close', missing: 'Auditoría y ejecución del cierre conectadas; quedan restos de mock. El estado de períodos existe en el backend sin uso.', src: M + ' §2.2, §6' },
    { id: 'annual-close', route: '/accounting/annual-close', title: 'Cierre anual (F22)', area: 'Contabilidad', navParent: 'close', roles: R.acc, dev: 'parcial', real: 'Parcial', mapa: '§2.2', code: '/accounting/annual-close', missing: 'Usa la auditoría del cierre de período; no calcula el F22 aunque existe POST /annual-close/f22/calculate/{año}.', src: M + ' §2.2, §6' },

    // Tesorería
    { id: 'treasury', route: '/treasury', title: 'Flujo de caja', area: 'Tesorería y bancos', roles: R.treas, dev: 'parcial', real: 'Con fallas', mapa: '§2.4', code: '/treasury', missing: 'Pronóstico IA conectado. Tipos de cambio llaman a un endpoint inexistente. Registrar movimiento usa /cash-entries y el backend tiene /cash-entry. Subir cartola no tiene backend.', src: M + ' §2.4, §4, §5' },
    { id: 'banking', route: '/banking', title: 'Bancos', area: 'Tesorería y bancos', roles: R.bank, dev: 'disponible', real: 'Conectada', mapa: '§2.4', code: '/banking', missing: 'Conectada, pero hoy sin acceso desde el menú. Es el paso previo a la conciliación.', src: M + ' §2.4, §3' },
    { id: 'reconciliation', route: '/banking/reconciliation', title: 'Conciliación bancaria', area: 'Tesorería y bancos', roles: R.bank, dev: 'disponible', real: 'Conectada', mapa: '§2.4', code: '/banking/reconciliation', missing: 'Conectada. El contador no tiene acceso en el backend (decisión pendiente D-06).', src: M + ' §2.4; PLAN D-06' },

    // Personas
    { id: 'payroll', route: '/payroll', title: 'Remuneraciones', area: 'Personas', roles: R.hr, dev: 'parcial', real: 'Con fallas', mapa: '§2.5', code: '/payroll', missing: 'La lista de empleados llama a un endpoint inexistente. El cálculo usa periodId 1 fijo. Previred funciona; el PDF de liquidación no.', src: M + ' §2.5, §4' },
    { id: 'employee', route: '/employee', title: 'Mi portal', area: 'Personas', layout: 'employee', roles: R.employee, dev: 'desarrollo', real: 'Rota para su público', mapa: '§2.5', code: '/employee', missing: 'El endpoint de liquidaciones exige ADMIN o HR: un empleado recibe error de permiso.', src: M + ' §2.5; PLAN B-07; PayrollController.java:20' },

    // Operaciones
    { id: 'inventory', route: '/inventory', title: 'Inventario', area: 'Operaciones', roles: R.ops, dev: 'parcial', real: 'Parcial', mapa: '§2.6', code: '/inventory', missing: 'La lista funciona. Exportar a Excel falla. Crear, editar y registrar movimientos existen en el backend y no en la pantalla.', src: M + ' §2.6, §4, §6' },
    { id: 'projects', route: '/projects', title: 'Centros de costo', area: 'Operaciones', roles: R.ops, dev: 'disponible', real: 'Conectada', mapa: '§2.2', code: '/projects', missing: 'Conectada. Hoy está en el menú de Contabilidad, pero el contador no tiene acceso.', src: M + ' §2.2, §7; PLAN D-06' },

    // Auditoría
    { id: 'audit', route: '/accounting/audit', title: 'Auditoría', area: 'Auditoría y control', roles: R.audit, dev: 'disponible', real: 'Conectada', mapa: '§2.2', code: '/accounting/audit', missing: 'La conciliación tributaria está conectada, pero usa /accounting/f29, que el auditor no puede llamar. Alertas, duplicados y espejo SII existen en el backend (/audit/*) sin pantalla.', src: M + ' §2.2, §6, §7; PLAN §3.4' },
    { id: 'sii-audit', route: '/sii/audit', title: 'Auditoría SII', area: 'Auditoría y control', navParent: 'audit', roles: R.audit, dev: 'disponible', real: 'Conectada', mapa: '§2.3', code: '/sii/audit', missing: 'Duplicado de /accounting/audit. En el rediseño redirige a Auditoría, pestaña Espejo SII.', src: M + ' §2.3, §9; PLAN §3.4' },
    { id: 'rules', route: '/audit/rules', title: 'Reglas de clasificación', area: 'Auditoría y control', roles: R.acc, dev: 'planificado', real: 'Solo backend', mapa: 'backend', missing: 'El CRUD /learning/rules existe, pero el update no guarda nada y la lectura por id no filtra por empresa.', src: M + ' §6; RIESGOS R-18, R-19' },

    // Documentos y reportes
    { id: 'documents', route: '/documents', title: 'Gestor documental', area: 'Documentos y reportes', roles: R.acc, dev: 'parcial', real: 'Parcial', mapa: '§2.7', code: '/documents', missing: 'Lista y análisis funcionan. No hay subida en la pantalla; la extracción de facturas con IA (/dms/ai-invoicing/process) existe sin uso.', src: M + ' §2.7, §6' },
    { id: 'reports', route: '/reports', title: 'Reportes', area: 'Documentos y reportes', roles: R.accAud, dev: 'parcial', real: 'Parcial', mapa: '§2.9', code: '/reports', missing: 'El reporte de ventas en Excel funciona; los demás tipos devuelven vacío. Reporte semanal y balance existen en el backend sin uso.', src: M + ' §2.9, §6' },
    { id: 'knowledge', route: '/ai/knowledge', title: 'Base de conocimiento IA', area: 'Documentos y reportes', roles: R.admin, dev: 'planificado', real: 'Solo backend', mapa: 'backend', missing: 'El backend tiene /ai/knowledge (subir y buscar, solo ADMIN). Los embeddings se guardan en memoria global, compartida entre empresas.', src: M + ' §6; RIESGOS R-12' },

    // Módulos
    { id: 'pos', route: '/pos', title: 'Punto de venta', area: 'Módulos', module: 'pos', roles: R.pos, dev: 'parcial', real: 'Con fallas', mapa: '§2.1', code: '/pos', missing: 'Ventas y sincronización funcionan. La búsqueda de producto por código llama a un endpoint inexistente. Las boletas se envían por el canal de facturas.', src: M + ' §2.1, §4; RIESGOS R-06' },
    { id: 'b2b-orders', route: '/b2b/orders', title: 'Pedidos B2B', area: 'Módulos', module: 'b2b', roles: R.pos, dev: 'planificado', real: 'Propuesta nueva', mapa: 'propuesta', missing: 'Vista interna de los pedidos del portal. El backend tiene los endpoints del cliente (modules/b2b) sin restricción de rol; falta la vista para el personal interno.', src: 'PLAN §3.3, §7.3, B-08' },
    { id: 'portal', route: '/portal', title: 'Portal B2B', area: 'Módulos', layout: 'b2b', module: 'b2b', roles: R.portal, dev: 'disponible', real: 'Conectada', mapa: '§2.1', code: '/portal', missing: 'Conectado (pedidos, crear y pagar), pero el backend no restringe el rol: cualquier usuario autenticado puede usarlo.', src: M + ' §2.1, §7; RIESGOS R-23' },
    { id: 'wms', route: '/wms', title: 'Bodega (WMS)', area: 'Módulos', module: 'wms', roles: R.ops, dev: 'parcial', real: 'Parcial', mapa: '§2.6', code: '/wms', missing: 'Solo lectura y completar picking. Crear ubicaciones y picking existe en el backend sin pantalla.', src: M + ' §2.6, §6' },
    { id: 'mrp', route: '/mrp', title: 'Producción (MRP)', area: 'Módulos', module: 'mrp', roles: R.ops, dev: 'parcial', real: 'Parcial', mapa: '§2.6', code: '/mrp', missing: 'Solo lectura y completar orden. Crear listas de materiales y órdenes existe en el backend sin pantalla.', src: M + ' §2.6, §6' },
    { id: 'fleet', route: '/fleet', title: 'Flota', area: 'Módulos', module: 'fleet', roles: R.ops, dev: 'disponible', real: 'Conectada', mapa: '§2.6', code: '/fleet', missing: 'Conectada a /fleet/metrics, el único endpoint del módulo. Hoy sin acceso desde el menú.', src: M + ' §2.6, §3' },
    { id: 'energy', route: '/energy', title: 'Energía', area: 'Módulos', module: 'energy', roles: R.energy, dev: 'desarrollo', real: 'Rota', mapa: '§2.8', code: '/energy', missing: 'Llama a cuatro endpoints /energy/* que no existen y no usa ninguno de los 70 reales (cargadores, precios, mantenimiento, OCPP). Decisión pendiente D-05.', src: M + ' §2.8, §4, §6; PLAN D-05' },
    { id: 'sustainability', route: '/sustainability', title: 'Sostenibilidad', area: 'Módulos', module: 'sustainability', roles: R.sust, dev: 'disponible', real: 'Conectada', mapa: '§2.8', code: '/sustainability', missing: 'Conectada (puntaje, reporte ESG y simulador Corfo). Hoy sin acceso desde el menú.', src: M + ' §2.8, §3' },

    // Configuración
    { id: 'settings', route: '/settings', title: 'Configuración', area: 'Configuración', roles: R.admin, dev: 'parcial', real: 'Con fallas', mapa: '§2.9', code: '/settings', missing: 'Perfil, usuarios e invitar funcionan. Eliminar usuario y guardar preferencias llaman a endpoints inexistentes. Roles, organizaciones y módulos son del rediseño.', src: M + ' §2.9, §4; PLAN D-04' },
    { id: 'settings-company', route: '/settings/company', title: 'Perfil de empresa', area: 'Configuración', navParent: 'settings', roles: R.admin, dev: 'disponible', real: 'Conectada', mapa: '§2.9', code: '/settings/company', missing: 'Duplicado de /settings. En el rediseño es la pestaña Empresa de Configuración.', src: M + ' §2.9, §3' },
    { id: 'notifications', route: '/notifications', title: 'Notificaciones', area: 'Configuración', roles: R.all, dev: 'planificado', real: 'Solo backend', mapa: 'backend', missing: 'El backend tiene /notifications/subscribe y preferencias, sin restricción de rol. Guardar preferencias desde el front falla.', src: M + ' §6; RIESGOS R-23' },

    // Prototipo
    { id: 'mapa', route: '/mapa', title: 'Mapa del prototipo', area: 'Prototipo', roles: null, stateful: false }
  ];
  var BY_ID = {}; SCREENS.forEach(function (s) { BY_ID[s.id] = s; if (s.module) s.moduleLabel = MODULE_LABEL[s.module]; });

  // Navegación (PLAN §3.1)
  var NAV = [
    { id: 'g-home', label: 'Inicio', icon: 'home', items: ['dashboard'], flat: true },
    { id: 'g-sales', label: 'Ventas', icon: 'receipt', items: ['invoices', 'quotes', 'leads'] },
    { id: 'g-buy', label: 'Compras y gastos', icon: 'cart', items: ['procurement', 'fees'] },
    { id: 'g-tax', label: 'Tributario (SII)', icon: 'landmark', items: ['compliance', 'f29', 'caf', 'sii', 'received'] },
    { id: 'g-acc', label: 'Contabilidad', icon: 'book', items: ['ledger', 'accounts', 'balance', 'income', 'partners', 'assets', 'close'] },
    { id: 'g-treas', label: 'Tesorería y bancos', icon: 'wallet', items: ['treasury', 'banking', 'reconciliation'] },
    { id: 'g-people', label: 'Personas', icon: 'users', items: ['payroll'] },
    { id: 'g-ops', label: 'Operaciones', icon: 'package', items: ['inventory', 'projects'] },
    { id: 'g-audit', label: 'Auditoría y control', icon: 'shield', items: ['audit', 'rules'] },
    { id: 'g-docs', label: 'Documentos y reportes', icon: 'folder', items: ['documents', 'reports', 'knowledge'] },
    { id: 'g-mod', label: 'Módulos', icon: 'layers', items: ['pos', 'b2b-orders', 'wms', 'mrp', 'fleet', 'energy', 'sustainability'] },
    { id: 'g-set', label: 'Configuración', icon: 'settings', items: ['settings'], flat: true }
  ];
  var NAV_LABEL = { invoices: 'Facturas y documentos', compliance: 'Salud tributaria', caf: 'Folios (CAF)', sii: 'Sincronización SII', close: 'Cierres', treasury: 'Flujo de caja', projects: 'Centros de costo', accounts: 'Plan de cuentas y mayor' };

  function effectiveRole(role) { return EFFECTIVE[role] || role; }
  function canAccess(sc, role) {
    if (!sc.roles) return true;
    var r = effectiveRole(role);
    return sc.roles.indexOf(r) >= 0;
  }
  function homeFor(role) {
    var r = effectiveRole(role);
    if (r === 'USER') return '/employee';
    if (r === 'B2B_CLIENT') return '/portal';
    return '/dashboard';
  }

  window.REG = {
    ROLES: ROLES, PERSONAS: PERSONAS, ROLE_LABEL: ROLE_LABEL, USERS_BY_ROLE: USERS_BY_ROLE, MODULES: MODULES, MODULE_LABEL: MODULE_LABEL,
    SCREENS: SCREENS, BY_ID: BY_ID, NAV: NAV, NAV_LABEL: NAV_LABEL, effectiveRole: effectiveRole, canAccess: canAccess, homeFor: homeFor, R: R
  };
})();
