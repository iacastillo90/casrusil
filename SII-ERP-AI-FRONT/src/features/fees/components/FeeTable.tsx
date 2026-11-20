'use client';

import { Badge } from '@/components/ui/badge';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { formatCLP, formatDate, formatRut } from '@/lib/formatters';
import { FileText } from 'lucide-react';
import type { Fee } from '../services/fee.service';
import { EmptyState, TableSkeleton } from '@/features/accounting/components/DataStates';

interface FeeTableProps {
    fees: Fee[];
    isLoading?: boolean;
}

export function FeeTable({ fees, isLoading = false }: FeeTableProps) {
    if (isLoading) {
        return <TableSkeleton rows={5} cols={8} />;
    }

    if (!fees.length) {
        return (
            <EmptyState
                icon={FileText}
                title="No hay boletas de honorarios registradas"
                description="Importa un archivo de boletas para verlas acá con su retención y monto líquido."
            />
        );
    }

    return (
        <div className="overflow-x-auto">
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
                        <TableHead>Estado</TableHead>
                    </TableRow>
                </TableHeader>
                <TableBody>
                    {fees.map((fee) => (
                        <TableRow key={fee.id}>
                            <TableCell className="font-mono text-xs">#{fee.folio}</TableCell>
                            <TableCell className="font-medium">{fee.issuerName}</TableCell>
                            <TableCell className="font-mono text-xs">
                                {formatRut(fee.issuerRut)}
                            </TableCell>
                            <TableCell className="text-muted-foreground">
                                {formatDate(fee.issueDate)}
                            </TableCell>
                            <TableCell className="text-right font-mono tabular-nums">
                                {formatCLP(fee.grossAmount)}
                            </TableCell>
                            <TableCell className="text-right font-mono tabular-nums text-warning">
                                {formatCLP(fee.retentionAmount)}
                            </TableCell>
                            <TableCell className="text-right font-mono font-semibold tabular-nums">
                                {formatCLP(fee.netAmount)}
                            </TableCell>
                            <TableCell>
                                <Badge variant="secondary">{fee.status}</Badge>
                            </TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </div>
    );
}

export default FeeTable;
