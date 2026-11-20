import React from 'react';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { cn } from '@/lib/utils';
import { DTE_STATUS } from '@/lib/constants';
import { formatDate } from '@/lib/formatters';
import { History, CheckCircle, Clock, XCircle, Send, FileText } from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import { accountingService } from '@/features/accounting/services/accounting.service';
import { AuditTrailEntry } from '@/features/accounting/services/accounting.service';
import { Skeleton } from '@/components/ui/skeleton';

interface DTEStatusTrackerProps {
    invoiceId: string;
    status: string;
}

// Status configuration with icons, colors, and labels
const statusConfig = {
    [DTE_STATUS.DRAFT]: {
        label: 'Borrador',
        description: 'Documento en borrador, no enviado al SII',
        icon: Clock,
        variant: 'secondary' as const,
        color: '#6B7280',
    },
    [DTE_STATUS.PENDING]: {
        label: 'Pendiente de envío',
        description: 'Documento emitido y con folio asignado, pendiente de envío al SII',
        icon: Clock,
        variant: 'secondary' as const,
        color: '#F59E0B',
    },
    [DTE_STATUS.SIGNED]: {
        label: 'Firmado',
        description: 'Documento firmado digitalmente',
        icon: FileText,
        variant: 'default' as const,
        color: '#3B82F6',
    },
    [DTE_STATUS.SENT]: {
        label: 'Enviado',
        description: 'Documento enviado al SII',
        icon: Send,
        variant: 'default' as const,
        color: '#8B5CF6',
    },
    [DTE_STATUS.ACCEPTED]: {
        label: 'Aceptado',
        description: 'Documento aceptado por el SII',
        icon: CheckCircle,
        variant: 'success' as const,
        color: '#10B981',
    },
    [DTE_STATUS.ACCEPTED_WITH_OBJECTIONS]: {
        label: 'Aceptado con objeciones',
        description: 'Documento aceptado por el SII con observaciones',
        icon: CheckCircle,
        variant: 'default' as const,
        color: '#0EA5E9',
    },
    [DTE_STATUS.REJECTED]: {
        label: 'Rechazado',
        description: 'Documento rechazado por el SII',
        icon: XCircle,
        variant: 'destructive' as const,
        color: '#EF4444',
    },
    [DTE_STATUS.CANCELLED]: {
        label: 'Anulado',
        description: 'Documento anulado',
        icon: XCircle,
        variant: 'secondary' as const,
        color: '#6B7280',
    },
};

// Timeline item component
interface TimelineItemProps {
    entry: AuditTrailEntry;
    isFirst?: boolean;
    isLast?: boolean;
}

const TimelineItem: React.FC<TimelineItemProps> = ({ entry, isFirst = false, isLast = false }) => {
    const Icon = entry.action.includes('enviado') ? Send :
                 entry.action.includes('aceptado') ? CheckCircle :
                 entry.action.includes('rechazado') ? XCircle :
                 entry.action.includes('firmado') ? FileText :
                 Clock;

    return (
        <div className="relative">
            {/* Connector line */}
            {!isLast && (
                <div className="absolute left-4 top-10 w-px h-full bg-border" />
            )}
            
            <div className="flex items-start gap-4">
                {/* Icon circle */}
                <div className="flex-shrink-0 w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center">
                    <Icon className="h-4 w-4 text-primary" />
                </div>
                
                {/* Content */}
                <div className="flex-1 min-w-0">
                    <p className="font-medium text-sm">{entry.action}</p>
                    {entry.detail && (
                        <p className="text-sm text-muted-foreground mt-1">{entry.detail}</p>
                    )}
                    <div className="flex items-center gap-3 mt-2 text-xs text-muted-foreground">
                        <span className="flex items-center gap-1">
                            {entry.user}
                        </span>
                        <span>{formatDate(entry.timestamp)}</span>
                    </div>
                </div>
            </div>
        </div>
    );
};

export const DTEStatusTracker: React.FC<DTEStatusTrackerProps> = ({ invoiceId, status }) => {
    const config = statusConfig[status as keyof typeof statusConfig] || statusConfig.DRAFT;
    const Icon = config.icon;

    // Fetch audit trail
    const { data: auditTrail, isLoading: isLoadingAudit } = useQuery({
        queryKey: ['invoice-audit-trail', invoiceId],
        queryFn: () => accountingService.getInvoiceAuditTrail(invoiceId),
        enabled: !!invoiceId,
    });

    return (
        <Card>
            <CardHeader>
                <CardTitle className="flex items-center gap-2">
                    <Icon className="h-5 w-5" />
                    Estado del Documento
                </CardTitle>
            </CardHeader>
            <CardContent>
                {/* Current Status Badge */}
                <div className="flex items-center gap-3 mb-6">
                    <Badge 
                        variant={config.variant}
                        className={cn(
                            "text-xs font-medium px-3 py-1",
                            "bg-opacity-10"
                        )}
                        style={{ 
                            backgroundColor: `${config.color}20`,
                            color: config.color,
                            borderColor: `${config.color}40`,
                        }}
                    >
                        {config.label}
                    </Badge>
                    
                    <p className="text-sm text-muted-foreground">
                        {config.description}
                    </p>
                </div>

                {/* Timeline Section */}
                <div>
                    <h4 className="text-sm font-medium mb-3 flex items-center gap-2">
                        <History className="h-4 w-4" />
                        Historial de Cambios
                    </h4>
                    
                    {isLoadingAudit ? (
                        <div className="space-y-3">
                            <Skeleton className="h-12 w-full" />
                            <Skeleton className="h-12 w-full" />
                        </div>
                    ) : auditTrail && auditTrail.length > 0 ? (
                        <div className="space-y-4">
                            {auditTrail.map((entry, index) => (
                                <TimelineItem
                                    key={entry.id}
                                    entry={entry}
                                    isFirst={index === 0}
                                    isLast={index === auditTrail.length - 1}
                                />
                            ))}
                        </div>
                    ) : (
                        <div className="text-center py-8 text-muted-foreground">
                            <p className="text-sm">No hay historial disponible</p>
                        </div>
                    )}
                </div>
            </CardContent>
        </Card>
    );
};

export default DTEStatusTracker;