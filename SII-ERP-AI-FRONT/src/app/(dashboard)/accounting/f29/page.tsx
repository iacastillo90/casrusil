'use client';

import { useState } from 'react';
import { Input } from '@/components/ui/input';
import { PageHeader } from '@/features/accounting/components/PageHeader';
import { ErrorState, TableSkeleton } from '@/features/accounting/components/DataStates';
import { F29Preview } from '@/features/accounting/components/F29Preview';
import { F29AlertsPanel } from '@/features/accounting/components/F29AlertsPanel';
import { useF29, useF29Alerts } from '@/features/accounting/hooks/useAccounting';

const currentPeriod = () => {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
};

export default function F29Page() {
    const [period, setPeriod] = useState<string>(currentPeriod);

    const f29 = useF29(period);
    const alerts = useF29Alerts();

    return (
        <div className="space-y-6">
            <PageHeader
                title="Formulario 29"
                description="IVA débito, crédito fiscal, PPM y retenciones del período. Los montos los calcula el backend."
                actions={
                    <div className="flex items-center gap-2">
                        <label htmlFor="f29-period" className="text-sm text-muted-foreground">
                            Período
                        </label>
                        <Input
                            id="f29-period"
                            type="month"
                            className="w-44"
                            value={period}
                            onChange={(event) => setPeriod(event.target.value)}
                        />
                    </div>
                }
            />

            {alerts.isLoading ? (
                <TableSkeleton rows={2} cols={4} />
            ) : alerts.error ? (
                <ErrorState
                    title="No se pudieron cargar las alertas del F29"
                    message={
                        alerts.error instanceof Error
                            ? alerts.error.message
                            : 'Error desconocido'
                    }
                    onRetry={() => alerts.refetch()}
                    retrying={alerts.isFetching}
                />
            ) : (
                <F29AlertsPanel alerts={alerts.data ?? []} />
            )}

            {f29.error ? (
                <ErrorState
                    title="No se pudo calcular el F29"
                    message={f29.error instanceof Error ? f29.error.message : 'Error desconocido'}
                    onRetry={() => f29.refetch()}
                    retrying={f29.isFetching}
                />
            ) : f29.isLoading ? (
                <TableSkeleton rows={8} cols={3} />
            ) : f29.data ? (
                <F29Preview report={f29.data} />
            ) : null}
        </div>
    );
}
