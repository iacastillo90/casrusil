-- STEP 16: SUSTAINABILITY SCHEMA
-- ============================================================================

CREATE TABLE IF NOT EXISTS sustainability.sustainability_records (
    id UUID PRIMARY KEY,
    invoice_id UUID NOT NULL UNIQUE,
    carbon_footprint_kg DECIMAL(19, 2) NOT NULL,
    category_name VARCHAR(255),
    confidence_score DOUBLE PRECISION,
    calculated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_sustainability_invoice FOREIGN KEY (invoice_id) REFERENCES invoicing.invoices(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS sustainability.green_metrics (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    accounting_entry_id UUID NOT NULL,
    category VARCHAR(50) NOT NULL,
    carbon_footprint DECIMAL(19, 4) NOT NULL,
    green_score DECIMAL(5, 2) NOT NULL,
    evidence VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_green_metrics_company FOREIGN KEY (company_id) REFERENCES sso.companies(id) ON DELETE CASCADE,
    CONSTRAINT fk_green_metrics_entry FOREIGN KEY (accounting_entry_id) REFERENCES accounting.accounting_entries(id) ON DELETE CASCADE
);

CREATE INDEX idx_green_metrics_company ON sustainability.green_metrics(company_id);
CREATE INDEX idx_green_metrics_entry ON sustainability.green_metrics(accounting_entry_id);
CREATE INDEX idx_green_metrics_category ON sustainability.green_metrics(category);

-- ============================================================================
-- STEP 17: USER MANAGEMENT (PUBLIC SCHEMA)
-- ============================================================================

CREATE TABLE IF NOT EXISTS organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    type VARCHAR(50) NOT NULL,
    parent_id UUID,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    settings JSONB,
    branding JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT organizations_type_check CHECK (type IN ('ENTERPRISE', 'SMB', 'STARTUP', 'INDIVIDUAL', 'PARTNER')),
    CONSTRAINT organizations_status_check CHECK (status IN ('ACTIVE', 'SUSPENDED', 'INACTIVE', 'PENDING')),
    CONSTRAINT fk_parent_organization FOREIGN KEY (parent_id) REFERENCES organizations(id) ON DELETE SET NULL
);

CREATE INDEX idx_organizations_parent ON organizations(parent_id) WHERE parent_id IS NOT NULL;
CREATE INDEX idx_organizations_status ON organizations(status);
CREATE INDEX idx_organizations_type ON organizations(type);

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT users_status_check CHECK (status IN ('ACTIVE', 'INVITED', 'SUSPENDED', 'INACTIVE')),
    CONSTRAINT fk_user_organization FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE
);

CREATE INDEX idx_users_organization ON users(organization_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_last_login ON users(last_login_at DESC);

CREATE TABLE IF NOT EXISTS roles (
    id UUID PRIMARY KEY,
    organization_id UUID,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_system_role BOOLEAN NOT NULL DEFAULT FALSE,
    permissions JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_role_organization FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT unique_role_name_per_org UNIQUE (organization_id, name)
);

CREATE INDEX idx_roles_organization ON roles(organization_id) WHERE organization_id IS NOT NULL;
CREATE INDEX idx_roles_system ON roles(is_system_role) WHERE is_system_role = TRUE;
CREATE INDEX idx_roles_name ON roles(name);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by UUID,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_assigned_by FOREIGN KEY (assigned_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_user_roles_user ON user_roles(user_id);
CREATE INDEX idx_user_roles_role ON user_roles(role_id);
CREATE INDEX idx_user_roles_assigned_at ON user_roles(assigned_at DESC);

CREATE TABLE IF NOT EXISTS user_audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50),
    resource_id UUID,
    details JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_audit_user ON user_audit_log(user_id, created_at DESC);
CREATE INDEX idx_audit_action ON user_audit_log(action);
CREATE INDEX idx_audit_created ON user_audit_log(created_at DESC);

-- ============================================================================
-- MIGRATION COMPLETE
-- ============================================================================
