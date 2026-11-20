'use client';

import Link from 'next/link';
import { AlertTriangle, Calculator, FileWarning, ShieldCheck } from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { PageHeader } from '@/features/accounting/components/PageHeader';
import { ErrorState, TableSkeleton } from '@/features/accounting/components/DataStates';
import { useComplianceDashboard } from '@/features/compliance/hooks/useCompliance';
import { formatCLP } from '@/lib/formatters';

const severityLabel: Record<string, string> = {
    CRITICAL: 'Crítica',
    WARNING: 'Advertencia',
    INFO: 'Informativa',
};

const severityClass = (severity: string) => {
    if (severity === 'CRITICAL') {
        return 'border-destructive-subtle-foreground/25 bg-destructive-subtle text-destructive-subtle-foreground';
    }
    if (severity === 'WARNING') {
        return 'border-warning-subtle-foreground/30 bg-warning-subtle text-warning-subtle-foreground';
    }
    return 'border-info-subtle-foreground/20 bg-info-subtle text-info-subtle-foreground';
};

export default function ComplianceCenterPage() {
    const { data: dashboard, isLoading, isError, error, refetch, isFetching } =
        useComplianceDashboard();

    return (
        <div className="space-y-6">
            <PageHeader
                title="Salud tributaria"
                description="Lo que necesita tu atención para estar al día con el SII: cruce de DTEs, acuses de recibo y simulación del F29."
                actions={
                    <Link
                        href="/accounting/f29"
                        className="text-sm font-medium text-primary underline-offset-4 hover:underline"
                    >
                        Revisar F29
                    </Link>
                }
            />

            {isLoading ? (
                <TableSkeleton rows={4} cols={3} />
            ) : isError || !dashboard ? (
                <ErrorState
                    title="No se pudo cargar la salud tributaria"
                    message={
                        error instanceof Error
                            ? `${error.message} El dashboard depende de la conexión autenticada al SII y del perfil tributario de la empresa; sin ellos el backend responde 500.`
                            : 'El servicio de compliance no respondió. Requiere rol ADMIN o ACCOUNTANT.'
                    }
                    onRetry={() => refetch()}
                    retrying={isFetching}
                />
            ) : (
                <>
                    <div className="grid gap-4 md:grid-cols-3">
                        {(() => {
                            const healthScore = dashboard.globalHealthScore ?? 0;
                            const healthy = healthScore >= 80;
                            return (
                                <Card>
                                    <CardHeader className="flex flex-row items-center justify-between pb-2">
                                        <CardTitle className="text-sm font-medium text-muted-foreground">
                                            Salud tributaria general
                                        </CardTitle>
                                        <ShieldCheck
                                            aria-hidden="true"
                                            className={`h-4 w-4 ${
                                                healthy ? 'text-success' : 'text-warning'
                                            }`}
                                        />
                                    </CardHeader>
                                    <CardContent>
                                        <p
                                            className={`text-2xl font-bold font-mono tabular-nums ${
                                                healthy ? 'text-success' : 'text-warning'
                                            }`}
                                        >
                                            {healthScore}%
                                        </p>
                                        <p className="mt-1 text-xs text-muted-foreground">
                                            Basado en discrepancias con el SII y alertas abiertas.
                                        </p>
                                    </CardContent>
                                </Card>
                            );
                        })()}

                        <Card>
                            <CardHeader className="flex flex-row items-center justify-between pb-2">
                                <CardTitle className="text-sm font-medium text-muted-foreground">
                                    IVA determinado (F29 del período)
                                </CardTitle>
                                <Calculator aria-hidden="true" className="h-4 w-4 text-muted-foreground" />
                            </CardHeader>
                            <CardContent>
                                <p className="text-2xl font-bold font-mono tabular-nums">
                                    {formatCLP(dashboard.f29Preview?.vatPayable ?? 0)}
                                </p>
                                <p className="mt-1 text-xs text-muted-foreground">
                                    {dashboard.f29Preview?.period
                                        ? `Período ${dashboard.f29Preview.period}`
                                        : 'Sin período informado por el backend'}
                                </p>
                            </CardContent>
                        </Card>

                        <Card>
                            <CardHeader className="flex flex-row items-center justify-between pb-2">
                                <CardTitle className="text-sm font-medium text-muted-foreground">
                                    Parámetros tributarios
                                </CardTitle>
                                <Calculator aria-hidden="true" className="h-4 w-4 text-muted-foreground" />
                            </CardHeader>
                            <CardContent>
                                <p className="text-sm font-medium">
                                    Retención BHE:{' '}
                                    {dashboard.taxParameters
                                        ? `${dashboard.taxParameters.bheRetentionRate}%`
                                        : '—'}
                                </p>
                                <p className="mt-1 text-xs text-muted-foreground">
                                    {dashboard.taxParameters
                                        ? `UTM ${formatCLP(dashboard.taxParameters.utmValue)} · UF ${formatCLP(dashboard.taxParameters.ufValue)}`
                                        : 'Sin parámetros cargados'}
                                </p>
                            </CardContent>
                        </Card>
                    </div>

                    <Card>
                        <CardHeader className="pb-2">
                            <CardTitle className="flex items-center gap-2 text-base font-semibold">
                                <AlertTriangle aria-hidden="true" className="h-5 w-5 text-warning" />
                                Acuse de recibo (Ley de Pago a 30 días)
                            </CardTitle>
                        </CardHeader>
                        <CardContent className="p-0">
                            {(dashboard.acuseReciboAlerts ?? []).length === 0 ? (
                                <p className="px-6 pb-6 text-sm text-muted-foreground">
                                    No hay facturas por vencer sin acuse de recibo.
                                </p>
                            ) : (
                                <ul className="divide-y">
                                    {(dashboard.acuseReciboAlerts ?? []).map((alert) => (
                                        <li
                                            key={alert.code}
                                            className="flex items-start justify-between gap-4 px-6 py-4"
                                        >
                                            <div className="flex items-start gap-3">
                                                <FileWarning
                                                    aria-hidden="true"
                                                    className="mt-0.5 h-5 w-5 shrink-0 text-warning"
                                                />
                                                <div>
                                                    <p className="text-sm font-medium">
                                                        {alert.message}
                                                    </p>
                                                    <p className="mt-1 font-mono text-xs text-muted-foreground">
                                                        Código: {alert.code}
                                                    </p>
                                                </div>
                                            </div>
                                            <Badge
                                                variant="outline"
                                                className={`whitespace-nowrap ${severityClass(alert.severity)}`}
                                            >
                                                {severityLabel[alert.severity] ?? alert.severity}
                                            </Badge>
                                        </li>
                                    ))}
                                </ul>
                            )}
                        </CardContent>
                    </Card>

                    <Card>
                        <CardHeader className="pb-2">
                            <CardTitle className="text-base font-semibold">
                                Cruce con el SII
                            </CardTitle>
                        </CardHeader>
                        <CardContent className="grid gap-4 text-sm md:grid-cols-3">
                            <div>
                                <p className="text-xs text-muted-foreground">Documentos en el ERP</p>
                                <p className="font-mono font-semibold tabular-nums">
                                    {dashboard.siiStatus?.totalErp ?? 0}
                                </p>
                            </div>
                            <div>
                                <p className="text-xs text-muted-foreground">Documentos en el SII</p>
                                <p className="font-mono font-semibold tabular-nums">
                                    {dashboard.siiStatus?.totalSii ?? 0}
                                </p>
                            </div>
                            <div>
                                <p className="text-xs text-muted-foreground">Faltantes en el ERP</p>
                                <p
                                    className={`font-mono font-semibold tabular-nums ${
                                        (dashboard.siiStatus?.missingInErp ?? 0) > 0
                                            ? 'text-destructive'
                                            : ''
                                    }`}
                                >
                                    {dashboard.siiStatus?.missingInErp ?? 0}
                                </p>
                            </div>
                        </CardContent>
                    </Card>
                </>
            )}
        </div>
    );
}
