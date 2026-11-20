'use client';

import { useQuery } from '@tanstack/react-query';
import { Info } from 'lucide-react';
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

            <div className="flex items-start gap-2 rounded-lg border border-info-subtle-foreground/20 bg-info-subtle p-3 text-sm text-info-subtle-foreground">
                <Info aria-hidden="true" className="mt-0.5 h-4 w-4 shrink-0" />
                <p>
                    El backend expone la importación de boletas
                    (<span className="font-mono text-xs">POST /api/v1/fees/import</span>) pero
                    todavía no un listado (<span className="font-mono text-xs">GET /api/v1/fees</span>).
                    Mientras tanto, la lista aparece como error de carga.
                </p>
            </div>

            <Card>
                <CardHeader>
                    <CardTitle className="text-base font-semibold">Listado de boletas</CardTitle>
                </CardHeader>
                <CardContent className="p-0">
                    {error ? (
                        <div className="p-6">
                            <ErrorState
                                title="No se pudo cargar el listado de boletas"
                                message={`${error instanceof Error ? error.message : 'Error desconocido'}. El endpoint GET /api/v1/fees no existe en el backend actual.`}
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
