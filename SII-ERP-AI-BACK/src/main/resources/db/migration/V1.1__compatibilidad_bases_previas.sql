-- Compatibilidad para bases creadas antes de Flyway/V1.
-- V1 ya contiene estas tablas; aquí se crean si la base existente no las tenía.
CREATE TABLE IF NOT EXISTS public.time_of_use_rates (
    id uuid NOT NULL,
    name character varying(100) NOT NULL,
    description text,
    active boolean NOT NULL DEFAULT true,
    created_at timestamp(6) without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp(6) without time zone,
    CONSTRAINT time_of_use_rates_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.time_of_use_periods (
    id uuid NOT NULL,
    rate_id uuid NOT NULL,
    day_of_week integer NOT NULL,
    start_time time without time zone NOT NULL,
    end_time time without time zone NOT NULL,
    rate_per_kwh numeric(10,2) NOT NULL,
    period_type character varying(50) NOT NULL,
    CONSTRAINT time_of_use_periods_pkey PRIMARY KEY (id),
    CONSTRAINT fk_tou_periods_rate FOREIGN KEY (rate_id) REFERENCES public.time_of_use_rates(id) ON DELETE CASCADE,
    CONSTRAINT check_day_of_week CHECK (day_of_week BETWEEN 0 AND 6),
    CONSTRAINT check_period_type CHECK (period_type::text IN ('PEAK','OFF_PEAK','SHOULDER'))
);

CREATE TABLE IF NOT EXISTS public.alert_rules (
    id uuid NOT NULL,
    name character varying(100) NOT NULL UNIQUE,
    rule_type character varying(50) NOT NULL,
    condition jsonb NOT NULL,
    threshold_value numeric(15,2),
    threshold_operator character varying(10),
    alert_type character varying(50) NOT NULL,
    alert_severity character varying(20) NOT NULL,
    enabled boolean NOT NULL DEFAULT true,
    notification_channels jsonb,
    created_at timestamp(6) without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp(6) without time zone,
    last_triggered_at timestamp(6) without time zone,
    CONSTRAINT alert_rules_pkey PRIMARY KEY (id),
    CONSTRAINT alert_rules_operator_check CHECK (threshold_operator::text IN ('>','<','>=','<=','=','!=')),
    CONSTRAINT alert_rules_severity_check CHECK (alert_severity::text IN ('INFO','WARNING','CRITICAL'))
);

CREATE INDEX IF NOT EXISTS idx_time_of_use_rates_active ON public.time_of_use_rates(active);
CREATE INDEX IF NOT EXISTS idx_tou_periods_rate ON public.time_of_use_periods(rate_id);
CREATE INDEX IF NOT EXISTS idx_alert_rules_enabled ON public.alert_rules(enabled) WHERE enabled = TRUE;
