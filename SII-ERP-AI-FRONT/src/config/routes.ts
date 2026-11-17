export const APP_ROUTES = {
    PUBLIC: {
        LOGIN: '/login',
        REGISTER: '/register',
    },
    PROTECTED: {
        DASHBOARD: '/dashboard',
        PORTAL: '/portal',
        INVOICES: {
            LIST: '/invoices',
            NEW: '/invoices/new',
            DETAIL: (id: string) => `/invoices/${id}`,
        },
        CRM: {
            QUOTES: '/crm/quotes',
            LEADS: '/crm/leads',
        },
        PURCHASES: {
            PROCUREMENT: '/procurement',
            FEES: '/fees',
        },
        TAX: {
            COMPLIANCE: '/compliance',
            F29: '/accounting/f29',
            CAF: '/sii/caf',
            SYNC: '/sii',
        },
        ACCOUNTING: {
            LEDGER: '/accounting/ledger',
            BALANCE: '/accounting/balance',
            INCOME: '/accounting/income',
            PARTNERS: '/accounting/partners',
            ASSETS: '/accounting/assets',
            CLOSE: '/accounting/close',
            ANNUAL_CLOSE: '/accounting/annual-close',
            AUDIT: '/accounting/audit',
        },
        TREASURY: {
            CASH_FLOW: '/treasury',
            BANKING: '/banking',
            RECONCILIATION: '/banking/reconciliation',
        },
        PEOPLE: {
            PAYROLL: '/payroll',
        },
        OPERATIONS: {
            INVENTORY: '/inventory',
            PROJECTS: '/projects',
        },
        DOCUMENTS: {
            MANAGER: '/documents',
            REPORTS: '/reports',
        },
        MODULES: {
            POS: '/pos',
            WMS: '/wms',
            MRP: '/mrp',
            FLEET: '/fleet',
            ENERGY: '/energy',
            SUSTAINABILITY: '/sustainability',
        },
        SETTINGS: '/settings',
    },
} as const;

export const API_ENDPOINTS = {
    AUTH: {
        LOGIN: '/auth/login',
        REGISTER: '/auth/register',
    },
    INVOICES: {
        BASE: '/invoices',
        IMPORT: '/invoices/import',
        SEND: (id: string) => `/invoices/${id}/send`,
    },
    FEES: {
        BASE: '/fees',
        IMPORT: '/fees/import',
    },
    COMPLIANCE: {
        DJ1879: (year: number) => `/compliance/dj1879/${year}`,
    },
    SUSTAINABILITY: {
        DASHBOARD: '/sustainability/dashboard',
        ECO_TIPS: '/sustainability/eco-tips',
    },
} as const;

/**
 * Roles que pueden ver cada módulo, según `MAPA-FUNCIONAL.md` (derivado de
 * los `@PreAuthorize` del backend). Es una copia local del union de
 * `features/auth/types` para no invertir la dependencia config → features.
 */
export type NavRole =
    | 'ADMIN'
    | 'ACCOUNTANT'
    | 'TREASURER'
    | 'HR'
    | 'SALES'
    | 'OPERATIONS'
    | 'AUDITOR'
    | 'USER';

export type NavIconName =
    | 'dashboard'
    | 'sales'
    | 'purchases'
    | 'tax'
    | 'accounting'
    | 'treasury'
    | 'people'
    | 'operations'
    | 'audit'
    | 'documents'
    | 'modules'
    | 'settings';

export interface NavItem {
    label: string;
    href: string;
    roles: NavRole[];
}

export interface NavGroup {
    label: string;
    icon: NavIconName;
    items: NavItem[];
}

const ALL_ROLES: NavRole[] = [
    'ADMIN',
    'ACCOUNTANT',
    'TREASURER',
    'HR',
    'SALES',
    'OPERATIONS',
    'AUDITOR',
    'USER',
];

const ADMIN: NavRole[] = ['ADMIN'];
const ADMIN_ACCOUNTANT: NavRole[] = ['ADMIN', 'ACCOUNTANT'];
const ADMIN_ACCOUNTANT_AUDITOR: NavRole[] = ['ADMIN', 'ACCOUNTANT', 'AUDITOR'];
const ADMIN_ACCOUNTANT_SALES: NavRole[] = ['ADMIN', 'ACCOUNTANT', 'SALES'];
const ADMIN_ACCOUNTANT_TREASURER: NavRole[] = ['ADMIN', 'ACCOUNTANT', 'TREASURER'];
const ADMIN_HR: NavRole[] = ['ADMIN', 'HR'];
const ADMIN_OPERATIONS: NavRole[] = ['ADMIN', 'OPERATIONS'];
const ADMIN_OPERATIONS_AUDITOR: NavRole[] = ['ADMIN', 'OPERATIONS', 'AUDITOR'];
const ADMIN_SALES: NavRole[] = ['ADMIN', 'SALES'];
const ADMIN_TREASURER: NavRole[] = ['ADMIN', 'TREASURER'];

/**
 * Estructura única del menú lateral (PLAN-REDISENO.md §3.1).
 * Los grupos sin ítems visibles para el rol del usuario se ocultan.
 */
