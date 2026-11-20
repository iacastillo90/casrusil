"use client";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Play, CheckCircle, Settings, ClipboardList } from "lucide-react";
import { useBoms, useWorkOrders, useCompleteWorkOrder } from "@/features/mrp/hooks/useMrp";
import { toast } from "sonner";

export default function MrpPage() {
    const { data: boms, isLoading: loadingBoms } = useBoms();
    const { data: orders, isLoading: loadingOrders } = useWorkOrders();
    const { mutate: completeOrder } = useCompleteWorkOrder();

    const handleComplete = (id: string) => {
        completeOrder(id, {
            onSuccess: () => toast.success("Órden completada con éxito.")
        });
    };

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Producción (MRP)</h1>
                    <p className="text-muted-foreground mt-1">Gestión de recetas y órdenes de trabajo.</p>
                </div>
            </div>

            <Tabs defaultValue="orders" className="space-y-4">
                <TabsList>
                    <TabsTrigger value="orders">Órdenes de Trabajo</TabsTrigger>
                    <TabsTrigger value="boms">Recetas (BOM)</TabsTrigger>
                </TabsList>

                <TabsContent value="orders" className="space-y-4">
                    <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                        {loadingOrders ? <p>Cargando órdenes...</p> : orders?.length === 0 ? <p>No hay órdenes activas.</p> : orders?.map((order) => (
                            <Card key={order.id}>
                                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                                    <CardTitle className="text-sm font-medium">OT: {order.id.split('-')[0]}</CardTitle>
                                    <ClipboardList className="h-4 w-4 text-muted-foreground" />
                                </CardHeader>
                                <CardContent>
                                    <div className="text-2xl font-bold">{order.targetQuantity} unid.</div>
                                    <p className="text-xs text-muted-foreground mb-4">Producto: {boms?.find((b)=>b.id===order.bomId)?.name || 'Desconocido'}</p>
                                    
                                    <div className="flex justify-between items-center">
                                        <Badge variant={order.status === 'COMPLETED' ? 'default' : 'outline'}>
                                            {order.status}
                                        </Badge>
                                        
                                        {order.status !== 'COMPLETED' && (
                                            <Button size="sm" onClick={() => handleComplete(order.id)}>
                                                <CheckCircle className="mr-2 h-4 w-4" /> Completar
                                            </Button>
                                        )}
                                    </div>
                                </CardContent>
                            </Card>
                        ))}
                    </div>
                </TabsContent>

                <TabsContent value="boms" className="space-y-4">
                     <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                        {loadingBoms ? <p>Cargando recetas...</p> : boms?.length === 0 ? <p>No hay recetas creadas.</p> : boms?.map((bom) => (
                            <Card key={bom.id}>
                                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                                    <CardTitle className="text-sm font-medium">{bom.name}</CardTitle>
                                    <Settings className="h-4 w-4 text-muted-foreground" />
                                </CardHeader>
                                <CardContent>
                                    <div className="mt-2 space-y-2">
                                        <p className="text-xs font-semibold uppercase text-gray-500">Componentes:</p>
                                        <ul className="text-sm space-y-1">
                                            {bom.items.map((item, idx) => (
                                                <li key={idx} className="flex justify-between border-b pb-1">
                                                    <span>{item.componentProductId}</span>
                                                    <span className="font-medium">{item.quantity}</span>
                                                </li>
                                            ))}
                                        </ul>
                                    </div>
                                </CardContent>
                            </Card>
                        ))}
                    </div>
                </TabsContent>
            </Tabs>
        </div>
    );
}