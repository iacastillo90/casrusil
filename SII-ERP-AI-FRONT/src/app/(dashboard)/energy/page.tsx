'use client';

import { Zap, BatteryCharging, Power, AlertTriangle, Loader2 } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { useEnergyMetrics, useEnergyAlerts } from '@/features/energy/hooks/useEnergy';
import { formatCLP } from '@/lib/formatters';

export default function EnergyPage() {
    const { data: metrics, isLoading: metricsLoading } = useEnergyMetrics();
    const { data: alerts, isLoading: alertsLoading } = useEnergyAlerts();

    const loading = metricsLoading || alertsLoading;

    return (
        <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
            <div>
                <h1 className="text-3xl font-bold tracking-tight">Monitoreo de Energía</h1>
                <p className="text-muted-foreground">Supervisión de estaciones de carga y métricas de consumo eléctrico.</p>
            </div>

            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Estaciones Activas</CardTitle>
                        <BatteryCharging className="h-4 w-4 text-primary" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">
                            {loading ? <Loader2 className="h-6 w-6 animate-spin" /> : `${metrics?.activeStations || 0} / ${metrics?.totalStations || 0}`}
                        </div>
                    </CardContent>
                </Card>
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Consumo Actual</CardTitle>
                        <Zap className="h-4 w-4 text-emerald-500" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">
                            {loading ? <Loader2 className="h-6 w-6 animate-spin" /> : `${metrics?.totalConsumptionKW || 0} kW`}
                        </div>
                        <p className="text-xs text-muted-foreground">Pico de {metrics?.peakConsumptionKW || 0} kW hoy</p>
                    </CardContent>
                </Card>
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Costo Estimado (Día)</CardTitle>
                        <Power className="h-4 w-4 text-blue-500" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">
                            {loading ? <Loader2 className="h-6 w-6 animate-spin" /> : formatCLP(metrics?.estimatedDailyCost || 0)}
                        </div>
                    </CardContent>
                </Card>
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Alertas de Red</CardTitle>
                        <AlertTriangle className="h-4 w-4 text-red-500" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">
                            {loading ? <Loader2 className="h-6 w-6 animate-spin" /> : (alerts?.length || 0)}
                        </div>
                    </CardContent>
                </Card>
            </div>
            {/* ... resto del componente igual */}

            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-7">
                <Card className="col-span-4">
                    <CardHeader>
                        <CardTitle>Curva de Consumo (OCPP)</CardTitle>
                        <CardDescription>
                            Flujo de energía en tiempo real desde los conectores.
                        </CardDescription>
                    </CardHeader>
                    <CardContent className="flex items-center justify-center h-[300px] border border-dashed rounded-lg bg-muted/20">
                        <div className="flex flex-col items-center text-muted-foreground">
                            <Zap className="h-12 w-12 mb-2 opacity-50" />
                            <p>Gráfico de carga interactivo estará disponible pronto.</p>
                            <p className="text-sm opacity-70">Esperando conexión WebSocket.</p>
                        </div>
                    </CardContent>
                </Card>
                <Card className="col-span-3">
                    <CardHeader>
                        <CardTitle>Estado de Cargadores</CardTitle>
                        <CardDescription>
                            Monitoreo de disponibilidad por estación.
                        </CardDescription>
                    </CardHeader>
                    <CardContent>
                        <div className="space-y-4">
                            <div className="flex items-center justify-between">
                                <div className="flex items-center space-x-2">
                                    <div className="w-2 h-2 bg-emerald-500 rounded-full animate-pulse" />
                                    <span className="font-medium">Estación Norte</span>
                                </div>
                                <span className="text-sm text-muted-foreground">Ocupado (80%)</span>
                            </div>
                            <div className="flex items-center justify-between">
                                <div className="flex items-center space-x-2">
                                    <div className="w-2 h-2 bg-primary rounded-full" />
                                    <span className="font-medium">Estación Sur</span>
                                </div>
                                <span className="text-sm text-muted-foreground">Libre</span>
                            </div>
                            <div className="flex items-center justify-between">
                                <div className="flex items-center space-x-2">
                                    <div className="w-2 h-2 bg-primary rounded-full" />
                                    <span className="font-medium">Centro Logístico</span>
                                </div>
                                <span className="text-sm text-muted-foreground">Libre</span>
                            </div>
                            <div className="flex items-center justify-between">
                                <div className="flex items-center space-x-2">
                                    <div className="w-2 h-2 bg-red-500 rounded-full" />
                                    <span className="font-medium">Estación Este</span>
                                </div>
                                <span className="text-sm text-red-500">Mantenimiento</span>
                            </div>
                        </div>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}
