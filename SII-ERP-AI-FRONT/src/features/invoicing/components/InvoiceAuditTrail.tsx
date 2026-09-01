import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { History, User } from "lucide-react";
import { formatDate } from "@/lib/formatters";
import type { AuditTrailEntry } from "@/features/accounting/services/accounting.service";

interface InvoiceAuditTrailProps {
    auditTrail?: AuditTrailEntry[];
    isLoading: boolean;
}

export function InvoiceAuditTrail({ auditTrail, isLoading }: InvoiceAuditTrailProps) {
    if (isLoading) {
        return <Skeleton className="h-48 w-full" />;
    }

    const events = auditTrail ?? [];

    if (events.length === 0) {
        return (
            <Card>
                <CardContent className="py-12 text-center text-muted-foreground">
                    No hay eventos de trazabilidad registrados para este documento.
                </CardContent>
            </Card>
        );
    }

    return (
        <Card>
            <CardContent className="py-6">
                <ol className="relative space-y-6 border-l pl-6">
                    {events.map((event) => (
                        <li key={event.id} className="relative">
                            <span className="absolute -left-[31px] flex h-6 w-6 items-center justify-center rounded-full bg-indigo-100 text-indigo-600">
                                <History className="h-3.5 w-3.5" />
                            </span>
                            <p className="font-medium">{event.action}</p>
                            {event.detail && <p className="text-sm text-muted-foreground">{event.detail}</p>}
                            <div className="mt-1 flex items-center gap-3 text-xs text-muted-foreground">
                                <span className="flex items-center gap-1">
                                    <User className="h-3 w-3" /> {event.user}
                                </span>
                                <span>{formatDate(event.timestamp)}</span>
                            </div>
                        </li>
                    ))}
                </ol>
            </CardContent>
        </Card>
    );
}
