'use client';

import { useMemo, useState } from 'react';
import {
    AlertTriangle,
    Download,
    Receipt,
    Users,
} from 'lucide-react';
import { toast } from 'sonner';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { PageHeader } from '@/features/accounting/components/PageHeader';
import { EmptyState, ErrorState, TableSkeleton } from '@/features/accounting/components/DataStates';
import { formatCLP } from '@/lib/formatters';
import { usePayroll, type Payslip } from '@/features/payroll/hooks/usePayroll';

const currentPeriod = () => {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
};

const periodLabel = (period: string) => {
    const [year, month] = period.split('-').map(Number);
    if (!year || !month) return period;
    return new Intl.DateTimeFormat('es-CL', { month: 'long', year: 'numeric' }).format(
        new Date(year, month - 1, 1),
    );
};

export default function PayrollPage() {
    const [period, setPeriod] = useState(currentPeriod);
    const [payslips, setPayslips] = useState<Payslip[] | null>(null);

    const [year, month] = period.split('-').map(Number);
    const {
        employees,
        isLoading,
        isFetching,
        error,
        refetch,
        calculatePayroll,
        exportPrevired,
    } = usePayroll();

    const totalBase = useMemo(
        () => employees.reduce((sum, employee) => sum + Number(employee.base ?? 0), 0),
        [employees],
    );

    const totals = useMemo(() => {
        if (!payslips) return null;
        return payslips.reduce(
            (acc, payslip) => ({
                discounts: acc.discounts + Number(payslip.totalDiscounts ?? 0),
                net: acc.net + Number(payslip.netSalary ?? 0),
            }),
            { discounts: 0, net: 0 },
        );
    }, [payslips]);

    const handleCalculate = async () => {
        try {
            const result = await calculatePayroll.mutateAsync({ year, month });
            setPayslips(result);
            toast.success(`Nómina de ${periodLabel(period)} calculada`, {
                description: `${result.length} liquidación${result.length === 1 ? '' : 'es'} generada${result.length === 1 ? '' : 's'}.`,
            });
        } catch (mutationError) {
            toast.error('No se pudo calcular la nómina', {
                description:
                    mutationError instanceof Error ? mutationError.message : undefined,
            });
        }
    };

    const handlePrevired = async () => {
        try {
            await exportPrevired.mutateAsync({ year, month });
            toast.success('Archivo Previred descargado');
        } catch (mutationError) {
            toast.error('No se pudo generar el archivo Previred', {
                description:
                    mutationError instanceof Error ? mutationError.message : undefined,
            });
        }
    };

    return (
        <div className="space-y-6">
            <PageHeader
                title="Remuneraciones"
                description="Nómina del período, descuentos legales y archivo Previred. Las liquidaciones se calculan en el backend."
                actions={
                    <div className="flex flex-wrap items-center gap-2">
                        <label htmlFor="payroll-period" className="text-sm text-muted-foreground">
                            Período
                        </label>
                        <Input
                            id="payroll-period"
                            type="month"
                            className="w-44"
                            value={period}
                            onChange={(event) => {
                                setPeriod(event.target.value);
                                setPayslips(null);
                            }}
                        />
                        <Button
                            variant="outline"
                            onClick={handlePrevired}
                            loading={exportPrevired.isPending}
                        >
                            {!exportPrevired.isPending && (
                                <Download aria-hidden="true" className="mr-2 h-4 w-4" />
                            )}
                            Archivo Previred
                        </Button>
                        <Button onClick={handleCalculate} loading={calculatePayroll.isPending}>
                            {!calculatePayroll.isPending && (
                                <Receipt aria-hidden="true" className="mr-2 h-4 w-4" />
                            )}
                            Calcular nómina
                        </Button>
                    </div>
                }
            />

            {error ? (
                <ErrorState
                    message={error instanceof Error ? error.message : 'Error desconocido'}
                    onRetry={() => refetch()}
                    retrying={isFetching}
                />
            ) : isLoading ? (
                <TableSkeleton rows={4} cols={4} />
            ) : (
                <>
                    <div className="grid grid-cols-1 gap-4 md:grid-cols-4">
                        <Card>
                            <CardHeader className="pb-2">
                                <CardTitle className="text-sm font-medium text-muted-foreground">
                                    Dotación activa
                                </CardTitle>
                            </CardHeader>
                            <CardContent>
                                <p className="flex items-center gap-2 text-2xl font-bold font-mono tabular-nums">
                                    <Users aria-hidden="true" className="h-5 w-5 text-muted-foreground" />
                                    {employees.length}
                                </p>
                                <p className="mt-1 text-xs text-muted-foreground">
                                    Empleados con contrato vigente
                                </p>
                            </CardContent>
                        </Card>
                        <Card>
                            <CardHeader className="pb-2">
                                <CardTitle className="text-sm font-medium text-muted-foreground">
                                    Total sueldos base
                                </CardTitle>
                            </CardHeader>
                            <CardContent>
                                <p className="text-2xl font-bold font-mono tabular-nums">
                                    {formatCLP(totalBase)}
                                </p>
                                <p className="mt-1 text-xs text-muted-foreground">
                                    {periodLabel(period)}
                                </p>
                            </CardContent>
                        </Card>
                        <Card>
                            <CardHeader className="pb-2">
                                <CardTitle className="text-sm font-medium text-muted-foreground">
                                    Descuentos legales
                                </CardTitle>
                            </CardHeader>
                            <CardContent>
                                <p className="text-2xl font-bold font-mono tabular-nums">
                                    {totals ? formatCLP(totals.discounts) : '—'}
                                </p>
                                <p className="mt-1 text-xs text-muted-foreground">
                                    {totals
                                        ? 'Calculados por el backend'
                                        : 'Calcula la nómina para verlos'}
                                </p>
                            </CardContent>
                        </Card>
                        <Card>
                            <CardHeader className="pb-2">
                                <CardTitle className="text-sm font-medium text-muted-foreground">
                                    Líquido a pagar
                                </CardTitle>
                            </CardHeader>
                            <CardContent>
                                <p className="text-2xl font-bold font-mono tabular-nums text-success">
                                    {totals ? formatCLP(totals.net) : '—'}
                                </p>
                                <p className="mt-1 text-xs text-muted-foreground">
                                    {totals ? 'Suma de liquidaciones' : 'Sin nómina calculada'}
                                </p>
                            </CardContent>
                        </Card>
                    </div>

                    {employees.length === 0 ? (
                        <EmptyState
                            icon={Users}
                            title="No hay empleados registrados"
                            description="Cuando la empresa tenga empleados con contrato vigente aparecerán acá con su AFP y sistema de salud."
                        />
                    ) : (
                        <Card>
                            <CardHeader>
                                <CardTitle className="text-base font-semibold">
                                    Detalle de nómina — {periodLabel(period)}
                                </CardTitle>
                            </CardHeader>
                            <CardContent className="p-0">
                                <div className="overflow-x-auto">
                                    <Table>
                                        <TableHeader>
                                            <TableRow>
                                                <TableHead>Empleado</TableHead>
                                                <TableHead>RUT</TableHead>
                                                <TableHead>Instituciones</TableHead>
                                                <TableHead className="text-right">Sueldo base</TableHead>
                                            </TableRow>
                                        </TableHeader>
                                        <TableBody>
                                            {employees.map((employee) => (
                                                <TableRow key={employee.id}>
                                                    <TableCell className="font-medium">
                                                        {employee.name}
                                                    </TableCell>
                                                    <TableCell className="font-mono text-xs">
                                                        {employee.rut}
                                                    </TableCell>
                                                    <TableCell>
                                                        <div className="flex gap-2">
                                                            <Badge variant="outline">{employee.afp}</Badge>
                                                            <Badge variant="outline">{employee.isapre}</Badge>
                                                        </div>
                                                    </TableCell>
                                                    <TableCell className="text-right font-mono tabular-nums">
                                                        {formatCLP(Number(employee.base ?? 0))}
                                                    </TableCell>
                                                </TableRow>
                                            ))}
                                        </TableBody>
                                    </Table>
                                </div>
                            </CardContent>
                        </Card>
                    )}

                    {payslips && payslips.length > 0 ? (
                        <Card>
                            <CardHeader>
                                <CardTitle className="text-base font-semibold">
                                    Liquidaciones calculadas — {periodLabel(period)}
                                </CardTitle>
                            </CardHeader>
                            <CardContent className="p-0">
                                <div className="overflow-x-auto">
                                    <Table>
                                        <TableHeader>
                                            <TableRow>
                                                <TableHead>Empleado</TableHead>
                                                <TableHead>RUT</TableHead>
                                                <TableHead className="text-right">Renta afecta</TableHead>
                                                <TableHead className="text-right">Descuentos</TableHead>
                                                <TableHead className="text-right">Líquido</TableHead>
                                            </TableRow>
                                        </TableHeader>
                                        <TableBody>
                                            {payslips.map((payslip) => (
                                                <TableRow key={payslip.id}>
                                                    <TableCell className="font-medium">
                                                        {payslip.employeeName ??
                                                            `Liquidación #${payslip.id}`}
                                                    </TableCell>
                                                    <TableCell className="font-mono text-xs">
                                                        {payslip.rut ?? '—'}
                                                    </TableCell>
                                                    <TableCell className="text-right font-mono tabular-nums">
                                                        {formatCLP(Number(payslip.taxableIncome ?? 0))}
                                                    </TableCell>
                                                    <TableCell className="text-right font-mono tabular-nums text-warning">
                                                        {formatCLP(Number(payslip.totalDiscounts ?? 0))}
                                                    </TableCell>
                                                    <TableCell className="text-right font-mono font-semibold tabular-nums">
                                                        {formatCLP(Number(payslip.netSalary ?? 0))}
                                                    </TableCell>
                                                </TableRow>
                                            ))}
                                        </TableBody>
                                    </Table>
                                </div>
                            </CardContent>
                        </Card>
                    ) : null}

                    {!payslips ? (
                        <div className="flex items-start gap-2 rounded-lg border border-warning-subtle-foreground/30 bg-warning-subtle p-3 text-sm text-warning-subtle-foreground">
                            <AlertTriangle aria-hidden="true" className="mt-0.5 h-4 w-4 shrink-0" />
                            <p>
                                Los descuentos y el líquido se muestran recién después de calcular la
                                nómina del período: el backend no entrega una estimación previa.
                            </p>
                        </div>
                    ) : null}
                </>
            )}
        </div>
    );
}
