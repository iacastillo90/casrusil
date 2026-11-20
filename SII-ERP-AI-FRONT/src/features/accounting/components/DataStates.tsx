'use client';

import type { LucideIcon } from 'lucide-react';
import { AlertTriangle, Inbox, RefreshCw } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { cn } from '@/lib/utils';

interface ErrorStateProps {
    title?: string;
    message: string;
    onRetry?: () => void;
    retrying?: boolean;
    className?: string;
}

/** Estado de error de una pantalla o tarjeta, con reintento explícito. */
export function ErrorState({
    title = 'No se pudieron cargar los datos',
    message,
    onRetry,
    retrying = false,
    className,
}: ErrorStateProps) {
    return (
        <Card className={cn('border-destructive/30', className)}>
            <CardContent className="flex flex-col items-start gap-3 py-8">
                <div className="flex items-center gap-2 text-destructive">
                    <AlertTriangle aria-hidden="true" className="h-5 w-5" />
                    <p className="font-medium">{title}</p>
                </div>
                <p className="text-sm text-muted-foreground">{message}</p>
                {onRetry ? (
                    <Button variant="outline" size="sm" onClick={onRetry} loading={retrying}>
                        {!retrying && <RefreshCw aria-hidden="true" className="mr-2 h-4 w-4" />}
                        Reintentar
                    </Button>
                ) : null}
            </CardContent>
        </Card>
    );
}

interface EmptyStateProps {
    title: string;
    description?: string;
    icon?: LucideIcon;
    action?: React.ReactNode;
    className?: string;
}

/** Estado vacío honesto: dice por qué no hay datos o qué hacer. */
export function EmptyState({
    title,
    description,
    icon: Icon = Inbox,
    action,
    className,
}: EmptyStateProps) {
    return (
        <div
            className={cn(
                'flex flex-col items-center justify-center gap-2 px-6 py-12 text-center',
                className,
            )}
        >
            <Icon aria-hidden="true" className="h-8 w-8 text-muted-foreground/60" />
            <p className="font-medium">{title}</p>
            {description ? (
                <p className="max-w-md text-sm text-muted-foreground">{description}</p>
            ) : null}
            {action ? <div className="mt-2">{action}</div> : null}
        </div>
    );
}

interface TableSkeletonProps {
    rows?: number;
    cols?: number;
    className?: string;
}

/** Placeholder de tabla con la misma estructura de columnas. */
export function TableSkeleton({ rows = 6, cols = 5, className }: TableSkeletonProps) {
    return (
        <div className={cn('space-y-2', className)} role="status" aria-label="Cargando datos">
            {Array.from({ length: rows }).map((_, rowIndex) => (
                <div key={rowIndex} className="grid gap-3" style={{ gridTemplateColumns: `repeat(${cols}, minmax(0, 1fr))` }}>
                    {Array.from({ length: cols }).map((__, colIndex) => (
                        <Skeleton key={colIndex} className="h-9 w-full" />
                    ))}
                </div>
            ))}
        </div>
    );
}
