"use client";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { ShoppingCart, CreditCard, Clock } from "lucide-react";
import { useCustomerOrders, usePayOrder } from "@/features/b2b/hooks/useB2b";
import { formatCLP } from "@/lib/utils/currency";
import { toast } from "sonner";

export default function PortalDashboard() {
    const customerId = "CUST-774"; // Mocked customer ID
    const { data: orders, isLoading } = useCustomerOrders(customerId);
    const { mutate: payOrder } = usePayOrder(customerId);

    const handlePay = (id: string) => {
        payOrder(id, {
            onSuccess: () => toast.success("Pago procesado correctamente.")
        });
    };

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between">
                <h1 className="text-3xl font-bold tracking-tight text-gray-900">Mis Pedidos</h1>
                <Button><ShoppingCart className="mr-2 h-4 w-4" /> Nuevo Pedido</Button>
            </div>

            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                {isLoading ? <p>Cargando tus pedidos...</p> : orders?.length === 0 ? <p>No has realizado pedidos aún.</p> : orders?.map((order: any) => (
                    <Card key={order.id} className="shadow-sm">
                        <CardHeader className="pb-2 bg-slate-50 border-b flex flex-row items-center justify-between">
                            <CardTitle className="text-sm">Pedido #{order.id.split('-')[0]}</CardTitle>
                            <Badge variant={order.status === 'PAID' ? 'default' : 'destructive'}>{order.status}</Badge>
                        </CardHeader>
                        <CardContent className="pt-4 space-y-4">
                            <div className="text-3xl font-bold">{formatCLP(order.totalAmount)}</div>
                            <div className="text-sm text-gray-500">
                                {order.items.length} productos • {order.createdAt}
                            </div>

                            <div className="border-t pt-4">
                                {order.status === 'PENDING' ? (
                                    <Button className="w-full" onClick={() => handlePay(order.id)}>
                                        <CreditCard className="mr-2 h-4 w-4" /> Pagar ahora
                                    </Button>
                                ) : (
                                    <Button className="w-full" variant="outline" disabled>
                                        <Clock className="mr-2 h-4 w-4" /> Pagado
                                    </Button>
                                )}
                            </div>
                        </CardContent>
                    </Card>
                ))}
            </div>
        </div>
    );
}