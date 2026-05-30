-- Ferrari Engine Phase 2: Green Metrics Table
-- Creates table for environmental impact tracking

CREATE TABLE IF NOT EXISTS green_metrics (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    accounting_entry_id UUID,
    category VARCHAR(50) NOT NULL,
    carbon_footprint DECIMAL(19, 4) NOT NULL DEFAULT 0,
    green_score DECIMAL(5, 2) NOT NULL DEFAULT 0,
    evidence VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    
    CONSTRAINT fk_green_metrics_company 
        FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    CONSTRAINT fk_green_metrics_entry 
        FOREIGN KEY (accounting_entry_id) REFERENCES accounting_entries(id) ON DELETE SET NULL
);

-- Indexes for performance
CREATE INDEX idx_green_metrics_company ON green_metrics(company_id);
CREATE INDEX idx_green_metrics_entry ON green_metrics(accounting_entry_id);
CREATE INDEX idx_green_metrics_category ON green_metrics(category);
CREATE INDEX idx_green_metrics_created_at ON green_metrics(created_at);

-- Comments for documentation
COMMENT ON TABLE green_metrics IS 'Ferrari Engine Phase 2: Environmental impact metrics for transactions';
COMMENT ON COLUMN green_metrics.carbon_footprint IS 'Carbon footprint in kg CO2e (CO2 equivalent)';
COMMENT ON COLUMN green_metrics.green_score IS 'Environmental score from -100 (negative) to +100 (positive)';
COMMENT ON COLUMN green_metrics.category IS 'Impact category: SCOPE_1_EMISSION, SCOPE_2_EMISSION, SCOPE_3_EMISSION, RENEWABLE_ENERGY, WASTE_MANAGEMENT, CARBON_OFFSET, SUSTAINABLE_SUPPLY';
