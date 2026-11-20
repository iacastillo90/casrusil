'use client';

import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { startOfMonth, endOfMonth } from 'date-fns';
import { AlertTriangle } from 'lucide-react';
import { accountingService } from '@/features/accounting/services/accounting.service';
import { PageHeader } from '@/features/accounting/components/PageHeader';
import { ErrorState, TableSkeleton } from '@/features/accounting/components/DataStates';
import { BalanceSheetTree } from '@/features/accounting/components/BalanceSheetTree';
import { SmartPeriodSelector, PeriodRange } from '@/components/shared/SmartPeriodSelector';
import { Card, CardContent } from '@/components/ui/card';
import { formatCLP } from '@/lib/formatters';

export default function BalanceSheetPage() {
    const [period, setPeriod] = useState<PeriodRange>({
        from: startOfMonth(new Date()),
        to: endOfMonth(new Date()),
        mode: 'MONTH',
    });

    const { data, isLoading, error, refetch, isFetching } = useQuery({
        queryKey: ['balance-sheet', period.to.toISOString().split('T')[0]],
        queryFn: () => accountingService.getBalanceSheet(period.to),
    });

    const difference = data
        ? data.totalAssets - (data.totalLiabilities + data.totalEquity)
        : 0;

    return (
        <div className="space-y-6">
            <PageHeader
                title="Balance general"
                description={
                    <>
                        Situación financiera al cierre del{' '}
                        <strong>{period.to.toLocaleDateString('es-CL')}</strong>.
                    </>
                }
                actions={<SmartPeriodSelector value={period} onChange={setPeriod} />}
            />

            {error ? (
                <ErrorState
                    message={error instanceof Error ? error.message : 'Error desconocido'}
                    onRetry={() => refetch()}
                    retrying={isFetching}
                />
            ) : isLoading ? (
                <TableSkeleton rows={5} cols={3} />
            ) : data ? (
                <div className="space-y-6">
                    <div className="grid gap-4 md:grid-cols-3">
                        <Card>
                            <CardContent className="p-6">
                                <p className="text-sm font-medium text-muted-foreground">
                                    Total activos
                                </p>
                                <p className="mt-1 text-2xl font-bold font-mono tabular-nums text-info">
                                    {formatCLP(data.totalAssets)}
                                </p>
                            </CardContent>
                        </Card>
                        <Card>
                            <CardContent className="p-6">
                                <p className="text-sm font-medium text-muted-foreground">
                                    Total pasivos
                                </p>
                                <p className="mt-1 text-2xl font-bold font-mono tabular-nums text-warning">
                                    {formatCLP(data.totalLiabilities)}
                                </p>
                            </CardContent>
                        </Card>
                        <Card>
                            <CardContent className="p-6">
                                <p className="text-sm font-medium text-muted-foreground">
                                    Patrimonio
                                </p>
                                <p className="mt-1 text-2xl font-bold font-mono tabular-nums text-success">
                                    {formatCLP(data.totalEquity)}
                                </p>
                            </CardContent>
                        </Card>
                    </div>

                    {!data.isBalanced || Math.abs(difference) >= 0.01 ? (
                        <div
                            role="alert"
                            className="flex items-start gap-3 rounded-lg border border-warning-subtle-foreground/30 bg-warning-subtle p-4 text-warning-subtle-foreground"
                        >
                            <AlertTriangle aria-hidden="true" className="mt-0.5 h-5 w-5 shrink-0" />
                            <div className="text-sm">
                                <p className="font-medium">El balance no cuadra</p>
                                <p>
                                    Diferencia de {formatCLP(difference)} entre activos y pasivos
                                    más patrimonio. Revisa los asientos manuales del período.
                                </p>
                            </div>
                        </div>
                    ) : null}

                    <BalanceSheetTree data={data} />
                </div>
            ) : null}
        </div>
    );
}
