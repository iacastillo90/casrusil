'use client';

import { useState } from 'react';
import { Input } from '@/components/ui/input';
import { SiiMirrorTable } from './SiiMirrorTable';
import { useSiiMirror } from '../hooks/useAccounting';

/**
 * Conciliación RCV del SII contra el ERP.
 *
 * Antes esta tabla usaba `GET /accounting/f29` (que solo entrega IVA y PPM) y
 * presentaba una conciliación que no correspondía a ese endpoint. Ahora usa el
 * espejo real (`GET /accounting/audit/sii-mirror`), documento por documento.
 */
export function TaxReconciliationTable() {
    const now = new Date();
    const [period, setPeriod] = useState(
        `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`,
    );
    const [year, month] = period.split('-').map(Number);

    const { data, isLoading, error, refetch, isFetching } = useSiiMirror(year, month);

    return (
        <div className="space-y-4">
            <div className="flex items-center gap-2">
                <label htmlFor="reconciliation-period" className="text-sm text-muted-foreground">
                    Período
                </label>
                <Input
                    id="reconciliation-period"
                    type="month"
                    className="w-44"
                    value={period}
                    onChange={(event) => setPeriod(event.target.value)}
                />
            </div>

            <SiiMirrorTable
                report={data}
                isLoading={isLoading}
                error={error instanceof Error ? error : null}
                onRetry={() => refetch()}
                isRetrying={isFetching && !isLoading}
            />
        </div>
    );
}

export default TaxReconciliationTable;
