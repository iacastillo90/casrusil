CREATE SCHEMA IF NOT EXISTS energy;
-- STEP 3: PUBLIC SCHEMA - Charging Infrastructure

CREATE TABLE IF NOT EXISTS charging_stations (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    company_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_maintenance_date TIMESTAMP,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    address VARCHAR(255),
    city VARCHAR(255),
    country VARCHAR(255),
    CONSTRAINT fk_charging_stations_company FOREIGN KEY (company_id) REFERENCES sso.companies(id) ON DELETE CASCADE
);

CREATE INDEX idx_charging_stations_company ON charging_stations(company_id);
CREATE INDEX idx_charging_stations_status ON charging_stations(status);
CREATE INDEX idx_charging_stations_active ON charging_stations(active);

CREATE TABLE IF NOT EXISTS chargers (
    id BIGSERIAL PRIMARY KEY,
    ocpp_id VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    alias VARCHAR(255) NOT NULL,
    manufacturer VARCHAR(255) NOT NULL,
    model VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT false,
    installation_date TIMESTAMP NOT NULL,
    last_heartbeat TIMESTAMP,
    station_id UUID NOT NULL,
    CONSTRAINT fk_chargers_station FOREIGN KEY (station_id) REFERENCES charging_stations(id) ON DELETE CASCADE
);

CREATE INDEX idx_chargers_station ON chargers(station_id);
CREATE INDEX idx_chargers_ocpp_id ON chargers(ocpp_id);
CREATE INDEX idx_chargers_status ON chargers(status);

CREATE TABLE IF NOT EXISTS connectors (
    id BIGSERIAL PRIMARY KEY,
    connector_number INTEGER NOT NULL,
    alias VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT false,
    last_status_change TIMESTAMP,
    charger_id BIGINT NOT NULL,
    max_voltage DECIMAL(19, 2),
    max_power DECIMAL(19, 2),
    max_current DECIMAL(19, 2),
    CONSTRAINT fk_connectors_charger FOREIGN KEY (charger_id) REFERENCES chargers(id) ON DELETE CASCADE
);

CREATE INDEX idx_connectors_charger ON connectors(charger_id);
CREATE INDEX idx_connectors_status ON connectors(status);

CREATE TABLE IF NOT EXISTS charging_sessions (
    id UUID PRIMARY KEY,
    station_id UUID NOT NULL,
    charger_ocpp_id VARCHAR(255) NOT NULL,
    connector_number INTEGER NOT NULL,
    user_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    started_at TIMESTAMP NOT NULL,
    ended_at TIMESTAMP,
    initial_energy_kwh DECIMAL(10, 3),
    final_energy_kwh DECIMAL(10, 3),
    stop_reason VARCHAR(500),
    CONSTRAINT fk_charging_sessions_station FOREIGN KEY (station_id) REFERENCES charging_stations(id) ON DELETE CASCADE
);

CREATE INDEX idx_charging_sessions_station ON charging_sessions(station_id);
CREATE INDEX idx_charging_sessions_status ON charging_sessions(status);
CREATE INDEX idx_charging_sessions_started_at ON charging_sessions(started_at DESC);
CREATE INDEX idx_charging_sessions_charger_ocpp ON charging_sessions(charger_ocpp_id);

CREATE TABLE IF NOT EXISTS energy_measurements (
    id BIGSERIAL PRIMARY KEY,
    session_id UUID NOT NULL,
    energy_kwh DECIMAL(10, 3) NOT NULL,
    measured_at TIMESTAMP NOT NULL,
    meter_value VARCHAR(500),
    CONSTRAINT fk_energy_measurements_session FOREIGN KEY (session_id) REFERENCES charging_sessions(id) ON DELETE CASCADE
);

CREATE INDEX idx_energy_measurements_session ON energy_measurements(session_id);
CREATE INDEX idx_energy_measurements_measured_at ON energy_measurements(measured_at);

-- STEP 8: ENERGY SCHEMA - Pricing and Security

