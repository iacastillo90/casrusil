'use client';

import { CreditCard, ClipboardList, Clock } from 'lucide-react';
import { toast } from 'sonner';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { PageHeader } from '@/features/accounting/components/PageHeader';
import { EmptyState, ErrorState, TableSkeleton } from '@/features/accounting/components/DataStates';
import { useCustomerOrders, usePayOrder } from '@/features/b2b/hooks/useB2b';
import { NewOrderModal } from '@/features/b2b/components/NewOrderModal';
import type { B2bOrder } from '@/features/b2b/services/b2b.service';
import { formatCLP, formatDate } from '@/lib/formatters';

const STATUS_LABEL: Record<B2bOrder['status'], string> = {
    PENDING: 'Pendiente de pago',
    PAID: 'Pagado',
    CANCELLED: 'Anulado',
};

const statusClass = (status: B2bOrder['status']) => {
    if (status === 'PAID') {
        return 'border-success-subtle-foreground/25 bg-success-subtle text-success-subtle-foreground';
    }
    if (status === 'CANCELLED') {
        return 'border-destructive-subtle-foreground/25 bg-destructive-subtle text-destructive-subtle-foreground';
    }
    return 'border-warning-subtle-foreground/30 bg-warning-subtle text-warning-subtle-foreground';
};

export default function PortalDashboard() {
    const { data: orders, isLoading, isError, error, refetch, isFetching } = useCustomerOrders();
    const payOrder = usePayOrder();

    const handlePay = (id: string) => {
        payOrder.mutate(id, {
            onSuccess: () => toast.success('Pago procesado'),
            onError: (mutationError) =>
                toast.error('No se pudo procesar el pago', {
                    description:
                        mutationError instanceof Error ? mutationError.message : undefined,
                }),
        });
    };

    return (
        <div className="space-y-6">
            <PageHeader
                title="Mis pedidos"
                description="Pedidos realizados a la empresa y su estado de pago."
                actions={<NewOrderModal />}
            />

            {isError ? (
                <ErrorState
                    message={error instanceof Error ? error.message : 'Error desconocido'}
                    onRetry={() => refetch()}
                    retrying={isFetching}
                />
            ) : isLoading ? (
                <TableSkeleton rows={2} cols={3} />
            ) : !orders || orders.length === 0 ? (
                <EmptyState
                    icon={ClipboardList}
                    title="Todavía no hay pedidos"
                    description="Crea tu primer pedido con el botón «Nuevo pedido»."
                />
            ) : (
                <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                    {orders.map((order) => (
                        <Card key={order.id}>
                            <CardHeader className="flex flex-row items-center justify-between gap-3 pb-2">
                                <CardTitle className="text-sm font-semibold">
                                    Pedido #{order.id.split('-')[0].toUpperCase()}
                                </CardTitle>
                                <Badge variant="outline" className={statusClass(order.status)}>
                                    {STATUS_LABEL[order.status] ?? order.status}
                                </Badge>
                            </CardHeader>
                            <CardContent className="space-y-3">
                                <div>
                                    <p className="text-2xl font-bold font-mono tabular-nums">
                                        {formatCLP(Number(order.totalAmount ?? 0))}
                                    </p>
                                    <p className="text-xs text-muted-foreground">
                                        {order.items.length} producto
                                        {order.items.length === 1 ? '' : 's'} ·{' '}
                                        {formatDate(order.createdAt)}
                                    </p>
                                </div>

                                <ul className="space-y-1 border-t pt-3 text-sm">
                                    {order.items.map((item, index) => (
                                        <li
                                            key={`${order.id}-${index}`}
                                            className="flex items-center justify-between gap-3"
                                        >
                                            <span className="text-muted-foreground">
                                                {item.quantity} × {item.productName}
                                            </span>
                                            <span className="font-mono tabular-nums">
                                                {formatCLP(Number(item.subtotal ?? 0))}
                                            </span>
                                        </li>
                                    ))}
                                </ul>

                                {order.status === 'PENDING' ? (
                                    <Button
                                        className="w-full"
                                        onClick={() => handlePay(order.id)}
                                        loading={
                                            payOrder.isPending && payOrder.variables === order.id
                                        }
                                    >
                                        {!(payOrder.isPending && payOrder.variables === order.id) && (
                                            <CreditCard aria-hidden="true" className="mr-2 h-4 w-4" />
                                        )}
                                        Pagar ahora
                                    </Button>
                                ) : order.status === 'PAID' ? (
                                    <Button className="w-full" variant="outline" disabled>
                                        <Clock aria-hidden="true" className="mr-2 h-4 w-4" />
                                        Pagado
                                    </Button>
                                ) : null}
                            </CardContent>
                        </Card>
                    ))}
                </div>
            )}
        </div>
    );
}
