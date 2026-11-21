-- Unifica el plan de cuentas al detallado de 6 dígitos.
-- Reasigna las líneas de asiento de las cuentas canónicas con puntos a su
-- equivalente detallado y elimina la canónica sólo si quedó sin movimientos.
-- Si una canónica no tiene equivalente, se crea el código detallado indicado.

DO $$
DECLARE
    v_company uuid;
    v_map record;
    v_created integer := 0;
BEGIN
    FOR v_company IN SELECT id FROM sso.companies LOOP
        -- Cuentas detalladas usadas por app.accounts.* aunque no tengan origen canónico.
        FOR v_map IN
            SELECT * FROM (VALUES
                ('1103', 'Existencias', 'ASSET', 'Inventarios'),
                ('1201', 'Depreciación Acumulada', 'ASSET', 'Depreciación acumulada de activos fijos'),
                ('5297', 'Depreciación', 'EXPENSE', 'Gasto por depreciación')
            ) AS t(detailed, name, type, description)
        LOOP
            IF NOT EXISTS (
                SELECT 1 FROM accounting.accounts
                WHERE company_id = v_company AND code = v_map.detailed
            ) THEN
                INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
                VALUES (gen_random_uuid(), v_company, v_map.detailed, v_map.name, v_map.type, v_map.description, true);
                v_created := v_created + 1;
            END IF;
        END LOOP;

        -- Asegura las cuentas detalladas de destino y las de defaults de app.accounts.*.
        FOR v_map IN
            SELECT * FROM (VALUES
                ('1.1.01.001', '110101', 'Caja Moneda Nacional', 'ASSET', 'Efectivo en pesos chilenos'),
                ('1.1.03.001', '110301', 'Mercaderías', 'ASSET', 'Existencias de mercaderías'),
                ('1.1.03.999', '110399', 'Existencias por Clasificar', 'ASSET', 'Existencias pendientes de clasificación'),
                ('1.1.05.001', '110501', 'Clientes Nacionales', 'ASSET', 'Cuentas por cobrar clientes nacionales'),
                ('1.1.08.001', '110801', 'IVA Crédito Fiscal del Mes', 'ASSET', 'IVA crédito fiscal del período'),
                ('2.1.01.001', '210101', 'Proveedores Nacionales', 'LIABILITY', 'Cuentas por pagar proveedores nacionales'),
                ('2.1.04.001', '210401', 'IVA Débito Fiscal del Mes', 'LIABILITY', 'IVA débito fiscal del período'),
                ('4.1.01.001', '410101', 'Ventas Nacionales', 'REVENUE', 'Ventas en el mercado nacional'),
                ('5.1.03.025', '5298', 'Gastos Generales', 'EXPENSE', 'Gastos generales de administración'),
                ('5.9.99.999', '5299', 'Gastos por Clasificar', 'EXPENSE', 'Gastos pendientes de clasificación')
            ) AS t(canonical, detailed, name, type, description)
        LOOP
            IF NOT EXISTS (
                SELECT 1 FROM accounting.accounts
                WHERE company_id = v_company AND code = v_map.detailed
            ) THEN
                INSERT INTO accounting.accounts (id, company_id, code, name, type, description, active)
                VALUES (gen_random_uuid(), v_company, v_map.detailed, v_map.name, v_map.type, v_map.description, true);
                v_created := v_created + 1;
            END IF;

            UPDATE accounting.accounting_entry_lines l
            SET account_code = v_map.detailed
            FROM accounting.accounting_entries e
            WHERE l.entry_id = e.id
              AND e.company_id = v_company
              AND l.account_code = v_map.canonical;

            DELETE FROM accounting.accounts a
            WHERE a.company_id = v_company
              AND a.code = v_map.canonical
              AND NOT EXISTS (
                  SELECT 1
                  FROM accounting.accounting_entry_lines l
                  JOIN accounting.accounting_entries e ON e.id = l.entry_id
                  WHERE e.company_id = v_company AND l.account_code = v_map.canonical
              );
        END LOOP;
    END LOOP;

    RAISE NOTICE 'Plan de cuentas unificado. Cuentas detalladas creadas: %', v_created;
END $$;
