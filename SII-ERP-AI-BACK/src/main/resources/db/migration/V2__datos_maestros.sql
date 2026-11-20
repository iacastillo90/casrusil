-- ============================================================================
-- MASTER DATA SEED MIGRATION
-- Idempotent seed for configuration/master tables only
-- Excludes: transactional data (invoices, payments, bank txns, charging sessions, etc.)
-- ============================================================================

-- Ensure required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================================
-- SSO SCHEMA: Companies (Tenants)
-- ============================================================================
INSERT INTO sso.companies (id, rut, razon_social, email, is_active, created_at, commercial_address, website, phone_number, logo_url, is_profile_complete)
VALUES
    ('00000000-0000-0000-0000-000000000001'::uuid, '76123456-7', 'Empresa Demo S.A.', 'admin@demo.cl', true, now(), 'Av. Providencia 123, Santiago', 'https://demo.cl', '+56223456789', null, true),
    ('00000000-0000-0000-0000-000000000002'::uuid, '76987654-3', 'Constructora Los Andes Ltda.', 'contacto@losandes.cl', true, now(), 'Los Andes 456, Santiago', 'https://losandes.cl', '+56229876543', null, true)
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- SSO SCHEMA: SII Certificates (Digital Certificates for Electronic Invoicing)
-- ============================================================================
-- Note: certificate_data is bytea, password is stored encrypted in practice
INSERT INTO sso.company_certificates (id, company_id, certificate_data, password, valid_until)
VALUES
    ('11111111-1111-1111-1111-111111111111'::uuid, '00000000-0000-0000-0000-000000000001'::uuid, decode('', 'hex'), 'changeme', '2026-12-31'),
    ('22222222-2222-2222-2222-222222222222'::uuid, '00000000-0000-0000-0000-000000000002'::uuid, decode('', 'hex'), 'changeme', '2026-12-31')
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- SSO SCHEMA: SSO Users (System Operators)
-- ============================================================================
-- password_hash should be bcrypt in production
INSERT INTO sso.users (id, email, full_name, password_hash, role, company_id, is_active, created_at)
VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'::uuid, 'admin@demo.cl', 'Administrador Demo', '$2a$10$dummyhash', 'ADMIN', '00000000-0000-0000-0000-000000000001'::uuid, true, now()),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'::uuid, 'contador@demo.cl', 'Contador Demo', '$2a$10$dummyhash', 'ACCOUNTANT', '00000000-0000-0000-0000-000000000001'::uuid, true, now()),
    ('cccccccc-cccc-cccc-cccc-cccccccccccc'::uuid, 'admin@losandes.cl', 'Admin Los Andes', '$2a$10$dummyhash', 'ADMIN', '00000000-0000-0000-0000-000000000002'::uuid, true, now())
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- PUBLIC SCHEMA: Organizations (from RBAC migration V4)
-- ============================================================================
INSERT INTO organizations (id, name, type, parent_id, status, settings, branding, created_at, updated_at)
VALUES
    ('10000000-0000-0000-0000-000000000001'::uuid, 'Demo Organization', 'SMB', null, 'ACTIVE', '{}'::jsonb, '{}'::jsonb, now(), now()),
    ('10000000-0000-0000-0000-000000000002'::uuid, 'Los Andes Organization', 'SMB', null, 'ACTIVE', '{}'::jsonb, '{}'::jsonb, now(), now())
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- PUBLIC SCHEMA: Roles (System & Organization Roles)
-- ============================================================================
INSERT INTO roles (id, organization_id, name, description, is_system_role, permissions, created_at, updated_at)
VALUES
    -- System roles (organization_id = null)
    ('20000000-0000-0000-0000-000000000001'::uuid, null, 'SUPER_ADMIN', 'Full system access across all organizations', true, '{"*": ["*"]}'::jsonb, now(), now()),
    ('20000000-0000-0000-0000-000000000002'::uuid, null, 'ADMIN', 'Organization administrator', true, '{"users": ["read", "write", "delete"], "roles": ["read", "write"], "settings": ["read", "write"]}'::jsonb, now(), now()),
    ('20000000-0000-0000-0000-000000000003'::uuid, null, 'ACCOUNTANT', 'Accounting module access', true, '{"accounting": ["read", "write"], "invoicing": ["read", "write"], "tax": ["read", "write"]}'::jsonb, now(), now()),
    ('20000000-0000-0000-0000-000000000004'::uuid, null, 'USER', 'Standard user', true, '{"profile": ["read", "write"], "invoicing": ["read", "create"]}'::jsonb, now(), now()),
    -- Organization-specific roles
    ('20000000-0000-0000-0000-000000000011'::uuid, '10000000-0000-0000-0000-000000000001'::uuid, 'GERENTE', 'Gerente General Demo', false, '{"*": ["read", "write"]}'::jsonb, now(), now()),
    ('20000000-0000-0000-0000-000000000012'::uuid, '10000000-0000-0000-0000-000000000001'::uuid, 'VENDEDOR', 'Vendedor Demo', false, '{"invoicing": ["read", "create"], "customers": ["read", "write"]}'::jsonb, now(), now()),
    ('20000000-0000-0000-0000-000000000021'::uuid, '10000000-0000-0000-0000-000000000002'::uuid, 'GERENTE', 'Gerente Los Andes', false, '{"*": ["read", "write"]}'::jsonb, now(), now())
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- PUBLIC SCHEMA: Users (Application Users)
-- ============================================================================
-- password_hash should be bcrypt in production
INSERT INTO users (id, organization_id, email, password_hash, first_name, last_name, phone, status, email_verified, last_login_at, created_at, updated_at)
VALUES
    ('30000000-0000-0000-0000-000000000001'::uuid, '10000000-0000-0000-0000-000000000001'::uuid, 'admin@demo.cl', '$2a$10$dummyhash', 'Admin', 'Demo', '+56912345678', 'ACTIVE', true, now(), now(), now()),
    ('30000000-0000-0000-0000-000000000002'::uuid, '10000000-0000-0000-0000-000000000001'::uuid, 'contador@demo.cl', '$2a$10$dummyhash', 'Juan', 'Pérez', '+56987654321', 'ACTIVE', true, now(), now(), now()),
    ('30000000-0000-0000-0000-000000000003'::uuid, '10000000-0000-0000-0000-000000000001'::uuid, 'vendedor@demo.cl', '$2a$10$dummyhash', 'María', 'González', '+56911223344', 'ACTIVE', true, now(), now(), now()),
    ('30000000-0000-0000-0000-000000000004'::uuid, '10000000-0000-0000-0000-000000000002'::uuid, 'admin@losandes.cl', '$2a$10$dummyhash', 'Carlos', 'López', '+56955667788', 'ACTIVE', true, now(), now(), now())
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- PUBLIC SCHEMA: User-Role Assignments
-- ============================================================================
INSERT INTO user_roles (user_id, role_id)
VALUES
    ('30000000-0000-0000-0000-000000000001'::uuid, '20000000-0000-0000-0000-000000000002'::uuid),
    ('30000000-0000-0000-0000-000000000001'::uuid, '20000000-0000-0000-0000-000000000011'::uuid),
    ('30000000-0000-0000-0000-000000000002'::uuid, '20000000-0000-0000-0000-000000000003'::uuid),
    ('30000000-0000-0000-0000-000000000003'::uuid, '20000000-0000-0000-0000-000000000004'::uuid),
    ('30000000-0000-0000-0000-000000000003'::uuid, '20000000-0000-0000-0000-000000000012'::uuid),
    ('30000000-0000-0000-0000-000000000004'::uuid, '20000000-0000-0000-0000-000000000002'::uuid),
    ('30000000-0000-0000-0000-000000000004'::uuid, '20000000-0000-0000-0000-000000000021'::uuid)
