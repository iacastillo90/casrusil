'use client';

import { AlertTriangle, BatteryCharging, PlugZap, Power, Zap } from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { PageHeader } from '@/features/accounting/components/PageHeader';
import { EmptyState, ErrorState, TableSkeleton } from '@/features/accounting/components/DataStates';
import { useEnergyAlerts, useEnergyMetrics, useEnergyStations } from '@/features/energy/hooks/useEnergy';
import { formatCLP, formatDate } from '@/lib/formatters';
import type { EnergyStation } from '@/features/energy/types/energy.types';

const STATUS_LABEL: Record<EnergyStation['status'], string> = {
    ACTIVE: 'Operativa',
    MAINTENANCE: 'En mantenimiento',
    OFFLINE: 'Fuera de línea',
};

const statusClass = (status: EnergyStation['status']) => {
    if (status === 'ACTIVE') {
        return 'border-success-subtle-foreground/25 bg-success-subtle text-success-subtle-foreground';
    }
    if (status === 'MAINTENANCE') {
        return 'border-warning-subtle-foreground/30 bg-warning-subtle text-warning-subtle-foreground';
    }
    return 'border-destructive-subtle-foreground/25 bg-destructive-subtle text-destructive-subtle-foreground';
};

const severityClass = (severity: string) => {
    if (severity === 'HIGH') {
        return 'border-destructive-subtle-foreground/25 bg-destructive-subtle text-destructive-subtle-foreground';
    }
    if (severity === 'MEDIUM') {
        return 'border-warning-subtle-foreground/30 bg-warning-subtle text-warning-subtle-foreground';
    }
    return 'border-info-subtle-foreground/20 bg-info-subtle text-info-subtle-foreground';
};

