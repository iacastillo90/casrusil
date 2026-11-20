'use client';

import { AlertTriangle, CheckCircle2, FileWarning } from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { formatCLP } from '@/lib/formatters';
import type { F29Alert } from '../services/accounting.service';

interface F29AlertsPanelProps {
    alerts: F29Alert[];
}

const monthName = (year: number, month: number) =>
    new Intl.DateTimeFormat('es-CL', { month: 'long', year: 'numeric' }).format(
        new Date(year, month - 1, 1),
    );

/**
 * Alertas de aceptaciones tardías: un DTE con IVA cayó en un período ya
 * declarado, por lo que el F29 de ese período requiere rectificatoria.
 */
export function F29AlertsPanel({ alerts }: F29AlertsPanelProps) {
    const pending = alerts.filter((alert) => !alert.resolved);

    return (
        <Card>
            <CardHeader className="flex flex-row items-start justify-between gap-4">
                <div className="space-y-1">
                    <CardTitle className="flex items-center gap-2 text-base font-semibold">
                        <FileWarning aria-hidden="true" className="h-5 w-5 text-warning" />
                        Aceptaciones tardías en períodos declarados
                    </CardTitle>
                    <p className="text-sm text-muted-foreground">
                        Documentos con IVA que llegaron después del cierre del período. Cada uno
                        requiere una rectificatoria del F29.
                    </p>
                </div>
                {pending.length > 0 ? (
                    <Badge
                        variant="outline"
                        className="whitespace-nowrap border-warning-subtle-foreground/30 bg-warning-subtle text-warning-subtle-foreground"
                    >
                        {pending.length} sin resolver
                    </Badge>
                ) : (
                    <Badge
                        variant="outline"
                        className="whitespace-nowrap border-success-subtle-foreground/25 bg-success-subtle text-success-subtle-foreground"
                    >
                        Sin pendientes
                    </Badge>
                )}
            </CardHeader>
            <CardContent className="p-0">
                {alerts.length === 0 ? (
                    <div className="flex flex-col items-center gap-2 px-6 pb-8 pt-2 text-center">
                        <CheckCircle2 aria-hidden="true" className="h-8 w-8 text-success" />
                        <p className="text-sm font-medium">
                            No hay aceptaciones tardías registradas
                        </p>
                        <p className="max-w-xl text-sm text-muted-foreground">
                            Cuando un documento aceptado por el SII corresponda a un período ya
                            declarado, aparecerá acá con el folio y la diferencia de IVA.
                        </p>
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full text-sm">
                            <caption className="sr-only">
                                Alertas de F29 desactualizado
                            </caption>
                            <thead>
                                <tr className="border-y bg-muted/50 text-xs text-muted-foreground">
                                    <th scope="col" className="px-4 py-2 text-left font-medium">
                                        Período declarado
                                    </th>
                                    <th scope="col" className="px-4 py-2 text-left font-medium">
                                        Folio
                                    </th>
                                    <th scope="col" className="px-4 py-2 text-left font-medium">
                                        Motivo
                                    </th>
                                    <th scope="col" className="px-4 py-2 text-right font-medium">
                                        Diferencia IVA
                                    </th>
                                    <th scope="col" className="px-4 py-2 text-left font-medium">
                                        Acción
                                    </th>
                                </tr>
                            </thead>
                            <tbody>
                                {alerts.map((alert) => (
                                    <tr key={alert.id} className="border-b last:border-b-0">
                                        <td className="px-4 py-3 font-medium">
                                            {monthName(alert.periodYear, alert.periodMonth)}
                                        </td>
                                        <td className="px-4 py-3 font-mono text-xs">
                                            {alert.invoiceFolio != null
                                                ? `#${alert.invoiceFolio}`
                                                : '—'}
                                        </td>
                                        <td className="px-4 py-3 text-muted-foreground">
                                            {alert.reason ?? 'Documento aceptado después del cierre'}
                                        </td>
                                        <td className="px-4 py-3 text-right font-mono tabular-nums">
                                            {formatCLP(alert.vatDifference)}
                                        </td>
                                        <td className="px-4 py-3">
                                            {alert.resolved ? (
                                                <Badge
                                                    variant="outline"
                                                    className="border-success-subtle-foreground/25 bg-success-subtle text-success-subtle-foreground"
                                                >
                                                    <CheckCircle2
                                                        aria-hidden="true"
                                                        className="mr-1 h-3.5 w-3.5"
                                                    />
                                                    Resuelta
                                                </Badge>
                                            ) : (
                                                <Badge
                                                    variant="outline"
                                                    className="border-warning-subtle-foreground/30 bg-warning-subtle text-warning-subtle-foreground"
                                                >
                                                    <AlertTriangle
                                                        aria-hidden="true"
                                                        className="mr-1 h-3.5 w-3.5"
                                                    />
                                                    Requiere rectificatoria
                                                </Badge>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </CardContent>
        </Card>
    );
}

export default F29AlertsPanel;