ON CONFLICT (user_id, role_id) DO NOTHING;

-- ============================================================================
-- ACCOUNTING SCHEMA: Chilean Chart of Accounts (Plan de Cuentas Chileno)
-- Per company - using CompanyId from sso.companies
-- ============================================================================
DO $$
DECLARE
    v_company_id uuid;
BEGIN
    FOR v_company_id IN SELECT id FROM sso.companies WHERE is_active LOOP
        -- ACTIVOS (Assets) - Level 1
        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '1', 'ACTIVO', 'ASSET', 'Activos totales', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '11', 'ACTIVO CIRCULANTE', 'ASSET', 'Activos corrientes', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '1101', 'Caja', 'ASSET', 'Efectivo en caja', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '110101', 'Caja Moneda Nacional', 'ASSET', 'Efectivo en pesos chilenos', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '1102', 'Banco', 'ASSET', 'Cuentas bancarias', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '110201', 'Banco Cuenta Corriente', 'ASSET', 'Cuenta corriente bancaria', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '1105', 'Clientes', 'ASSET', 'Cuentas por cobrar a clientes', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '110501', 'Clientes Nacionales', 'ASSET', 'Cuentas por cobrar clientes nacionales', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '1108', 'IVA Crédito Fiscal', 'ASSET', 'IVA por recuperar', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '110801', 'IVA Crédito Fiscal del Mes', 'ASSET', 'IVA crédito fiscal del período', true)
        ON CONFLICT DO NOTHING;

        -- PASIVOS (Liabilities) - Level 1
        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '2', 'PASIVO', 'LIABILITY', 'Pasivos totales', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '21', 'PASIVO CIRCULANTE', 'LIABILITY', 'Pasivos corrientes', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '2101', 'Proveedores', 'LIABILITY', 'Cuentas por pagar a proveedores', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '210101', 'Proveedores Nacionales', 'LIABILITY', 'Cuentas por pagar proveedores nacionales', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '2104', 'IVA Débito Fiscal', 'LIABILITY', 'IVA por pagar', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '210401', 'IVA Débito Fiscal del Mes', 'LIABILITY', 'IVA débito fiscal del período', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '2105', 'Retenciones', 'LIABILITY', 'Retenciones de impuestos', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '210501', 'Retenciones de Segunda Categoría', 'LIABILITY', 'Retenciones de trabajadores dependientes', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '2106', 'Provisiones', 'LIABILITY', 'Provisiones diversas', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '210601', 'Provisión Vacaciones', 'LIABILITY', 'Provisión para vacaciones del personal', true)
        ON CONFLICT DO NOTHING;

        -- PATRIMONIO (Equity) - Level 1
        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '3', 'PATRIMONIO', 'EQUITY', 'Patrimonio total', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '31', 'CAPITAL', 'EQUITY', 'Capital social', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '3101', 'Capital', 'EQUITY', 'Capital aportado por socios', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '310101', 'Capital Pagado', 'EQUITY', 'Capital efectivamente pagado', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '33', 'RESULTADOS', 'EQUITY', 'Resultados acumulados', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '3301', 'Utilidades Acumuladas', 'EQUITY', 'Utilidades de ejercicios anteriores', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '3302', 'Utilidad del Ejercicio', 'EQUITY', 'Resultado del período actual', true)
        ON CONFLICT DO NOTHING;

        -- INGRESOS (Revenue) - Level 1
        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '4', 'INGRESOS', 'REVENUE', 'Ingresos totales', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '41', 'INGRESOS OPERACIONALES', 'REVENUE', 'Ingresos de la actividad principal', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '4101', 'Ventas', 'REVENUE', 'Ventas de bienes y servicios', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '410101', 'Ventas Nacionales', 'REVENUE', 'Ventas en el mercado nacional', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '410102', 'Ventas Exentas', 'REVENUE', 'Ventas exentas de IVA', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '42', 'INGRESOS NO OPERACIONALES', 'REVENUE', 'Otros ingresos', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '4201', 'Otros Ingresos', 'REVENUE', 'Ingresos diversos', true)
        ON CONFLICT DO NOTHING;

        -- EGRESOS/COSTOS (Expenses) - Level 1
        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '5', 'COSTOS Y GASTOS', 'EXPENSE', 'Costos y gastos totales', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '51', 'COSTO DE VENTAS', 'EXPENSE', 'Costo de los productos vendidos', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '5101', 'Costo de Ventas', 'EXPENSE', 'Costo directo de ventas', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '510101', 'Costo de Ventas General', 'EXPENSE', 'Costo de ventas general', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '52', 'GASTOS DE ADMINISTRACIÓN', 'EXPENSE', 'Gastos administrativos', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '5201', 'Remuneraciones', 'EXPENSE', 'Sueldos y salarios', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '520101', 'Sueldos', 'EXPENSE', 'Sueldos del personal', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '5202', 'Honorarios', 'EXPENSE', 'Honorarios profesionales', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '5203', 'Arriendos', 'EXPENSE', 'Gastos de arriendo', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '5204', 'Servicios Básicos', 'EXPENSE', 'Luz, agua, gas, etc.', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '520401', 'Electricidad', 'EXPENSE', 'Consumo eléctrico', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '520402', 'Agua', 'EXPENSE', 'Consumo de agua', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '520403', 'Teléfono e Internet', 'EXPENSE', 'Servicios de comunicación', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '53', 'GASTOS DE VENTAS', 'EXPENSE', 'Gastos relacionados con ventas', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '5301', 'Publicidad', 'EXPENSE', 'Gastos de marketing y publicidad', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '5302', 'Comisiones de Venta', 'EXPENSE', 'Comisiones a vendedores', true)
        ON CONFLICT DO NOTHING;

        -- Additional accounts from ensureAccountingBasics (GENESIS)
        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '1.1.03.001', 'Existencias', 'ASSET', 'Existencias de inventario', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '1.1.03.999', 'Existencias por Clasificar', 'ASSET', 'Existencias pendientes de clasificación', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '2.1.04.001', 'IVA Débito Fiscal', 'LIABILITY', 'IVA débito fiscal', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '4.1.01.001', 'Ventas Nacionales', 'REVENUE', 'Ventas nacionales', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '5.1.03.025', 'Gastos Generales', 'EXPENSE', 'Gastos generales', true)
        ON CONFLICT DO NOTHING;

        INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
        VALUES (gen_random_uuid(), v_company_id, '5.9.99.999', 'Gastos por Clasificar', 'EXPENSE', 'Gastos pendientes de clasificación', true)
        ON CONFLICT DO NOTHING;
    END LOOP;
