"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { complianceService } from "@/features/compliance/services/compliance.service";
import PeriodSelector from "@/components/shared/PeriodSelector";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { CheckCircle2, AlertTriangle, XCircle, BrainCircuit } from "lucide-react";
import { SkeletonLoader } from "@/components/shared/SkeletonLoader";
import { formatCLP } from "@/lib/formatters";

export default function CompliancePage() {
    const [period, setPeriod] = useState(new Date().toISOString().slice(0, 7)); // YYYY-MM

    const { data, isLoading, error } = useQuery<any>({
        queryKey: ['compliance', period],
        queryFn: () => complianceService.getDashboard(period),
    });

    if (isLoading) return <SkeletonLoader />;
    if (error) return <div className="text-red-500">Error cargando el panel de control.</div>;
    if (!data) return null;

    return (
        <div className="space-y-6 p-6">
            {/* HEADER & SCORE */}
            <div className="flex justify-between items-center">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Centro de Control (Modo Dios)</h1>
                    <p className="text-muted-foreground">Inteligencia Financiera y Tributaria Unificada</p>
                </div>
                <div className="flex items-center gap-4">
                    <PeriodSelector value={period} onChange={setPeriod} />
                    <div className={`flex flex-col items-center p-4 rounded-xl border-2 ${data.globalHealthScore > 80 ? 'border-green-500 bg-green-50' : 'border-yellow-500 bg-yellow-50'
                        }`}>
                        <span className="text-xs font-bold uppercase">Salud Global</span>
                        <span className="text-3xl font-black">{data.globalHealthScore}%</span>
                    </div>
                </div>
            </div>

            {/* ALERTAS IA (Prioridad Alta) */}
            {data.aiAlerts.length > 0 && (
                <Card className="border-l-4 border-l-purple-500 bg-purple-50/20">
                    <CardHeader className="pb-2">
                        <CardTitle className="flex items-center gap-2 text-lg text-purple-700">
                            <BrainCircuit className="h-5 w-5" />
                            Insights de Inteligencia Artificial
                        </CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-2">
                        {data.aiAlerts.map((alert: any, idx: number) => (
                            <Alert key={idx} variant={alert.severity === 'CRITICAL' ? 'destructive' : 'default'} className="bg-white">
                                <AlertTriangle className="h-4 w-4" />
                                <AlertTitle>{alert.code}</AlertTitle>
                                <AlertDescription>{alert.message}</AlertDescription>
                            </Alert>
                        ))}
                    </CardContent>
                </Card>
            )}

            {/* GRID DE CONTROL */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">

                {/* 1. Radar SII */}
                <Card>
                    <CardHeader><CardTitle>Conciliación SII</CardTitle></CardHeader>
                    <CardContent>
                        <div className="flex justify-between mb-2">
                            <span>En ERP:</span> <span className="font-bold">{data.siiStatus.totalErp}</span>
                        </div>
                        <div className="flex justify-between mb-4">
                            <span>En SII:</span> <span className="font-bold">{data.siiStatus.totalSii}</span>
                        </div>
                        {data.siiStatus.isBalanced ? (
                            <Alert className="bg-green-100 border-green-200">
                                <CheckCircle2 className="h-4 w-4 text-green-600" />
                                <AlertTitle className="text-green-800">Conciliado</AlertTitle>
                            </Alert>
                        ) : (
                            <Alert variant="destructive">
                                <XCircle className="h-4 w-4" />
                                <AlertTitle>Descuadre: {data.siiStatus.missingInErp} docs faltantes</AlertTitle>
                            </Alert>
                        )}
                    </CardContent>
                </Card>

                {/* 2. Predicción F29 */}
                <Card>
                    <CardHeader><CardTitle>Impuestos (F29)</CardTitle></CardHeader>
                    <CardContent className="space-y-3">
                        <div className="flex justify-between text-sm">
                            <span>Débito (Ventas):</span>
                            <span className="text-red-500 font-mono">{formatCLP(data.f29Preview.vatDebit)}</span>
                        </div>
                        <div className="flex justify-between text-sm">
                            <span>Crédito (Compras):</span>
                            <span className="text-green-600 font-mono">{formatCLP(data.f29Preview.vatCredit)}</span>
                        </div>
                        <div className="border-t pt-2 mt-2 flex justify-between font-bold text-lg">
                            <span>A Pagar:</span>
                            <span>{formatCLP(data.f29Preview.totalTax)}</span>
                        </div>
                    </CardContent>
                </Card>

                {/* 3. Salud Contable */}
                <Card>
                    <CardHeader><CardTitle>Integridad Contable</CardTitle></CardHeader>
                    <CardContent>
                        <div className="space-y-4">
                            <div className="flex items-center justify-between">
                                <span>Asientos Descuadrados</span>
                                <span className={`px-2 py-1 rounded font-bold ${data.accountingStatus.unbalancedEntries > 0 ? 'bg-red-100 text-red-700' : 'bg-gray-100'}`}>
                                    {data.accountingStatus.unbalancedEntries}
                                </span>
                            </div>
                            <div className="flex items-center justify-between">
                                <span>Pendientes de Clasificar</span>
                                <span className={`px-2 py-1 rounded font-bold ${data.accountingStatus.pendingClassification > 0 ? 'bg-yellow-100 text-yellow-700' : 'bg-gray-100'}`}>
                                    {data.accountingStatus.pendingClassification}
                                </span>
                            </div>
                            {data.accountingStatus.pendingClassification > 0 && (
                                <button className="w-full mt-2 text-sm text-blue-600 hover:underline">
                                    Resolver Clasificaciones &rarr;
                                </button>
                            )}
                        </div>
                    </CardContent>
                </Card>

            </div>
        </div>
    );
}
