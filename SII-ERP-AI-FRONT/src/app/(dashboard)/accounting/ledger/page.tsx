'use client';

import { PageHeader } from '@/features/accounting/components/PageHeader';
import { JournalEntryTable } from '@/features/accounting/components/JournalEntryTable';
import { useLedger } from '@/features/accounting/hooks/useAccounting';

export default function LedgerPage() {
    const { data, isLoading, isFetching, error, refetch } = useLedger();

    return (
        <div className="space-y-6">
            <PageHeader
                title="Libro diario"
                description="Asientos contables registrados, con su debe y haber. Se generan al emitir documentos, registrar pagos o cerrar el mes."
            />
            <JournalEntryTable
                entries={data ?? []}
                isLoading={isLoading}
                error={error instanceof Error ? error : null}
                onRetry={() => refetch()}
                isRetrying={isFetching && !isLoading}
            />
        </div>
    );
}
