"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { DollarSign, CreditCard, Activity, Receipt } from "lucide-react";
import { formatCLP } from "@/lib/formatters";
import { useDashboardMetrics } from "../hooks/useDashboardMetrics";

const placeholder = (loading: boolean, value: string) => (loading ? '—' : value);

export function KPIGrid() {
    const { metrics, isLoading, isError } = useDashboardMetrics();
    const unavailable = isLoading || isError || !metrics;

    const delta = metrics?.salesDeltaPct ?? null;

    if (!metrics) {
        return (
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
                {[...Array(4)].map((_, i) => (
                    <Card key={i}>
                        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                            <CardTitle className="text-sm font-medium">
                                <div className="h-4 w-24 bg-muted/10 animate-pulse rounded" />
                            </CardTitle>
                        </CardHeader>
                        <CardContent>
                            <div className="text-2xl font-bold">
                                <div className="h-8 w-16 bg-muted/10 animate-pulse rounded" />
                            </div>
                        </CardContent>
                    </Card>
                ))}
            </div>
        );
    }

    return (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
            <Card>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                    <CardTitle className="text-sm font-medium">Ventas del Mes</CardTitle>
                    <DollarSign className="h-4 w-4 text-muted-foreground" />
                </CardHeader>
                <CardContent>
                    <div className="text-2xl font-bold" suppressHydrationWarning>
                        {placeholder(unavailable, formatCLP(metrics.currentMonthSales))}
                    </div>
                    <p className="text-xs text-muted-foreground">
                        {delta === null
                            ? 'Sin ventas el mes anterior para comparar'
                            : `${delta >= 0 ? '+' : ''}${delta.toFixed(1)}% respecto al mes anterior`}
                    </p>
                </CardContent>
            </Card>
            <Card>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                    <CardTitle className="text-sm font-medium">IVA Débito (Mes)</CardTitle>
                    <CreditCard className="h-4 w-4 text-muted-foreground" />
                </CardHeader>
                <CardContent>
                    <div className="text-2xl font-bold" suppressHydrationWarning>
                        {placeholder(unavailable, formatCLP(metrics.currentMonthVat))}
                    </div>
                    <p className="text-xs text-muted-foreground">
                        Calculado sobre facturas de venta emitidas
                    </p>
                </CardContent>
            </Card>
            <Card>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                    <CardTitle className="text-sm font-medium">Facturas Pendientes</CardTitle>
                    <Activity className="h-4 w-4 text-muted-foreground" />
                </CardHeader>
                <CardContent>
                    <div className="text-2xl font-bold">
                        {placeholder(unavailable, String(metrics.pendingCount))}
                    </div>
                    <p className="text-xs text-muted-foreground">
                        Por un total de <span suppressHydrationWarning>{formatCLP(metrics.pendingTotal)}</span>
                    </p>
                </CardContent>
            </Card>
            <Card>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                    <CardTitle className="text-sm font-medium">Ticket Promedio</CardTitle>
                    <Receipt className="h-4 w-4 text-muted-foreground" />
                </CardHeader>
                <CardContent>
                    <div className="text-2xl font-bold" suppressHydrationWarning>
                        {placeholder(unavailable, formatCLP(metrics.averageTicket))}
                    </div>
                    <p className="text-xs text-muted-foreground">
                        {metrics.currentMonthCount} ventas registradas este mes
                    </p>
                </CardContent>
            </Card>
        </div>
    );
}