CREATE TABLE IF NOT EXISTS energy.pricing_rules (
    id UUID PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    base_rate DECIMAL(10, 2) NOT NULL,
    peak_multiplier DECIMAL(5, 2),
    off_peak_multiplier DECIMAL(5, 2),
    time_windows JSONB,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_pricing_rules_type ON energy.pricing_rules(type);
CREATE INDEX idx_pricing_rules_active ON energy.pricing_rules(active);

CREATE TABLE IF NOT EXISTS energy.certificates (
    id UUID PRIMARY KEY,
    station_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    certificate_data TEXT NOT NULL,
    serial_number VARCHAR(255),
    issuer VARCHAR(255),
    valid_from TIMESTAMP,
    valid_to TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_certificates_station FOREIGN KEY (station_id) REFERENCES charging_stations(id) ON DELETE CASCADE
);

CREATE INDEX idx_certificates_station ON energy.certificates(station_id);
CREATE INDEX idx_certificates_type ON energy.certificates(type);
CREATE INDEX idx_certificates_valid_to ON energy.certificates(valid_to);
CREATE INDEX idx_certificates_status ON energy.certificates(status);

CREATE TABLE IF NOT EXISTS energy.security_events (
    id UUID PRIMARY KEY,
    station_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    tech_info TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_security_events_station FOREIGN KEY (station_id) REFERENCES charging_stations(id) ON DELETE CASCADE
);

CREATE INDEX idx_security_events_station ON energy.security_events(station_id);
CREATE INDEX idx_security_events_type ON energy.security_events(event_type);
CREATE INDEX idx_security_events_timestamp ON energy.security_events(timestamp DESC);



-- STEP 11: OCPP INFRASTRUCTURE (PUBLIC SCHEMA)

CREATE TABLE IF NOT EXISTS ocpp_station_mapping (
    ocpp_id VARCHAR(255) PRIMARY KEY,
    station_id UUID NOT NULL UNIQUE,
    company_id UUID NOT NULL,
    last_seen TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ocpp_station_company FOREIGN KEY (company_id) REFERENCES sso.companies(id) ON DELETE CASCADE,
    CONSTRAINT fk_ocpp_station_station FOREIGN KEY (station_id) REFERENCES charging_stations(id) ON DELETE CASCADE
);

CREATE INDEX idx_ocpp_station_mapping_station ON ocpp_station_mapping(station_id);
CREATE INDEX idx_ocpp_station_mapping_company ON ocpp_station_mapping(company_id);
CREATE INDEX idx_ocpp_station_mapping_last_seen ON ocpp_station_mapping(last_seen);

CREATE TABLE IF NOT EXISTS ocpp_transaction_mapping (
    transaction_id INTEGER PRIMARY KEY,
    session_id UUID NOT NULL UNIQUE,
    ocpp_id VARCHAR(255) NOT NULL,
    connector_id INTEGER NOT NULL,
    id_tag VARCHAR(255),
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    stopped_at TIMESTAMP,
    CONSTRAINT fk_ocpp_transaction_station FOREIGN KEY (ocpp_id) REFERENCES ocpp_station_mapping(ocpp_id) ON DELETE CASCADE,
    CONSTRAINT fk_ocpp_transaction_session FOREIGN KEY (session_id) REFERENCES charging_sessions(id) ON DELETE CASCADE
);

CREATE INDEX idx_ocpp_transaction_session ON ocpp_transaction_mapping(session_id);
CREATE INDEX idx_ocpp_transaction_station ON ocpp_transaction_mapping(ocpp_id);
CREATE INDEX idx_ocpp_transaction_id_tag ON ocpp_transaction_mapping(id_tag);
CREATE INDEX idx_ocpp_transaction_started ON ocpp_transaction_mapping(started_at);

CREATE TABLE IF NOT EXISTS ocpp_diagnostics_status (
    id UUID PRIMARY KEY,
    ocpp_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    file_location VARCHAR(500),
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ocpp_diagnostics_station FOREIGN KEY (ocpp_id) REFERENCES ocpp_station_mapping(ocpp_id) ON DELETE CASCADE
);

CREATE INDEX idx_ocpp_diagnostics_station ON ocpp_diagnostics_status(ocpp_id);
CREATE INDEX idx_ocpp_diagnostics_status ON ocpp_diagnostics_status(status);
CREATE INDEX idx_ocpp_diagnostics_requested ON ocpp_diagnostics_status(requested_at);

CREATE TABLE IF NOT EXISTS ocpp_firmware_status (
    id UUID PRIMARY KEY,
    ocpp_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    firmware_location VARCHAR(500),
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ocpp_firmware_station FOREIGN KEY (ocpp_id) REFERENCES ocpp_station_mapping(ocpp_id) ON DELETE CASCADE
);

CREATE INDEX idx_ocpp_firmware_station ON ocpp_firmware_status(ocpp_id);
CREATE INDEX idx_ocpp_firmware_status ON ocpp_firmware_status(status);
CREATE INDEX idx_ocpp_firmware_requested ON ocpp_firmware_status(requested_at);

CREATE TABLE IF NOT EXISTS ocpp_reservations (
    reservation_id INTEGER PRIMARY KEY,
    ocpp_id VARCHAR(255) NOT NULL,
    connector_id INTEGER NOT NULL,
    id_tag VARCHAR(255) NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    parent_id_tag VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cancelled_at TIMESTAMP,
    CONSTRAINT fk_ocpp_reservation_station FOREIGN KEY (ocpp_id) REFERENCES ocpp_station_mapping(ocpp_id) ON DELETE CASCADE
);

CREATE INDEX idx_ocpp_reservation_station ON ocpp_reservations(ocpp_id);
CREATE INDEX idx_ocpp_reservation_id_tag ON ocpp_reservations(id_tag);
CREATE INDEX idx_ocpp_reservation_expiry ON ocpp_reservations(expiry_date);
CREATE INDEX idx_ocpp_reservation_status ON ocpp_reservations(status);

-- STEP 12: SECURITY TABLES (PUBLIC SCHEMA)

CREATE TABLE IF NOT EXISTS authorization_cache (
    id_tag VARCHAR(255) PRIMARY KEY,
    status VARCHAR(50) NOT NULL,
    expiry_date TIMESTAMP,
    parent_id_tag VARCHAR(255),
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_authorization_cache_status ON authorization_cache(status);
CREATE INDEX idx_authorization_cache_expiry ON authorization_cache(expiry_date);

-- STEP 13: ADVANCED PRICING (PUBLIC SCHEMA)

CREATE TABLE IF NOT EXISTS time_of_use_rates (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_time_of_use_rates_active ON time_of_use_rates(active);

CREATE TABLE IF NOT EXISTS time_of_use_periods (
    id UUID PRIMARY KEY,
    rate_id UUID NOT NULL,
    day_of_week INTEGER NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    rate_per_kwh DECIMAL(10, 2) NOT NULL,
    period_type VARCHAR(50) NOT NULL,
    CONSTRAINT fk_tou_periods_rate FOREIGN KEY (rate_id) REFERENCES time_of_use_rates(id) ON DELETE CASCADE,
    CONSTRAINT check_day_of_week CHECK (day_of_week BETWEEN 0 AND 6),
    CONSTRAINT check_period_type CHECK (period_type IN ('PEAK', 'OFF_PEAK', 'SHOULDER'))
);

CREATE INDEX idx_tou_periods_rate ON time_of_use_periods(rate_id);
CREATE INDEX idx_tou_periods_day_time ON time_of_use_periods(day_of_week, start_time);

CREATE TABLE IF NOT EXISTS dynamic_pricing_events (
    id UUID PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    multiplier DECIMAL(5, 2) NOT NULL,
    reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_event_type CHECK (event_type IN ('PEAK_DEMAND', 'GRID_STRESS', 'RENEWABLE_SURPLUS', 'SPECIAL_EVENT', 'MAINTENANCE'))
);

CREATE INDEX idx_dynamic_pricing_events_time ON dynamic_pricing_events(start_time, end_time);
CREATE INDEX idx_dynamic_pricing_events_type ON dynamic_pricing_events(event_type);

-- STEP 14: CHARGER METADATA (PUBLIC SCHEMA)

CREATE TABLE IF NOT EXISTS manufacturers (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    website VARCHAR(255),
    support_email VARCHAR(255),
    support_phone VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_manufacturers_name ON manufacturers(name);

CREATE TABLE IF NOT EXISTS charger_models (
    id UUID PRIMARY KEY,
    manufacturer_id UUID NOT NULL,
    model_name VARCHAR(100) NOT NULL,
    max_power_kw DECIMAL(10, 2) NOT NULL,
    connector_types JSONB NOT NULL,
    ocpp_version VARCHAR(20),
    specifications JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_charger_models_manufacturer FOREIGN KEY (manufacturer_id) REFERENCES manufacturers(id) ON DELETE CASCADE,
    CONSTRAINT uk_manufacturer_model UNIQUE (manufacturer_id, model_name)
);

CREATE INDEX idx_charger_models_manufacturer ON charger_models(manufacturer_id);
CREATE INDEX idx_charger_models_power ON charger_models(max_power_kw);

-- STEP 15: MAINTENANCE TABLES (PUBLIC SCHEMA)

CREATE TABLE IF NOT EXISTS maintenance_records (
    id UUID PRIMARY KEY,
    station_id UUID NOT NULL,
    maintenance_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    scheduled_date TIMESTAMP NOT NULL,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    description TEXT NOT NULL,
    technician VARCHAR(100),
    notes TEXT,
    cost DECIMAL(10, 2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_maintenance_station FOREIGN KEY (station_id) REFERENCES charging_stations(id) ON DELETE CASCADE
);

CREATE INDEX idx_maintenance_station ON maintenance_records(station_id);
CREATE INDEX idx_maintenance_status ON maintenance_records(status);
CREATE INDEX idx_maintenance_type ON maintenance_records(maintenance_type);
CREATE INDEX idx_maintenance_scheduled ON maintenance_records(scheduled_date);
CREATE INDEX idx_maintenance_completed ON maintenance_records(completed_at);

-- STEP 16: MONITORING TABLES (PUBLIC SCHEMA)

CREATE TABLE IF NOT EXISTS dashboard_metrics (
    id UUID PRIMARY KEY,
    metric_type VARCHAR(50) NOT NULL,
    metric_value DECIMAL(15,2) NOT NULL,
    unit VARCHAR(20),
    station_id UUID,
    timestamp TIMESTAMP NOT NULL,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT dashboard_metrics_type_check CHECK (metric_type IN (
        'ACTIVE_SESSIONS', 'TOTAL_SESSIONS_TODAY', 'COMPLETED_SESSIONS_TODAY',
        'TOTAL_ENERGY_TODAY', 'TOTAL_ENERGY_THIS_WEEK', 'TOTAL_ENERGY_THIS_MONTH',
        'CURRENT_POWER_CONSUMPTION', 'AVERAGE_ENERGY_PER_SESSION',
        'TOTAL_REVENUE_TODAY', 'TOTAL_REVENUE_THIS_WEEK', 'TOTAL_REVENUE_THIS_MONTH',
        'AVERAGE_REVENUE_PER_SESSION', 'ACTIVE_STATIONS', 'OFFLINE_STATIONS',
        'TOTAL_STATIONS', 'STATION_UTILIZATION_RATE', 'AVERAGE_SESSION_DURATION',
        'PEAK_POWER_TODAY', 'ENERGY_EFFICIENCY'
    ))
);

CREATE INDEX idx_metrics_type_timestamp ON dashboard_metrics(metric_type, timestamp DESC);
CREATE INDEX idx_metrics_station ON dashboard_metrics(station_id) WHERE station_id IS NOT NULL;
CREATE INDEX idx_metrics_timestamp ON dashboard_metrics(timestamp DESC);

CREATE TABLE IF NOT EXISTS alerts (
    id UUID PRIMARY KEY,
    alert_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    station_id UUID,
    session_id UUID,
    acknowledged BOOLEAN NOT NULL DEFAULT FALSE,
    acknowledged_at TIMESTAMP,
    acknowledged_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP,
    CONSTRAINT alerts_severity_check CHECK (severity IN ('INFO', 'WARNING', 'CRITICAL')),
    CONSTRAINT alerts_station_fk FOREIGN KEY (station_id) REFERENCES charging_stations(id) ON DELETE SET NULL
);

CREATE INDEX idx_alerts_active ON alerts(acknowledged, resolved_at) WHERE resolved_at IS NULL;
CREATE INDEX idx_alerts_unacknowledged ON alerts(acknowledged, created_at DESC) WHERE acknowledged = FALSE AND resolved_at IS NULL;
CREATE INDEX idx_alerts_station ON alerts(station_id) WHERE station_id IS NOT NULL;
CREATE INDEX idx_alerts_type ON alerts(alert_type);
CREATE INDEX idx_alerts_severity ON alerts(severity);
CREATE INDEX idx_alerts_created ON alerts(created_at DESC);

CREATE TABLE IF NOT EXISTS alert_rules (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    rule_type VARCHAR(50) NOT NULL,
    condition JSONB NOT NULL,
    threshold_value DECIMAL(15,2),
    threshold_operator VARCHAR(10),
    alert_type VARCHAR(50) NOT NULL,
    alert_severity VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    notification_channels JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    last_triggered_at TIMESTAMP,
    CONSTRAINT alert_rules_operator_check CHECK (threshold_operator IN ('>', '<', '>=', '<=', '=', '!=')),
    CONSTRAINT alert_rules_severity_check CHECK (alert_severity IN ('INFO', 'WARNING', 'CRITICAL'))
);

CREATE INDEX idx_alert_rules_enabled ON alert_rules(enabled) WHERE enabled = TRUE;
CREATE INDEX idx_alert_rules_type ON alert_rules(rule_type);

CREATE TABLE IF NOT EXISTS system_health_snapshots (
    id UUID PRIMARY KEY,
    overall_status VARCHAR(20) NOT NULL,
    active_stations INT NOT NULL DEFAULT 0,
    offline_stations INT NOT NULL DEFAULT 0,
    active_sessions INT NOT NULL DEFAULT 0,
    total_power_kw DECIMAL(10,2) NOT NULL DEFAULT 0,
    database_status VARCHAR(20) NOT NULL DEFAULT 'HEALTHY',
    api_status VARCHAR(20) NOT NULL DEFAULT 'HEALTHY',
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    details JSONB,
    CONSTRAINT health_overall_status_check CHECK (overall_status IN ('HEALTHY', 'DEGRADED', 'CRITICAL', 'DOWN')),
    CONSTRAINT health_database_status_check CHECK (database_status IN ('HEALTHY', 'DEGRADED', 'DOWN')),
    CONSTRAINT health_api_status_check CHECK (api_status IN ('HEALTHY', 'DEGRADED', 'DOWN'))
);

CREATE INDEX idx_health_timestamp ON system_health_snapshots(timestamp DESC);
CREATE INDEX idx_health_status ON system_health_snapshots(overall_status, timestamp DESC);

