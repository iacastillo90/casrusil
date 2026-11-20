'use client';

import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { startOfMonth, endOfMonth } from 'date-fns';
import { accountingService } from '@/features/accounting/services/accounting.service';
import { PageHeader } from '@/features/accounting/components/PageHeader';
import { ErrorState, TableSkeleton } from '@/features/accounting/components/DataStates';
import { SmartPeriodSelector, PeriodRange } from '@/components/shared/SmartPeriodSelector';
import { Card, CardContent } from '@/components/ui/card';
import { formatCLP } from '@/lib/formatters';

const margin = (value: number) =>
    `${new Intl.NumberFormat('es-CL', { maximumFractionDigits: 1 }).format(value)}%`;

export default function IncomeStatementPage() {
    const [period, setPeriod] = useState<PeriodRange>({
        from: startOfMonth(new Date()),
        to: endOfMonth(new Date()),
        mode: 'MONTH',
    });

    const { data, isLoading, error, refetch, isFetching } = useQuery({
        queryKey: ['income-statement', period.from, period.to],
        queryFn: () => accountingService.getIncomeStatement(period.from, period.to),
    });

    return (
        <div className="space-y-6">
            <PageHeader
                title="Estado de resultados"
                description="Rendimiento económico del período seleccionado, con la cascada de ingresos, costos y gastos."
                actions={<SmartPeriodSelector value={period} onChange={setPeriod} />}
            />

            {error ? (
                <ErrorState
                    message={
                        error instanceof Error
                            ? `${error.message} El endpoint genera el resumen ejecutivo con IA; si el proveedor no tiene credenciales válidas, el backend responde 500.`
                            : 'Error desconocido'
                    }
                    onRetry={() => refetch()}
                    retrying={isFetching}
                />
            ) : isLoading ? (
                <TableSkeleton rows={6} cols={3} />
            ) : data ? (
                <div className="space-y-6">
                    <div className="grid gap-4 md:grid-cols-2">
                        <Card className="border-l-4 border-l-primary">
                            <CardContent className="p-6">
                                <p className="text-sm font-medium text-muted-foreground">
                                    Resultado del ejercicio
                                </p>
                                <p
                                    className={`mt-1 text-3xl font-bold font-mono tabular-nums ${
                                        data.netIncome >= 0 ? 'text-success' : 'text-destructive'
                                    }`}
                                >
                                    {formatCLP(data.netIncome)}
                                </p>
                            </CardContent>
                        </Card>
                        <Card>
                            <CardContent className="p-6">
                                <p className="text-sm font-medium text-muted-foreground">
                                    Margen neto
                                </p>
                                <p className="mt-1 text-3xl font-bold font-mono tabular-nums">
                                    {margin(data.netMargin)}
                                </p>
                            </CardContent>
                        </Card>
                    </div>

                    <Card>
                        <CardContent className="p-0">
                            <table className="w-full text-sm">
                                <caption className="sr-only">
                                    Estado de resultados del período
                                </caption>
                                <tbody>
                                    <tr className="bg-muted/50">
                                        <td className="p-4 font-semibold">Ingresos operacionales</td>
                                        <td className="p-4 text-right font-mono font-semibold tabular-nums">
                                            {formatCLP(data.totalRevenue)}
                                        </td>
                                    </tr>
                                    <tr className="border-t">
                                        <td className="p-4 pl-8 text-muted-foreground">
                                            (−) Costo de ventas
                                        </td>
                                        <td className="p-4 text-right font-mono tabular-nums text-destructive">
                                            {formatCLP(data.cogs)}
                                        </td>
                                    </tr>
                                    <tr className="border-t bg-muted/50 font-medium">
                                        <td className="p-4">Utilidad bruta</td>
                                        <td className="p-4 text-right font-mono tabular-nums">
                                            {formatCLP(data.grossProfit)}
                                        </td>
                                    </tr>
                                    <tr className="border-t">
                                        <td className="p-4 pl-8 text-muted-foreground">
                                            (−) Gastos de administración
                                        </td>
                                        <td className="p-4 text-right font-mono tabular-nums text-destructive">
                                            {formatCLP(data.adminExpenses)}
                                        </td>
                                    </tr>
                                    <tr className="border-t-2 bg-muted font-semibold">
                                        <td className="p-4 text-base">Utilidad neta</td>
                                        <td
                                            className={`p-4 text-right font-mono text-base tabular-nums ${
                                                data.netIncome >= 0
                                                    ? 'text-success'
                                                    : 'text-destructive'
                                            }`}
                                        >
                                            {formatCLP(data.netIncome)}
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </CardContent>
                    </Card>
                </div>
            ) : null}
        </div>
    );
}
