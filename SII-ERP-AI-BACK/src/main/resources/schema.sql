
CREATE SCHEMA IF NOT EXISTS accounting;

CREATE SCHEMA IF NOT EXISTS b2b;

CREATE SCHEMA IF NOT EXISTS energy;

CREATE SCHEMA IF NOT EXISTS fees;

CREATE SCHEMA IF NOT EXISTS fleet;

CREATE SCHEMA IF NOT EXISTS invoicing;

CREATE SCHEMA IF NOT EXISTS sso;

CREATE SCHEMA IF NOT EXISTS sustainability;

CREATE TABLE IF NOT EXISTS accounting.accounting_entries (
    id uuid NOT NULL,
    company_id uuid NOT NULL,
    description character varying(255) NOT NULL,
    document_number character varying(255),
    document_type character varying(255),
    occurred_on timestamp(6) with time zone NOT NULL,
    reference_id character varying(255),
    reference_type character varying(255),
    status character varying(255),
    tax_payer_id character varying(255),
    tax_payer_name character varying(255),
    type character varying(255) NOT NULL,
    version bigint,
    CONSTRAINT accounting_entries_type_check CHECK (((type)::text = ANY ((ARRAY['OPENING'::character varying, 'NORMAL'::character varying, 'CLOSING'::character varying, 'ADJUSTMENT'::character varying, 'PURCHASE'::character varying, 'SALE'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS accounting.accounting_entry_lines (
    entry_id uuid NOT NULL,
    account_code character varying(255) NOT NULL,
    credit numeric(38,2) NOT NULL,
    debit numeric(38,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS accounting.accounts (
    id uuid NOT NULL,
    active boolean NOT NULL,
    code character varying(255) NOT NULL,
    company_id uuid NOT NULL,
    description character varying(255),
    name character varying(255) NOT NULL,
    type character varying(255) NOT NULL,
    CONSTRAINT accounts_type_check CHECK (((type)::text = ANY ((ARRAY['ASSET'::character varying, 'LIABILITY'::character varying, 'EQUITY'::character varying, 'REVENUE'::character varying, 'EXPENSE'::character varying, 'GAIN'::character varying, 'LOSS'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS accounting.closed_periods (
    id uuid NOT NULL,
    closed_at timestamp(6) with time zone NOT NULL,
    closed_by uuid NOT NULL,
    company_id uuid NOT NULL,
    period character varying(7) NOT NULL,
    profit_loss numeric(19,2) NOT NULL
);

CREATE TABLE b2b.order_items (
    id bigint NOT NULL,
    order_id uuid NOT NULL,
    product_name character varying(255) NOT NULL,
    quantity integer NOT NULL,
    unit_price numeric(19,4) NOT NULL,
    "position" integer NOT NULL
);

CREATE SEQUENCE b2b.order_items_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE b2b.order_items_id_seq OWNED BY b2b.order_items.id;

CREATE TABLE b2b.orders (
    id uuid NOT NULL,
    customer_id uuid NOT NULL,
    status character varying(20) NOT NULL,
    total_amount numeric(19,4) NOT NULL,
    created_at timestamp without time zone NOT NULL
);

CREATE TABLE IF NOT EXISTS energy.certificates (
    id uuid NOT NULL,
    certificate_data text NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    issuer character varying(255),
    serial_number character varying(255),
    station_id uuid NOT NULL,
    status character varying(50) NOT NULL,
    type character varying(50) NOT NULL,
    valid_from timestamp(6) without time zone,
    valid_to timestamp(6) without time zone
);

CREATE TABLE IF NOT EXISTS energy.charger_models (
    id uuid NOT NULL,
    active boolean NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    firmware_version character varying(255),
    manufacturer_id uuid NOT NULL,
    max_power_kw numeric(38,2),
    model_name character varying(255) NOT NULL,
    model_number character varying(255),
    number_of_connectors integer NOT NULL,
    ocpp_version character varying(255),
    supports_reservation boolean,
    supports_smart_charging boolean,
    updated_at timestamp(6) without time zone
);

CREATE TABLE IF NOT EXISTS energy.demand_pricings (
    id uuid NOT NULL,
    active boolean NOT NULL,
    applicable_days character varying(255),
    base_rate numeric(10,2) NOT NULL,
    demand_threshold_kw integer NOT NULL,
    description character varying(255),
    name character varying(255) NOT NULL,
    off_peak_multiplier numeric(5,2) NOT NULL,
    peak_end_time time(6) without time zone NOT NULL,
    peak_multiplier numeric(5,2) NOT NULL,
    peak_start_time time(6) without time zone NOT NULL
);

CREATE TABLE IF NOT EXISTS energy.manufacturers (
    id uuid NOT NULL,
    active boolean NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    name character varying(100) NOT NULL,
    support_email character varying(255),
    support_phone character varying(255),
    updated_at timestamp(6) without time zone,
    website character varying(255)
);

CREATE TABLE IF NOT EXISTS energy.pricing_rules (
    id uuid NOT NULL,
    active boolean NOT NULL,
    base_rate numeric(10,2) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    off_peak_multiplier numeric(5,2),
    peak_multiplier numeric(5,2),
    time_windows jsonb,
    type character varying(50) NOT NULL,
    updated_at timestamp(6) without time zone
);

CREATE TABLE IF NOT EXISTS energy.promotional_campaigns (
    id uuid NOT NULL,
    active boolean NOT NULL,
    code character varying(255) NOT NULL,
    current_usage_count integer NOT NULL,
    description character varying(255),
    discount_value numeric(10,2) NOT NULL,
    eligible_user_ids text,
    max_usage_count integer,
    maximum_discount numeric(10,2),
    minimum_purchase numeric(10,2),
    type character varying(255) NOT NULL,
    valid_from timestamp(6) without time zone NOT NULL,
    valid_until timestamp(6) without time zone NOT NULL
);

CREATE TABLE IF NOT EXISTS energy.seasonal_pricings (
    id uuid NOT NULL,
    description character varying(255),
    end_date date NOT NULL,
    multiplier numeric(5,2) NOT NULL,
    season character varying(255) NOT NULL,
    start_date date NOT NULL
);

CREATE TABLE IF NOT EXISTS energy.security_events (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    event_type character varying(100) NOT NULL,
    station_id uuid NOT NULL,
    tech_info text,
    "timestamp" timestamp(6) without time zone NOT NULL
);

CREATE TABLE IF NOT EXISTS fees.fee_receipts (
    id uuid NOT NULL,
    company_id uuid NOT NULL,
    folio bigint NOT NULL,
    gross_amount numeric(38,2) NOT NULL,
    issue_date date NOT NULL,
    issuer_name character varying(255),
    issuer_rut character varying(255) NOT NULL,
    net_amount numeric(38,2) NOT NULL,
    receiver_rut character varying(255) NOT NULL,
    retention_amount numeric(38,2) NOT NULL,
    status character varying(255) NOT NULL,
    CONSTRAINT fee_receipts_status_check CHECK (((status)::text = ANY ((ARRAY['VALID'::character varying, 'NULLIFIED'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS fleet.vehicles (
    id uuid NOT NULL,
    assigned_user_id uuid,
    battery_level integer,
    brand character varying(255),
    created_at timestamp(6) without time zone NOT NULL,
    current_charging_station_id uuid,
    last_charging_session timestamp(6) without time zone,
    license_plate character varying(255) NOT NULL,
    model character varying(255),
    rfid_tag character varying(255),
    status character varying(255) NOT NULL,
    year integer
);

CREATE TABLE IF NOT EXISTS invoicing.invoices (
    id uuid NOT NULL,
    business_name character varying(255),
    receiver_name character varying(255),
    common_use_vat_amount numeric(38,2),
    company_id uuid NOT NULL,
    date date NOT NULL,
    due_date date,
    fixed_asset_amount numeric(38,2),
    folio bigint NOT NULL,
    issuer_rut character varying(255) NOT NULL,
    net_amount numeric(38,2),
    origin character varying(255),
    receiver_rut character varying(255) NOT NULL,
    payment_status character varying(255),
    tax_amount numeric(38,2),
    total_amount numeric(38,2) NOT NULL,
    transaction_type character varying(255),
    type_code integer NOT NULL,
    version bigint,
    CONSTRAINT invoices_payment_status_check CHECK (((payment_status)::text = ANY ((ARRAY['PAID'::character varying, 'PENDING'::character varying, 'OVERDUE'::character varying, 'CANCELLED'::character varying])::text[]))),
    CONSTRAINT invoices_transaction_type_check CHECK (((transaction_type)::text = ANY ((ARRAY['PURCHASE'::character varying, 'SALE'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS invoicing.invoice_audit_log (
    id uuid NOT NULL,
    invoice_id uuid NOT NULL,
    company_id uuid NOT NULL,
    action character varying(32) NOT NULL,
    username character varying(150) NOT NULL,
    detail character varying(500),
    occurred_at timestamp with time zone NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_invoice_audit_log_invoice
    ON invoicing.invoice_audit_log (company_id, invoice_id, occurred_at DESC);

CREATE TABLE IF NOT EXISTS public.ai_insights (
    id character varying(255) NOT NULL,
    analysis_context text NOT NULL,
    company_id character varying(255) NOT NULL,
    created_at timestamp(6) without time zone,
    generated_at timestamp(6) without time zone NOT NULL,
    generated_insight text NOT NULL,
    suggested_action text
);

CREATE TABLE IF NOT EXISTS public.ai_period_audit_issues (
    audit_log_id bigint NOT NULL,
    issue_description character varying(255)
);

CREATE TABLE IF NOT EXISTS public.ai_period_audit_logs (
    id bigint NOT NULL,
    audit_date timestamp(6) without time zone NOT NULL,
    company_id character varying(255) NOT NULL,
    created_at timestamp(6) without time zone,
    is_resolved boolean NOT NULL,
    period bytea NOT NULL
);

ALTER TABLE public.ai_period_audit_logs ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.ai_period_audit_logs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE IF NOT EXISTS public.bank_connections (
    id uuid NOT NULL,
    account_number character varying(255) NOT NULL,
    bank_name character varying(255) NOT NULL,
    company_id uuid NOT NULL,
    last_sync character varying(255),
    status character varying(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS public.bank_reconciliation_transactions (
    id uuid NOT NULL,
    amount numeric(19,2) NOT NULL,
    company_id uuid NOT NULL,
    date date NOT NULL,
    description character varying(500) NOT NULL,
    reconciled boolean NOT NULL,
    reconciled_with_entry_id uuid,
    reference character varying(100)
);

CREATE TABLE IF NOT EXISTS public.bank_sync_transactions (
    id uuid NOT NULL,
    account_number character varying(255) NOT NULL,
    amount numeric(38,2) NOT NULL,
    bank_name character varying(255) NOT NULL,
    company_id uuid NOT NULL,
    description character varying(255),
    status character varying(255) NOT NULL,
    transaction_date timestamp(6) without time zone NOT NULL,
    transaction_id character varying(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS public.bank_transactions (
    id uuid NOT NULL,
    account_number character varying(255) NOT NULL,
    amount numeric(38,2) NOT NULL,
    bank_name character varying(255) NOT NULL,
    company_id uuid NOT NULL,
    description character varying(255),
    status character varying(255) NOT NULL,
    transaction_date timestamp(6) without time zone NOT NULL,
    transaction_id character varying(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS public.chargers (
    id bigint NOT NULL,
    active boolean NOT NULL,
    alias character varying(255) NOT NULL,
    firmware_version character varying(255),
    installation_date timestamp(6) without time zone NOT NULL,
    last_heartbeat timestamp(6) without time zone,
    manufacturer character varying(255) NOT NULL,
    model character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    ocpp_id character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    station_id uuid NOT NULL,
    CONSTRAINT chargers_status_check CHECK (((status)::text = ANY ((ARRAY['ONLINE'::character varying, 'OFFLINE'::character varying, 'MAINTENANCE'::character varying, 'ERROR'::character varying, 'DISABLED'::character varying])::text[])))
);

ALTER TABLE public.chargers ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.chargers_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE IF NOT EXISTS public.charging_sessions (
    id uuid NOT NULL,
    charger_ocpp_id character varying(255) NOT NULL,
    connector_number integer NOT NULL,
    ended_at timestamp(6) without time zone,
    final_energy_kwh numeric(10,3),
    initial_energy_kwh numeric(10,3),
    started_at timestamp(6) without time zone NOT NULL,
    station_id uuid NOT NULL,
    status character varying(255) NOT NULL,
    stop_reason character varying(500),
    user_id uuid NOT NULL,
    CONSTRAINT charging_sessions_status_check CHECK (((status)::text = ANY ((ARRAY['INITIATED'::character varying, 'CHARGING'::character varying, 'SUSPENDED'::character varying, 'COMPLETED'::character varying, 'FAILED'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS public.charging_stations (
    id uuid NOT NULL,
    active boolean NOT NULL,
    company_id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    last_maintenance_date timestamp(6) without time zone,
    address character varying(255),
    city character varying(255),
    country character varying(255),
    latitude double precision,
    longitude double precision,
    name character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    CONSTRAINT charging_stations_status_check CHECK (((status)::text = ANY ((ARRAY['UNDER_CONSTRUCTION'::character varying, 'OPERATIONAL'::character varying, 'MAINTENANCE'::character varying, 'DECOMMISSIONED'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS public.classification_rules (
    id uuid NOT NULL,
    account_code character varying(20) NOT NULL,
    company_id uuid NOT NULL,
    confidence double precision NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    learned_from character varying(1000),
    pattern character varying(500) NOT NULL,
    times_applied integer NOT NULL,
    times_confirmed integer NOT NULL
);

CREATE TABLE IF NOT EXISTS public.connectors (
    id bigint NOT NULL,
    active boolean NOT NULL,
    alias character varying(255) NOT NULL,
    connector_number integer NOT NULL,
    max_current numeric(38,2),
    max_power numeric(38,2),
    max_voltage numeric(38,2),
    last_status_change timestamp(6) without time zone,
    status character varying(255) NOT NULL,
    type character varying(255) NOT NULL,
    charger_id bigint NOT NULL,
    CONSTRAINT connectors_status_check CHECK (((status)::text = ANY ((ARRAY['AVAILABLE'::character varying, 'CHARGING'::character varying, 'SUSPENDED_EV'::character varying, 'SUSPENDED_EVSE'::character varying, 'RESERVED'::character varying, 'UNAVAILABLE'::character varying, 'FAULTED'::character varying, 'PREPARING'::character varying, 'FINISHING'::character varying])::text[]))),
    CONSTRAINT connectors_type_check CHECK (((type)::text = ANY ((ARRAY['TYPE_1'::character varying, 'TYPE_2'::character varying, 'CCS_TYPE_1'::character varying, 'CCS_TYPE_2'::character varying, 'CHADEMO'::character varying, 'TESLA'::character varying, 'GB_T_AC'::character varying, 'GB_T_DC'::character varying])::text[])))
);

ALTER TABLE public.connectors ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.connectors_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE IF NOT EXISTS public.corporate_documents (
    id uuid NOT NULL,
    ai_summary text,
    company_id character varying(255),
    document_type character varying(255),
    expiration_date date,
    storage_url character varying(255),
    title character varying(255)
);

CREATE TABLE IF NOT EXISTS public.crm_leads (
    id uuid NOT NULL,
    company_id uuid NOT NULL,
    company_name character varying(255),
    contact_name character varying(255) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    email character varying(255),
    phone character varying(255),
    status character varying(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS public.crm_quote_lines (
    id uuid NOT NULL,
    description character varying(255) NOT NULL,
    quantity numeric(38,2) NOT NULL,
    total_amount numeric(38,2) NOT NULL,
    unit_price numeric(38,2) NOT NULL,
    quote_id uuid
);

CREATE TABLE IF NOT EXISTS public.crm_quotes (
    id uuid NOT NULL,
    client_name character varying(255) NOT NULL,
    client_rut character varying(255) NOT NULL,
    company_id uuid NOT NULL,
    expiration_date date,
    lead_id uuid,
    quote_date date NOT NULL,
    status character varying(255) NOT NULL,
    total_amount numeric(38,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS public.energy_alerts (
    id uuid NOT NULL,
    acknowledged boolean NOT NULL,
    acknowledged_at timestamp(6) without time zone,
    acknowledged_by uuid,
    created_at timestamp(6) without time zone NOT NULL,
    message character varying(255) NOT NULL,
    resolved_at timestamp(6) without time zone,
    session_id uuid,
    severity character varying(255) NOT NULL,
    station_id uuid,
    title character varying(255) NOT NULL,
    type character varying(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS public.energy_measurements (
    id bigint NOT NULL,
    energy_kwh numeric(10,3) NOT NULL,
    measured_at timestamp(6) without time zone NOT NULL,
    meter_value character varying(500),
    session_id uuid NOT NULL
);

ALTER TABLE public.energy_measurements ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.energy_measurements_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE IF NOT EXISTS public.failed_charging_events (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    error_message character varying(2000),
    resolved boolean NOT NULL,
    retry_count integer NOT NULL,
    session_id character varying(255) NOT NULL,
    total_energy_kwh character varying(255),
    user_id character varying(255)
);

CREATE TABLE IF NOT EXISTS public.fixed_assets (
    id uuid NOT NULL,
    category character varying(255),
    company_id character varying(255),
    current_residual_value numeric(38,2),
    name character varying(255),
    purchase_date date,
    purchase_value numeric(38,2)
);

CREATE TABLE IF NOT EXISTS public.fs_green_suppliers (
    id uuid NOT NULL,
    category character varying(255),
    certification_level character varying(255),
    name character varying(255)
);

CREATE TABLE IF NOT EXISTS public.inv_products (
    id bigint NOT NULL,
    average_cost numeric(19,4) NOT NULL,
    category character varying(255),
    company_id character varying(255) NOT NULL,
    created_at timestamp(6) without time zone,
    current_stock numeric(19,4) NOT NULL,
    description character varying(255),
    name character varying(255) NOT NULL,
    sale_price numeric(19,4) NOT NULL,
    sku character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone
);

ALTER TABLE public.inv_products ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.inv_products_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE IF NOT EXISTS public.inv_stock_movements (
    id bigint NOT NULL,
    company_id character varying(255) NOT NULL,
    created_at timestamp(6) without time zone,
    description character varying(255),
    movement_date timestamp(6) without time zone NOT NULL,
    product_id bigint NOT NULL,
    quantity numeric(19,4) NOT NULL,
    reference_document character varying(255),
    total_cost numeric(19,4) NOT NULL,
    type character varying(255) NOT NULL,
    unit_cost numeric(19,4) NOT NULL,
    CONSTRAINT inv_stock_movements_type_check CHECK (((type)::text = ANY ((ARRAY['IN'::character varying, 'OUT'::character varying, 'ADJUSTMENT'::character varying])::text[])))
);

ALTER TABLE public.inv_stock_movements ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.inv_stock_movements_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE IF NOT EXISTS public.invoice_items (
    id uuid NOT NULL,
    amount numeric(38,2) NOT NULL,
    item_code character varying(255),
    item_name character varying(255) NOT NULL,
    line_number integer NOT NULL,
    price numeric(38,2) NOT NULL,
    quantity numeric(38,2) NOT NULL,
    unit_measure character varying(255),
    invoice_id uuid NOT NULL
);

CREATE TABLE IF NOT EXISTS public.jv_commit (
    commit_pk bigint NOT NULL,
    author character varying(200),
    commit_date timestamp without time zone,
    commit_date_instant character varying(30),
    commit_id numeric(22,2)
);

CREATE SEQUENCE public.jv_commit_pk_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS public.jv_commit_property (
    property_name character varying(191) NOT NULL,
    property_value character varying(600),
    commit_fk bigint NOT NULL
);

CREATE TABLE IF NOT EXISTS public.jv_global_id (
    global_id_pk bigint NOT NULL,
    local_id character varying(191),
    fragment character varying(200),
    type_name character varying(200),
    owner_id_fk bigint
);

CREATE SEQUENCE public.jv_global_id_pk_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS public.jv_snapshot (
    snapshot_pk bigint NOT NULL,
    type character varying(200),
    version bigint,
    state text,
    changed_properties text,
    managed_type character varying(200),
    global_id_fk bigint,
    commit_fk bigint
);

CREATE SEQUENCE public.jv_snapshot_pk_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS public.maintenance_records (
    id uuid NOT NULL,
    completed_at timestamp(6) without time zone,
    cost numeric(10,2),
    created_at timestamp(6) without time zone NOT NULL,
    description text NOT NULL,
    notes text,
    scheduled_date timestamp(6) without time zone NOT NULL,
    started_at timestamp(6) without time zone,
    station_id uuid NOT NULL,
    status character varying(50) NOT NULL,
    technician character varying(100),
    maintenance_type character varying(50) NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    CONSTRAINT maintenance_records_maintenance_type_check CHECK (((maintenance_type)::text = ANY ((ARRAY['PREVENTIVE'::character varying, 'CORRECTIVE'::character varying, 'EMERGENCY'::character varying, 'INSPECTION'::character varying, 'SOFTWARE_UPDATE'::character varying])::text[]))),
    CONSTRAINT maintenance_records_status_check CHECK (((status)::text = ANY ((ARRAY['SCHEDULED'::character varying, 'IN_PROGRESS'::character varying, 'COMPLETED'::character varying, 'CANCELLED'::character varying, 'FAILED'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS public.mrp_bom_items (
    bom_id uuid NOT NULL,
    component_product_id character varying(255),
    quantity numeric(38,2)
);

CREATE TABLE IF NOT EXISTS public.mrp_boms (
    id uuid NOT NULL,
    company_id character varying(255),
    created_at timestamp(6) without time zone,
    name character varying(255),
    product_id character varying(255),
    updated_at timestamp(6) without time zone
);

CREATE TABLE IF NOT EXISTS public.mrp_work_orders (
    id uuid NOT NULL,
    bom_id uuid,
    company_id character varying(255),
    created_at date,
    status character varying(255),
    target_quantity numeric(38,2)
);

CREATE TABLE IF NOT EXISTS public.ocpp_diagnostics_audit (
    id uuid NOT NULL,
    event_type character varying(255) NOT NULL,
    ocpp_id character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    "timestamp" timestamp(6) without time zone NOT NULL
);

CREATE TABLE IF NOT EXISTS public.organizations (
    id uuid NOT NULL,
    branding jsonb,
    created_at timestamp(6) without time zone NOT NULL,
    name character varying(200) NOT NULL,
    parent_id uuid,
    settings jsonb,
    status character varying(20) NOT NULL,
    type character varying(50) NOT NULL,
    updated_at timestamp(6) without time zone,
    CONSTRAINT organizations_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'SUSPENDED'::character varying, 'INACTIVE'::character varying, 'PENDING'::character varying])::text[]))),
    CONSTRAINT organizations_type_check CHECK (((type)::text = ANY ((ARRAY['ENTERPRISE'::character varying, 'SMB'::character varying, 'STARTUP'::character varying, 'INDIVIDUAL'::character varying, 'PARTNER'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS public.partner_types (
    partner_id uuid NOT NULL,
    type character varying(255)
);

CREATE TABLE IF NOT EXISTS public.partners (
    id uuid NOT NULL,
    company_id uuid NOT NULL,
    name character varying(255) NOT NULL,
    rut character varying(255) NOT NULL,
    email character varying(255)
);

CREATE TABLE IF NOT EXISTS public.pay_employees (
    id bigint NOT NULL,
    afp character varying(255) NOT NULL,
    base_salary numeric(19,4) NOT NULL,
    company_id character varying(255) NOT NULL,
    created_at timestamp(6) without time zone,
    family_loads integer,
    full_name character varying(255) NOT NULL,
    health_percentage numeric(19,4),
    health_system character varying(255) NOT NULL,
    rut character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone
);

ALTER TABLE public.pay_employees ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.pay_employees_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE IF NOT EXISTS public.pay_payslips (
    id bigint NOT NULL,
    afp_discount numeric(19,4),
    base_salary numeric(19,4),
    company_id character varying(255) NOT NULL,
    created_at timestamp(6) without time zone,
    gratification numeric(19,4),
    health_discount numeric(19,4),
    net_salary numeric(19,4),
    non_taxable_income numeric(19,4),
    period_id bigint NOT NULL,
    single_tax numeric(19,4),
    taxable_income numeric(19,4),
    total_discounts numeric(19,4),
    unemployment_insurance numeric(19,4),
    employee_id uuid NOT NULL
);

ALTER TABLE public.pay_payslips ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.pay_payslips_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE IF NOT EXISTS public.pay_periods (
    id bigint NOT NULL,
    company_id character varying(255) NOT NULL,
    is_closed boolean NOT NULL,
    period bytea NOT NULL,
    uf_value numeric(19,4) NOT NULL,
    utm_value numeric(19,4) NOT NULL
);

ALTER TABLE public.pay_periods ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.pay_periods_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE IF NOT EXISTS public.pos_transaction_items (
    id uuid NOT NULL,
    product_id character varying(255) NOT NULL,
    product_name character varying(255) NOT NULL,
    quantity numeric(38,2) NOT NULL,
    subtotal numeric(38,2) NOT NULL,
    unit_price numeric(38,2) NOT NULL,
    pos_transaction_id uuid
);

CREATE TABLE IF NOT EXISTS public.pos_transactions (
    id uuid NOT NULL,
    cashier_name character varying(255) NOT NULL,
    company_id uuid NOT NULL,
    payment_method character varying(255) NOT NULL,
    pos_terminal_id character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    total_amount numeric(38,2) NOT NULL,
    transaction_date timestamp(6) without time zone NOT NULL
);

CREATE TABLE IF NOT EXISTS public.projects (
    id uuid NOT NULL,
    budget numeric(38,2),
    company_id uuid NOT NULL,
    description character varying(255),
    end_date date,
    name character varying(255) NOT NULL,
    start_date date NOT NULL,
    status character varying(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS public.purchase_order_lines (
    id uuid NOT NULL,
    description character varying(255) NOT NULL,
    product_id uuid,
    quantity numeric(38,2) NOT NULL,
    total_amount numeric(38,2) NOT NULL,
    unit_price numeric(38,2) NOT NULL,
    purchase_order_id uuid
);

CREATE TABLE IF NOT EXISTS public.purchase_orders (
    id uuid NOT NULL,
    company_id uuid NOT NULL,
    expected_delivery_date date,
    order_date date NOT NULL,
    status character varying(255) NOT NULL,
    supplier_name character varying(255) NOT NULL,
    supplier_rut character varying(255) NOT NULL,
    total_amount numeric(38,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS public.reconciliation_learning_records (
    id uuid NOT NULL,
    accounting_entry_id uuid NOT NULL,
    bank_transaction_id uuid NOT NULL,
    company_id character varying(255) NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    suggested_confidence double precision NOT NULL,
    user_confirmed boolean NOT NULL,
    user_feedback character varying(255)
);

CREATE TABLE IF NOT EXISTS public.roles (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    description text,
    name character varying(100) NOT NULL,
    organization_id uuid,
    permissions jsonb NOT NULL,
    is_system_role boolean NOT NULL,
    updated_at timestamp(6) without time zone
);

CREATE TABLE IF NOT EXISTS public.sii_cafs (
    id uuid NOT NULL,
    company_id character varying(255),
    current_folio bigint,
    final_folio bigint,
    initial_folio bigint,
    tipo_dte character varying(255),
    fecha_autorizacion date,
    fecha_vencimiento date,
    xml_content text
);

-- Migración idempotente para bases existentes creadas con xml_content varchar(255).
ALTER TABLE public.sii_cafs ADD COLUMN IF NOT EXISTS tipo_dte character varying(255);
ALTER TABLE public.sii_cafs ADD COLUMN IF NOT EXISTS fecha_autorizacion date;
ALTER TABLE public.sii_cafs ADD COLUMN IF NOT EXISTS fecha_vencimiento date;
ALTER TABLE public.sii_cafs ALTER COLUMN xml_content TYPE text;

CREATE UNIQUE INDEX IF NOT EXISTS uk_caf_company_type_range
    ON public.sii_cafs (company_id, tipo_dte, initial_folio, final_folio);

ALTER TABLE invoicing.invoices ADD COLUMN IF NOT EXISTS receiver_name character varying(255);

ALTER TABLE public.partners ADD COLUMN IF NOT EXISTS email character varying(255);
ALTER TABLE invoicing.invoices ADD COLUMN IF NOT EXISTS sii_status_attempts integer DEFAULT 0;

CREATE UNIQUE INDEX IF NOT EXISTS uk_invoices_company_type_folio
    ON invoicing.invoices (company_id, type_code, folio);

CREATE TABLE IF NOT EXISTS public.sii_tokens (
    id uuid NOT NULL,
    company_id character varying(255),
    expiration timestamp(6) without time zone,
    token character varying(255)
);

CREATE TABLE IF NOT EXISTS public.tax_adjustments (
    id bigint NOT NULL,
    amount numeric(19,4) NOT NULL,
    company_id character varying(255) NOT NULL,
    created_at timestamp(6) without time zone,
    description character varying(255) NOT NULL,
    fiscal_year integer NOT NULL,
    type character varying(255) NOT NULL
);

ALTER TABLE public.tax_adjustments ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.tax_adjustments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE IF NOT EXISTS public.tax_parameters (
    id character varying(255) NOT NULL,
    bhe_retention_rate numeric(38,2),
    uf_value numeric(38,2),
    utm_value numeric(38,2)
);

-- Parámetros tributarios 2026.
-- UTM enero 2026 = 69.751 y UF 01-01-2026 = 39.731,79: SII "Valores y fechas"
-- (utm/utm2026.htm y uf/uf2026.htm).
-- Retención BHE 2026 = 15,25% (SII, noticia del 26-12-2025).
INSERT INTO public.tax_parameters (id, utm_value, uf_value, bhe_retention_rate)
VALUES ('YEAR_2026', 69751, 39731.79, 15.25),
       ('YEAR_2025', 67429, 38419.17, 14.50)
ON CONFLICT (id) DO UPDATE
    SET utm_value = EXCLUDED.utm_value,
        uf_value = EXCLUDED.uf_value,
        bhe_retention_rate = EXCLUDED.bhe_retention_rate;

CREATE TABLE IF NOT EXISTS public.tax_profiles (
    company_id character varying(255) NOT NULL,
    created_at timestamp(6) without time zone,
    ppm_rate numeric(5,4) NOT NULL,
    regime character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone,
    CONSTRAINT tax_profiles_regime_check CHECK (((regime)::text = ANY ((ARRAY['REGIME_14A_SEMI_INTEGRADO'::character varying, 'REGIME_14D3_PROPYME_GENERAL'::character varying, 'REGIME_14D8_PROPYME_TRANSPARENTE'::character varying, 'REGIME_RENTA_PRESUNTA'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS public.user_roles (
    user_id uuid NOT NULL,
    role_id uuid NOT NULL
);

CREATE TABLE IF NOT EXISTS public.users (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    email character varying(255) NOT NULL,
    email_verified boolean NOT NULL,
    first_name character varying(100),
    last_login_at timestamp(6) without time zone,
    last_name character varying(100),
    organization_id uuid NOT NULL,
    password_hash character varying(255),
    phone character varying(50),
    status character varying(20) NOT NULL,
    updated_at timestamp(6) without time zone,
    CONSTRAINT users_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INVITED'::character varying, 'SUSPENDED'::character varying, 'INACTIVE'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS public.wms_locations (
    id uuid NOT NULL,
    code character varying(255),
    company_id character varying(255),
    description character varying(255),
    zone character varying(255)
);

CREATE TABLE IF NOT EXISTS public.wms_picking_items (
    task_id uuid NOT NULL,
    location_code character varying(255),
    product_id character varying(255),
    quantity integer
);

CREATE TABLE IF NOT EXISTS public.wms_picking_tasks (
    id uuid NOT NULL,
    company_id character varying(255),
    order_id character varying(255),
    status character varying(255)
);

CREATE TABLE IF NOT EXISTS sso.companies (
    id uuid NOT NULL,
    commercial_address character varying(255),
    created_at timestamp(6) with time zone NOT NULL,
    email character varying(255) NOT NULL,
    is_active boolean NOT NULL,
    is_profile_complete boolean DEFAULT false NOT NULL,
    logo_url character varying(255),
    phone_number character varying(255),
    razon_social character varying(255) NOT NULL,
    rut character varying(255) NOT NULL,
    website character varying(255),
    giro character varying(255),
    acteco character varying(255),
    comuna character varying(255)
);

ALTER TABLE sso.companies ADD COLUMN IF NOT EXISTS giro character varying(255);
ALTER TABLE sso.companies ADD COLUMN IF NOT EXISTS acteco character varying(255);
ALTER TABLE sso.companies ADD COLUMN IF NOT EXISTS comuna character varying(255);

CREATE TABLE IF NOT EXISTS sso.company_certificates (
    id uuid NOT NULL,
    certificate_data bytea NOT NULL,
    company_id uuid NOT NULL,
    password character varying(255) NOT NULL,
    valid_until date
);

CREATE TABLE IF NOT EXISTS sso.user_invitations (
    id uuid NOT NULL,
    email character varying(255) NOT NULL,
    expires_at timestamp(6) with time zone NOT NULL,
    role character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    target_company_id uuid NOT NULL,
    token character varying(255) NOT NULL,
    CONSTRAINT user_invitations_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'ACCEPTED'::character varying, 'EXPIRED'::character varying, 'REVOKED'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS sso.user_preferences (
    user_id uuid NOT NULL,
    pref_value character varying(255),
    pref_key character varying(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS sso.users (
    id uuid NOT NULL,
    company_id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    email character varying(255) NOT NULL,
    full_name character varying(255),
    is_active boolean NOT NULL,
    password_hash character varying(255) NOT NULL,
    role character varying(255) NOT NULL,
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['ADMIN'::character varying, 'ACCOUNTANT'::character varying, 'TREASURER'::character varying, 'HR'::character varying, 'SALES'::character varying, 'OPERATIONS'::character varying, 'AUDITOR'::character varying, 'USER'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS sustainability.green_metrics (
    id uuid NOT NULL,
    accounting_entry_id uuid,
    carbon_footprint numeric(19,4) NOT NULL,
    category character varying(50) NOT NULL,
    company_id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    evidence character varying(500),
    green_score numeric(5,2) NOT NULL,
    CONSTRAINT green_metrics_category_check CHECK (((category)::text = ANY ((ARRAY['SCOPE_1_EMISSION'::character varying, 'SCOPE_2_EMISSION'::character varying, 'SCOPE_3_EMISSION'::character varying, 'RENEWABLE_ENERGY'::character varying, 'WASTE_MANAGEMENT'::character varying, 'CARBON_OFFSET'::character varying, 'SUSTAINABLE_SUPPLY'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS sustainability.sustainability_records (
    id uuid NOT NULL,
    calculated_at timestamp(6) without time zone NOT NULL,
    carbon_footprint_kg numeric(38,2) NOT NULL,
    category_name character varying(255),
    confidence_score double precision,
    invoice_id uuid NOT NULL
);

ALTER TABLE ONLY b2b.order_items ALTER COLUMN id SET DEFAULT nextval('b2b.order_items_id_seq'::regclass);

ALTER TABLE ONLY accounting.accounting_entries
    ADD CONSTRAINT accounting_entries_pkey PRIMARY KEY (id);

ALTER TABLE ONLY accounting.accounts
    ADD CONSTRAINT accounts_pkey PRIMARY KEY (id);

ALTER TABLE ONLY accounting.closed_periods
    ADD CONSTRAINT closed_periods_pkey PRIMARY KEY (id);

ALTER TABLE ONLY accounting.closed_periods
    ADD CONSTRAINT ukm4a2h72gwvvh2a6t4e5b72bcb UNIQUE (company_id, period);

ALTER TABLE ONLY b2b.order_items
    ADD CONSTRAINT order_items_pkey PRIMARY KEY (id);

ALTER TABLE ONLY b2b.orders
    ADD CONSTRAINT orders_pkey PRIMARY KEY (id);

ALTER TABLE ONLY energy.certificates
    ADD CONSTRAINT certificates_pkey PRIMARY KEY (id);

ALTER TABLE ONLY energy.charger_models
    ADD CONSTRAINT charger_models_pkey PRIMARY KEY (id);

ALTER TABLE ONLY energy.demand_pricings
    ADD CONSTRAINT demand_pricings_pkey PRIMARY KEY (id);

ALTER TABLE ONLY energy.manufacturers
    ADD CONSTRAINT manufacturers_pkey PRIMARY KEY (id);

ALTER TABLE ONLY energy.pricing_rules
    ADD CONSTRAINT pricing_rules_pkey PRIMARY KEY (id);

ALTER TABLE ONLY energy.promotional_campaigns
    ADD CONSTRAINT promotional_campaigns_pkey PRIMARY KEY (id);

ALTER TABLE ONLY energy.seasonal_pricings
    ADD CONSTRAINT seasonal_pricings_pkey PRIMARY KEY (id);

ALTER TABLE ONLY energy.security_events
    ADD CONSTRAINT security_events_pkey PRIMARY KEY (id);

ALTER TABLE ONLY energy.promotional_campaigns
    ADD CONSTRAINT uk33m0xku3k79sxoqgx97rmfi94 UNIQUE (code);

ALTER TABLE ONLY fees.fee_receipts
    ADD CONSTRAINT fee_receipts_pkey PRIMARY KEY (id);

ALTER TABLE ONLY fleet.vehicles
    ADD CONSTRAINT uk9vovnbiegxevdhqfcwvp2g8pj UNIQUE (license_plate);

ALTER TABLE ONLY fleet.vehicles
    ADD CONSTRAINT uks2g13tvy2h129ohm301tqxgn9 UNIQUE (rfid_tag);

ALTER TABLE ONLY fleet.vehicles
    ADD CONSTRAINT vehicles_pkey PRIMARY KEY (id);

ALTER TABLE ONLY invoicing.invoices
    ADD CONSTRAINT invoices_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.ai_insights
    ADD CONSTRAINT ai_insights_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.ai_period_audit_logs
    ADD CONSTRAINT ai_period_audit_logs_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.bank_connections
    ADD CONSTRAINT bank_connections_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.bank_reconciliation_transactions
    ADD CONSTRAINT bank_reconciliation_transactions_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.bank_sync_transactions
    ADD CONSTRAINT bank_sync_transactions_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.bank_transactions
    ADD CONSTRAINT bank_transactions_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.chargers
    ADD CONSTRAINT chargers_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.charging_sessions
    ADD CONSTRAINT charging_sessions_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.charging_stations
    ADD CONSTRAINT charging_stations_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.classification_rules
    ADD CONSTRAINT classification_rules_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.connectors
    ADD CONSTRAINT connectors_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.corporate_documents
    ADD CONSTRAINT corporate_documents_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.crm_leads
    ADD CONSTRAINT crm_leads_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.crm_quote_lines
    ADD CONSTRAINT crm_quote_lines_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.crm_quotes
    ADD CONSTRAINT crm_quotes_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.energy_alerts
    ADD CONSTRAINT energy_alerts_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.energy_measurements
    ADD CONSTRAINT energy_measurements_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.failed_charging_events
    ADD CONSTRAINT failed_charging_events_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.fixed_assets
    ADD CONSTRAINT fixed_assets_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.fs_green_suppliers
    ADD CONSTRAINT fs_green_suppliers_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.inv_products
    ADD CONSTRAINT inv_products_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.inv_stock_movements
    ADD CONSTRAINT inv_stock_movements_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.invoice_items
    ADD CONSTRAINT invoice_items_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.jv_commit
    ADD CONSTRAINT jv_commit_pk PRIMARY KEY (commit_pk);

ALTER TABLE ONLY public.jv_commit_property
    ADD CONSTRAINT jv_commit_property_pk PRIMARY KEY (commit_fk, property_name);

ALTER TABLE ONLY public.jv_global_id
    ADD CONSTRAINT jv_global_id_pk PRIMARY KEY (global_id_pk);

ALTER TABLE ONLY public.jv_snapshot
    ADD CONSTRAINT jv_snapshot_pk PRIMARY KEY (snapshot_pk);

ALTER TABLE ONLY public.maintenance_records
    ADD CONSTRAINT maintenance_records_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.mrp_boms
    ADD CONSTRAINT mrp_boms_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.mrp_work_orders
    ADD CONSTRAINT mrp_work_orders_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.ocpp_diagnostics_audit
    ADD CONSTRAINT ocpp_diagnostics_audit_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.organizations
    ADD CONSTRAINT organizations_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.partners
    ADD CONSTRAINT partners_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.pay_employees
    ADD CONSTRAINT pay_employees_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.pay_payslips
    ADD CONSTRAINT pay_payslips_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.pay_periods
    ADD CONSTRAINT pay_periods_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.pos_transaction_items
    ADD CONSTRAINT pos_transaction_items_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.pos_transactions
    ADD CONSTRAINT pos_transactions_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.projects
    ADD CONSTRAINT projects_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.purchase_order_lines
    ADD CONSTRAINT purchase_order_lines_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.purchase_orders
    ADD CONSTRAINT purchase_orders_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.reconciliation_learning_records
    ADD CONSTRAINT reconciliation_learning_records_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.sii_cafs
    ADD CONSTRAINT sii_cafs_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.sii_tokens
    ADD CONSTRAINT sii_tokens_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.tax_adjustments
    ADD CONSTRAINT tax_adjustments_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.tax_parameters
    ADD CONSTRAINT tax_parameters_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.tax_profiles
    ADD CONSTRAINT tax_profiles_pkey PRIMARY KEY (company_id);

ALTER TABLE ONLY public.chargers
    ADD CONSTRAINT uk2ija1ujl00asoikugevcbjxhn UNIQUE (ocpp_id);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);

ALTER TABLE ONLY public.partners
    ADD CONSTRAINT ukevimny2sv6c5dtdx8n18dg578 UNIQUE (company_id, rut);

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_pkey PRIMARY KEY (user_id, role_id);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.wms_locations
    ADD CONSTRAINT wms_locations_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.wms_picking_tasks
    ADD CONSTRAINT wms_picking_tasks_pkey PRIMARY KEY (id);

ALTER TABLE ONLY sso.companies
    ADD CONSTRAINT companies_pkey PRIMARY KEY (id);

ALTER TABLE ONLY sso.company_certificates
    ADD CONSTRAINT company_certificates_pkey PRIMARY KEY (id);

ALTER TABLE ONLY sso.users
    ADD CONSTRAINT uk6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);

ALTER TABLE ONLY sso.companies
    ADD CONSTRAINT ukeb8oy6ruaf03eq74w8qnrchqv UNIQUE (rut);

ALTER TABLE ONLY sso.user_invitations
    ADD CONSTRAINT ukevk8fq824da1yfg84o0a7blbg UNIQUE (token);

ALTER TABLE ONLY sso.user_invitations
    ADD CONSTRAINT user_invitations_pkey PRIMARY KEY (id);

ALTER TABLE ONLY sso.user_preferences
    ADD CONSTRAINT user_preferences_pkey PRIMARY KEY (user_id, pref_key);

ALTER TABLE ONLY sso.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);

ALTER TABLE ONLY sustainability.green_metrics
    ADD CONSTRAINT green_metrics_pkey PRIMARY KEY (id);

ALTER TABLE ONLY sustainability.sustainability_records
    ADD CONSTRAINT sustainability_records_pkey PRIMARY KEY (id);

ALTER TABLE ONLY sustainability.sustainability_records
    ADD CONSTRAINT ukn3x3wv2d8dklnqaqd0aypslcd UNIQUE (invoice_id);

CREATE INDEX idx_accounting_entry_company ON accounting.accounting_entries USING btree (company_id);

CREATE INDEX idx_b2b_orders_customer ON b2b.orders USING btree (customer_id);

CREATE INDEX idx_invoice_company ON invoicing.invoices USING btree (company_id);

CREATE INDEX idx_bank_reco_tx_company ON public.bank_reconciliation_transactions USING btree (company_id);

CREATE INDEX idx_bank_sync_company ON public.bank_sync_transactions USING btree (company_id);

CREATE INDEX idx_caf_company ON public.sii_cafs USING btree (company_id);

CREATE INDEX idx_recon_tx_company ON public.bank_reconciliation_transactions USING btree (company_id);

CREATE INDEX idx_recon_tx_date ON public.bank_reconciliation_transactions USING btree (date);

CREATE INDEX idx_sii_token_company ON public.sii_tokens USING btree (company_id);

CREATE INDEX jv_commit_commit_id_idx ON public.jv_commit USING btree (commit_id);

CREATE INDEX jv_commit_property_commit_fk_idx ON public.jv_commit_property USING btree (commit_fk);

CREATE INDEX jv_commit_property_property_name_property_value_idx ON public.jv_commit_property USING btree (property_name, property_value);

CREATE INDEX jv_global_id_local_id_idx ON public.jv_global_id USING btree (local_id);

CREATE INDEX jv_global_id_owner_id_fk_idx ON public.jv_global_id USING btree (owner_id_fk);

CREATE INDEX jv_snapshot_commit_fk_idx ON public.jv_snapshot USING btree (commit_fk);

CREATE INDEX jv_snapshot_global_id_fk_idx ON public.jv_snapshot USING btree (global_id_fk);

CREATE INDEX idx_green_metrics_category ON sustainability.green_metrics USING btree (category);

CREATE INDEX idx_green_metrics_company ON sustainability.green_metrics USING btree (company_id);

CREATE INDEX idx_green_metrics_entry ON sustainability.green_metrics USING btree (accounting_entry_id);

ALTER TABLE ONLY accounting.accounting_entry_lines
    ADD CONSTRAINT fkn0l3bl0686ip3s873lnanl68e FOREIGN KEY (entry_id) REFERENCES accounting.accounting_entries(id);

ALTER TABLE ONLY b2b.order_items
    ADD CONSTRAINT order_items_order_id_fkey FOREIGN KEY (order_id) REFERENCES b2b.orders(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.invoice_items
    ADD CONSTRAINT fk46ae0lhu1oqs7cv91fn6y9n7w FOREIGN KEY (invoice_id) REFERENCES invoicing.invoices(id);

ALTER TABLE ONLY public.mrp_bom_items
    ADD CONSTRAINT fk8towchpq9w7e0b166pqn78a5f FOREIGN KEY (bom_id) REFERENCES public.mrp_boms(id);

ALTER TABLE ONLY public.ai_period_audit_issues
    ADD CONSTRAINT fk9e431dk8o6xmnu66oaatfi8w6 FOREIGN KEY (audit_log_id) REFERENCES public.ai_period_audit_logs(id);

ALTER TABLE ONLY public.connectors
    ADD CONSTRAINT fkaswbebg1ne3q10d1dcp9t1mm4 FOREIGN KEY (charger_id) REFERENCES public.chargers(id);

ALTER TABLE ONLY public.partner_types
    ADD CONSTRAINT fkbtlh83ttdh2fmb2303h5jcqsa FOREIGN KEY (partner_id) REFERENCES public.partners(id);

ALTER TABLE ONLY public.energy_measurements
    ADD CONSTRAINT fkeyaps45ok1mjwyntdnup1x9ms FOREIGN KEY (session_id) REFERENCES public.charging_sessions(id);

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT fkh8ciramu9cc9q3qcqiv4ue8a6 FOREIGN KEY (role_id) REFERENCES public.roles(id);

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT fkhfh9dx7w3ubf1co1vdev94g3f FOREIGN KEY (user_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.chargers
    ADD CONSTRAINT fkimxr8sqe936u47dvvgt7cn7qh FOREIGN KEY (station_id) REFERENCES public.charging_stations(id);

ALTER TABLE ONLY public.purchase_order_lines
    ADD CONSTRAINT fklm5ieywqw1p1l4oxnkup8j2m6 FOREIGN KEY (purchase_order_id) REFERENCES public.purchase_orders(id);

ALTER TABLE ONLY public.wms_picking_items
    ADD CONSTRAINT fkoqu6sltvpmx6bm36j3cgp235f FOREIGN KEY (task_id) REFERENCES public.wms_picking_tasks(id);

ALTER TABLE ONLY public.pos_transaction_items
    ADD CONSTRAINT fksmfkt8j55jbkaxwgof6piaoej FOREIGN KEY (pos_transaction_id) REFERENCES public.pos_transactions(id);

ALTER TABLE ONLY public.crm_quote_lines
    ADD CONSTRAINT fksy7wfahg3s9xdbyuujpsia6kq FOREIGN KEY (quote_id) REFERENCES public.crm_quotes(id);

ALTER TABLE ONLY public.jv_commit_property
    ADD CONSTRAINT jv_commit_property_commit_fk FOREIGN KEY (commit_fk) REFERENCES public.jv_commit(commit_pk);

ALTER TABLE ONLY public.jv_global_id
    ADD CONSTRAINT jv_global_id_owner_id_fk FOREIGN KEY (owner_id_fk) REFERENCES public.jv_global_id(global_id_pk);

ALTER TABLE ONLY public.jv_snapshot
    ADD CONSTRAINT jv_snapshot_commit_fk FOREIGN KEY (commit_fk) REFERENCES public.jv_commit(commit_pk);

ALTER TABLE ONLY public.jv_snapshot
    ADD CONSTRAINT jv_snapshot_global_id_fk FOREIGN KEY (global_id_fk) REFERENCES public.jv_global_id(global_id_pk);

ALTER TABLE ONLY sso.user_preferences
    ADD CONSTRAINT fkepakpib0qnm82vmaiismkqf88 FOREIGN KEY (user_id) REFERENCES sso.users(id);

ALTER TABLE ONLY sustainability.sustainability_records
    ADD CONSTRAINT fknkv7999kpx2lq9f3fg54asqcy FOREIGN KEY (invoice_id) REFERENCES invoicing.invoices(id);


CREATE SCHEMA IF NOT EXISTS integration_sii;

CREATE TABLE IF NOT EXISTS integration_sii.sii_sync_state (
    company_id uuid NOT NULL,
    last_sync_at timestamp(6),
    next_sync_at timestamp(6),
    sii_status character varying(16),
    caf_status character varying(16),
    dte_status character varying(16),
    last_error character varying(500),
    updated_at timestamp(6),
    CONSTRAINT sii_sync_state_pkey PRIMARY KEY (company_id)
);

ALTER TABLE invoicing.invoices ADD COLUMN IF NOT EXISTS dte_status character varying(16) NOT NULL DEFAULT 'PENDIENTE';
ALTER TABLE invoicing.invoices ADD COLUMN IF NOT EXISTS sii_track_id character varying(64);
ALTER TABLE invoicing.invoices ADD COLUMN IF NOT EXISTS sent_at timestamp(6);
