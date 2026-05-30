'use client';

import { Truck, MapPin, Activity, CheckCircle2 } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

export default function FleetPage() {
    return (
        <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
            <div>
                <h1 className="text-3xl font-bold tracking-tight">Gestión de Flotas</h1>
                <p className="text-muted-foreground">Monitoreo en tiempo real de vehículos y logística.</p>
            </div>

            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Vehículos Activos</CardTitle>
                        <Truck className="h-4 w-4 text-primary" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">12</div>
                        <p className="text-xs text-muted-foreground">De 15 totales</p>
                    </CardContent>
                </Card>
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">En Ruta</CardTitle>
                        <MapPin className="h-4 w-4 text-emerald-500" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">8</div>
                        <p className="text-xs text-muted-foreground">+2 desde ayer</p>
                    </CardContent>
                </Card>
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Eficiencia</CardTitle>
                        <Activity className="h-4 w-4 text-blue-500" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">94%</div>
                        <p className="text-xs text-muted-foreground">+2.1% esta semana</p>
                    </CardContent>
                </Card>
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Mantenimiento</CardTitle>
                        <CheckCircle2 className="h-4 w-4 text-amber-500" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">2</div>
                        <p className="text-xs text-muted-foreground">Vehículos programados</p>
                    </CardContent>
                </Card>
            </div>

            <Card className="col-span-4 min-h-[400px]">
                <CardHeader>
                    <CardTitle>Mapa de Telemetría (Demo)</CardTitle>
                    <CardDescription>
                        Vista simulada del rastreo GPS y consumo en tiempo real.
                    </CardDescription>
                </CardHeader>
                <CardContent className="flex items-center justify-center h-[300px] border border-dashed rounded-lg bg-muted/20">
                    <div className="flex flex-col items-center text-muted-foreground">
                        <MapPin className="h-12 w-12 mb-2 opacity-50" />
                        <p>El mapa interactivo estará disponible pronto.</p>
                        <p className="text-sm opacity-70">Integración con telemetría en curso.</p>
                    </div>
                </CardContent>
            </Card>
        </div>
    );
}
