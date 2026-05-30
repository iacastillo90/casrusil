"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { accountingService } from "@/features/accounting/services/accounting.service";
import { SmartPeriodSelector, PeriodRange } from "@/components/shared/SmartPeriodSelector";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { SkeletonLoader } from "@/components/shared/SkeletonLoader";
import { formatCLP } from "@/lib/formatters";
import { startOfMonth, endOfMonth } from "date-fns";

export default function IncomeStatementPage() {
    const [period, setPeriod] = useState<PeriodRange>({
        from: startOfMonth(new Date()),
        to: endOfMonth(new Date()),
        mode: 'MONTH'
    });

    const { data, isLoading } = useQuery({
        queryKey: ['income-statement', period.from, period.to],
        queryFn: () => accountingService.getIncomeStatement(period.from, period.to),
    });

    return (
        <div className="space-y-6 p-6">
            <div className="flex flex-col md:flex-row justify-between items-center gap-4">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Estado de Resultados</h1>
                    <p className="text-muted-foreground">
                        Rendimiento económico del <strong>{period.from.toLocaleDateString()}</strong> al <strong>{period.to.toLocaleDateString()}</strong>.
                    </p>
                </div>
                <SmartPeriodSelector value={period} onChange={setPeriod} />
            </div>

            {isLoading ? <SkeletonLoader /> : data && (
                <div className="space-y-6">
                    {/* KPI Principal: Utilidad Neta */}
                    <Card className={data.netIncome >= 0 ? "border-l-4 border-l-green-500" : "border-l-4 border-l-red-500"}>
                        <CardContent className="p-6 flex justify-between items-center">
                            <div>
                                <p className="text-sm font-medium text-muted-foreground">Resultado del Ejercicio</p>
                                <h2 className={`text-4xl font-bold ${data.netIncome >= 0 ? "text-green-600" : "text-red-600"}`}>
                                    {formatCLP(data.netIncome)}
                                </h2>
                            </div>
                            <div className="text-right">
                                <p className="text-sm text-muted-foreground">Margen Neto</p>
                                <p className="text-xl font-bold">{data.netMargin}%</p>
                            </div>
                        </CardContent>
                    </Card>

                    {/* Tabla de Cascada */}
                    <Card>
                        <CardContent className="p-0">
                            <table className="w-full text-sm">
                                <tbody className="divide-y">
                                    {/* Ingresos */}
                                    <tr className="bg-slate-50">
                                        <td className="p-4 font-bold">Ingresos Operacionales</td>
                                        <td className="p-4 text-right font-bold text-slate-700">{formatCLP(data.totalRevenue)}</td>
                                    </tr>

                                    {/* Costos */}
                                    <tr>
                                        <td className="p-4 pl-8 text-slate-600">(-) Costo de Ventas</td>
                                        <td className="p-4 text-right text-red-600 font-mono">{formatCLP(data.cogs)}</td>
                                    </tr>

                                    {/* Margen Bruto */}
                                    <tr className="bg-slate-50 font-medium">
                                        <td className="p-4 border-t-2 border-slate-200">Utilidad Bruta</td>
                                        <td className="p-4 text-right border-t-2 border-slate-200 text-slate-900">{formatCLP(data.grossProfit)}</td>
                                    </tr>

                                    {/* Gastos */}
                                    <tr>
                                        <td className="p-4 pl-8 text-slate-600">(-) Gastos de Administración</td>
                                        <td className="p-4 text-right text-red-500 font-mono">{formatCLP(data.adminExpenses)}</td>
                                    </tr>

                                    {/* Utilidad Final */}
                                    <tr className="bg-slate-100 text-lg font-bold">
                                        <td className="p-4">Utilidad Neta</td>
                                        <td className={`p-4 text-right ${data.netIncome >= 0 ? "text-green-700" : "text-red-700"}`}>
                                            {formatCLP(data.netIncome)}
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </CardContent>
                    </Card>
                </div>
            )}
        </div>
    );
}
