"use client";

import React, { useState } from 'react';
import { usePurchaseOrders, useMarkAsSent, useMarkAsReceived } from '@/features/procurement/hooks/useProcurement';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Plus, Check, Send } from 'lucide-react';
import { PurchaseOrder } from '@/features/procurement/services/procurement.service';

export default function ProcurementPage() {
    const { data: orders, isLoading } = usePurchaseOrders();
    const markAsSent = useMarkAsSent();
    const markAsReceived = useMarkAsReceived();

    if (isLoading) {
        return <div className="p-8 text-center text-gray-500">Cargando órdenes de compra...</div>;
    }

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <div>
                    <h2 className="text-2xl font-bold tracking-tight">Adquisiciones</h2>
                    <p className="text-muted-foreground">
                        Gestiona tus Órdenes de Compra y recepciones.
                    </p>
                </div>
                <Button>
                    <Plus className="mr-2 h-4 w-4" />
                    Nueva Orden
                </Button>
            </div>

            <Card>
                <CardHeader>
                    <CardTitle>Órdenes de Compra Recientes</CardTitle>
                </CardHeader>
                <CardContent>
                    {(!orders || orders.length === 0) ? (
                        <div className="text-center py-8 text-muted-foreground">
                            No hay órdenes de compra registradas.
                        </div>
                    ) : (
                        <div className="overflow-x-auto">
                            <table className="w-full text-sm text-left">
                                <thead className="text-xs text-gray-700 uppercase bg-gray-50 border-b">
                                    <tr>
                                        <th className="px-4 py-3">ID / Fecha</th>
                                        <th className="px-4 py-3">Proveedor</th>
                                        <th className="px-4 py-3">Estado</th>
                                        <th className="px-4 py-3 text-right">Total</th>
                                        <th className="px-4 py-3 text-center">Acciones</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {orders.map((order: PurchaseOrder) => (
                                        <tr key={order.id} className="border-b hover:bg-gray-50">
                                            <td className="px-4 py-3">
                                                <div className="font-medium text-gray-900">{order.id.split('-')[0].toUpperCase()}</div>
                                                <div className="text-xs text-gray-500">{order.orderDate}</div>
                                            </td>
                                            <td className="px-4 py-3">
                                                <div className="font-medium">{order.supplierName}</div>
                                                <div className="text-xs text-gray-500">{order.supplierRut}</div>
                                            </td>
                                            <td className="px-4 py-3">
                                                <span className={`px-2 py-1 rounded-full text-xs font-medium
                                                    ${order.status === 'DRAFT' ? 'bg-gray-100 text-gray-800' :
                                                      order.status === 'SENT' ? 'bg-blue-100 text-blue-800' :
                                                      order.status === 'RECEIVED' ? 'bg-green-100 text-green-800' :
                                                      'bg-gray-100 text-gray-800'}`}>
                                                    {order.status}
                                                </span>
                                            </td>
                                            <td className="px-4 py-3 text-right font-medium">
                                                ${order.totalAmount.toLocaleString('es-CL')}
                                            </td>
                                            <td className="px-4 py-3 text-center space-x-2">
                                                {order.status === 'DRAFT' && (
                                                    <Button 
                                                        variant="outline" 
                                                        size="sm"
                                                        onClick={() => markAsSent.mutate(order.id)}
                                                        disabled={markAsSent.isPending}
                                                    >
                                                        <Send className="mr-1 h-3 w-3" />
                                                        Enviar a Prov.
                                                    </Button>
                                                )}
                                                {order.status === 'SENT' && (
                                                    <Button 
                                                        variant="default" 
                                                        size="sm"
                                                        className="bg-green-600 hover:bg-green-700 text-white"
                                                        onClick={() => markAsReceived.mutate(order.id)}
                                                        disabled={markAsReceived.isPending}
                                                    >
                                                        <Check className="mr-1 h-3 w-3" />
                                                        Marcar Recibido
                                                    </Button>
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
