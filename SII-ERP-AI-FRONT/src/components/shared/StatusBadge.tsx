import {
    AlertTriangle,
    Ban,
    CheckCircle2,
    FileSignature,
    FileText,
    Send,
    XCircle,
    type LucideIcon,
} from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import { cn } from '@/lib/utils';

/**
 * Estados del ciclo de vida de un DTE.
 * El mapa vive acá (y no en `lib/constants`) para no acoplar el badge a un
 * subconjunto de estados: el backend puede enviar estados nuevos sin romper tipos.
 */
type DteStatus =
    | 'DRAFT'
    | 'SIGNED'
    | 'SENT'
    | 'ACCEPTED'
    | 'ACCEPTED_WITH_OBJECTIONS'
    | 'REJECTED'
    | 'CANCELLED';

interface StatusConfig {
    label: string;
    icon: LucideIcon;
    className: string;
}

const STATUS_CONFIG: Record<DteStatus, StatusConfig> = {
    DRAFT: {
        label: 'Borrador',
        icon: FileText,
        className: 'border-neutral-subtle-foreground/20 bg-neutral-subtle text-neutral-subtle-foreground',
    },
    SIGNED: {
        label: 'Firmado',
        icon: FileSignature,
        className: 'border-signed-subtle-foreground/25 bg-signed-subtle text-signed-subtle-foreground',
    },
    SENT: {
        label: 'Enviado al SII',
        icon: Send,
        className: 'border-info-subtle-foreground/25 bg-info-subtle text-info-subtle-foreground',
    },
    ACCEPTED: {
        label: 'Aceptado',
        icon: CheckCircle2,
        className: 'border-success-subtle-foreground/25 bg-success-subtle text-success-subtle-foreground',
    },
    ACCEPTED_WITH_OBJECTIONS: {
        label: 'Aceptado con reparos',
        icon: AlertTriangle,
        className: 'border-warning-subtle-foreground/25 bg-warning-subtle text-warning-subtle-foreground',
    },
    REJECTED: {
        label: 'Rechazado',
        icon: XCircle,
        className: 'border-destructive-subtle-foreground/25 bg-destructive-subtle text-destructive-subtle-foreground',
    },
    CANCELLED: {
        label: 'Anulado',
        icon: Ban,
        className: 'border-neutral-subtle-foreground/20 bg-neutral-subtle text-neutral-subtle-foreground',
    },
};

interface StatusBadgeProps {
    status: string;
    className?: string;
}

export function StatusBadge({ status, className }: StatusBadgeProps) {
    const key = typeof status === 'string' ? status.trim().toUpperCase() : '';
    const config = STATUS_CONFIG[key as DteStatus];

    // Un estado desconocido se muestra tal cual llega, sin inventar semántica.
    if (!config) {
        return (
            <Badge variant="outline" className={cn('whitespace-nowrap', className)}>
                {status}
            </Badge>
        );
    }

    const Icon = config.icon;

    return (
        <Badge
            variant="outline"
            className={cn('gap-1 whitespace-nowrap font-medium', config.className, className)}
        >
            <Icon aria-hidden="true" className="h-3.5 w-3.5 shrink-0" />
            {config.label}
        </Badge>
    );
}
