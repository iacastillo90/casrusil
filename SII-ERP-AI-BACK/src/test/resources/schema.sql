CREATE SCHEMA IF NOT EXISTS sso;
CREATE SCHEMA IF NOT EXISTS invoicing;
CREATE SCHEMA IF NOT EXISTS accounting;
CREATE SCHEMA IF NOT EXISTS ai_assistant;
CREATE SCHEMA IF NOT EXISTS integration_sii;

-- Bank reconciliation transactions table (manual reconciliation)
CREATE TABLE IF NOT EXISTS bank_reconciliation_transactions (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    date DATE NOT NULL,
    description VARCHAR(500) NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    reference VARCHAR(100),
    reconciled BOOLEAN NOT NULL DEFAULT FALSE,
    reconciled_with_entry_id UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_recon_tx_company ON bank_reconciliation_transactions (company_id);
CREATE INDEX IF NOT EXISTS idx_recon_tx_date ON bank_reconciliation_transactions (date);

-- Bank sync transactions table (automated sync with Fintoc)
CREATE TABLE IF NOT EXISTS bank_sync_transactions (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    bank_name VARCHAR(255) NOT NULL,
    account_number VARCHAR(100) NOT NULL,
    transaction_id VARCHAR(255) NOT NULL,
    transaction_date TIMESTAMP WITH TIME ZONE NOT NULL,
    description TEXT,
    amount NUMERIC(19,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_bank_sync_company ON bank_sync_transactions (company_id);