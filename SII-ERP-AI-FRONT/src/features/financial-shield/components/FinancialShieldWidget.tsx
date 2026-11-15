"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { ShieldCheck, ShieldAlert, ShieldX } from "lucide-react";
import { formatCLP, formatDate } from "@/lib/formatters";
import { useFinancialShieldHealth } from "../hooks/useFinancialShieldHealth";
import type { HealthStatus } from "../services/financial-shield.service";

const statusConfig: Record<HealthStatus, { label: string; className: string; Icon: typeof ShieldCheck }> = {
    HEALTHY: { label: 'Saludable', className: 'text-green-600', Icon: ShieldCheck },
    WARNING: { label: 'En Observación', className: 'text-amber-600', Icon: ShieldAlert },
    CRITICAL: { label: 'Crítico', className: 'text-red-600', Icon: ShieldX },
};

export const FinancialShieldWidget = () => {
    const { data, isLoading, isError } = useFinancialShieldHealth();

    if (isLoading) {
        return (
            <Card>
                <CardHeader>
                    <CardTitle className="text-base font-semibold">Escudo Financiero (IA)</CardTitle>
                </CardHeader>
                <CardContent>
                    <div className="h-16 w-full animate-pulse rounded bg-muted/20" />
                </CardContent>
            </Card>
        );
    }

    if (isError || !data) {
        return (
            <Card className="border-destructive/30">
                <CardHeader>
                    <CardTitle className="text-base font-semibold">Escudo Financiero (IA)</CardTitle>
                </CardHeader>
                <CardContent>
                    <p className="text-sm text-muted-foreground">
                        No se pudo calcular la salud financiera. Verificá el acceso al módulo (ADMIN, ACCOUNTANT o TREASURER).
                    </p>
                </CardContent>
            </Card>
        );
    }

    const { label, className, Icon } = statusConfig[data.status] ?? statusConfig.WARNING;
    const risks = data.riskFactors ?? [];

    return (
        <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-base font-semibold">Escudo Financiero (IA)</CardTitle>
                <Badge variant="outline" className={className}>
                    <Icon className="mr-1 h-3.5 w-3.5" />
                    {label}
                </Badge>
            </CardHeader>
            <CardContent className="space-y-3">
                <div className="grid gap-3 sm:grid-cols-2">
                    <div>
                        <p className="text-xs text-muted-foreground">Saldo proyectado a 30 días</p>
                        <p className="text-xl font-bold" suppressHydrationWarning>
                            {formatCLP(data.projectedBalance30Days)}
                        </p>
                    </div>
                    <div>
                        <p className="text-xs text-muted-foreground">Runway de caja hasta</p>
                        <p className="text-xl font-bold">
                            {data.runwayEnd ? formatDate(data.runwayEnd) : 'Sin fecha de quiebre estimada'}
                        </p>
                    </div>
                </div>

                {risks.length > 0 ? (
                    <div className="space-y-1">
                        <p className="text-xs font-medium text-muted-foreground">Factores de riesgo detectados</p>
                        <ul className="space-y-1">
                            {risks.slice(0, 3).map((risk) => (
                                <li key={`${risk.severity}-${risk.description}`} className="flex items-start justify-between gap-2 text-sm">
                                    <span>{risk.description}</span>
                                    <Badge variant={risk.severity === 'CRITICAL' || risk.severity === 'HIGH' ? 'destructive' : 'secondary'}>
                                        {risk.severity}
                                    </Badge>
                                </li>
                            ))}
                        </ul>
                    </div>
                ) : (
                    <p className="text-sm text-muted-foreground">Sin factores de riesgo relevantes.</p>
                )}
            </CardContent>
        </Card>
    );
};
