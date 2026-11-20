'use client';

import { useState } from 'react';
import dynamic from 'next/dynamic';
import { FileDown, FileSpreadsheet } from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { PageHeader } from '@/features/accounting/components/PageHeader';
import { EmptyState, ErrorState, TableSkeleton } from '@/features/accounting/components/DataStates';
import { formatCLP } from '@/lib/formatters';
import { useReports, type ReportType } from '@/features/reports/hooks/useReports';

const PDFDownloadButton = dynamic(
    () => import('@/components/ui/pdf-download-button'),
    { ssr: false, loading: () => <Button disabled className="w-full">Generando PDF…</Button> },
);
import { GenericReportPDF } from '@/features/reports/components/ReportTemplates';

const TITLES: Record<ReportType, string> = {
    sales: 'Libro de ventas',
    purchases: 'Libro de compras',
    balance: 'Balance general',
};

export default function ReportsPage() {
    const [reportType, setReportType] = useState<ReportType>('sales');
    const { data, isLoading, error, refetch, isFetching, downloadExcel } = useReports(reportType);

    const handleExcel = () => {
        downloadExcel.mutate(undefined, {
            onError: (mutationError) =>
                toast.error('No se pudo descargar el Excel', {
                    description:
                        mutationError instanceof Error ? mutationError.message : undefined,
                }),
        });
    };

    return (
        <div className="space-y-6">
            <PageHeader
                title="Reportes"
                description="Libros de ventas y compras, y balance general, con exportación a PDF y Excel."
            />

            <div className="grid gap-6 lg:grid-cols-[320px_1fr]">
                <Card>
                    <CardHeader>
                        <CardTitle className="text-base font-semibold">Generar reporte</CardTitle>
                        <CardDescription>
                            Elige el tipo de reporte y revisa los datos antes de descargarlo.
                        </CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-4">
                        <div className="space-y-1.5">
                            <Label htmlFor="report-type">Tipo de reporte</Label>
                            <Select
                                value={reportType}
                                onValueChange={(value) => setReportType(value as ReportType)}
                            >
                                <SelectTrigger id="report-type">
                                    <SelectValue />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value="sales">Libro de ventas</SelectItem>
                                    <SelectItem value="purchases">Libro de compras</SelectItem>
                                    <SelectItem value="balance">Balance general</SelectItem>
                                </SelectContent>
                            </Select>
                        </div>

                        <div className="flex flex-col gap-2 pt-2">
                            <PDFDownloadButton
                                document={<GenericReportPDF title={TITLES[reportType]} data={data} />}
                                fileName={`${reportType}_report.pdf`}
                                className="w-full"
                            >
                                {({ loading }: { loading: boolean }) => (
                                    <Button
                                        className="w-full"
                                        disabled={loading || isLoading || data.length === 0}
                                    >
                                        <FileDown aria-hidden="true" className="mr-2 h-4 w-4" />
                                        {loading ? 'Generando…' : 'Descargar PDF'}
                                    </Button>
                                )}
                            </PDFDownloadButton>

                            <Button
                                variant="outline"
                                className="w-full"
                                onClick={handleExcel}
                                loading={downloadExcel.isPending}
                                disabled={reportType !== 'sales'}
                            >
                                {!downloadExcel.isPending && (
                                    <FileSpreadsheet aria-hidden="true" className="mr-2 h-4 w-4" />
                                )}
                                Exportar Excel
                            </Button>
                            {reportType !== 'sales' ? (
                                <p className="text-xs text-muted-foreground">
                                    El backend solo exporta a Excel el libro de ventas.
                                </p>
                            ) : null}
                        </div>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle className="text-base font-semibold">
                            {TITLES[reportType]}
                        </CardTitle>
                    </CardHeader>
                    <CardContent className="p-0">
                        {error ? (
                            <div className="p-6">
                                <ErrorState
                                    message={
                                        error instanceof Error ? error.message : 'Error desconocido'
                                    }
                                    onRetry={() => refetch()}
                                    retrying={isFetching}
                                />
                            </div>
                        ) : isLoading ? (
                            <TableSkeleton rows={5} cols={3} className="px-6 pb-6" />
                        ) : data.length === 0 ? (
                            <EmptyState
                                title="No hay datos para este reporte"
                                description={
                                    reportType === 'purchases'
                                        ? 'No hay documentos de compra registrados en el período.'
                                        : 'Registra documentos para poder generar el reporte.'
                                }
                            />
                        ) : (
                            <div className="overflow-x-auto">
                                <table className="w-full text-sm">
                                    <caption className="sr-only">{TITLES[reportType]}</caption>
                                    <thead>
                                        <tr className="border-y bg-muted/50 text-xs text-muted-foreground">
                                            <th scope="col" className="px-4 py-2 text-left font-medium">
                                                Fecha
                                            </th>
                                            <th scope="col" className="px-4 py-2 text-left font-medium">
                                                Descripción
                                            </th>
                                            <th scope="col" className="px-4 py-2 text-right font-medium">
                                                Monto
                                            </th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {data.map((item, index) => (
                                            <tr
                                                key={`${item.description}-${index}`}
                                                className="border-b last:border-b-0"
                                            >
                                                <td className="whitespace-nowrap px-4 py-2 text-muted-foreground">
                                                    {item.date}
                                                </td>
                                                <td className="px-4 py-2">{item.description}</td>
                                                <td className="px-4 py-2 text-right font-mono tabular-nums">
                                                    {formatCLP(item.amount)}
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}
