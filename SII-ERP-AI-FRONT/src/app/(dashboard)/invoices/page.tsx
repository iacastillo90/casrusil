"use client";


import { useInvoices } from "@/features/invoicing/hooks/useInvoices";
import { InvoiceTable } from "@/features/invoicing/components/InvoiceTable";
import { CsvImportModal } from "@/features/invoicing/components/CsvImportModal";
import { Button } from "@/components/ui/button";
import { Plus } from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { invoiceService } from "@/features/invoicing/services/invoice.service";
import { toast } from "sonner";
import { useState } from "react";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { PageHeader } from "@/features/accounting/components/PageHeader";
import { ErrorState } from "@/features/accounting/components/DataStates";

export default function InvoicesPage() {
    const { data, isLoading, error, refetch, isFetching } = useInvoices({ page: 1, pageSize: 20 });
    const router = useRouter();
    const [sendTarget, setSendTarget] = useState<string | null>(null);
    const [email, setEmail] = useState("");
    const [isSending, setIsSending] = useState(false);

    const handleView = (id: string) => {
        router.push(`/invoices/${id}`);
    };

    const total = data?.invoices?.length ?? 0;

    const handleSend = async () => {
        if (!sendTarget) return;

        const trimmed = email.trim();
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(trimmed)) {
            toast.error("Ingresa un email válido");
            return;
        }

        setIsSending(true);
        try {
            await invoiceService.sendInvoice(sendTarget, trimmed);
            toast.success("Factura enviada por email");
            setSendTarget(null);
            setEmail("");
        } catch (sendError) {
            toast.error("No se pudo enviar la factura", {
                description: sendError instanceof Error ? sendError.message : undefined,
            });
        } finally {
            setIsSending(false);
        }
    };

    return (
        <div className="space-y-6">
            <PageHeader
                title="Facturación"
                description={
                    total > 0
                        ? `${total} documento${total === 1 ? '' : 's'} en el período.`
                        : 'Documentos tributarios emitidos y su estado ante el SII.'
                }
                actions={
                    <div className="flex gap-2">
                        <CsvImportModal defaultBookType="SALE" />
                        <Link href="/invoices/new">
                            <Button>
                                <Plus aria-hidden="true" className="mr-2 h-4 w-4" /> Nueva factura
                            </Button>
                        </Link>
                    </div>
                }
            />

            {error ? (
                <ErrorState
                    title="No se pudieron cargar las facturas"
                    message={error instanceof Error ? error.message : 'Error desconocido'}
                    onRetry={() => refetch()}
                    retrying={isFetching}
                />
            ) : (
                <InvoiceTable
                    invoices={data?.invoices || []}
                    isLoading={isLoading}
                    onView={handleView}
                    onSend={setSendTarget}
                />
            )}

            <Dialog open={!!sendTarget} onOpenChange={(open) => !open && setSendTarget(null)}>
                <DialogContent className="sm:max-w-[420px]">
                    <DialogHeader>
                        <DialogTitle>Enviar factura por email</DialogTitle>
                        <DialogDescription>
                            Ingresa el correo del destinatario. La factura se enviará como adjunto.
                        </DialogDescription>
                    </DialogHeader>
                    <div className="space-y-2 py-2">
                        <Label htmlFor="send-email">Email del destinatario</Label>
                        <Input
                            id="send-email"
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            placeholder="cliente@ejemplo.com"
                        />
                    </div>
                    <DialogFooter>
                        <Button variant="outline" onClick={() => setSendTarget(null)} disabled={isSending}>
                            Cancelar
                        </Button>
                        <Button
                            onClick={handleSend}
                            loading={isSending}
                            disabled={!email.trim()}
                        >
                            Enviar
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </div>
    );
}
