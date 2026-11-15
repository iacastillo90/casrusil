"use client";


import { useInvoices } from "@/features/invoicing/hooks/useInvoices";
import { InvoiceTable } from "@/features/invoicing/components/InvoiceTable";
import { CsvImportModal } from "@/features/invoicing/components/CsvImportModal";
import { Button } from "@/components/ui/button";
import { Plus, Loader2 } from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { invoiceService } from "@/features/invoicing/services/invoice.service";
import { toast } from "sonner";
import { useState } from "react";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

export default function InvoicesPage() {
    const { data, isLoading } = useInvoices({ page: 1, pageSize: 20 });
    const router = useRouter();
    const [sendTarget, setSendTarget] = useState<string | null>(null);
    const [email, setEmail] = useState("");
    const [isSending, setIsSending] = useState(false);

    const handleView = (id: string) => {
        router.push(`/invoices/${id}`);
    };

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
        } catch (error) {
            toast.error("Error al enviar factura");
        } finally {
            setIsSending(false);
        }
    };

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between">
                <h1 className="text-3xl font-bold tracking-tight">Facturación</h1>
                <div className="flex gap-2">
                    <CsvImportModal defaultBookType="SALE" />
                    <Link href="/invoices/new">
                        <Button>
                            <Plus className="mr-2 h-4 w-4" /> Nueva Factura
                        </Button>
                    </Link>
                </div>
            </div>

            <InvoiceTable
                invoices={data?.invoices || []}
                isLoading={isLoading}
                onView={handleView}
                onSend={setSendTarget}
            />

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
                        <Button onClick={handleSend} disabled={isSending || !email.trim()}>
                            {isSending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                            Enviar
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </div>
    );
}
