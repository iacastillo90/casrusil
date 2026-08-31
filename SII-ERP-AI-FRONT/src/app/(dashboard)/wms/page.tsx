"use client";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Package, MapPin, CheckSquare, ScanBarcode } from "lucide-react";
import { useLocations, usePickingTasks, useCompletePickingTask } from "@/features/wms/hooks/useWms";
import { toast } from "sonner";

export default function WmsPage() {
    const { data: locations, isLoading: loadingLocations } = useLocations();
    const { data: tasks, isLoading: loadingTasks } = usePickingTasks();
    const { mutate: completeTask } = useCompletePickingTask();

    const handleComplete = (id: string) => {
        completeTask(id, {
            onSuccess: () => toast.success("Picking finalizado exitosamente.")
        });
    };

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Logística y Bodega (WMS)</h1>
                    <p className="text-muted-foreground mt-1">Gestión avanzada de ubicaciones y picking.</p>
                </div>
                <Button variant="outline"><ScanBarcode className="mr-2 h-4 w-4" /> Escanear QR</Button>
            </div>

            <Tabs defaultValue="picking" className="space-y-4">
                <TabsList>
                    <TabsTrigger value="picking">Tareas de Picking</TabsTrigger>
                    <TabsTrigger value="locations">Ubicaciones</TabsTrigger>
                </TabsList>

                <TabsContent value="picking" className="space-y-4">
                    <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                        {loadingTasks ? <p>Cargando tareas...</p> : tasks?.length === 0 ? <p>No hay tareas de picking activas.</p> : tasks?.map((task: any) => (
                            <Card key={task.id} className={task.status === 'COMPLETED' ? 'opacity-60' : ''}>
                                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                                    <CardTitle className="text-sm font-medium">Pedido: {task.orderId}</CardTitle>
                                    <Package className="h-4 w-4 text-muted-foreground" />
                                </CardHeader>
                                <CardContent>
                                    <div className="space-y-3 mt-2">
                                        {task.items.map((item:any, idx:number) => (
                                            <div key={idx} className="flex items-center justify-between border-b pb-2">
                                                <div>
                                                    <p className="text-sm font-medium">{item.productId}</p>
                                                    <p className="text-xs text-muted-foreground flex items-center">
                                                        <MapPin className="h-3 w-3 mr-1" /> {item.locationCode}
                                                    </p>
                                                </div>
                                                <Badge variant="secondary">x{item.quantity}</Badge>
                                            </div>
                                        ))}
                                    </div>
                                    <div className="flex justify-between items-center mt-4">
                                        <Badge variant={task.status === 'COMPLETED' ? 'default' : 'outline'}>{task.status}</Badge>
                                        {task.status !== 'COMPLETED' && (
                                            <Button size="sm" onClick={() => handleComplete(task.id)}>
                                                <CheckSquare className="mr-2 h-4 w-4" /> Finalizar Picking
                                            </Button>
                                        )}
                                    </div>
                                </CardContent>
                            </Card>
                        ))}
                    </div>
                </TabsContent>

                <TabsContent value="locations" className="space-y-4">
                     <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
                        {loadingLocations ? <p>Cargando ubicaciones...</p> : locations?.length === 0 ? <p>No hay ubicaciones registradas.</p> : locations?.map((loc: any) => (
                            <Card key={loc.id}>
                                <CardHeader className="pb-2 bg-slate-50 border-b">
                                    <CardTitle className="text-lg flex justify-between">
                                        {loc.code}
                                        <Badge variant="outline">{loc.zone}</Badge>
                                    </CardTitle>
                                </CardHeader>
                                <CardContent className="pt-4">
                                    <p className="text-sm text-muted-foreground">{loc.description}</p>
                                </CardContent>
                            </Card>
                        ))}
                    </div>
                </TabsContent>
            </Tabs>
        </div>
    );
}