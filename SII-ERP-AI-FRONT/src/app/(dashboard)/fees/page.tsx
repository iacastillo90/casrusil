'use client';

import { useQuery } from '@tanstack/react-query';
import { useAuthStore } from '@/features/auth/stores/auth.store';
import { feeService } from '@/features/fees/services/fee.service';
import { FeeTable } from '@/features/fees/components/FeeTable';
import { FeeImportModal } from '@/features/fees/components/FeeImportModal';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { PageHeader } from '@/features/accounting/components/PageHeader';
import { ErrorState } from '@/features/accounting/components/DataStates';

export default function FeesPage() {
    const companyId = useAuthStore((state) => state.companyId);

    const { data: fees = [], isLoading, error, refetch, isFetching } = useQuery({
        queryKey: ['fees', companyId],
        queryFn: () => feeService.getFees({}),
        enabled: !!companyId,
    });

    return (
        <div className="space-y-6">
            <PageHeader
                title="Boletas de honorarios"
                description="Boletas de honorarios electrónicas recibidas, con retención y monto líquido."
                actions={<FeeImportModal />}
            />

            <Card>
                <CardHeader>
                    <CardTitle className="text-base font-semibold">Listado de boletas</CardTitle>
                </CardHeader>
                <CardContent className="p-0">
                    {error ? (
                        <div className="p-6">
                            <ErrorState
                                title="No se pudo cargar el listado de boletas"
                                message={error instanceof Error ? error.message : 'Error desconocido'}
                                onRetry={() => refetch()}
                                retrying={isFetching}
                            />
                        </div>
                    ) : (
                        <FeeTable fees={fees} isLoading={isLoading} />
                    )}
                </CardContent>
            </Card>
        </div>
    );
}
