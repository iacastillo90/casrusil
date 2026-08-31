"use client";

import React from 'react';
import { useQuotes, useAcceptQuote, useRejectQuote } from '@/features/crm/hooks/useCrm';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Plus, Check, X } from 'lucide-react';
import { Quote } from '@/features/crm/services/crm.service';

export default function CrmQuotesPage() {
    const { data: quotes, isLoading } = useQuotes();
    const acceptQuote = useAcceptQuote();
    const rejectQuote = useRejectQuote();

    if (isLoading) {
        return <div className="p-8 text-center text-gray-500">Cargando presupuestos...</div>;
    }

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <div>
                    <h2 className="text-2xl font-bold tracking-tight">Cotizaciones</h2>
                    <p className="text-muted-foreground">Emite presupuestos y conviértelos en facturas automáticamente.</p>
                </div>
                <Button>
                    <Plus className="mr-2 h-4 w-4" />
                    Nueva Cotización
                </Button>
            </div>

            <Card>
                <CardHeader>
                    <CardTitle>Últimas Cotizaciones</CardTitle>
                </CardHeader>
                <CardContent>
                    {(!quotes || quotes.length === 0) ? (
                        <div className="text-center py-8 text-muted-foreground">
                            No hay cotizaciones registradas.
                        </div>
                    ) : (
                        <div className="overflow-x-auto">
                            <table className="w-full text-sm text-left">
                                <thead className="text-xs text-gray-700 uppercase bg-gray-50 border-b">
                                    <tr>
                                        <th className="px-4 py-3">ID / Fecha</th>
                                        <th className="px-4 py-3">Cliente</th>
                                        <th className="px-4 py-3">Estado</th>
                                        <th className="px-4 py-3 text-right">Total</th>
                                        <th className="px-4 py-3 text-center">Acciones</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {quotes.map((quote: Quote) => (
                                        <tr key={quote.id} className="border-b hover:bg-gray-50">
                                            <td className="px-4 py-3">
                                                <div className="font-medium text-gray-900">{quote.id.split('-')[0].toUpperCase()}</div>
                                                <div className="text-xs text-gray-500">{quote.quoteDate}</div>
                                            </td>
                                            <td className="px-4 py-3">
                                                <div className="font-medium">{quote.clientName}</div>
                                                <div className="text-xs text-gray-500">{quote.clientRut}</div>
                                            </td>
                                            <td className="px-4 py-3">
                                                <span className={`px-2 py-1 rounded-full text-xs font-medium
                                                    ${quote.status === 'DRAFT' ? 'bg-gray-100 text-gray-800' :
                                                      quote.status === 'ACCEPTED' ? 'bg-green-100 text-green-800' :
                                                      quote.status === 'REJECTED' ? 'bg-red-100 text-red-800' :
                                                      'bg-gray-100 text-gray-800'}`}>
                                                    {quote.status}
                                                </span>
                                            </td>
                                            <td className="px-4 py-3 text-right font-medium">
                                                ${quote.totalAmount.toLocaleString('es-CL')}
                                            </td>
                                            <td className="px-4 py-3 text-center space-x-2">
                                                {quote.status === 'DRAFT' && (
                                                    <>
                                                        <Button 
                                                            variant="default" 
                                                            size="sm"
                                                            className="bg-green-600 hover:bg-green-700 text-white"
                                                            onClick={() => acceptQuote.mutate(quote.id)}
                                                            disabled={acceptQuote.isPending}
                                                        >
                                                            <Check className="mr-1 h-3 w-3" />
                                                            Aprobar y Facturar
                                                        </Button>
                                                        <Button 
                                                            variant="destructive" 
                                                            size="sm"
                                                            onClick={() => rejectQuote.mutate(quote.id)}
                                                            disabled={rejectQuote.isPending}
                                                        >
                                                            <X className="mr-1 h-3 w-3" />
                                                            Rechazar
                                                        </Button>
                                                    </>
                                                )}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </CardContent>
            </Card>
        </div>
    );
}
