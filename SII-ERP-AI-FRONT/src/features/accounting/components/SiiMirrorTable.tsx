'use client';

import { CheckCircle2, AlertTriangle } from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { formatCLP, formatRut } from '@/lib/formatters';
import type { SiiAuditReport } from '../services/accounting.service';
import { EmptyState, ErrorState, TableSkeleton } from './DataStates';

interface SiiMirrorTableProps {
    report?: SiiAuditReport;
    isLoading: boolean;
    error?: Error | null;
    onRetry?: () => void;
    isRetrying?: boolean;
}

const typeLabel: Record<string, string> = {
    MISSING_IN_ERP: 'Falta en el ERP',
    MISSING_IN_SII: 'Falta en el SII',
    AMOUNT_MISMATCH: 'Monto distinto',
};

export function SiiMirrorTable({
    report,
    isLoading,
    error,
    onRetry,
    isRetrying = false,
}: SiiMirrorTableProps) {
    if (error) {
        return <ErrorState message={error.message} onRetry={onRetry} retrying={isRetrying} />;
    }

    if (isLoading) {
        return <TableSkeleton rows={5} cols={5} />;
    }

    if (!report) return null;

    return (
        <div className="space-y-6">
            <div className="grid gap-4 md:grid-cols-2">
                {(
                    [
                        { title: 'SII (RCV)', summary: report.siiTotal },
                        { title: 'ERP (contabilidad interna)', summary: report.erpTotal },
                    ] as const
                ).map(({ title, summary }) => (
                    <Card key={title}>
                        <CardHeader className="pb-2">
                            <CardTitle className="text-sm font-medium text-muted-foreground">
                                {title}
                            </CardTitle>
                        </CardHeader>
                        <CardContent>
                            <div className="grid grid-cols-3 gap-4 text-sm">
                                <div>
                                    <p className="text-xs text-muted-foreground">Neto</p>
                                    <p className="font-mono font-semibold tabular-nums">
                                        {formatCLP(summary.netAmount)}
                                    </p>
                                </div>
                                <div>
                                    <p className="text-xs text-muted-foreground">IVA</p>
                                    <p className="font-mono font-semibold tabular-nums">
                                        {formatCLP(summary.iva)}
                                    </p>
                                </div>
                                <div>
                                    <p className="text-xs text-muted-foreground">Documentos</p>
                                    <p className="font-mono font-semibold tabular-nums">
                                        {summary.documentCount}
                                    </p>
                                </div>
                            </div>
                        </CardContent>
                    </Card>
                ))}
            </div>

            {report.isMatch ? (
                <div
                    role="status"
                    className="flex items-center gap-2 rounded-lg border border-success-subtle-foreground/25 bg-success-subtle p-4 text-sm text-success-subtle-foreground"
                >
                    <CheckCircle2 aria-hidden="true" className="h-5 w-5" />
                    El espejo SII y la contabilidad interna coinciden en este período.
                </div>
            ) : (
                <div
                    role="alert"
                    className="flex items-center gap-2 rounded-lg border border-warning-subtle-foreground/30 bg-warning-subtle p-4 text-sm text-warning-subtle-foreground"
                >
                    <AlertTriangle aria-hidden="true" className="h-5 w-5" />
                    Hay {report.discrepancies.length} diferencia
                    {report.discrepancies.length === 1 ? '' : 's'} entre el RCV del SII y el ERP.
                </div>
            )}

            <Card>
                <CardHeader className="pb-2">
                    <CardTitle className="text-base font-semibold">Diferencias detectadas</CardTitle>
                </CardHeader>
                <CardContent className="p-0">
                    {report.discrepancies.length === 0 ? (
                        <EmptyState
                            title="Sin diferencias en el período"
                            description="Todos los documentos del RCV están registrados con el mismo monto en el ERP."
                        />
                    ) : (
                        <div className="overflow-x-auto">
                            <table className="w-full text-sm">
                                <caption className="sr-only">Diferencias entre el SII y el ERP</caption>
                                <thead>
                                    <tr className="border-y bg-muted/50 text-xs text-muted-foreground">
                                        <th scope="col" className="px-4 py-2 text-left font-medium">
                                            Tipo
                                        </th>
                                        <th scope="col" className="px-4 py-2 text-left font-medium">
                                            Folio
                                        </th>
                                        <th scope="col" className="px-4 py-2 text-left font-medium">
                                            Contraparte
                                        </th>
                                        <th scope="col" className="px-4 py-2 text-right font-medium">
                                            SII
                                        </th>
                                        <th scope="col" className="px-4 py-2 text-right font-medium">
                                            ERP
                                        </th>
                                        <th scope="col" className="px-4 py-2 text-left font-medium">
                                            Severidad
                                        </th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {report.discrepancies.map((item, index) => (
                                        <tr
                                            key={`${item.type}-${item.folio ?? index}`}
                                            className="border-b last:border-b-0"
                                        >
                                            <td className="px-4 py-3">
                                                {typeLabel[item.type] ?? item.type}
                                            </td>
                                            <td className="px-4 py-3 font-mono text-xs">
                                                {item.folio != null ? `#${item.folio}` : '—'}
                                            </td>
                                            <td className="px-4 py-3 font-mono text-xs">
                                                {item.rutCounterpart
                                                    ? formatRut(item.rutCounterpart)
                                                    : '—'}
                                            </td>
                                            <td className="px-4 py-3 text-right font-mono tabular-nums">
                                                {formatCLP(item.amountSii)}
                                            </td>
                                            <td className="px-4 py-3 text-right font-mono tabular-nums">
                                                {formatCLP(item.amountErp)}
                                            </td>
                                            <td className="px-4 py-3">
                                                <Badge
                                                    variant="outline"
                                                    className={
                                                        item.severity === 'CRITICAL'
                                                            ? 'border-destructive-subtle-foreground/25 bg-destructive-subtle text-destructive-subtle-foreground'
                                                            : 'border-warning-subtle-foreground/30 bg-warning-subtle text-warning-subtle-foreground'
                                                    }
                                                >
                                                    {item.severity === 'CRITICAL'
                                                        ? 'Crítica'
                                                        : 'Advertencia'}
                                                </Badge>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </CardContent>
            </Card>
        </div>
    );
}

export default SiiMirrorTable;