END $$;

-- ============================================================================
-- COMPLIANCE (PUBLIC): Tax Parameters (Parámetros Tributarios)
-- ============================================================================
INSERT INTO tax_parameters (id, utm_value, uf_value, bhe_retention_rate)
VALUES
    ('YEAR_2024', 64793, 36900, 13.75),
    -- 2025: UTM enero 2025 = 67.429 y UF 01-01-2025 = 38.419,17
    -- (SII "Valores y fechas": utm/utm2025.htm, uf/uf2025.htm).
    -- Retención BHE 2025 = 14,5% (SII, boletas de honorarios).
    ('YEAR_2025', 67429, 38419.17, 14.50),
    -- 2026: UTM enero 2026 = 69.751 y UF 01-01-2026 = 39.731,79
    -- (SII "Valores y fechas": utm/utm2026.htm, uf/uf2026.htm).
    -- Retención BHE 2026 = 15,25% (SII, noticia 26-12-2025).
    ('YEAR_2026', 69751, 39731.79, 15.25)
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- ENERGY SCHEMA: Manufacturers (Fabricantes de Cargadores)
-- ============================================================================
INSERT INTO energy.manufacturers (id, name, website, support_email, support_phone, active, created_at, updated_at)
VALUES
    ('40000000-0000-0000-0000-000000000001'::uuid, 'ABB', 'https://www.abb.com', 'support@abb.com', '+56223456789', true, now(), now()),
    ('40000000-0000-0000-0000-000000000002'::uuid, 'Schneider Electric', 'https://www.se.com', 'support@se.com', '+56229876543', true, now(), now()),
    ('40000000-0000-0000-0000-000000000003'::uuid, 'Wallbox', 'https://www.wallbox.com', 'support@wallbox.com', '+34900123456', true, now(), now()),
    ('40000000-0000-0000-0000-000000000004'::uuid, 'Circontrol', 'https://www.circontrol.com', 'support@circontrol.com', '+34938765432', true, now(), now()),
    ('40000000-0000-0000-0000-000000000005'::uuid, 'EVBox', 'https://www.evbox.com', 'support@evbox.com', '+31201234567', true, now(), now())
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- ENERGY SCHEMA: Charger Models (Modelos de Cargadores)
-- ============================================================================
INSERT INTO energy.charger_models (id, manufacturer_id, model_name, model_number, number_of_connectors, max_power_kw, firmware_version, ocpp_version, supports_smart_charging, supports_reservation, active, created_at, updated_at)
VALUES
    ('50000000-0000-0000-0000-000000000001'::uuid, '40000000-0000-0000-0000-000000000001'::uuid, 'Terra AC W22-T-RD-MC-0', 'TAC-W22', 2, 22.0, '1.2.3', '1.6', true, true, true, now(), now()),
    ('50000000-0000-0000-0000-000000000002'::uuid, '40000000-0000-0000-0000-000000000001'::uuid, 'Terra 54', 'T54', 2, 50.0, '2.1.0', '1.6', true, true, true, now(), now()),
    ('50000000-0000-0000-0000-000000000003'::uuid, '40000000-0000-0000-0000-000000000002'::uuid, 'EVlink Pro AC 22kW', 'EVLP-22', 2, 22.0, '1.0.5', '1.6', true, false, true, now(), now()),
    ('50000000-0000-0000-0000-000000000004'::uuid, '40000000-0000-0000-0000-000000000003'::uuid, 'Pulsar Plus', 'PP-22', 1, 22.0, '3.4.1', '1.6', true, true, true, now(), now()),
    ('50000000-0000-0000-0000-000000000005'::uuid, '40000000-0000-0000-0000-000000000004'::uuid, 'eVolve Smart 22', 'EVS-22', 2, 22.0, '1.8.2', '1.6', true, true, true, now(), now()),
    ('50000000-0000-0000-0000-000000000006'::uuid, '40000000-0000-0000-0000-000000000005'::uuid, 'BusinessLine 22', 'BL-22', 2, 22.0, '2.0.0', '1.6', true, false, true, now(), now())
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- ENERGY SCHEMA: Pricing Rules (Reglas de Precios)
-- ============================================================================
INSERT INTO energy.pricing_rules (id, type, base_rate, peak_multiplier, off_peak_multiplier, time_windows, active, created_at, updated_at)
VALUES
    ('60000000-0000-0000-0000-000000000001'::uuid, 'FLAT_RATE', 150.00, null, null, '{}'::jsonb, true, now(), now()),
    ('60000000-0000-0000-0000-000000000002'::uuid, 'TIME_OF_USE', 120.00, 1.50, 0.75, '{"peak": ["18:00-22:00"], "off_peak": ["00:00-06:00", "22:00-24:00"]}'::jsonb, true, now(), now()),
    ('60000000-0000-0000-0000-000000000003'::uuid, 'DYNAMIC', 100.00, 2.00, 0.50, '{"grid_dependent": true}'::jsonb, true, now(), now())
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- ENERGY SCHEMA: Time-of-Use Rates (Tarifa Horaria)
-- ============================================================================
INSERT INTO time_of_use_rates (id, name, description, active, created_at, updated_at)
VALUES
    ('70000000-0000-0000-0000-000000000001'::uuid, 'Residencial Estándar', 'Tarifa residencial estándar con horarios punta/valle', true, now(), now()),
    ('70000000-0000-0000-0000-000000000002'::uuid, 'Comercial', 'Tarifa para clientes comerciales', true, now(), now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO time_of_use_periods (id, rate_id, day_of_week, start_time, end_time, rate_per_kwh, period_type)
VALUES
    -- Residencial Estándar
    (gen_random_uuid(), '70000000-0000-0000-0000-000000000001'::uuid, 1, '00:00', '06:00', 80.00, 'OFF_PEAK'),  -- Lunes
    (gen_random_uuid(), '70000000-0000-0000-0000-000000000001'::uuid, 1, '06:00', '18:00', 150.00, 'SHOULDER'),
    (gen_random_uuid(), '70000000-0000-0000-0000-000000000001'::uuid, 1, '18:00', '22:00', 220.00, 'PEAK'),
    (gen_random_uuid(), '70000000-0000-0000-0000-000000000001'::uuid, 1, '22:00', '24:00', 80.00, 'OFF_PEAK'),
    (gen_random_uuid(), '70000000-0000-0000-0000-000000000001'::uuid, 2, '00:00', '06:00', 80.00, 'OFF_PEAK'),  -- Martes
    (gen_random_uuid(), '70000000-0000-0000-0000-000000000001'::uuid, 2, '06:00', '18:00', 150.00, 'SHOULDER'),
    (gen_random_uuid(), '70000000-0000-0000-0000-000000000001'::uuid, 2, '18:00', '22:00', 220.00, 'PEAK'),
    (gen_random_uuid(), '70000000-0000-0000-0000-000000000001'::uuid, 2, '22:00', '24:00', 80.00, 'OFF_PEAK'),
    (gen_random_uuid(), '70000000-0000-0000-0000-000000000001'::uuid, 3, '00:00', '06:00', 80.00, 'OFF_PEAK'),  -- Miércoles
    (gen_random_uuid(), '70000000-0000-0000-0000-000000000001'::uuid, 3, '06:00', '18:00', 150.00, 'SHOULDER'),
    (gen_random_uuid(), '70000000-0000-0000-0000-000000000001'::uuid, 3, '18:00', '22:00', 220.00, 'PEAK'),
    (gen_random_uuid(), '70000000-0000-0000-0000-000000000001'::uuid, 3, '22:00', '24:00', 80.00, 'OFF_PEAK'),
    (gen_random_uuid(), '70000000-0000-0000-0000-000000000001'::uuid, 4, '00:00', '06:00', 80.00, 'OFF_PEAK'),  -- Jueves
    (gen_random_uuid(), '70000000-0000-0000-0000-000000000001'::uuid, 4, '06:00', '18:00', 150.00, 'SHOULDER'),
    (gen_random_uuid(), '70000000-0000-0000-0000-000000000001'::uuid, 4, '18:00', '22:00', 220.00, 'PEAK'),
    (gen_random_uuid(), '70000000-0000-0000-0000-000000000001'::uuid, 4, '22:00', '24:00', 80.00, 'OFF_PEAK'),
    (gen_random_uuid(), '70000000-0000-0000-0000-000000000001'::uuid, 5, '00:00', '06:00', 80.00, 'OFF_PEAK'),  -- Viernes
    (gen_random_uuid(), '70000000-0000-0000-0000-000000000001'::uuid, 5, '06:00', '18:00', 150.00, 'SHOULDER'),
    (gen_random_uuid(), '70000000-0000-0000-0000-000000000001'::uuid, 5, '18:00', '22:00', 220.00, 'PEAK'),
    (gen_random_uuid(), '70000000-0000-0000-0000-000000000001'::uuid, 5, '22:00', '24:00', 80.00, 'OFF_PEAK'),
    (gen_random_uuid(), '70000000-0000-0000-0000-000000000001'::uuid, 6, '00:00', '24:00', 80.00, 'OFF_PEAK'),  -- Sábado todo off-peak
    (gen_random_uuid(), '70000000-0000-0000-0000-000000000001'::uuid, 0, '00:00', '24:00', 80.00, 'OFF_PEAK')   -- Domingo todo off-peak
ON CONFLICT DO NOTHING;

-- ============================================================================
-- FLEET SCHEMA: Vehicle Status Reference (Estados de Vehículo)
-- ============================================================================
-- Note: fleet.vehicles is a master table for fleet management
-- Seeding reference vehicles if needed
INSERT INTO fleet.vehicles (id, license_plate, brand, model, year, rfid_tag, status, battery_level, current_charging_station_id, last_charging_session, created_at)
VALUES
    ('80000000-0000-0000-0000-000000000001'::uuid, 'ABCD12', 'Nissan', 'Leaf', 2023, 'RFID001', 'AVAILABLE', 85, null, null, now()),
    ('80000000-0000-0000-0000-000000000002'::uuid, 'EFGH34', 'Hyundai', 'Kona Electric', 2023, 'RFID002', 'AVAILABLE', 92, null, null, now()),
    ('80000000-0000-0000-0000-000000000003'::uuid, 'IJKL56', 'Kia', 'EV6', 2024, 'RFID003', 'CHARGING', 45, null, now() - interval '2 hours', now())
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- FEES SCHEMA: Fee Configuration (Configuración de Honorarios/Recibos)
-- Note: fee_receipts is transactional - NOT seeded here
-- Only reference/config data would go here if any
-- ============================================================================

-- ============================================================================
-- PUBLIC SCHEMA: Alert Rules (Reglas de Alertas - Monitoring)
-- ============================================================================
INSERT INTO alert_rules (id, name, rule_type, condition, threshold_value, threshold_operator, alert_type, alert_severity, enabled, notification_channels, created_at, updated_at)
VALUES
    ('90000000-0000-0000-0000-000000000001'::uuid, 'Estación Offline > 10 min', 'STATION_OFFLINE', '{"duration_minutes": 10}'::jsonb, 10, '>', 'STATION_OFFLINE', 'WARNING', true, '["email", "push"]'::jsonb, now(), now()),
    ('90000000-0000-0000-0000-000000000002'::uuid, 'Potencia > Límite Contratado', 'POWER_THRESHOLD', '{"metric": "power_kw"}'::jsonb, 100, '>', 'POWER_EXCEEDED', 'CRITICAL', true, '["email", "sms"]'::jsonb, now(), now()),
    ('90000000-0000-0000-0000-000000000003'::uuid, 'Sesión Fallida', 'SESSION_FAILED', '{}'::jsonb, 1, '=', 'SESSION_ERROR', 'WARNING', true, '["push"]'::jsonb, now(), now())
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- PUBLIC SCHEMA: Authorization Cache (Cache de Autorización OCPP)
-- ============================================================================
-- Not seeded - populated dynamically from OCPP transactions

-- ============================================================================
-- PUBLIC SCHEMA: Maintenance Types Reference
-- ============================================================================
-- Not a separate table - maintenance_type is a varchar in maintenance_records

-- ============================================================================
-- COMPLETION MESSAGE
-- ============================================================================
DO $$
BEGIN
    RAISE NOTICE 'Master data seed migration completed successfully.';
    RAISE NOTICE 'Seeded: Companies, SSO Users, Organizations, Roles, Users, UserRoles,';
    RAISE NOTICE '        Chart of Accounts (Chilean), Tax Parameters,';
    RAISE NOTICE '        Energy Manufacturers, Charger Models, Pricing Rules, TOU Rates,';
    RAISE NOTICE '        Fleet Vehicles, Alert Rules';
    RAISE NOTICE 'EXCLUDED (transactional): Invoices, Payments, Bank Transactions,';
    RAISE NOTICE '        Charging Sessions, Energy Measurements, Fee Receipts,';
    RAISE NOTICE '        Accounting Entries, B2B Orders, etc.';
END $$;