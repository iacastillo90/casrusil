'use client';

import { useState } from 'react';
import { Check, Plus, X } from 'lucide-react';
import { toast } from 'sonner';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
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
import { useAcceptQuote, useCreateQuote, useQuotes, useRejectQuote } from '@/features/crm/hooks/useCrm';
import { Quote } from '@/features/crm/services/crm.service';
import { formatCLP, formatDate, formatRut } from '@/lib/formatters';

const STATUS_LABEL: Record<Quote['status'], string> = {
    DRAFT: 'Borrador',
    SENT: 'Enviada',
    ACCEPTED: 'Aceptada',
    REJECTED: 'Rechazada',
};

const statusClass = (status: Quote['status']) => {
    if (status === 'ACCEPTED') {
        return 'border-success-subtle-foreground/25 bg-success-subtle text-success-subtle-foreground';
    }
    if (status === 'REJECTED') {
        return 'border-destructive-subtle-foreground/25 bg-destructive-subtle text-destructive-subtle-foreground';
    }
    if (status === 'SENT') {
        return 'border-info-subtle-foreground/20 bg-info-subtle text-info-subtle-foreground';
    }
    return 'border-neutral-subtle-foreground/20 bg-neutral-subtle text-neutral-subtle-foreground';
};

const EMPTY_FORM = {
    clientName: '',
    clientRut: '',
    expirationDate: '',
    description: '',
    quantity: '1',
    unitPrice: '',
};