export default function EnergyPage() {
    const metrics = useEnergyMetrics();
    const stations = useEnergyStations();
    const alerts = useEnergyAlerts();

    const metricValue = (value: number | string) =>
        metrics.isLoading ? '…' : value;

    return (
        <div className="space-y-6">
            <PageHeader
                title="Energía"
                description="Estaciones de carga, consumo eléctrico y alertas de red de la empresa."
            />

            {metrics.error ? (
                <ErrorState
                    title="No se pudieron cargar las métricas de energía"
                    message={
                        metrics.error instanceof Error ? metrics.error.message : 'Error desconocido'
                    }
                    onRetry={() => metrics.refetch()}
                    retrying={metrics.isFetching}
                />
            ) : (
                <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
                    <Card>
                        <CardHeader className="flex flex-row items-center justify-between pb-2">
                            <CardTitle className="text-sm font-medium text-muted-foreground">
                                Estaciones activas
                            </CardTitle>
                            <BatteryCharging aria-hidden="true" className="h-4 w-4 text-muted-foreground" />
                        </CardHeader>
                        <CardContent>
                            <p className="text-2xl font-bold font-mono tabular-nums">
                                {metricValue(
                                    `${metrics.data?.activeStations ?? 0} / ${metrics.data?.totalStations ?? 0}`,
                                )}
                            </p>
                        </CardContent>
                    </Card>
                    <Card>
                        <CardHeader className="flex flex-row items-center justify-between pb-2">
                            <CardTitle className="text-sm font-medium text-muted-foreground">
                                Consumo de hoy
                            </CardTitle>
                            <Zap aria-hidden="true" className="h-4 w-4 text-muted-foreground" />
                        </CardHeader>
                        <CardContent>
                            <p className="text-2xl font-bold font-mono tabular-nums">
                                {metricValue(`${metrics.data?.totalConsumptionKW ?? 0} kWh`)}
                            </p>
                        </CardContent>
                    </Card>
                    <Card>
                        <CardHeader className="flex flex-row items-center justify-between pb-2">
                            <CardTitle className="text-sm font-medium text-muted-foreground">
                                Costo estimado del día
                            </CardTitle>
                            <Power aria-hidden="true" className="h-4 w-4 text-muted-foreground" />
                        </CardHeader>
                        <CardContent>
                            <p className="text-2xl font-bold font-mono tabular-nums">
                                {metricValue(formatCLP(metrics.data?.estimatedDailyCost ?? 0))}
                            </p>
                        </CardContent>
                    </Card>
                    <Card>
                        <CardHeader className="flex flex-row items-center justify-between pb-2">
                            <CardTitle className="text-sm font-medium text-muted-foreground">
                                Carga promedio
                            </CardTitle>
                            <PlugZap aria-hidden="true" className="h-4 w-4 text-muted-foreground" />
                        </CardHeader>
                        <CardContent>
                            <p className="text-2xl font-bold font-mono tabular-nums">
                                {metricValue(`${Math.round(metrics.data?.averageLoadPercent ?? 0)}%`)}
                            </p>
                        </CardContent>
                    </Card>
                </div>
            )}

            <div className="grid gap-4 lg:grid-cols-2">
                <Card>
                    <CardHeader>
                        <CardTitle className="text-base font-semibold">Estaciones de carga</CardTitle>
                    </CardHeader>
                    <CardContent className="p-0">
                        {stations.error ? (
                            <div className="p-6">
                                <ErrorState
                                    title="No se pudieron cargar las estaciones"
                                    message={
                                        stations.error instanceof Error
                                            ? stations.error.message
                                            : 'Error desconocido'
                                    }
                                    onRetry={() => stations.refetch()}
                                    retrying={stations.isFetching}
                                />
                            </div>
                        ) : stations.isLoading ? (
                            <TableSkeleton rows={4} cols={3} className="px-6 pb-6" />
                        ) : !stations.data || stations.data.length === 0 ? (
                            <EmptyState
                                icon={BatteryCharging}
                                title="No hay estaciones registradas"
                                description="Cuando la empresa tenga estaciones de carga aparecerán acá con su estado y capacidad."
                            />
                        ) : (
                            <ul className="divide-y">
                                {stations.data.map((station) => (
                                    <li
                                        key={station.id}
                                        className="flex flex-wrap items-center justify-between gap-3 px-6 py-3"
                                    >
                                        <div>
                                            <p className="font-medium">{station.name}</p>
                                            <p className="text-xs text-muted-foreground">
                                                {station.connectorCount ?? 0} conector
                                                {station.connectorCount === 1 ? '' : 'es'} ·{' '}
                                                {station.maxCapacityKW} kW máx.
                                            </p>
                                        </div>
                                        <div className="flex items-center gap-2">
                                            <Badge variant="outline" className={statusClass(station.status)}>
                                                {STATUS_LABEL[station.status] ?? station.status}
                                            </Badge>
                                            <span className="text-xs text-muted-foreground">
                                                {station.connected ? 'Conectada' : 'Sin conexión'}
                                            </span>
                                        </div>
                                    </li>
                                ))}
                            </ul>
                        )}
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle className="flex items-center gap-2 text-base font-semibold">
                            <AlertTriangle aria-hidden="true" className="h-5 w-5 text-warning" />
                            Alertas de red
                        </CardTitle>
                    </CardHeader>
                    <CardContent className="p-0">
                        {alerts.error ? (
                            <div className="p-6">
                                <ErrorState
                                    title="No se pudieron cargar las alertas"
                                    message={
                                        alerts.error instanceof Error
                                            ? alerts.error.message
                                            : 'Error desconocido'
                                    }
                                    onRetry={() => alerts.refetch()}
                                    retrying={alerts.isFetching}
                                />
                            </div>
                        ) : alerts.isLoading ? (
                            <TableSkeleton rows={4} cols={2} className="px-6 pb-6" />
                        ) : !alerts.data || alerts.data.length === 0 ? (
                            <EmptyState
                                icon={Zap}
                                title="Sin alertas activas"
                                description="No hay caídas de voltaje, sobrecargas ni problemas de conectividad reportados."
                            />
                        ) : (
                            <ul className="divide-y">
                                {alerts.data.map((alert) => (
                                    <li
                                        key={alert.id}
                                        className="flex items-start justify-between gap-3 px-6 py-3"
                                    >
                                        <div>
                                            <p className="text-sm font-medium">{alert.message}</p>
                                            <p className="text-xs text-muted-foreground">
                                                {alert.type} · {formatDate(alert.timestamp)}
                                            </p>
                                        </div>
                                        <Badge variant="outline" className={severityClass(alert.severity)}>
                                            {alert.severity === 'HIGH'
                                                ? 'Alta'
                                                : alert.severity === 'MEDIUM'
                                                  ? 'Media'
                                                  : 'Baja'}
                                        </Badge>
                                    </li>
                                ))}
                            </ul>
                        )}
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}
