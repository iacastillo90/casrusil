"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { useAiForecast } from "../hooks/useTreasury";
import { Wallet, TrendingUp, TrendingDown, AlertTriangle } from "lucide-react";
import { formatCLP } from "@/lib/formatters";

export function LiquidityKPIs() {
    const { data: forecast, isLoading } = useAiForecast(30);

    if (isLoading) {
        return (
            <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
                {Array.from({ length: 3 }).map((_, i) => (
                    <Skeleton key={i} className="h-28 w-full" />
                ))}
            </div>
        );
    }

    const current = forecast?.currentLiquidity ?? 0;
    const predicted = forecast?.predictedLiquidity ?? 0;
    const trendPositive = (forecast?.trend ?? "POSITIVE") === "POSITIVE";
    const alertCount = forecast?.riskAlerts?.length ?? 0;

    return (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
            <Card className="border-l-4 border-l-indigo-500">
                <CardHeader className="pb-2">
                    <CardTitle className="flex items-center gap-2 text-sm font-medium text-muted-foreground">
                        <Wallet className="h-4 w-4" /> Liquidez Actual
                    </CardTitle>
                </CardHeader>
                <CardContent>
                    <div className="text-3xl font-bold">{formatCLP(current)}</div>
                    <p className="mt-1 text-xs text-muted-foreground">Saldo disponible hoy</p>
                </CardContent>
            </Card>

            <Card className={trendPositive ? "border-l-4 border-l-green-500" : "border-l-4 border-l-red-500"}>
                <CardHeader className="pb-2">
                    <CardTitle className="flex items-center gap-2 text-sm font-medium text-muted-foreground">
                        {trendPositive ? (
                            <TrendingUp className="h-4 w-4 text-green-600" />
                        ) : (
                            <TrendingDown className="h-4 w-4 text-red-600" />
                        )}
                        Proyección 30 días
                    </CardTitle>
                </CardHeader>
                <CardContent>
                    <div className={`text-3xl font-bold ${trendPositive ? "text-green-600" : "text-red-600"}`}>
                        {formatCLP(predicted)}
                    </div>
                    <p className="mt-1 text-xs text-muted-foreground">Estimación asistida por IA</p>
                </CardContent>
            </Card>

            <Card className="border-l-4 border-l-amber-500">
                <CardHeader className="pb-2">
                    <CardTitle className="flex items-center gap-2 text-sm font-medium text-muted-foreground">
                        <AlertTriangle className="h-4 w-4 text-amber-600" /> Alertas de Riesgo
                    </CardTitle>
                </CardHeader>
                <CardContent>
                    <div className="text-3xl font-bold text-amber-700">{alertCount}</div>
                    <p className="mt-1 text-xs text-muted-foreground">Eventos que requieren atención</p>
                </CardContent>
            </Card>
        </div>
    );
}
