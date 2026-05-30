"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { accountingService } from "@/features/accounting/services/accounting.service";
import { SmartPeriodSelector, PeriodRange } from "@/components/shared/SmartPeriodSelector";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { SkeletonLoader } from "@/components/shared/SkeletonLoader";
import { formatCLP } from "@/lib/formatters";
import { startOfMonth, endOfMonth } from "date-fns";
import { BalanceSheetTree } from "@/features/accounting/components/BalanceSheetTree";

export default function BalanceSheetPage() {
    // Por defecto: Mes actual
    const [period, setPeriod] = useState<PeriodRange>({
        from: startOfMonth(new Date()),
        to: endOfMonth(new Date()),
        mode: 'MONTH'
    });

    const { data, isLoading, error } = useQuery({
        queryKey: ['balance-sheet', period.to], // Depende de la fecha de corte
        queryFn: () => accountingService.getBalanceSheet(period.to),
    });

    return (
        <div className="space-y-6 p-6">
            <div className="flex flex-col md:flex-row justify-between items-center gap-4">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Balance General</h1>
                    <p className="text-muted-foreground">
                        Estado de situación financiera al cierre del <strong>{period.to.toLocaleDateString()}</strong>.
                    </p>
                </div>
                <SmartPeriodSelector value={period} onChange={setPeriod} />
            </div>

            {isLoading && <SkeletonLoader />}
            {error && <div className="text-red-500">Error cargando el balance.</div>}

            {data && (
                <div className="grid gap-6">
                    {/* Tarjetas de Resumen (Ecuación Contable) */}
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                        <Card className="bg-blue-50 border-blue-200">
                            <CardHeader className="pb-2"><CardTitle className="text-sm font-medium text-blue-800">Total Activos</CardTitle></CardHeader>
                            <CardContent><div className="text-2xl font-bold text-blue-900">{formatCLP(data.totalAssets)}</div></CardContent>
                        </Card>
                        <Card className="bg-red-50 border-red-200">
                            <CardHeader className="pb-2"><CardTitle className="text-sm font-medium text-red-800">Total Pasivos</CardTitle></CardHeader>
                            <CardContent><div className="text-2xl font-bold text-red-900">{formatCLP(data.totalLiabilities)}</div></CardContent>
                        </Card>
                        <Card className="bg-green-50 border-green-200">
                            <CardHeader className="pb-2"><CardTitle className="text-sm font-medium text-green-800">Patrimonio</CardTitle></CardHeader>
                            <CardContent><div className="text-2xl font-bold text-green-900">{formatCLP(data.totalEquity)}</div></CardContent>
                        </Card>
                    </div>

                    {/* Validar Ecuación Contable */}
                    {data.totalAssets !== (data.totalLiabilities + data.totalEquity) && (
                        <div className="bg-yellow-100 p-4 rounded-md text-yellow-800 font-bold text-center border border-yellow-300 animate-pulse">
                            ⚠️ ALERTA: El balance no cuadra (Dif: {formatCLP(data.totalAssets - (data.totalLiabilities + data.totalEquity))}). Revisa los asientos manuales.
                        </div>
                    )}

                    {/* Detalle de Cuentas */}
                    <Card>
                        <CardContent className="pt-6">
                            <BalanceSheetTree data={data} />
                        </CardContent>
                    </Card>
                </div>
            )}
        </div>
    );
}
