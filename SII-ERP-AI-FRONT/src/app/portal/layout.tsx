import React from 'react';

export default function PortalLayout({ children }: { children: React.ReactNode }) {
    return (
        <div className="min-h-screen bg-gray-50 flex flex-col">
            <header className="bg-white border-b h-16 flex items-center px-6 justify-between shadow-sm">
                <div className="font-bold text-xl text-primary">Acme Corp - Portal de Clientes</div>
                <div className="text-sm text-gray-500">Bienvenido, Cliente #774</div>
            </header>
            <main className="flex-1 p-6 max-w-7xl mx-auto w-full">
                {children}
            </main>
        </div>
    );
}