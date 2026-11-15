"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { AlertTriangle, ShieldCheck, Calculator, FileWarning } from "lucide-react";
import { useComplianceDashboard } from "@/features/compliance/hooks/useCompliance";
import { formatCLP } from "@/lib/formatters";

const severityVariant = (severity: string) => {
    if (severity === "CRITICAL") return "destructive" as const;
    if (severity === "WARNING") return "secondary" as const;
    return "outline" as const;
};

export default function ComplianceCenterPage() {
    const { data: dashboard, isLoading, isError } = useComplianceDashboard();

    if (isLoading) {
        return <div className="p-8 text-center text-muted-foreground animate-pulse">Consultando estado tributario SII...</div>;
    }

    if (isError || !dashboard) {
        return (
            <div className="p-8">
                <Card className="border-destructive/40">
                    <CardHeader>
                        <CardTitle className="text-lg text-destructive flex items-center">
                            <AlertTriangle className="mr-2 h-5 w-5" />
                            No se pudo cargar el Centro de Salud Tributaria
                        </CardTitle>
                    </CardHeader>
                    <CardContent>
                        <p className="text-sm text-muted-foreground">
                            El servicio de compliance no respondió. Revisá la conexión con el backend o los permisos del usuario
                            (requiere rol ADMIN o ACCOUNTANT).
                        </p>
                    </CardContent>
                </Card>
            </div>
        );
    }

    const healthScore = dashboard.globalHealthScore ?? 0;
    const f29 = dashboard.f29Preview;
    const taxParameters = dashboard.taxParameters;
    const acuseAlerts = dashboard.acuseReciboAlerts ?? [];

    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-3xl font-bold tracking-tight">Centro de Salud Tributaria (SII)</h1>
                <p className="text-muted-foreground mt-1">Cruce de DTEs, Alertas de Acuse de Recibo y Simulador de F29.</p>
            </div>

            {/* KPIs */}
            <div className="grid gap-4 md:grid-cols-3">
                <Card className={healthScore >= 80 ? "border-green-500/50" : "border-amber-500/50"}>
                    <CardHeader className="flex flex-row items-center justify-between pb-2">
                        <CardTitle className="text-sm font-medium">Salud Tributaria General</CardTitle>
                        <ShieldCheck className={`h-4 w-4 ${healthScore >= 80 ? 'text-green-500' : 'text-amber-500'}`} />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{healthScore}%</div>
                        <p className="text-xs text-muted-foreground mt-1">Basado en discrepancias SII y alertas.</p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader className="flex flex-row items-center justify-between pb-2">
                        <CardTitle className="text-sm font-medium">IVA Proporcional Determinado</CardTitle>
                        <Calculator className="h-4 w-4 text-blue-500" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{formatCLP(f29?.vatPayable ?? 0)}</div>
                        <p className="text-xs text-muted-foreground mt-1">Factor de IVA ajustado por ventas exentas.</p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader className="flex flex-row items-center justify-between pb-2">
                        <CardTitle className="text-sm font-medium">Parámetros Dinámicos</CardTitle>
                        <Calculator className="h-4 w-4 text-purple-500" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-sm font-bold mt-2">
                            Retención BHE: {taxParameters?.bheRetentionRate ?? '—'}%
                        </div>
                        <p className="text-xs text-muted-foreground mt-1">
                            UTM: {taxParameters ? formatCLP(taxParameters.utmValue) : 'Sin parámetros cargados'}
                        </p>
                    </CardContent>
                </Card>
            </div>

            {/* Acuse de Recibo */}
            <Card className="border-red-500/20 shadow-sm">
                <CardHeader>
                    <CardTitle className="text-lg text-red-600 flex items-center">
                        <AlertTriangle className="mr-2 h-5 w-5" />
                        Alertas Urgentes: Ley Pago a 30 Días (Acuse de Recibo)
                    </CardTitle>
                </CardHeader>
                <CardContent>
                    {acuseAlerts.length > 0 ? (
                        <div className="space-y-4">
                            {acuseAlerts.map((alert) => (
                                <div key={alert.code} className="flex items-start justify-between p-4 bg-red-50 rounded-lg border border-red-100">
                                    <div className="flex items-start">
                                        <FileWarning className="h-5 w-5 text-red-500 mt-0.5 mr-3" />
                                        <div>
                                            <p className="text-sm font-medium text-red-900">{alert.message}</p>
                                            <p className="text-xs text-red-700 mt-1">Código: {alert.code}</p>
                                        </div>
                                    </div>
                                    <Badge variant={severityVariant(alert.severity)}>{alert.severity}</Badge>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <p className="text-sm text-muted-foreground">No hay facturas por vencer sin acuse de recibo.</p>
                    )}
                </CardContent>
            </Card>
        </div>
    );
}
