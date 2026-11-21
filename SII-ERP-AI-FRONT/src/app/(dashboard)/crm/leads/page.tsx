'use client';

import { useState } from 'react';
import { GripVertical, Plus } from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/components/ui/button';
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
import { PageHeader } from '@/features/accounting/components/PageHeader';
import { ErrorState, TableSkeleton } from '@/features/accounting/components/DataStates';
import { useCreateLead, useLeads, useUpdateLeadStatus } from '@/features/crm/hooks/useCrm';
import { Lead, LeadStatus } from '@/features/crm/services/crm.service';

const COLUMNS: { id: LeadStatus; title: string }[] = [
    { id: 'NEW', title: 'Nuevos' },
    { id: 'CONTACTED', title: 'Contactados' },
    { id: 'QUALIFIED', title: 'Calificados' },
    { id: 'PROPOSAL_SENT', title: 'Propuesta' },
    { id: 'WON', title: 'Ganados' },
    { id: 'LOST', title: 'Perdidos' },
];

const EMPTY_FORM = { contactName: '', companyName: '', email: '', phone: '' };

export default function CrmLeadsPage() {
    const { data: leads, isLoading, error, refetch, isFetching } = useLeads();
    const updateStatus = useUpdateLeadStatus();
    const createLead = useCreateLead();

    const [draggedLead, setDraggedLead] = useState<Lead | null>(null);
    const [open, setOpen] = useState(false);
    const [form, setForm] = useState(EMPTY_FORM);

    const handleDrop = (event: React.DragEvent, newStatus: LeadStatus) => {
        event.preventDefault();
        if (!draggedLead || draggedLead.status === newStatus) {
            setDraggedLead(null);
            return;
        }
        updateStatus.mutate(
            { id: draggedLead.id, status: newStatus },
            {
                onSuccess: () =>
                    toast.success(`${draggedLead.contactName} → ${newStatus.toLowerCase()}`),
                onError: (mutationError) =>
                    toast.error('No se pudo mover el lead', {
                        description:
                            mutationError instanceof Error
                                ? mutationError.message
                                : undefined,
                    }),
            },
        );
        setDraggedLead(null);
    };

    const handleCreate = async (event: React.FormEvent) => {
        event.preventDefault();
        if (!form.contactName.trim() || !form.companyName.trim() || !form.email.trim()) {
            toast.error('Completa contacto, empresa y email');
            return;
        }
        try {
            await createLead.mutateAsync({
                contactName: form.contactName.trim(),
                companyName: form.companyName.trim(),
                email: form.email.trim(),
                phone: form.phone.trim(),
            });
            toast.success('Lead creado');
            setOpen(false);
            setForm(EMPTY_FORM);
        } catch (mutationError) {
            toast.error('No se pudo crear el lead', {
                description:
                    mutationError instanceof Error ? mutationError.message : undefined,
            });
        }
    };

    return (
        <div className="flex h-[calc(100vh-8rem)] flex-col space-y-6">
            <PageHeader
                title="Oportunidades"
                description="Embudo comercial: arrastra cada lead para avanzar de etapa."
                actions={
                    <Button onClick={() => setOpen(true)}>
                        <Plus aria-hidden="true" className="mr-2 h-4 w-4" />
                        Nuevo lead
                    </Button>
                }
            />

            {error ? (
                <ErrorState
                    title="No se pudo cargar el embudo"
                    message={error instanceof Error ? error.message : 'Error desconocido'}
                    onRetry={() => refetch()}
                    retrying={isFetching}
                />
            ) : isLoading ? (
                <TableSkeleton rows={3} cols={4} />
            ) : (
                <div className="flex flex-1 gap-4 overflow-x-auto pb-4">
                    {COLUMNS.map((column) => {
                        const columnLeads =
                            leads?.filter((lead) => lead.status === column.id) ?? [];
                        return (
                            <div
                                key={column.id}
                                className="flex w-80 min-w-[280px] flex-col rounded-lg border bg-muted/40 p-4"
                                onDrop={(event) => handleDrop(event, column.id)}
                                onDragOver={(event) => event.preventDefault()}
                            >
                                <div className="mb-3 flex items-center justify-between">
                                    <h2 className="text-sm font-semibold">{column.title}</h2>
                                    <span className="rounded-full bg-muted px-2 py-0.5 text-xs text-muted-foreground">
                                        {columnLeads.length}
                                    </span>
                                </div>

                                <div className="flex-1 space-y-3 overflow-y-auto">
                                    {columnLeads.length === 0 ? (
                                        <p className="py-6 text-center text-xs text-muted-foreground">
                                            Sin oportunidades
                                        </p>
                                    ) : (
                                        columnLeads.map((lead) => (
                                            <div
                                                key={lead.id}
                                                draggable
                                                onDragStart={() => setDraggedLead(lead)}
                                                className="cursor-grab rounded-md border bg-card p-3 shadow-sm transition-colors hover:border-primary/40 active:cursor-grabbing"
                                            >
                                                <div className="flex items-start justify-between gap-2">
                                                    <div>
                                                        <p className="text-sm font-medium">
                                                            {lead.contactName}
                                                        </p>
                                                        <p className="text-xs text-muted-foreground">
                                                            {lead.companyName}
                                                        </p>
                                                    </div>
                                                    <GripVertical
                                                        aria-hidden="true"
                                                        className="h-4 w-4 shrink-0 text-muted-foreground"
                                                    />
                                                </div>
                                                <p className="mt-2 text-xs text-muted-foreground">
                                                    {lead.email}
                                                </p>
                                            </div>
                                        ))
                                    )}
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}

            <Dialog open={open} onOpenChange={setOpen}>
                <DialogContent>
                    <form onSubmit={handleCreate} className="space-y-4">
                        <DialogHeader>
                            <DialogTitle>Nuevo lead</DialogTitle>
                            <DialogDescription>
                                Registra el contacto para seguirlo en el embudo comercial.
                            </DialogDescription>
                        </DialogHeader>
                        <div className="grid gap-3 sm:grid-cols-2">
                            <div className="space-y-1.5">
                                <Label htmlFor="lead-contact">Contacto</Label>
                                <Input
                                    id="lead-contact"
                                    value={form.contactName}
                                    onChange={(event) =>
                                        setForm({ ...form, contactName: event.target.value })
                                    }
                                    required
                                />
                            </div>
                            <div className="space-y-1.5">
                                <Label htmlFor="lead-company">Empresa</Label>
                                <Input
                                    id="lead-company"
                                    value={form.companyName}
                                    onChange={(event) =>
                                        setForm({ ...form, companyName: event.target.value })
                                    }
                                    required
                                />
                            </div>
                            <div className="space-y-1.5">
                                <Label htmlFor="lead-email">Email</Label>
                                <Input
                                    id="lead-email"
                                    type="email"
                                    value={form.email}
                                    onChange={(event) =>
                                        setForm({ ...form, email: event.target.value })
                                    }
                                    required
                                />
                            </div>
                            <div className="space-y-1.5">
                                <Label htmlFor="lead-phone">Teléfono</Label>
                                <Input
                                    id="lead-phone"
                                    value={form.phone}
                                    onChange={(event) =>
                                        setForm({ ...form, phone: event.target.value })
                                    }
                                />
                            </div>
                        </div>
                        <DialogFooter>
                            <Button
                                type="button"
                                variant="outline"
                                onClick={() => setOpen(false)}
                                disabled={createLead.isPending}
                            >
                                Cancelar
                            </Button>
                            <Button type="submit" loading={createLead.isPending}>
                                Crear lead
                            </Button>
                        </DialogFooter>
                    </form>
                </DialogContent>
            </Dialog>
        </div>
    );
}
