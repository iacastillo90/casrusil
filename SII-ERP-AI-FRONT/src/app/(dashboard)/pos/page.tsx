"use client";

import { useState } from "react";
import { Banknote, CreditCard, MonitorPlay, ShoppingBag } from "lucide-react";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { PageHeader } from "@/features/accounting/components/PageHeader";
import { EmptyState } from "@/features/accounting/components/DataStates";
import { useAuthStore } from "@/features/auth/stores/auth.store";
import { usePosTransactions, useRegisterSale } from "@/features/pos/hooks/usePos";
import { posService, type PosTransactionItem } from "@/features/pos/services/pos.service";
import { formatCLP } from "@/lib/formatters";

const TERMINAL_ID = process.env.NEXT_PUBLIC_POS_TERMINAL_ID ?? "CAJA-01";

export default function PosPage() {
    const { isError: transactionsError } = usePosTransactions();
    const registerSale = useRegisterSale();
    const cashierName = useAuthStore((state) => state.user?.name ?? "");

    const [cart, setCart] = useState<PosTransactionItem[]>([]);
    const [barcodeInput, setBarcodeInput] = useState("");
    const [searching, setSearching] = useState(false);

    const handleBarcodeSubmit = async (event: React.FormEvent) => {
        event.preventDefault();
        const query = barcodeInput.trim();
        if (!query) return;

        setSearching(true);
        try {
            const product = await posService.searchProduct(query);
            setCart((previous) => {
                const existing = previous.find((item) => item.productId === product.productId);
                if (existing) {
                    return previous.map((item) =>
                        item.productId === product.productId
                            ? { ...item, quantity: item.quantity + 1 }
                            : item,
                    );
                }
                return [...previous, product];
            });
            setBarcodeInput("");
        } catch (error) {
            const status = (error as { response?: { status?: number } })?.response?.status;
            toast.error(
                status === 404
                    ? `No existe un producto con el código "${query}"`
                    : "No se pudo buscar el producto en el inventario",
            );
        } finally {
            setSearching(false);
        }
    };

    const handlePayment = (method: "CASH" | "CARD") => {
        if (cart.length === 0) return;
        if (!cashierName) {
            toast.error("No se pudo identificar al cajero autenticado");
            return;
        }

        registerSale.mutate(
            {
                posTerminalId: TERMINAL_ID,
                cashierName,
                paymentMethod: method,
                items: cart,
            },
            {
                onSuccess: () => {
                    toast.success(`Venta registrada en ${TERMINAL_ID}`);
                    setCart([]);
                },
                onError: () => toast.error("No se pudo registrar la venta"),
            },
        );
    };

    const cartTotal = cart.reduce((acc, item) => acc + item.quantity * item.unitPrice, 0);

    return (
        <div className="space-y-6">
            <PageHeader
                title="Punto de venta"
                description={`Terminal ${TERMINAL_ID} · Cajero: ${cashierName || "sin sesión"}. La búsqueda usa el inventario real.`}
            />

            {transactionsError && (
                <div className="rounded-lg border border-destructive/30 bg-destructive-subtle px-4 py-3 text-sm text-destructive-subtle-foreground">
                    No se pudieron cargar las ventas anteriores; el terminal sigue operativo.
                </div>
            )}

            <div className="grid gap-6 lg:grid-cols-[1.2fr_1fr]">
                <Card>
                    <CardHeader className="border-b pb-4">
                        <CardTitle className="flex items-center text-lg">
                            <MonitorPlay className="mr-2 h-5 w-5 text-primary" aria-hidden="true" />
                            Escanear producto
                        </CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-4 pt-4">
                        <form onSubmit={handleBarcodeSubmit} className="flex gap-2">
                            <Input
                                placeholder="Código de barras o SKU…"
                                value={barcodeInput}
                                onChange={(event) => setBarcodeInput(event.target.value)}
                                autoFocus
                            />
                            <Button type="submit" loading={searching}>
                                Agregar
                            </Button>
                        </form>
                        <p className="text-xs text-muted-foreground">
                            La búsqueda consulta <code>GET /pos/products/search</code> sobre los productos del inventario.
                            Si el código no existe, se muestra el error (no se simula).
                        </p>
                    </CardContent>
                </Card>

                <Card className="flex flex-col">
                    <CardHeader className="border-b pb-4">
                        <CardTitle className="flex items-center text-lg">
                            <ShoppingBag className="mr-2 h-5 w-5" aria-hidden="true" />
                            Carrito ({cart.length})
                        </CardTitle>
                    </CardHeader>
                    <CardContent className="flex-1 space-y-3 pt-4">
                        {cart.length === 0 ? (
                            <EmptyState
                                title="Carrito vacío"
                                description="Escanea o ingresa un SKU para comenzar la venta."
                                icon={ShoppingBag}
                            />
                        ) : (
                            <ul className="space-y-3">
                                {cart.map((item) => (
                                    <li key={item.productId} className="flex items-center justify-between text-sm">
                                        <div>
                                            <p className="font-medium">{item.productName}</p>
                                            <p className="text-muted-foreground">
                                                {item.quantity} × {formatCLP(item.unitPrice)}
                                            </p>
                                        </div>
                                        <p className="font-semibold">{formatCLP(item.quantity * item.unitPrice)}</p>
                                    </li>
                                ))}
                            </ul>
                        )}
                    </CardContent>
                    <div className="space-y-4 border-t bg-muted/30 p-4">
                        <div className="flex items-center justify-between text-xl font-semibold">
                            <span>Total</span>
                            <span>{formatCLP(cartTotal)}</span>
                        </div>
                        <div className="grid grid-cols-2 gap-2">
                            <Button
                                size="lg"
                                className="h-16 text-lg"
                                disabled={cart.length === 0}
                                loading={registerSale.isPending}
                                onClick={() => handlePayment("CASH")}
                            >
                                <Banknote className="mr-2 h-5 w-5" /> Efectivo
                            </Button>
                            <Button
                                size="lg"
                                variant="secondary"
                                className="h-16 text-lg"
                                disabled={cart.length === 0}
                                loading={registerSale.isPending}
                                onClick={() => handlePayment("CARD")}
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
