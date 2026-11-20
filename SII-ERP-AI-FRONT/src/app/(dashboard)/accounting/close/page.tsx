'use client';

import { useState } from 'react';
import { AlertTriangle, CheckCircle2, Lock, ShieldCheck, Sparkles } from 'lucide-react';
import { toast } from 'sonner';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { PageHeader } from '@/features/accounting/components/PageHeader';
import { ErrorState, TableSkeleton } from '@/features/accounting/components/DataStates';
import { usePeriodStatus } from '@/features/accounting/hooks/useAccounting';
import { usePeriodClose } from '@/features/period_close/hooks/usePeriodClose';

const currentPeriod = () => {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
};

const periodLabel = (period: string) => {
    const [year, month] = period.split('-').map(Number);
    if (!year || !month) return period;
    return new Intl.DateTimeFormat('es-CL', { month: 'long', year: 'numeric' }).format(
        new Date(year, month - 1, 1),
    );
};

export default function PeriodClosePage() {
    const [period, setPeriod] = useState(currentPeriod);
    const [year, month] = period.split('-').map(Number);
    const [justClosed, setJustClosed] = useState(false);

    const status = usePeriodStatus(period);
    const { auditPeriod, executeClose } = usePeriodClose();

    const isClosed = status.data?.isClosed || justClosed;
    const audit = auditPeriod.data;

    const runAudit = () => {
        auditPeriod.mutate(
            { year, month },
            {
                onError: (error) =>
                    toast.error('No se pudo auditar el período', {
                        description: error instanceof Error ? error.message : undefined,
                    }),
            },
        );
    };

    const closePeriod = () => {
        const confirmed = window.confirm(
            `¿Cerrar ${periodLabel(period)}? El período quedará bloqueado y no podrás registrar asientos nuevos en él.`,
        );
        if (!confirmed) return;

        executeClose.mutate(
            { year, month },
            {
                onSuccess: () => {
                    setJustClosed(true);
                    status.refetch();
                    toast.success(`${periodLabel(period)} cerrado`);
                },
                onError: (error) =>
                    toast.error('No se pudo cerrar el período', {
                        description: error instanceof Error ? error.message : undefined,
                    }),
            },
        );
    };

    return (
        <div className="space-y-6">
            <PageHeader
                title="Cierre de período"
                description="Auditoría previa y bloqueo del período contable. El cierre lo ejecuta el backend; la IA solo propone observaciones."
                actions={
                    <div className="flex items-center gap-2">
                        <label htmlFor="close-period" className="text-sm text-muted-foreground">
                            Período
                        </label>
                        <Input
                            id="close-period"
                            type="month"
                            className="w-44"
                            value={period}
                            onChange={(event) => {
                                setPeriod(event.target.value);
                                setJustClosed(false);
                                auditPeriod.reset();
                            }}
                        />
                    </div>
                }
            />

            <div className="flex flex-wrap items-center gap-3">
                {status.isLoading ? (
                    <Badge variant="outline">Consultando estado…</Badge>
                ) : isClosed ? (
                    <Badge
                        variant="outline"
                        className="border-success-subtle-foreground/25 bg-success-subtle text-success-subtle-foreground"
                    >
                        <Lock aria-hidden="true" className="mr-1 h-3.5 w-3.5" />
                        Período cerrado
                    </Badge>
                ) : (
                    <Badge variant="secondary">Período abierto</Badge>
                )}
                <span className="text-sm text-muted-foreground">{periodLabel(period)}</span>
            </div>

            {status.error ? (
                <ErrorState
                    title="No se pudo consultar el estado del período"
                    message={
                        status.error instanceof Error
                            ? status.error.message
                            : 'Error desconocido'
                    }
                    onRetry={() => status.refetch()}
                    retrying={status.isFetching}
                />
            ) : null}

            <Card>
                <CardHeader className="flex flex-row items-center justify-between gap-4">
                    <CardTitle className="flex items-center gap-2 text-base font-semibold">
                        <ShieldCheck aria-hidden="true" className="h-5 w-5 text-muted-foreground" />
                        Auditoría previa al cierre
                    </CardTitle>
                    <Button
                        onClick={runAudit}
                        loading={auditPeriod.isPending}
                        disabled={isClosed}
                    >
                        {!auditPeriod.isPending && (
                            <Sparkles aria-hidden="true" className="mr-2 h-4 w-4" />
                        )}
                        {audit ? 'Re-auditar' : 'Auditar período'}
                    </Button>
                </CardHeader>
                <CardContent>
                    {auditPeriod.isPending ? (
                        <TableSkeleton rows={3} cols={2} />
                    ) : auditPeriod.error ? (
                        <ErrorState
                            title="La auditoría del período falló"
                            message={
                                auditPeriod.error instanceof Error
                                    ? auditPeriod.error.message
                                    : 'Error desconocido'
                            }
                            onRetry={runAudit}
                        />
                    ) : !audit ? (
                        <p className="text-sm text-muted-foreground">
                            Ejecuta la auditoría para revisar documentos, conciliación, depreciación y
                            cuentas de resultado antes de cerrar.
                        </p>
                    ) : (
                        <div className="space-y-4">
                            <div className="flex flex-wrap items-center gap-3">
                                {audit.readyToClose ? (
                                    <Badge
                                        variant="outline"
                                        className="border-success-subtle-foreground/25 bg-success-subtle text-success-subtle-foreground"
                                    >
                                        <CheckCircle2 aria-hidden="true" className="mr-1 h-3.5 w-3.5" />
                                        Sin bloqueos
                                    </Badge>
                                ) : (
                                    <Badge
                                        variant="outline"
                                        className="border-warning-subtle-foreground/30 bg-warning-subtle text-warning-subtle-foreground"
                                    >
                                        <AlertTriangle aria-hidden="true" className="mr-1 h-3.5 w-3.5" />
                                        Requiere revisión
                                    </Badge>
                                )}
                                <span className="text-sm text-muted-foreground">
                                    {audit.issuesFound.length} observación
                                    {audit.issuesFound.length === 1 ? '' : 'es'}
                                </span>
                            </div>

                            {audit.issuesFound.length > 0 ? (
                                <ul className="space-y-2">
                                    {audit.issuesFound.map((issue, index) => (
                                        <li
                                            key={`${index}-${issue.slice(0, 24)}`}
                                            className="rounded-lg border border-warning-subtle-foreground/30 bg-warning-subtle px-4 py-2 text-sm text-warning-subtle-foreground"
                                        >
                                            {issue}
                                        </li>
                                    ))}
                                </ul>
                            ) : (
                                <p className="text-sm text-muted-foreground">
                                    No se encontraron observaciones en el período.
                                </p>
                            )}

                            {audit.aiRecommendation ? (
                                <div className="rounded-lg border bg-muted/40 p-4 text-sm">
                                    <p className="mb-1 flex items-center gap-2 font-medium">
                                        <Sparkles
                                            aria-hidden="true"
                                            className="h-4 w-4 text-muted-foreground"
                                        />
                                        Recomendación del asistente
                                    </p>
                                    <p className="text-muted-foreground">
                                        {audit.aiRecommendation}
                                    </p>
                                </div>
                            ) : null}
                        </div>
                    )}
                </CardContent>
            </Card>

            <Card>
                <CardHeader className="pb-2">
                    <CardTitle className="text-base font-semibold">Cerrar el período</CardTitle>
                </CardHeader>
                <CardContent className="space-y-3">
                    {isClosed ? (
                        <div className="flex items-center gap-3 rounded-lg border border-success-subtle-foreground/25 bg-success-subtle p-4 text-success-subtle-foreground">
                            <CheckCircle2 aria-hidden="true" className="h-5 w-5 shrink-0" />
                            <p className="text-sm font-medium">
                                {periodLabel(period)} está cerrado. No se pueden registrar asientos
                                nuevos en este período.
                            </p>
                        </div>
                    ) : (
                        <>
                            <p className="text-sm text-muted-foreground">
                                El cierre bloquea el período y genera el asiento de cierre. Revisa la
                                auditoría antes de confirmar: la acción no se puede deshacer desde la
                                interfaz.
                            </p>
                            <Button
                                onClick={closePeriod}
                                loading={executeClose.isPending}
                                disabled={!audit?.readyToClose}
                            >
                                {!executeClose.isPending && (
                                    <Lock aria-hidden="true" className="mr-2 h-4 w-4" />
                                )}
                                Confirmar y cerrar período
                            </Button>
                            {!audit?.readyToClose ? (
                                <p className="text-xs text-muted-foreground">
                                    Ejecuta una auditoría sin bloqueos para habilitar el cierre.
                                </p>
                            ) : null}
                        </>
                    )}
                </CardContent>
            </Card>
        </div>
    );
}
