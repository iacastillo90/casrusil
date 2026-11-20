'use client';

import { Fragment, useMemo, useState } from 'react';
import { ChevronDown, ChevronRight, Download, Search } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { formatCLP, formatDate } from '@/lib/formatters';
import type { AccountingEntry } from '../services/accounting.service';
import { EmptyState, ErrorState, TableSkeleton } from './DataStates';

interface JournalEntryTableProps {
    entries: AccountingEntry[];
    isLoading?: boolean;
    error?: Error | null;
    onRetry?: () => void;
    isRetrying?: boolean;
}

const entryLabel = (entry: AccountingEntry) =>
    entry.documentNumber ? `N° ${entry.documentNumber}` : entry.id.slice(0, 8).toUpperCase();

/**
 * Libro diario: asientos con su debe y haber, totales del período y detalle
 * de las líneas. Presentacional: los datos los resuelve la página.
 */
export function JournalEntryTable({
    entries,
    isLoading = false,
    error,
    onRetry,
    isRetrying = false,
}: JournalEntryTableProps) {
    const [search, setSearch] = useState('');
    const [expanded, setExpanded] = useState<Set<string>>(new Set());

    const filtered = useMemo(() => {
        const term = search.trim().toLowerCase();
        if (!term) return entries;
        return entries.filter(
            (entry) =>
                entry.description.toLowerCase().includes(term) ||
                entryLabel(entry).toLowerCase().includes(term) ||
                entry.lines.some(
                    (line) =>
                        line.accountCode.toLowerCase().includes(term) ||
                        line.accountName.toLowerCase().includes(term),
                ),
        );
    }, [entries, search]);

    const totals = useMemo(
        () =>
            filtered.reduce(
                (acc, entry) => ({
                    debit: acc.debit + entry.totalDebit,
                    credit: acc.credit + entry.totalCredit,
                }),
                { debit: 0, credit: 0 },
            ),
        [filtered],
    );

    const toggle = (id: string) => {
        setExpanded((prev) => {
            const next = new Set(prev);
            if (next.has(id)) next.delete(id);
            else next.add(id);
            return next;
        });
    };

    const exportCsv = () => {
        const rows = [
            ['Asiento', 'Fecha', 'Glosa', 'Cuenta', 'Debe', 'Haber'],
            ...filtered.flatMap((entry) =>
                entry.lines.map((line) => [
                    entryLabel(entry),
                    entry.date,
                    entry.description,
                    `${line.accountCode} ${line.accountName}`.trim(),
                    String(line.debit),
                    String(line.credit),
                ]),
            ),
        ];
        const blob = new Blob([`\uFEFF${rows.map((row) => row.join(';')).join('\n')}`], {
            type: 'text/csv;charset=utf-8',
        });
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = 'libro-diario.csv';
        link.click();
        URL.revokeObjectURL(url);
    };

    return (
        <Card>
            <CardContent className="space-y-4 pt-6">
                <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                    <div className="relative w-full sm:max-w-sm">
                        <Search
                            aria-hidden="true"
                            className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground"
                        />
                        <Input
                            className="pl-9"
                            placeholder="Buscar por glosa, cuenta o número…"
                            value={search}
                            onChange={(event) => setSearch(event.target.value)}
                            aria-label="Buscar asientos"
                        />
                    </div>
                    <div className="flex items-center gap-2">
                        <span className="text-sm text-muted-foreground">
                            {filtered.length} asiento{filtered.length === 1 ? '' : 's'}
                        </span>
                        <Button
                            variant="outline"
                            size="sm"
                            onClick={exportCsv}
                            disabled={filtered.length === 0}
                        >
                            <Download aria-hidden="true" className="mr-2 h-4 w-4" />
                            Exportar CSV
                        </Button>
                    </div>
                </div>

                {error ? (
                    <ErrorState
                        message={error.message}
                        onRetry={onRetry}
                        retrying={isRetrying}
                    />
                ) : isLoading ? (
                    <TableSkeleton rows={6} cols={6} />
                ) : filtered.length === 0 ? (
                    <EmptyState
                        title={
                            entries.length === 0
                                ? 'No hay asientos registrados'
                                : 'Ningún asiento coincide con la búsqueda'
                        }
                        description={
                            entries.length === 0
                                ? 'Los asientos se generan al emitir documentos, registrar pagos o cerrar el mes.'
                                : 'Prueba con otra glosa, cuenta o número de documento.'
                        }
                    />
                ) : (
                    <div className="overflow-x-auto">
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead className="w-10">
                                        <span className="sr-only">Detalle</span>
                                    </TableHead>
                                    <TableHead>Asiento</TableHead>
                                    <TableHead>Fecha</TableHead>
                                    <TableHead>Glosa</TableHead>
                                    <TableHead className="text-right">Debe</TableHead>
                                    <TableHead className="text-right">Haber</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {filtered.map((entry) => {
                                    const isOpen = expanded.has(entry.id);
                                    const balanced =
                                        Math.abs(entry.totalDebit - entry.totalCredit) < 0.01;
                                    return (
                                        <Fragment key={entry.id}>
                                            <TableRow>
                                                <TableCell>
                                                    <Button
                                                        variant="ghost"
                                                        size="icon"
                                                        aria-label={
                                                            isOpen
                                                                ? 'Ocultar líneas del asiento'
                                                                : 'Ver líneas del asiento'
                                                        }
                                                        aria-expanded={isOpen}
                                                        onClick={() => toggle(entry.id)}
                                                    >
                                                        {isOpen ? (
                                                            <ChevronDown
                                                                aria-hidden="true"
                                                                className="h-4 w-4"
                                                            />
                                                        ) : (
                                                            <ChevronRight
                                                                aria-hidden="true"
                                                                className="h-4 w-4"
                                                            />
                                                        )}
                                                    </Button>
                                                </TableCell>
                                                <TableCell className="font-mono text-xs">
                                                    {entryLabel(entry)}
                                                </TableCell>
                                                <TableCell className="whitespace-nowrap text-muted-foreground">
                                                    {formatDate(entry.date)}
                                                </TableCell>
                                                <TableCell className="min-w-[220px]">
                                                    {entry.description}
                                                </TableCell>
                                                <TableCell className="text-right font-mono tabular-nums">
                                                    {entry.totalDebit ? formatCLP(entry.totalDebit) : '—'}
                                                </TableCell>
                                                <TableCell
                                                    className={`text-right font-mono tabular-nums ${
                                                        balanced ? '' : 'text-destructive'
                                                    }`}
                                                >
                                                    {entry.totalCredit ? formatCLP(entry.totalCredit) : '—'}
                                                </TableCell>
                                            </TableRow>
                                            {isOpen ? (
                                                <TableRow>
                                                    <TableCell colSpan={6} className="bg-muted/40 p-0">
                                                        <table className="w-full text-sm">
                                                            <thead>
                                                                <tr className="text-xs text-muted-foreground">
                                                                    <th className="px-4 py-2 text-left font-medium">
                                                                        Cuenta
                                                                    </th>
                                                                    <th className="px-4 py-2 text-right font-medium">
                                                                        Debe
                                                                    </th>
                                                                    <th className="px-4 py-2 text-right font-medium">
                                                                        Haber
                                                                    </th>
                                                                </tr>
                                                            </thead>
                                                            <tbody>
                                                                {entry.lines.map((line) => (
                                                                    <tr key={line.id}>
                                                                        <td className="px-4 py-1.5">
                                                                            <span className="font-mono text-xs text-muted-foreground">
                                                                                {line.accountCode}
                                                                            </span>{' '}
                                                                            {line.accountName}
                                                                        </td>
                                                                        <td className="px-4 py-1.5 text-right font-mono tabular-nums">
                                                                            {line.debit ? formatCLP(line.debit) : ''}
                                                                        </td>
                                                                        <td className="px-4 py-1.5 text-right font-mono tabular-nums">
                                                                            {line.credit ? formatCLP(line.credit) : ''}
                                                                        </td>
                                                                    </tr>
                                                                ))}
                                                            </tbody>
                                                        </table>
                                                    </TableCell>
                                                </TableRow>
                                            ) : null}
                                        </Fragment>
                                    );
                                })}
                            </TableBody>
                        </Table>
                    </div>
                )}

                {!error && !isLoading && filtered.length > 0 ? (
                    <div className="flex flex-wrap items-center justify-end gap-6 border-t pt-3 text-sm">
                        <span className="text-muted-foreground">
                            Total debe{' '}
                            <span className="ml-2 font-mono font-semibold tabular-nums text-foreground">
                                {formatCLP(totals.debit)}
                            </span>
                        </span>
                        <span className="text-muted-foreground">
                            Total haber{' '}
                            <span className="ml-2 font-mono font-semibold tabular-nums text-foreground">
                                {formatCLP(totals.credit)}
                            </span>
                        </span>
                        <span
                            className={
                                Math.abs(totals.debit - totals.credit) < 0.01
                                    ? 'text-success'
                                    : 'text-destructive'
                            }
                        >
                            {Math.abs(totals.debit - totals.credit) < 0.01
                                ? 'Partida doble cuadrada'
                                : `Diferencia ${formatCLP(totals.debit - totals.credit)}`}
                        </span>
                    </div>
                ) : null}
            </CardContent>
        </Card>
    );
}

export default JournalEntryTable;
