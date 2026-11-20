'use client';

import { Users } from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import api from '@/lib/axios';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { formatCLP, formatDate, formatRut } from '@/lib/formatters';
import { EmptyState, ErrorState, TableSkeleton } from './DataStates';

/** DTO real de `GET /accounting/ledger/partners` (PartnerSummaryDTO). */
export interface PartnerSummary {
    rut: string;
    name: string;
    totalDebt: number;
    overdueDebt: number;
    pendingInvoices: number;
    lastMovement: string | null;
}

async function fetchPartnerSummaries(): Promise<PartnerSummary[]> {
    const { data } = await api.get<PartnerSummary[]>('/accounting/ledger/partners');
    return data ?? [];
}

/**
 * Libro mayor de terceros (cuentas corrientes por cliente).
 * Los errores se muestran al usuario: antes se ocultaban como lista vacía.
 */
export function PartnerLedger() {
    const { data: partners = [], isLoading, error, refetch, isFetching } = useQuery({
        queryKey: ['partner-ledger'],
        queryFn: fetchPartnerSummaries,
    });

    if (error) {
        return (
            <ErrorState
                title="No se pudieron cargar las cuentas corrientes"
                message={error instanceof Error ? error.message : 'Error desconocido'}
                onRetry={() => refetch()}
                retrying={isFetching}
            />
        );
    }

    return (
        <Card>
            <CardHeader>
                <CardTitle className="flex items-center gap-2 text-base font-semibold">
                    <Users aria-hidden="true" className="h-5 w-5 text-muted-foreground" />
                    Clientes con saldo pendiente
                </CardTitle>
            </CardHeader>
            <CardContent className="p-0">
                {isLoading ? (
                    <TableSkeleton rows={5} cols={6} className="px-6 pb-6" />
                ) : partners.length === 0 ? (
                    <EmptyState
                        title="No hay movimientos de terceros"
                        description="Los saldos por cliente aparecen cuando registras documentos con RUT de contraparte."
                    />
                ) : (
                    <div className="overflow-x-auto">
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead>RUT</TableHead>
                                    <TableHead>Cliente</TableHead>
                                    <TableHead className="text-right">Deuda total</TableHead>
                                    <TableHead className="text-right">Deuda vencida</TableHead>
                                    <TableHead className="text-right">Documentos</TableHead>
                                    <TableHead>Último movimiento</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {partners.map((partner) => (
                                    <TableRow key={partner.rut}>
                                        <TableCell className="font-mono text-xs">
                                            {formatRut(partner.rut)}
                                        </TableCell>
                                        <TableCell className="font-medium">
                                            {partner.name}
                                        </TableCell>
                                        <TableCell className="text-right font-mono tabular-nums">
                                            {formatCLP(partner.totalDebt)}
                                        </TableCell>
                                        <TableCell
                                            className={`text-right font-mono tabular-nums ${
                                                partner.overdueDebt > 0 ? 'text-destructive' : ''
                                            }`}
                                        >
                                            {formatCLP(partner.overdueDebt)}
                                        </TableCell>
                                        <TableCell className="text-right font-mono tabular-nums">
                                            {partner.pendingInvoices}
                                        </TableCell>
                                        <TableCell className="text-muted-foreground">
                                            {formatDate(partner.lastMovement)}
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </div>
                )}
            </CardContent>
        </Card>
    );
}

export default PartnerLedger;
