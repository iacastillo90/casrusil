'use client';

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { formatCLP } from '@/lib/formatters';
import type { F29Report } from '../services/accounting.service';
import { EmptyState } from './DataStates';

interface F29PreviewProps {
    report: F29Report;
}

const periodLabel = (period: string) => {
    // `yyyy-MM` → "noviembre de 2026"
    const match = /^(\d{4})-(\d{2})$/.exec(period);
    if (!match) return period;
    const date = new Date(Number(match[1]), Number(match[2]) - 1, 1);
    return new Intl.DateTimeFormat('es-CL', { month: 'long', year: 'numeric' }).format(date);
};

/**
 * Vista del F29 calculado por el backend. Los montos salen del reporte real
 * (`details` viene con los códigos del SII); no se recalculan acá.
 */
export function F29Preview({ report }: F29PreviewProps) {
    return (
        <div className="grid gap-6 lg:grid-cols-[2fr_1fr]">
            <Card>
                <CardHeader>
                    <CardTitle className="text-base font-semibold">
                        Formulario 29, {periodLabel(report.period)}
                    </CardTitle>
                </CardHeader>
                <CardContent className="p-0">
                    {report.details.length === 0 ? (
                        <EmptyState
                            title="El F29 no tiene líneas calculadas"
                            description="No hay movimientos de ventas ni compras registrados en este período."
                        />
                    ) : (
                        <div className="overflow-x-auto">
                            <table className="w-full text-sm">
                                <caption className="sr-only">
                                    Detalle del Formulario 29 por código SII
                                </caption>
                                <thead>
                                    <tr className="border-y bg-muted/50 text-xs text-muted-foreground">
                                        <th scope="col" className="px-4 py-2 text-left font-medium">
                                            Código
                                        </th>
                                        <th scope="col" className="px-4 py-2 text-left font-medium">
                                            Concepto
                                        </th>
                                        <th scope="col" className="px-4 py-2 text-right font-medium">
                                            Monto
                                        </th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {report.details.map((line) => (
                                        <tr key={`${line.code}-${line.description}`} className="border-b last:border-b-0">
                                            <td className="px-4 py-2 font-mono text-xs text-muted-foreground">
                                                {line.code}
                                            </td>
                                            <td className="px-4 py-2">{line.description}</td>
                                            <td className="px-4 py-2 text-right font-mono tabular-nums">
                                                {formatCLP(line.amount)}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </CardContent>
            </Card>

            <div className="space-y-4">
                <Card>
                    <CardHeader className="pb-2">
                        <CardTitle className="text-base font-semibold">Total a pagar</CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-1">
                        <p className="text-3xl font-bold font-mono tabular-nums">
                            {formatCLP(report.totalPayable)}
                        </p>
                        <p className="text-xs text-muted-foreground">
                            Vence el día 20 del mes siguiente al período.
                        </p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader className="pb-2">
                        <CardTitle className="text-base font-semibold">Determinación</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <dl className="space-y-2 text-sm">
                            <div className="flex items-center justify-between gap-4">
                                <dt className="text-muted-foreground">IVA débito</dt>
                                <dd className="font-mono tabular-nums">{formatCLP(report.vatDebit)}</dd>
                            </div>
                            <div className="flex items-center justify-between gap-4">
                                <dt className="text-muted-foreground">IVA crédito</dt>
                                <dd className="font-mono tabular-nums">{formatCLP(report.vatCredit)}</dd>
                            </div>
                            <div className="flex items-center justify-between gap-4 border-t pt-2">
                                <dt className="text-muted-foreground">IVA a pagar</dt>
                                <dd className="font-mono font-semibold tabular-nums">
                                    {formatCLP(report.vatPayable)}
                                </dd>
                            </div>
                            {report.vatRecoverable > 0 ? (
                                <div className="flex items-center justify-between gap-4">
                                    <dt className="text-muted-foreground">IVA recuperable</dt>
                                    <dd className="font-mono tabular-nums text-success">
                                        {formatCLP(report.vatRecoverable)}
                                    </dd>
                                </div>
                            ) : null}
                            <div className="flex items-center justify-between gap-4">
                                <dt className="text-muted-foreground">Retención honorarios</dt>
                                <dd className="font-mono tabular-nums">
                                    {formatCLP(report.feeWithholding)}
                                </dd>
                            </div>
                            <div className="flex items-center justify-between gap-4 border-t pt-2 font-semibold">
                                <dt>Total a pagar</dt>
                                <dd className="font-mono tabular-nums">
                                    {formatCLP(report.totalPayable)}
                                </dd>
                            </div>
                        </dl>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}

export default F29Preview;
