"use client";

import { AlertTriangle, Info, ShieldAlert, CheckCircle2 } from "lucide-react";
import { cn } from "@/lib/utils";
import { formatDate } from "@/lib/formatters";

export type AuditAlertSeverity = "INFO" | "WARNING" | "CRITICAL";

export interface AuditAlert {
    id: string;
    severity: AuditAlertSeverity;
    title: string;
    message: string;
    /** El endpoint de auditoría no expone fecha; puede venir vacío. */
    createdAt?: string;
}

interface AuditAlertsListProps {
    alerts: AuditAlert[];
}

const severityConfig: Record<
    AuditAlertSeverity,
    { icon: typeof Info; className: string; iconClass: string }
> = {
    INFO: { icon: Info, className: "bg-blue-50 border-blue-200", iconClass: "text-blue-600" },
    WARNING: { icon: AlertTriangle, className: "bg-amber-50 border-amber-200", iconClass: "text-amber-600" },
    CRITICAL: { icon: ShieldAlert, className: "bg-red-50 border-red-200", iconClass: "text-red-600" },
};

export function AuditAlertsList({ alerts }: AuditAlertsListProps) {
    if (!alerts.length) {
        return (
            <div className="flex flex-col items-center justify-center py-8 text-center text-muted-foreground">
                <CheckCircle2 className="mb-2 h-8 w-8 text-green-500 opacity-70" />
                <p className="text-sm">No hay alertas de auditoría pendientes.</p>
            </div>
        );
    }

    return (
        <ul className="space-y-3">
            {alerts.map((alert) => {
                const config = severityConfig[alert.severity] ?? severityConfig.INFO;
                const Icon = config.icon;
                return (
                    <li key={alert.id} className={cn("flex gap-3 rounded-lg border p-3", config.className)}>
                        <Icon className={cn("mt-0.5 h-5 w-5 shrink-0", config.iconClass)} />
                        <div className="min-w-0 flex-1">
                            <p className="font-medium leading-tight">{alert.title}</p>
                            <p className="mt-0.5 text-sm text-muted-foreground">{alert.message}</p>
                            {alert.createdAt ? (
                                <p className="mt-1 text-xs text-muted-foreground">
                                    {formatDate(alert.createdAt)}
                                </p>
                            ) : null}
                        </div>
                    </li>
                );
            })}
        </ul>
    );
}
