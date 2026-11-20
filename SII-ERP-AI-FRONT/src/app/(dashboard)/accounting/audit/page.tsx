'use client';

import { useState } from 'react';
import { Input } from '@/components/ui/input';
import { PageHeader } from '@/features/accounting/components/PageHeader';
import { SiiMirrorTable } from '@/features/accounting/components/SiiMirrorTable';
import { useSiiMirror } from '@/features/accounting/hooks/useAccounting';

const now = new Date();

export default function AuditPage() {
    const [period, setPeriod] = useState(
        `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`,
    );
    const [year, month] = period.split('-').map(Number);

    const { data, isLoading, error, refetch, isFetching } = useSiiMirror(year, month);

    return (
        <div className="space-y-6">
            <PageHeader
                title="Auditoría tributaria"
                description="Comparación del RCV del SII contra la contabilidad interna, documento por documento."
                actions={
                    <div className="flex items-center gap-2">
                        <label htmlFor="audit-period" className="text-sm text-muted-foreground">
                            Período
                        </label>
                        <Input
                            id="audit-period"
                            type="month"
                            className="w-44"
                            value={period}
                            onChange={(event) => setPeriod(event.target.value)}
                        />
                    </div>
                }
            />

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
