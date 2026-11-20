import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Skeleton } from "@/components/ui/skeleton";
import { Calculator } from "lucide-react";
import { formatCLP } from "@/lib/formatters";
import type { AccountingEntry } from "../services/accounting.service";

interface AccountingEntryCardProps {
    entry: AccountingEntry | null;
    isLoading: boolean;
}

export function AccountingEntryCard({ entry, isLoading }: AccountingEntryCardProps) {
    if (isLoading) {
        return <Skeleton className="h-48 w-full" />;
    }

    if (!entry) {
        return (
            <Card>
                <CardContent className="py-12 text-center">
                    <Calculator className="mx-auto mb-3 h-8 w-8 text-muted-foreground opacity-50" />
                    <p className="text-muted-foreground">
                        El asiento contable se genera automáticamente cuando el SII acepta el documento.
                    </p>
                </CardContent>
            </Card>
        );
    }

    return (
        <Card>
            <CardHeader>
                <CardTitle className="flex items-center gap-2 text-base">
                    <Calculator className="h-5 w-5 text-indigo-600" />
                    Asiento Contable
                </CardTitle>
                <p className="text-sm text-muted-foreground">{entry.description}</p>
            </CardHeader>
            <CardContent>
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>Cuenta</TableHead>
                            <TableHead className="text-right">Debe</TableHead>
                            <TableHead className="text-right">Haber</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {entry.lines.map((line) => (
                            <TableRow key={line.id}>
                                <TableCell>
                                    <span className="font-mono text-xs text-muted-foreground">{line.accountCode}</span>{" "}
                                    {line.accountName}
                                </TableCell>
                                <TableCell className="text-right">
                                    {line.debit ? formatCLP(line.debit) : "-"}
                                </TableCell>
                                <TableCell className="text-right">
                                    {line.credit ? formatCLP(line.credit) : "-"}
                                </TableCell>
                            </TableRow>
                        ))}
                        <TableRow className="font-semibold">
                            <TableCell>Totales</TableCell>
                            <TableCell className="text-right">{formatCLP(entry.totalDebit)}</TableCell>
                            <TableCell className="text-right">{formatCLP(entry.totalCredit)}</TableCell>
                        </TableRow>
                    </TableBody>
                </Table>
            </CardContent>
        </Card>
    );
}
