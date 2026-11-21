'use client';

import React from 'react';
import { LogOut } from 'lucide-react';
import { useAuthStore } from '@/features/auth/stores/auth.store';
import { Button } from '@/components/ui/button';

/**
 * Layout del portal B2B: identifica a la empresa y al usuario autenticado
 * (antes mostraba "Acme Corp" y "Cliente #774" fijos).
 */
export default function PortalLayout({ children }: { children: React.ReactNode }) {
    const user = useAuthStore((state) => state.user);
    const companyId = useAuthStore((state) => state.companyId);
    const companies = useAuthStore((state) => state.companies);
    const logout = useAuthStore((state) => state.logout);

    const company = companies.find((item) => item.id === companyId);

    return (
        <div className="flex min-h-screen flex-col bg-background">
            <header className="flex h-16 items-center justify-between border-b bg-card px-6">
                <div className="flex items-baseline gap-3">
                    <span className="text-lg font-semibold text-primary">
                        {company?.razonSocial ?? 'Portal de clientes'}
                    </span>
                    <span className="text-sm text-muted-foreground">Pedidos B2B</span>
                </div>
                <div className="flex items-center gap-3">
                    <span className="text-sm text-muted-foreground">
                        {user?.name ?? user?.email ?? 'Cliente'}
                    </span>
                    <Button variant="outline" size="sm" onClick={logout}>
                        <LogOut aria-hidden="true" className="mr-2 h-4 w-4" />
                        Salir
                    </Button>
                </div>
            </header>
            <main className="mx-auto w-full max-w-7xl flex-1 p-6">{children}</main>
        </div>
    );
}
