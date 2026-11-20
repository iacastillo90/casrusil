'use client';

import { Activity, CheckCircle2, MapPin, Truck } from 'lucide-react';

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { PageHeader } from '@/features/accounting/components/PageHeader';
import { ErrorState } from '@/features/accounting/components/DataStates';
import { useFleet } from '@/features/fleet/hooks/useFleet';

export default function FleetPage() {
    const { metrics, isLoading, error, refetch, isFetching } = useFleet();

    return (
        <div className="space-y-6">
            <PageHeader
                title="Gestión de flota"
                description="Vehículos, viajes y mantenimiento programado de la empresa."
            />

            {error ? (
                <ErrorState
                    message={error instanceof Error ? error.message : 'Error al cargar las métricas de flota'}
                    onRetry={() => refetch()}
                    retrying={isFetching && !isLoading}
                />
            ) : isLoading || !metrics ? (
                <p className="text-sm text-muted-foreground">Cargando flota…</p>
            ) : (
                <>
                    <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
                        <Card>
                            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                                <CardTitle className="text-sm font-medium">Vehículos activos</CardTitle>
                                <Truck className="h-4 w-4 text-primary" aria-hidden="true" />
                            </CardHeader>
                            <CardContent>
                                <div className="text-2xl font-semibold">{metrics.activeVehicles}</div>
                                <p className="text-xs text-muted-foreground">De {metrics.totalVehicles} totales</p>
                            </CardContent>
                        </Card>
                        <Card>
                            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                                <CardTitle className="text-sm font-medium">En ruta</CardTitle>
                                <MapPin className="h-4 w-4 text-success" aria-hidden="true" />
                            </CardHeader>
                            <CardContent>
                                <div className="text-2xl font-semibold">{metrics.enRoute}</div>
                                <p className="text-xs text-muted-foreground">Vehículos con viaje en curso</p>
                            </CardContent>
                        </Card>
                        <Card>
                            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                                <CardTitle className="text-sm font-medium">Eficiencia</CardTitle>
                                <Activity className="h-4 w-4 text-info" aria-hidden="true" />
                            </CardHeader>
                            <CardContent>
                                <div className="text-2xl font-semibold">{metrics.efficiencyPercent}%</div>
                                <p className="text-xs text-muted-foreground">Calculada sobre viajes del período</p>
                            </CardContent>
                        </Card>
                        <Card>
                            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                                <CardTitle className="text-sm font-medium">Mantenimiento</CardTitle>
                                <CheckCircle2 className="h-4 w-4 text-warning" aria-hidden="true" />
                            </CardHeader>
                            <CardContent>
                                <div className="text-2xl font-semibold">{metrics.maintenanceScheduled}</div>
                                <p className="text-xs text-muted-foreground">Vehículos programados</p>
                            </CardContent>
                        </Card>
                    </div>

                    <Card>
                        <CardHeader>
                            <CardTitle>Mapa de telemetría</CardTitle>
                            <CardDescription>Rastreo GPS y estado de batería.</CardDescription>
                        </CardHeader>
                        <CardContent className="flex h-[300px] items-center justify-center rounded-lg border border-dashed bg-muted/20">
                            <div className="flex flex-col items-center text-muted-foreground">
                                <MapPin className="mb-2 h-12 w-12 opacity-50" aria-hidden="true" />
                                <p>Sin proveedor de telemetría integrado.</p>
                                <p className="text-sm opacity-70">El mapa se habilitará cuando exista el endpoint de posiciones.</p>
                            </div>
                        </CardContent>
                    </Card>
                </>
            )}
        </div>
    );
}
