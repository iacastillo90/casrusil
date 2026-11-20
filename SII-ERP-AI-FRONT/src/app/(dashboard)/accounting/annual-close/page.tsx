'use client';

import { useState } from 'react';
import { Calculator, FileBadge, Info } from 'lucide-react';
import { useMutation } from '@tanstack/react-query';
import api from '@/lib/axios';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { PageHeader } from '@/features/accounting/components/PageHeader';
import { ErrorState } from '@/features/accounting/components/DataStates';
import { formatCLP } from '@/lib/formatters';

interface AnnualCloseResponse {
    companyId: string;
    fiscalYear: number;
    financialResult: number;
    totalAdditions: number;
    totalDeductions: number;
    rli: number;
    calculatedTax: number;
    totalPPM: number;
    taxToPayOrReturn: number;
}

/**
 * Cierre anual (F22): cálculo preliminar de la RLI y el IDPC del año.
 * El backend devuelve un borrador; no declara nada ante el SII.
 */
export default function AnnualClosePage() {
    const currentYear = new Date().getFullYear();
    const [year, setYear] = useState(currentYear);

    const calculate = useMutation({
        mutationFn: async (fiscalYear: number) => {
            const { data } = await api.post<AnnualCloseResponse>(
                `/annual-close/f22/calculate/${fiscalYear}`,
            );
            return data;
        },
    });

    const result = calculate.data;

    return (
        <div className="space-y-6">
            <PageHeader
                title="Cierre anual (F22)"
                description="Borrador del impuesto de primera categoría del año: resultado financiero, agregados, deducciones, RLI e IDPC."
                actions={
                    <div className="flex items-center gap-2">
                        <label htmlFor="f22-year" className="text-sm text-muted-foreground">
                            Año tributario
                        </label>
                        <Input
                            id="f22-year"
                            type="number"
                            min={2000}
                            max={currentYear}
                            className="w-28"
                            value={year}
                            onChange={(event) => setYear(Number(event.target.value))}
                        />
                        <Button
                            onClick={() => calculate.mutate(year)}
                            loading={calculate.isPending}
                        >
                            {!calculate.isPending && (
                                <Calculator aria-hidden="true" className="mr-2 h-4 w-4" />
                            )}
                            Calcular F22
                        </Button>
                    </div>
                }
            />

            <div className="flex items-start gap-2 rounded-lg border border-info-subtle-foreground/20 bg-info-subtle p-3 text-sm text-info-subtle-foreground">
                <Info aria-hidden="true" className="mt-0.5 h-4 w-4 shrink-0" />
                <p>
                    El cálculo es un borrador del backend para revisión del contador. No envía la
                    declaración al SII.
                </p>
            </div>

            {calculate.error ? (
                <ErrorState
                    title="No se pudo calcular el F22"
                    message={
                        calculate.error instanceof Error
                            ? `${calculate.error.message} El cálculo requiere que la empresa tenga un perfil tributario configurado; si no existe, el backend responde 500.`
                            : 'Error desconocido'
                    }
                    onRetry={() => calculate.mutate(year)}
                />
            ) : null}

            {result ? (
                <div className="space-y-6">
                    <div className="grid gap-4 md:grid-cols-3">
                        <Card>
                            <CardContent className="p-6">
                                <p className="text-sm text-muted-foreground">
                                    Renta líquida imponible
                                </p>
                                <p className="mt-1 text-2xl font-bold font-mono tabular-nums">
                                    {formatCLP(result.rli)}
                                </p>
                            </CardContent>
                        </Card>
                        <Card>
                            <CardContent className="p-6">
                                <p className="text-sm text-muted-foreground">
                                    Impuesto de primera categoría
                                </p>
                                <p className="mt-1 text-2xl font-bold font-mono tabular-nums">
                                    {formatCLP(result.calculatedTax)}
                                </p>
                            </CardContent>
                        </Card>
                        <Card>
                            <CardContent className="p-6">
                                <p className="text-sm text-muted-foreground">
                                    A pagar o devolver
                                </p>
                                <p
                                    className={`mt-1 text-2xl font-bold font-mono tabular-nums ${
                                        result.taxToPayOrReturn > 0
                                            ? 'text-destructive'
                                            : 'text-success'
                                    }`}
                                >
                                    {formatCLP(result.taxToPayOrReturn)}
                                </p>
                            </CardContent>
                        </Card>
                    </div>

                    <Card>
                        <CardHeader className="pb-2">
                            <CardTitle className="flex items-center gap-2 text-base font-semibold">
                                <FileBadge aria-hidden="true" className="h-5 w-5 text-muted-foreground" />
                                Detalle del cálculo {result.fiscalYear}
                            </CardTitle>
                        </CardHeader>
                        <CardContent>
                            <dl className="space-y-2 text-sm">
                                {[
                                    ['Resultado financiero', result.financialResult],
                                    ['Agregados (gastos rechazados)', result.totalAdditions],
                                    ['Deducciones', result.totalDeductions],
                                    ['Renta líquida imponible', result.rli],
                                    ['IDPC calculado', result.calculatedTax],
                                    ['PPM del período', result.totalPPM],
                                    ['A pagar o devolver', result.taxToPayOrReturn],
                                ].map(([label, value]) => (
                                    <div
                                        key={String(label)}
                                        className="flex items-center justify-between gap-4 border-b py-1.5 last:border-b-0"
                                    >
                                        <dt className="text-muted-foreground">{label}</dt>
                                        <dd className="font-mono tabular-nums">
                                            {formatCLP(Number(value))}
                                        </dd>
                                    </div>
                                ))}
                            </dl>
                        </CardContent>
                    </Card>
                </div>
            ) : !calculate.isPending ? (
                <Card>
                    <CardContent className="py-10 text-center text-sm text-muted-foreground">
                        Selecciona el año tributario y calcula el borrador del F22 para ver el
                        detalle.
                    </CardContent>
                </Card>
            ) : null}
        </div>
    );
}
