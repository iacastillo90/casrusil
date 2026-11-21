-- pay_periods.period era bytea con un java.time.YearMonth serializado (Java
-- serialization). Se convierte a varchar(7) 'yyyy-MM'.
ALTER TABLE public.pay_periods ADD COLUMN period_text varchar(7);

UPDATE public.pay_periods
SET period_text = to_char(
        make_date(
            (get_byte(period, 39) * 256 + get_byte(period, 40)),
            get_byte(period, 41),
            1),
        'YYYY-MM')
WHERE period_text IS NULL;

ALTER TABLE public.pay_periods ALTER COLUMN period DROP NOT NULL;
ALTER TABLE public.pay_periods DROP COLUMN period;
ALTER TABLE public.pay_periods RENAME COLUMN period_text TO period;
ALTER TABLE public.pay_periods ALTER COLUMN period SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_pay_periods_company_period
    ON public.pay_periods (company_id, period);
