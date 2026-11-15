"use client";

import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { StatusBadge } from "@/components/shared/StatusBadge";
import { formatCLP, formatDate, formatRut } from "@/lib/formatters";
import { Eye, Send, FileText } from "lucide-react";
import type { Invoice } from "../types/invoice.types";

interface InvoiceTableProps {
    invoices: Invoice[];
    isLoading?: boolean;
    onView?: (id: string) => void;
    onSend?: (id: string) => void;
}

export function InvoiceTable({ invoices, isLoading = false, onView, onSend }: InvoiceTableProps) {
    if (isLoading) {
        return (
            <div className="space-y-2">
                {Array.from({ length: 6 }).map((_, i) => (
                    <Skeleton key={i} className="h-12 w-full" />
                ))}
            </div>
        );
    }

    if (!invoices.length) {
        return (
            <div className="flex flex-col items-center justify-center rounded-lg border py-16 text-center text-muted-foreground">
                <FileText className="mb-2 h-10 w-10 opacity-50" />
                <p className="font-medium">No hay facturas</p>
                <p className="text-sm">Crea una nueva factura o importa desde CSV para comenzar.</p>
            </div>
        );
    }

    return (
        <div className="rounded-lg border">
            <Table>
                <TableHeader>
                    <TableRow>
                        <TableHead>Folio</TableHead>
                        <TableHead>Receptor</TableHead>
                        <TableHead>RUT</TableHead>
                        <TableHead>Emisión</TableHead>
                        <TableHead className="text-right">Total</TableHead>
                        <TableHead className="text-center">Estado</TableHead>
                        <TableHead className="text-right">Acciones</TableHead>
                    </TableRow>
                </TableHeader>
                <TableBody>
                    {invoices.map((invoice) => (
                        <TableRow key={invoice.id}>
                            <TableCell className="font-mono text-xs">#{invoice.folio}</TableCell>
                            <TableCell className="font-medium">{invoice.recipientName}</TableCell>
                            <TableCell className="font-mono text-xs">
                                {invoice.recipientRut ? formatRut(invoice.recipientRut) : "-"}
                            </TableCell>
                            <TableCell>{formatDate(invoice.issueDate)}</TableCell>
                            <TableCell className="text-right font-semibold">{formatCLP(invoice.totalAmount)}</TableCell>
                            <TableCell className="text-center">
                                <StatusBadge status={invoice.status as 'DRAFT' | 'SIGNED' | 'SENT' | 'ACCEPTED' | 'REJECTED'} />
                            </TableCell>
                            <TableCell className="text-right">
                                <div className="flex justify-end gap-1">
                                    {onView && (
                                        <Button variant="ghost" size="icon" onClick={() => onView(invoice.id)} title="Ver detalle">
                                            <Eye className="h-4 w-4" />
                                        </Button>
                                    )}
                                    {onSend && (
                                        <Button variant="ghost" size="icon" onClick={() => onSend(invoice.id)} title="Enviar por email">
                                            <Send className="h-4 w-4" />
                                        </Button>
                                    )}
                                </div>
                            </TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </div>
    );
}
