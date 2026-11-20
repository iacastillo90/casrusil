import type { ReactNode } from 'react';
import { cn } from '@/lib/utils';

interface PageHeaderProps {
    /** Título principal de la pantalla (h1). */
    title: string;
    /** Descripción breve, en lenguaje del contador. */
    description?: ReactNode;
    /** Acciones principales, alineadas a la derecha en desktop. */
    actions?: ReactNode;
    className?: string;
}

/**
 * Encabezado de pantalla compartido por Contabilidad y Tributario:
 * misma jerarquía tipográfica y misma posición de las acciones.
 */
export function PageHeader({ title, description, actions, className }: PageHeaderProps) {
    return (
        <div
            className={cn(
                'flex flex-col gap-4 md:flex-row md:items-start md:justify-between',
                className,
            )}
        >
            <div className="space-y-1">
                <h1 className="text-2xl font-semibold tracking-tight md:text-3xl">{title}</h1>
                {description ? (
                    <p className="max-w-3xl text-sm text-muted-foreground">{description}</p>
                ) : null}
            </div>
            {actions ? (
                <div className="flex shrink-0 flex-wrap items-center gap-2">{actions}</div>
            ) : null}
        </div>
    );
}
