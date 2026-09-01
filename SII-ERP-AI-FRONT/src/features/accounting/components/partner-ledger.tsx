"use client";

import { useQuery } from "@tanstack/react-query";
import api from "@/lib/axios";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Skeleton } from "@/components/ui/skeleton";
import { Users } from "lucide-react";
import { formatCLP, formatRut } from "@/lib/formatters";

export interface PartnerSummary {
    partnerRut: string;
    partnerName: string;
    totalDebit: number;
    totalCredit: number;
    balance: number;
}

/**
 * Libro Mayor de Terceros (cuentas corrientes por proveedor/cliente).
 * Consume el endpoint backend `/accounting/ledger/partners`.
 * NOTA: si el endpoint aún no responde, se muestra un estado vacío.
 */
async function fetchPartnerSummaries(): Promise<PartnerSummary[]> {
    try {
        const { data } = await api.get<PartnerSummary[]>("/accounting/ledger/partners");
        return data ?? [];
    } catch {
        return [];
    }
}

export function PartnerLedger() {
    const { data: partners = [], isLoading } = useQuery({
        queryKey: ["partner-ledger"],
        queryFn: fetchPartnerSummaries,
    });

    return (
        <Card>
            <CardHeader>
                <CardTitle className="flex items-center gap-2">
                    <Users className="h-5 w-5 text-indigo-600" />
                    Cuentas Corrientes por Tercero
                </CardTitle>
            </CardHeader>
            <CardContent>
                {isLoading ? (
                    <div className="space-y-2">
                        {Array.from({ length: 5 }).map((_, i) => (
                            <Skeleton key={i} className="h-12 w-full" />
                        ))}
                    </div>
                ) : partners.length === 0 ? (
                    <div className="py-12 text-center text-sm text-muted-foreground">
                        No hay movimientos de terceros registrados.
                    </div>
                ) : (
                    <Table>
                        <TableHeader>
                            <TableRow>
                                <TableHead>RUT</TableHead>
                                <TableHead>Nombre</TableHead>
                                <TableHead className="text-right">Debe</TableHead>
                                <TableHead className="text-right">Haber</TableHead>
                                <TableHead className="text-right">Saldo</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {partners.map((partner) => (
                                <TableRow key={partner.partnerRut}>
                                    <TableCell className="font-mono text-xs">{formatRut(partner.partnerRut)}</TableCell>
                                    <TableCell className="font-medium">{partner.partnerName}</TableCell>
                                    <TableCell className="text-right">{formatCLP(partner.totalDebit)}</TableCell>
                                    <TableCell className="text-right">{formatCLP(partner.totalCredit)}</TableCell>
                                    <TableCell className="text-right font-semibold">{formatCLP(partner.balance)}</TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                )}
            </CardContent>
        </Card>
    );
}
