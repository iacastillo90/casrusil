'use client';

import { useState } from 'react';
import { Check, Plus, Send, ShoppingCart } from 'lucide-react';
import { toast } from 'sonner';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { PageHeader } from '@/features/accounting/components/PageHeader';
import { EmptyState, ErrorState, TableSkeleton } from '@/features/accounting/components/DataStates';
import { formatCLP, formatDate, formatRut } from '@/lib/formatters';
import {
    useCreatePurchaseOrder,
    useMarkAsReceived,
    useMarkAsSent,
    usePurchaseOrders,
} from '@/features/procurement/hooks/useProcurement';
import type { PurchaseOrder } from '@/features/procurement/services/procurement.service';

const STATUS_LABEL: Record<PurchaseOrder['status'], string> = {
    DRAFT: 'Borrador',
    SENT: 'Enviada',
    PARTIALLY_RECEIVED: 'Recepción parcial',
    RECEIVED: 'Recibida',
    INVOICED: 'Facturada',
    CANCELLED: 'Anulada',
};

const statusClass = (status: PurchaseOrder['status']) => {
    switch (status) {
        case 'RECEIVED':
        case 'INVOICED':
            return 'border-success-subtle-foreground/25 bg-success-subtle text-success-subtle-foreground';
        case 'SENT':
            return 'border-info-subtle-foreground/20 bg-info-subtle text-info-subtle-foreground';
        case 'CANCELLED':
            return 'border-destructive-subtle-foreground/25 bg-destructive-subtle text-destructive-subtle-foreground';
        default:
            return 'border-neutral-subtle-foreground/20 bg-neutral-subtle text-neutral-subtle-foreground';
    }
};