export const NAV_GROUPS: NavGroup[] = [
    {
        label: 'Inicio',
        icon: 'dashboard',
        items: [
            { label: 'Panel', href: APP_ROUTES.PROTECTED.DASHBOARD, roles: ALL_ROLES },
        ],
    },
    {
        label: 'Ventas',
        icon: 'sales',
        items: [
            { label: 'Facturas y documentos', href: APP_ROUTES.PROTECTED.INVOICES.LIST, roles: ADMIN_ACCOUNTANT_SALES },
            { label: 'Cotizaciones', href: APP_ROUTES.PROTECTED.CRM.QUOTES, roles: ADMIN_SALES },
            { label: 'Oportunidades', href: APP_ROUTES.PROTECTED.CRM.LEADS, roles: ADMIN_SALES },
        ],
    },
    {
        label: 'Compras y gastos',
        icon: 'purchases',
        items: [
            { label: 'Órdenes de compra', href: APP_ROUTES.PROTECTED.PURCHASES.PROCUREMENT, roles: ADMIN_OPERATIONS },
            { label: 'Boletas de honorarios', href: APP_ROUTES.PROTECTED.PURCHASES.FEES, roles: ADMIN_ACCOUNTANT },
        ],
    },
    {
        label: 'Tributario (SII)',
        icon: 'tax',
        items: [
            { label: 'Salud tributaria', href: APP_ROUTES.PROTECTED.TAX.COMPLIANCE, roles: ADMIN_ACCOUNTANT },
            { label: 'Formulario 29', href: APP_ROUTES.PROTECTED.TAX.F29, roles: ADMIN_ACCOUNTANT },
            { label: 'Folios CAF', href: APP_ROUTES.PROTECTED.TAX.CAF, roles: ADMIN_ACCOUNTANT },
            { label: 'Sincronización SII', href: APP_ROUTES.PROTECTED.TAX.SYNC, roles: ADMIN_ACCOUNTANT },
        ],
    },
    {
        label: 'Contabilidad',
        icon: 'accounting',
        items: [
            { label: 'Libro diario', href: APP_ROUTES.PROTECTED.ACCOUNTING.LEDGER, roles: ADMIN_ACCOUNTANT },
            { label: 'Balance', href: APP_ROUTES.PROTECTED.ACCOUNTING.BALANCE, roles: ADMIN_ACCOUNTANT },
            { label: 'Estado de resultados', href: APP_ROUTES.PROTECTED.ACCOUNTING.INCOME, roles: ADMIN_ACCOUNTANT_AUDITOR },
            { label: 'Cuentas corrientes', href: APP_ROUTES.PROTECTED.ACCOUNTING.PARTNERS, roles: ADMIN_ACCOUNTANT },
            { label: 'Activos fijos', href: APP_ROUTES.PROTECTED.ACCOUNTING.ASSETS, roles: ADMIN_ACCOUNTANT },
            { label: 'Cierres de período', href: APP_ROUTES.PROTECTED.ACCOUNTING.CLOSE, roles: ADMIN_ACCOUNTANT },
            { label: 'Cierre anual', href: APP_ROUTES.PROTECTED.ACCOUNTING.ANNUAL_CLOSE, roles: ADMIN_ACCOUNTANT },
        ],
    },
    {
        label: 'Tesorería y bancos',
        icon: 'treasury',
        items: [
            { label: 'Flujo de caja', href: APP_ROUTES.PROTECTED.TREASURY.CASH_FLOW, roles: ADMIN_ACCOUNTANT_TREASURER },
            { label: 'Bancos', href: APP_ROUTES.PROTECTED.TREASURY.BANKING, roles: ADMIN_TREASURER },
            { label: 'Conciliación bancaria', href: APP_ROUTES.PROTECTED.TREASURY.RECONCILIATION, roles: ADMIN_TREASURER },
        ],
    },
    {
        label: 'Personas',
        icon: 'people',
        items: [
            { label: 'Remuneraciones', href: APP_ROUTES.PROTECTED.PEOPLE.PAYROLL, roles: ADMIN_HR },
        ],
    },
    {
        label: 'Operaciones',
        icon: 'operations',
        items: [
            { label: 'Inventario', href: APP_ROUTES.PROTECTED.OPERATIONS.INVENTORY, roles: ADMIN_OPERATIONS },
            { label: 'Centros de costo', href: APP_ROUTES.PROTECTED.OPERATIONS.PROJECTS, roles: ADMIN_OPERATIONS },
        ],
    },
    {
        label: 'Auditoría y control',
        icon: 'audit',
        items: [
            { label: 'Auditoría', href: APP_ROUTES.PROTECTED.ACCOUNTING.AUDIT, roles: ADMIN_ACCOUNTANT },
        ],
    },
    {
        label: 'Documentos y reportes',
        icon: 'documents',
        items: [
            { label: 'Gestor documental', href: APP_ROUTES.PROTECTED.DOCUMENTS.MANAGER, roles: ADMIN_ACCOUNTANT },
            { label: 'Reportes', href: APP_ROUTES.PROTECTED.DOCUMENTS.REPORTS, roles: ADMIN_ACCOUNTANT_AUDITOR },
        ],
    },
    {
        label: 'Módulos',
        icon: 'modules',
        items: [
            { label: 'POS', href: APP_ROUTES.PROTECTED.MODULES.POS, roles: ADMIN_SALES },
            { label: 'Bodega (WMS)', href: APP_ROUTES.PROTECTED.MODULES.WMS, roles: ADMIN_OPERATIONS },
            { label: 'Producción (MRP)', href: APP_ROUTES.PROTECTED.MODULES.MRP, roles: ADMIN_OPERATIONS },
            { label: 'Flota', href: APP_ROUTES.PROTECTED.MODULES.FLEET, roles: ADMIN_OPERATIONS },
            { label: 'Energía', href: APP_ROUTES.PROTECTED.MODULES.ENERGY, roles: ADMIN_OPERATIONS_AUDITOR },
            { label: 'Sostenibilidad', href: APP_ROUTES.PROTECTED.MODULES.SUSTAINABILITY, roles: ADMIN_OPERATIONS_AUDITOR },
        ],
    },
    {
        label: 'Configuración',
        icon: 'settings',
        items: [
            { label: 'Configuración', href: APP_ROUTES.PROTECTED.SETTINGS, roles: ADMIN },
        ],
    },
];
