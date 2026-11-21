import type { ReactNode } from 'react';

/**
 * Fondo del login/registro. Solo estilos: los formularios mantienen su lógica.
 */
export default function AuthLayout({ children }: { children: ReactNode }) {
    return (
        <div className="relative flex min-h-screen items-center justify-center bg-background p-4">
            <div
                aria-hidden="true"
                className="pointer-events-none absolute inset-0 bg-gradient-to-br from-primary-subtle/60 via-background to-background"
            />
            <div className="relative z-10 w-full max-w-md">
                {children}
                <p className="mt-6 text-center text-xs text-muted-foreground">
                    SII-ERP-AI · Contabilidad y tributario para pymes chilenas
                </p>
            </div>
        </div>
    );
}
