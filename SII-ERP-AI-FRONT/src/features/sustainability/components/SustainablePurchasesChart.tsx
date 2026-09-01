"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from "recharts";
import { formatCLP } from "@/lib/formatters";
import type { MonthlyComparison } from "../services/sustainability.service";

interface SustainablePurchasesChartProps {
    data: MonthlyComparison[];
}

export function SustainablePurchasesChart({ data }: SustainablePurchasesChartProps) {
    return (
        <Card>
            <CardHeader>
                <CardTitle className="text-base font-semibold">Compras: Total vs. Sostenibles</CardTitle>
            </CardHeader>
            <CardContent>
                {data.length === 0 ? (
                    <div className="flex h-[300px] items-center justify-center text-sm text-muted-foreground">
                        No hay datos de comparación mensual disponibles.
                    </div>
                ) : (
                    <div className="h-[300px]">
                        <ResponsiveContainer width="100%" height="100%">
                            <BarChart data={data}>
                                <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
                                <XAxis dataKey="month" fontSize={12} />
                                <YAxis
                                    fontSize={12}
                                    tickFormatter={(v: number) => formatCLP(v, false)}
                                    width={80}
                                />
                                <Tooltip formatter={(value: number) => formatCLP(value)} />
                                <Legend />
                                <Bar dataKey="total" name="Total" fill="#94a3b8" radius={[4, 4, 0, 0]} />
                                <Bar dataKey="sustainable" name="Sostenibles" fill="#16a34a" radius={[4, 4, 0, 0]} />
                            </BarChart>
                        </ResponsiveContainer>
                    </div>
                )}
            </CardContent>
        </Card>
    );
}
