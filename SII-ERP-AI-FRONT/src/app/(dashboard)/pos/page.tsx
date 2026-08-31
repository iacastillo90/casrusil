"use client";

import React, { useState } from 'react';
import { usePosTransactions, useRegisterSale, useSyncTransactions } from '@/features/pos/hooks/usePos';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { MonitorPlay, ShoppingBag, CreditCard, Banknote, RefreshCw } from 'lucide-react';
import { PosTransactionItem } from '@/features/pos/services/pos.service';

export default function PosPage() {
    const { data: transactions, isLoading } = usePosTransactions();
    const registerSale = useRegisterSale();
    const syncTransactions = useSyncTransactions();

    const [cart, setCart] = useState<PosTransactionItem[]>([]);
    const [barcodeInput, setBarcodeInput] = useState('');

    const handleBarcodeSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (!barcodeInput) return;

        // Mock product lookup
        const newItem: PosTransactionItem = {
            productId: barcodeInput,
            productName: `Producto ${barcodeInput}`,
            quantity: 1,
            unitPrice: Math.floor(Math.random() * 5000) + 1000
        };

        setCart(prev => [...prev, newItem]);
        setBarcodeInput('');
    };

    const cartTotal = cart.reduce((acc, item) => acc + (item.quantity * item.unitPrice), 0);

    const handlePayment = (method: string) => {
        if (cart.length === 0) return;
        
        registerSale.mutate({
            posTerminalId: 'CAJA-01',
            cashierName: 'Cajero Principal',
            paymentMethod: method,
            items: cart
        }, {
            onSuccess: () => setCart([])
        });
    };

    if (isLoading) {
        return <div className="p-8 text-center text-gray-500">Cargando terminal POS...</div>;
    }

    return (
        <div className="flex h-[calc(100vh-8rem)] gap-6">
            {/* PRODUCT SCANNER & CATALOG */}
            <div className="flex-1 flex flex-col space-y-4">
                <Card className="flex-1 flex flex-col">
                    <CardHeader className="bg-gray-50 border-b pb-4">
                        <CardTitle className="flex items-center text-lg">
                            <MonitorPlay className="mr-2 h-5 w-5 text-blue-600" />
                            Terminal de Venta (CAJA-01)
                        </CardTitle>
                    </CardHeader>
                    <CardContent className="flex-1 flex flex-col pt-4">
                        <form onSubmit={handleBarcodeSubmit} className="mb-6 flex gap-2">
                            <Input 
                                autoFocus
                                placeholder="Escanear código de barras o ingresar SKU..."
                                value={barcodeInput}
                                onChange={e => setBarcodeInput(e.target.value)}
                                className="text-lg"
                            />
                            <Button type="submit" size="lg">Agregar</Button>
                        </form>
                        
                        <div className="grid grid-cols-3 gap-4">
                            {/* Mock Quick Items */}
                            {['Bebida', 'Snack', 'Pan', 'Lácteo'].map(cat => (
                                <Button 
                                    key={cat} 
                                    variant="outline" 
                                    className="h-24 flex flex-col items-center justify-center text-lg"
                                    onClick={() => handleBarcodeSubmit({ preventDefault: () => {} } as any)}
                                >
                                    <ShoppingBag className="h-6 w-6 mb-2 text-gray-400" />
                                    {cat}
                                </Button>
                            ))}
                        </div>
                    </CardContent>
                </Card>
            </div>

            {/* CART & PAYMENT */}
            <div className="w-[400px] flex flex-col space-y-4">
                <Card className="flex-1 flex flex-col">
                    <CardHeader className="bg-gray-50 border-b pb-4">
                        <CardTitle className="text-lg">Boleta Actual</CardTitle>
                    </CardHeader>
                    <CardContent className="flex-1 overflow-y-auto p-4">
                        {cart.length === 0 ? (
                            <div className="h-full flex items-center justify-center text-gray-400 text-sm">
                                Carrito vacío
                            </div>
                        ) : (
                            <ul className="space-y-3">
                                {cart.map((item, idx) => (
                                    <li key={idx} className="flex justify-between items-center text-sm">
                                        <div>
                                            <p className="font-medium">{item.productName}</p>
                                            <p className="text-gray-500">{item.quantity} x ${item.unitPrice.toLocaleString('es-CL')}</p>
                                        </div>
                                        <p className="font-semibold text-gray-900">${(item.quantity * item.unitPrice).toLocaleString('es-CL')}</p>
                                    </li>
                                ))}
                            </ul>
                        )}
                    </CardContent>
                    <div className="bg-gray-100 p-4 border-t space-y-4">
                        <div className="flex justify-between items-center text-xl font-bold">
                            <span>Total</span>
                            <span>${cartTotal.toLocaleString('es-CL')}</span>
                        </div>
                        <div className="grid grid-cols-2 gap-2">
                            <Button 
                                size="lg" 
                                className="bg-green-600 hover:bg-green-700 h-16 text-lg"
                                disabled={cart.length === 0}
                                onClick={() => handlePayment('CASH')}
                            >
                                <Banknote className="mr-2 h-5 w-5" /> Efectivo
                            </Button>
                            <Button 
                                size="lg" 
                                className="bg-blue-600 hover:bg-blue-700 h-16 text-lg"
                                disabled={cart.length === 0}
                                onClick={() => handlePayment('CARD')}
                            >
                                <CreditCard className="mr-2 h-5 w-5" /> Tarjeta
                            </Button>
                        </div>
                    </div>
                </Card>
            </div>
        </div>
    );
}
