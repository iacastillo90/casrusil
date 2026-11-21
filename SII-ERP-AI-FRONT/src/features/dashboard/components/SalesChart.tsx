"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Bar, BarChart, ResponsiveContainer, XAxis, YAxis, Tooltip } from "recharts";
import { formatCLP } from "@/lib/formatters";
import { useEffect, useState } from "react";
import { useDashboardMetrics } from "../hooks/useDashboardMetrics";

export function SalesChart() {
    const [mounted, setMounted] = useState(false);
    const { metrics, isLoading } = useDashboardMetrics();

    useEffect(() => {
        setMounted(true);
    }, []);

    if (!mounted || isLoading || !metrics) {
        return (
            <Card className="col-span-4">
                <CardHeader>
                    <CardTitle>Resumen de Ventas</CardTitle>
                </CardHeader>
                <CardContent className="pl-2 h-[350px] flex items-center justify-center">
                    <div className="h-full w-full bg-muted/10 animate-pulse rounded" />
                </CardContent>
            </Card>
        );
    }

    const hasSales = metrics.monthlySales.some((point) => point.total > 0);

    return (
        <Card className="col-span-4">
            <CardHeader>
                <CardTitle>Resumen de Ventas</CardTitle>
            </CardHeader>
            <CardContent className="pl-2">
                {!hasSales ? (
                    <div className="h-[350px] flex flex-col items-center justify-center text-center text-muted-foreground">
                        <p className="text-sm">Todavía no hay ventas registradas en los últimos 12 meses.</p>
                    </div>
                ) : (
                    <ResponsiveContainer width="100%" height={350}>
                        <BarChart data={metrics.monthlySales}>
                            <XAxis
                                dataKey="name"
                                stroke="hsl(var(--muted-foreground))"
                                fontSize={12}
                                tickLine={false}
                                axisLine={false}
                            />
                            <YAxis
                                stroke="hsl(var(--muted-foreground))"
                                fontSize={12}
                                width={90}
                                tickLine={false}
                                axisLine={false}
                                tickFormatter={(value) => formatCLP(value)}
                            />
                            <Tooltip
                                formatter={(value: number) => formatCLP(value)}
                                cursor={{ fill: 'transparent' }}
                            />
                            <Bar dataKey="total" fill="hsl(var(--primary))" radius={[4, 4, 0, 0]} />
                        </BarChart>
                    </ResponsiveContainer>
                )}
            </CardContent>
        </Card>
    );
}