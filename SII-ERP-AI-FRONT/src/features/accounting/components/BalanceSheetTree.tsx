'use client';

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { formatCLP } from '@/lib/formatters';
import type { BalanceSheet } from '../services/accounting.service';
import { EmptyState } from './DataStates';

interface BalanceSheetTreeProps {
    data: BalanceSheet;
}

interface SectionProps {
    title: string;
    accounts: Record<string, number>;
    total: number;
}

function Section({ title, accounts, total }: SectionProps) {
    const rows = Object.entries(accounts).sort(([a], [b]) => a.localeCompare(b));

    return (
        <Card>
            <CardHeader className="pb-2">
                <CardTitle className="text-base font-semibold">{title}</CardTitle>
            </CardHeader>
            <CardContent className="p-0">
                {rows.length === 0 ? (
                    <p className="px-6 pb-4 text-sm text-muted-foreground">
                        Sin cuentas con saldo a la fecha de corte.
                    </p>
                ) : (
                    <table className="w-full text-sm">
                        <tbody>
                            {rows.map(([code, balance]) => (
                                <tr key={code} className="border-t first:border-t-0">
                                    <td className="py-2 pl-6 pr-4 font-mono text-xs text-muted-foreground">
                                        {code}
                                    </td>
                                    <td className="py-2 pr-4 text-right font-mono tabular-nums">
                                        {formatCLP(balance)}
                                    </td>
                                </tr>
                            ))}
                            <tr className="border-t bg-muted/40 font-semibold">
                                <td className="py-2 pl-6 pr-4">Total {title.toLowerCase()}</td>
                                <td className="py-2 pr-4 text-right font-mono tabular-nums">
                                    {formatCLP(total)}
                                </td>
                            </tr>
                        </tbody>
                    </table>
                )}
            </CardContent>
        </Card>
    );
}

/**
 * Detalle del Balance General por secciones (activos, pasivos, patrimonio).
 * Se alimenta del mismo reporte que los totales: no hay datos simulados.
 */
export function BalanceSheetTree({ data }: BalanceSheetTreeProps) {
    const hasAccounts =
        Object.keys(data.assetAccounts).length +
            Object.keys(data.liabilityAccounts).length +
            Object.keys(data.equityAccounts).length >
        0;

    if (!hasAccounts) {
        return (
            <EmptyState
                title="El balance no tiene cuentas con movimiento"
                description="Registra asientos o sube el balance de apertura para ver el detalle por cuenta."
            />
        );
    }

    return (
        <div className="grid gap-4 lg:grid-cols-2">
            <Section title="Activos" accounts={data.assetAccounts} total={data.totalAssets} />
            <div className="grid gap-4">
                <Section
                    title="Pasivos"
                    accounts={data.liabilityAccounts}
                    total={data.totalLiabilities}
                />
                <Section
                    title="Patrimonio"
                    accounts={data.equityAccounts}
                    total={data.totalEquity}
                />
            </div>
        </div>
    );
}

export default BalanceSheetTree;
