"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import {
    AreaChart,
    Area,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer,
    ReferenceLine,
} from "recharts";
import { Activity } from "lucide-react";
import { useAiForecast } from "../hooks/useTreasury";
import { formatCLP } from "@/lib/formatters";

export function CashFlowChart() {
    const { data: forecast, isLoading } = useAiForecast(30);

    return (
        <Card>
            <CardHeader>
                <CardTitle className="flex items-center gap-2 text-lg">
                    <Activity className="h-5 w-5 text-indigo-600" />
                    Flujo de Caja Proyectado
                </CardTitle>
            </CardHeader>
            <CardContent>
                {isLoading ? (
                    <Skeleton className="h-[300px] w-full" />
                ) : !forecast || forecast.dailyForecast.length === 0 ? (
                    <div className="flex h-[300px] items-center justify-center text-sm text-muted-foreground">
                        No hay datos de flujo de caja disponibles.
                    </div>
                ) : (
                    <div className="h-[300px] w-full">
                        <ResponsiveContainer width="100%" height="100%">
                            <AreaChart data={forecast.dailyForecast}>
                                <defs>
                                    <linearGradient id="cashflowFill" x1="0" y1="0" x2="0" y2="1">
                                        <stop offset="5%" stopColor="#4f46e5" stopOpacity={0.4} />
                                        <stop offset="95%" stopColor="#4f46e5" stopOpacity={0} />
                                    </linearGradient>
                                </defs>
                                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                                <XAxis
                                    dataKey="date"
                                    tick={{ fontSize: 12 }}
                                    tickFormatter={(val: string) => val.substring(5)}
                                />
                                <YAxis
                                    tick={{ fontSize: 12 }}
                                    width={80}
                                    tickFormatter={(val: number) => formatCLP(val, false)}
                                />
                                <Tooltip formatter={(value: number) => [formatCLP(value), "Liquidez estimada"]} />
                                <ReferenceLine y={0} stroke="#dc2626" strokeDasharray="3 3" />
                                <Area
                                    type="monotone"
                                    dataKey="estimatedAmount"
                                    stroke="#4f46e5"
                                    strokeWidth={2}
                                    fill="url(#cashflowFill)"
                                />
                            </AreaChart>
                        </ResponsiveContainer>
                    </div>
                )}
            </CardContent>
        </Card>
    );
}
