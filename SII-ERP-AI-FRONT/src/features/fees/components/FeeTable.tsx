"use client";

import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { formatCLP, formatRut, formatDate } from "@/lib/formatters";
import { FileText } from "lucide-react";
import type { Fee } from "../services/fee.service";

interface FeeTableProps {
    fees: Fee[];
    isLoading?: boolean;
}

export function FeeTable({ fees, isLoading = false }: FeeTableProps) {
    if (isLoading) {
        return (
            <div className="space-y-2">
                {Array.from({ length: 5 }).map((_, i) => (
                    <Skeleton key={i} className="h-12 w-full" />
                ))}
            </div>
        );
    }

    if (!fees.length) {
        return (
            <div className="flex flex-col items-center justify-center py-12 text-center text-muted-foreground">
                <FileText className="mb-2 h-8 w-8 opacity-50" />
                <p>No hay boletas de honorarios registradas.</p>
            </div>
        );
    }

    return (
        <Table>
            <TableHeader>
                <TableRow>
                    <TableHead>Folio</TableHead>
                    <TableHead>Emisor</TableHead>
                    <TableHead>RUT</TableHead>
                    <TableHead>Fecha</TableHead>
                    <TableHead className="text-right">Bruto</TableHead>
                    <TableHead className="text-right">Retención</TableHead>
                    <TableHead className="text-right">Líquido</TableHead>
                    <TableHead className="text-center">Estado</TableHead>
                </TableRow>
            </TableHeader>
            <TableBody>
                {fees.map((fee) => (
                    <TableRow key={fee.id}>
                        <TableCell className="font-mono text-xs">#{fee.folio}</TableCell>
                        <TableCell className="font-medium">{fee.issuerName}</TableCell>
                        <TableCell className="font-mono text-xs">{formatRut(fee.issuerRut)}</TableCell>
                        <TableCell>{formatDate(fee.issueDate)}</TableCell>
                        <TableCell className="text-right">{formatCLP(fee.grossAmount)}</TableCell>
                        <TableCell className="text-right text-red-600">{formatCLP(fee.retentionAmount)}</TableCell>
                        <TableCell className="text-right font-semibold">{formatCLP(fee.netAmount)}</TableCell>
                        <TableCell className="text-center">
                            <Badge variant="secondary">{fee.status}</Badge>
                        </TableCell>
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    );
}