export default function ProcurementPage() {
    const [open, setOpen] = useState(false);
    const [form, setForm] = useState({
        supplierRut: '',
        supplierName: '',
        expectedDeliveryDate: '',
        description: '',
        quantity: '1',
        unitPrice: '',
    });

    const orders = usePurchaseOrders();
    const createOrder = useCreatePurchaseOrder();
    const markAsSent = useMarkAsSent();
    const markAsReceived = useMarkAsReceived();

    const resetForm = () =>
        setForm({
            supplierRut: '',
            supplierName: '',
            expectedDeliveryDate: '',
            description: '',
            quantity: '1',
            unitPrice: '',
        });

    const handleCreate = async (event: React.FormEvent) => {
        event.preventDefault();
        if (!form.supplierRut || !form.supplierName || !form.description) {
            toast.error('Completa proveedor, RUT y descripción de la línea');
            return;
        }
        try {
            await createOrder.mutateAsync({
                supplierRut: form.supplierRut,
                supplierName: form.supplierName,
                expectedDeliveryDate:
                    form.expectedDeliveryDate || new Date().toISOString().slice(0, 10),
                lines: [
                    {
                        productId: null,
                        description: form.description,
                        quantity: Number(form.quantity) || 1,
                        unitPrice: Number(form.unitPrice) || 0,
                    },
                ],
            });
            toast.success('Orden de compra creada');
            setOpen(false);
            resetForm();
        } catch (error) {
            toast.error('No se pudo crear la orden', {
                description: error instanceof Error ? error.message : undefined,
            });
        }
    };

    return (
        <div className="space-y-6">
            <PageHeader
                title="Adquisiciones"
                description="Órdenes de compra a proveedores y su ciclo hasta la recepción."
                actions={
                    <Button onClick={() => setOpen(true)}>
                        <Plus aria-hidden="true" className="mr-2 h-4 w-4" />
                        Nueva orden
                    </Button>
                }
            />

            {orders.error ? (
                <ErrorState
                    title="No se pudieron cargar las órdenes de compra"
                    message={
                        orders.error instanceof Error ? orders.error.message : 'Error desconocido'
                    }
                    onRetry={() => orders.refetch()}
                    retrying={orders.isFetching}
                />
            ) : orders.isLoading ? (
                <TableSkeleton rows={5} cols={6} />
            ) : !orders.data || orders.data.length === 0 ? (
                <EmptyState
                    icon={ShoppingCart}
                    title="No hay órdenes de compra"
                    description="Crea la primera orden con el botón «Nueva orden»: queda en borrador hasta que la envíes al proveedor."
                    action={
                        <Button variant="outline" onClick={() => setOpen(true)}>
                            <Plus aria-hidden="true" className="mr-2 h-4 w-4" />
                            Nueva orden
                        </Button>
                    }
                />
            ) : (
                <Card>
                    <CardHeader>
                        <CardTitle className="text-base font-semibold">
                            Órdenes de compra
                        </CardTitle>
                    </CardHeader>
                    <CardContent className="p-0">
                        <div className="overflow-x-auto">
                            <Table>
                                <TableHeader>
                                    <TableRow>
                                        <TableHead>Orden / fecha</TableHead>
                                        <TableHead>Proveedor</TableHead>
                                        <TableHead>Estado</TableHead>
                                        <TableHead className="text-right">Total</TableHead>
                                        <TableHead className="text-right">Acciones</TableHead>
                                    </TableRow>
                                </TableHeader>
                                <TableBody>
                                    {orders.data.map((order) => (
                                        <TableRow key={order.id}>
                                            <TableCell>
                                                <div className="font-mono text-xs">
                                                    {order.id.slice(0, 8).toUpperCase()}
                                                </div>
                                                <div className="text-xs text-muted-foreground">
                                                    {formatDate(order.orderDate)}
                                                </div>
                                            </TableCell>
                                            <TableCell>
                                                <div className="font-medium">{order.supplierName}</div>
                                                <div className="font-mono text-xs text-muted-foreground">
                                                    {formatRut(order.supplierRut)}
                                                </div>
                                            </TableCell>
                                            <TableCell>
                                                <Badge variant="outline" className={statusClass(order.status)}>
                                                    {STATUS_LABEL[order.status] ?? order.status}
                                                </Badge>
                                            </TableCell>
                                            <TableCell className="text-right font-mono tabular-nums">
                                                {formatCLP(Number(order.totalAmount ?? 0))}
                                            </TableCell>
                                            <TableCell className="text-right">
                                                {order.status === 'DRAFT' ? (
                                                    <Button
                                                        variant="outline"
                                                        size="sm"
                                                        onClick={() => markAsSent.mutate(order.id)}
                                                        loading={
                                                            markAsSent.isPending &&
                                                            markAsSent.variables === order.id
                                                        }
                                                    >
                                                        <Send aria-hidden="true" className="mr-1 h-3 w-3" />
                                                        Enviar
                                                    </Button>
                                                ) : order.status === 'SENT' ? (
                                                    <Button
                                                        size="sm"
                                                        onClick={() => markAsReceived.mutate(order.id)}
                                                        loading={
                                                            markAsReceived.isPending &&
                                                            markAsReceived.variables === order.id
                                                        }
                                                    >
                                                        <Check aria-hidden="true" className="mr-1 h-3 w-3" />
                                                        Recibir
                                                    </Button>
                                                ) : (
                                                    <span className="text-xs text-muted-foreground">—</span>
                                                )}
                                            </TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                        </div>
                    </CardContent>
                </Card>
            )}

            <Dialog open={open} onOpenChange={setOpen}>
                <DialogContent>
                    <form onSubmit={handleCreate} className="space-y-4">
                        <DialogHeader>
                            <DialogTitle>Nueva orden de compra</DialogTitle>
                            <DialogDescription>
                                La orden se crea en borrador; luego puedes enviarla al proveedor y
                                marcarla como recibida.
                            </DialogDescription>
                        </DialogHeader>

                        <div className="grid gap-3 sm:grid-cols-2">
                            <div className="space-y-1.5">
                                <Label htmlFor="supplier-rut">RUT proveedor</Label>
                                <Input
                                    id="supplier-rut"
                                    value={form.supplierRut}
                                    onChange={(event) =>
                                        setForm({ ...form, supplierRut: event.target.value })
                                    }
                                    placeholder="76.123.456-7"
                                    required
                                />
                            </div>
                            <div className="space-y-1.5">
                                <Label htmlFor="supplier-name">Razón social</Label>
                                <Input
                                    id="supplier-name"
                                    value={form.supplierName}
                                    onChange={(event) =>
                                        setForm({ ...form, supplierName: event.target.value })
                                    }
                                    placeholder="Proveedor SpA"
                                    required
                                />
                            </div>
                            <div className="space-y-1.5 sm:col-span-2">
                                <Label htmlFor="delivery-date">Fecha de entrega esperada</Label>
                                <Input
                                    id="delivery-date"
                                    type="date"
                                    value={form.expectedDeliveryDate}
                                    onChange={(event) =>
                                        setForm({
                                            ...form,
                                            expectedDeliveryDate: event.target.value,
                                        })
                                    }
                                />
                            </div>
                            <div className="space-y-1.5 sm:col-span-2">
                                <Label htmlFor="line-description">Descripción de la línea</Label>
                                <Input
                                    id="line-description"
                                    value={form.description}
                                    onChange={(event) =>
                                        setForm({ ...form, description: event.target.value })
                                    }
                                    placeholder="Insumos de oficina"
                                    required
                                />
                            </div>
                            <div className="space-y-1.5">
                                <Label htmlFor="line-quantity">Cantidad</Label>
                                <Input
                                    id="line-quantity"
                                    type="number"
                                    min="1"
                                    step="1"
                                    value={form.quantity}
                                    onChange={(event) =>
                                        setForm({ ...form, quantity: event.target.value })
                                    }
                                />
                            </div>
                            <div className="space-y-1.5">
                                <Label htmlFor="line-price">Precio unitario</Label>
                                <Input
                                    id="line-price"
                                    type="number"
                                    min="0"
                                    step="1"
                                    value={form.unitPrice}
                                    onChange={(event) =>
                                        setForm({ ...form, unitPrice: event.target.value })
                                    }
                                    placeholder="0"
                                />
                            </div>
                        </div>

                        <DialogFooter>
                            <Button
                                type="button"
                                variant="outline"
                                onClick={() => setOpen(false)}
                                disabled={createOrder.isPending}
                            >
                                Cancelar
                            </Button>
                            <Button type="submit" loading={createOrder.isPending}>
                                Crear orden
                            </Button>
                        </DialogFooter>
                    </form>
                </DialogContent>
            </Dialog>
        </div>
    );
}