export default function CrmQuotesPage() {
    const { data: quotes, isLoading, error, refetch, isFetching } = useQuotes();
    const acceptQuote = useAcceptQuote();
    const rejectQuote = useRejectQuote();
    const createQuote = useCreateQuote();

    const [open, setOpen] = useState(false);
    const [form, setForm] = useState(EMPTY_FORM);

    const handleCreate = async (event: React.FormEvent) => {
        event.preventDefault();
        const quantity = Number(form.quantity);
        const unitPrice = Number(form.unitPrice);
        if (!form.clientName.trim() || !form.clientRut.trim() || !form.description.trim()) {
            toast.error('Completa cliente, RUT y descripción de la línea');
            return;
        }
        if (!Number.isFinite(quantity) || quantity <= 0 || !Number.isFinite(unitPrice)) {
            toast.error('Cantidad y precio deben ser mayores a 0');
            return;
        }
        try {
            await createQuote.mutateAsync({
                clientRut: form.clientRut.trim(),
                clientName: form.clientName.trim(),
                expirationDate:
                    form.expirationDate || new Date(Date.now() + 30 * 86400000).toISOString().slice(0, 10),
                lines: [
                    {
                        description: form.description.trim(),
                        quantity,
                        unitPrice,
                    },
                ],
            });
            toast.success('Cotización creada');
            setOpen(false);
            setForm(EMPTY_FORM);
        } catch (mutationError) {
            toast.error('No se pudo crear la cotización', {
                description:
                    mutationError instanceof Error ? mutationError.message : undefined,
            });
        }
    };

    return (
        <div className="space-y-6">
            <PageHeader
                title="Cotizaciones"
                description="Presupuestos enviados a clientes y su conversión a factura."
                actions={
                    <Button onClick={() => setOpen(true)}>
                        <Plus aria-hidden="true" className="mr-2 h-4 w-4" />
                        Nueva cotización
                    </Button>
                }
            />

            {error ? (
                <ErrorState
                    title="No se pudieron cargar las cotizaciones"
                    message={error instanceof Error ? error.message : 'Error desconocido'}
                    onRetry={() => refetch()}
                    retrying={isFetching}
                />
            ) : isLoading ? (
                <TableSkeleton rows={5} cols={5} />
            ) : !quotes || quotes.length === 0 ? (
                <EmptyState
                    title="No hay cotizaciones registradas"
                    description="Crea la primera cotización para enviarla al cliente."
                    action={
                        <Button variant="outline" onClick={() => setOpen(true)}>
                            <Plus aria-hidden="true" className="mr-2 h-4 w-4" />
                            Nueva cotización
                        </Button>
                    }
                />
            ) : (
                <Card>
                    <CardContent className="p-0">
                        <div className="overflow-x-auto">
                            <Table>
                                <TableHeader>
                                    <TableRow>
                                        <TableHead>Cotización</TableHead>
                                        <TableHead>Cliente</TableHead>
                                        <TableHead>Estado</TableHead>
                                        <TableHead className="text-right">Total</TableHead>
                                        <TableHead className="text-right">Acciones</TableHead>
                                    </TableRow>
                                </TableHeader>
                                <TableBody>
                                    {quotes.map((quote) => (
                                        <TableRow key={quote.id}>
                                            <TableCell>
                                                <div className="font-mono text-xs">
                                                    {quote.id.slice(0, 8).toUpperCase()}
                                                </div>
                                                <div className="text-xs text-muted-foreground">
                                                    {formatDate(quote.quoteDate)}
                                                </div>
                                            </TableCell>
                                            <TableCell>
                                                <div className="font-medium">{quote.clientName}</div>
                                                <div className="font-mono text-xs text-muted-foreground">
                                                    {formatRut(quote.clientRut)}
                                                </div>
                                            </TableCell>
                                            <TableCell>
                                                <Badge variant="outline" className={statusClass(quote.status)}>
                                                    {STATUS_LABEL[quote.status] ?? quote.status}
                                                </Badge>
                                            </TableCell>
                                            <TableCell className="text-right font-mono tabular-nums">
                                                {formatCLP(Number(quote.totalAmount ?? 0))}
                                            </TableCell>
                                            <TableCell className="text-right">
                                                {quote.status === 'DRAFT' ? (
                                                    <div className="flex justify-end gap-2">
                                                        <Button
                                                            size="sm"
                                                            onClick={() => acceptQuote.mutate(quote.id)}
                                                            loading={
                                                                acceptQuote.isPending &&
                                                                acceptQuote.variables === quote.id
                                                            }
                                                        >
                                                            <Check aria-hidden="true" className="mr-1 h-3 w-3" />
                                                            Aprobar
                                                        </Button>
                                                        <Button
                                                            variant="outline"
                                                            size="sm"
                                                            onClick={() => rejectQuote.mutate(quote.id)}
                                                            loading={
                                                                rejectQuote.isPending &&
                                                                rejectQuote.variables === quote.id
                                                            }
                                                        >
                                                            <X aria-hidden="true" className="mr-1 h-3 w-3" />
                                                            Rechazar
                                                        </Button>
                                                    </div>
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
                            <DialogTitle>Nueva cotización</DialogTitle>
                            <DialogDescription>
                                Se crea en borrador con una línea; luego puedes aprobarla o rechazarla.
                            </DialogDescription>
                        </DialogHeader>

                        <div className="grid gap-3 sm:grid-cols-2">
                            <div className="space-y-1.5">
                                <Label htmlFor="quote-client">Cliente</Label>
                                <Input
                                    id="quote-client"
                                    value={form.clientName}
                                    onChange={(event) =>
                                        setForm({ ...form, clientName: event.target.value })
                                    }
                                    required
                                />
                            </div>
                            <div className="space-y-1.5">
                                <Label htmlFor="quote-rut">RUT cliente</Label>
                                <Input
                                    id="quote-rut"
                                    value={form.clientRut}
                                    onChange={(event) =>
                                        setForm({ ...form, clientRut: event.target.value })
                                    }
                                    placeholder="12.345.678-9"
                                    required
                                />
                            </div>
                            <div className="space-y-1.5 sm:col-span-2">
                                <Label htmlFor="quote-expiration">Vigencia hasta</Label>
                                <Input
                                    id="quote-expiration"
                                    type="date"
                                    value={form.expirationDate}
                                    onChange={(event) =>
                                        setForm({ ...form, expirationDate: event.target.value })
                                    }
                                />
                            </div>
                            <div className="space-y-1.5 sm:col-span-2">
                                <Label htmlFor="quote-description">Descripción de la línea</Label>
                                <Input
                                    id="quote-description"
                                    value={form.description}
                                    onChange={(event) =>
                                        setForm({ ...form, description: event.target.value })
                                    }
                                    placeholder="Instalación eléctrica"
                                    required
                                />
                            </div>
                            <div className="space-y-1.5">
                                <Label htmlFor="quote-quantity">Cantidad</Label>
                                <Input
                                    id="quote-quantity"
                                    type="number"
                                    min="1"
                                    value={form.quantity}
                                    onChange={(event) =>
                                        setForm({ ...form, quantity: event.target.value })
                                    }
                                />
                            </div>
                            <div className="space-y-1.5">
                                <Label htmlFor="quote-price">Precio unitario</Label>
                                <Input
                                    id="quote-price"
                                    type="number"
                                    min="0"
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
                                disabled={createQuote.isPending}
                            >
                                Cancelar
                            </Button>
                            <Button type="submit" loading={createQuote.isPending}>
                                Crear cotización
                            </Button>
                        </DialogFooter>
                    </form>
                </DialogContent>
            </Dialog>
        </div>
    );
}
