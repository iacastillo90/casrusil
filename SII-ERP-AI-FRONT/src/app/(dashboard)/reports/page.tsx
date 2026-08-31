"use client";

import { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Label } from "@/components/ui/label";
import dynamic from "next/dynamic";
const PDFDownloadButton = dynamic(
    () => import("@/components/ui/pdf-download-button"),
    { ssr: false, loading: () => <Button disabled className="w-full">Generando PDF...</Button> }
);
import { GenericReportPDF } from "@/features/reports/components/ReportTemplates";
import { FileDown, FileSpreadsheet } from "lucide-react";
import { useReports } from "@/features/reports/hooks/useReports";

export default function ReportsPage() {
    const [reportType, setReportType] = useState("sales");

    const { salesData, isLoading, handleDownloadExcel } = useReports();
    
    // For other types, fallback to empty or mock
    const reportData = reportType === 'sales' && salesData ? salesData : [];

    const getReportTitle = () => {
        switch (reportType) {
            case 'sales': return 'Libro de Ventas';
            case 'purchases': return 'Libro de Compras';
            case 'balance': return 'Balance General';
            default: return 'Reporte';
        }
    };

    return (
        <div className="space-y-6">
            <h1 className="text-3xl font-bold tracking-tight">Reportes y Exportaciones</h1>

            <div className="grid gap-6 md:grid-cols-2">
                <Card>
                    <CardHeader>
                        <CardTitle>Generar Reporte</CardTitle>
                        <CardDescription>Selecciona el tipo de reporte y el formato de descarga.</CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-4">
                        <div className="space-y-2">
                            <Label>Tipo de Reporte</Label>
                            <Select value={reportType} onValueChange={setReportType}>
                                <SelectTrigger>
                                    <SelectValue />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value="sales">Libro de Ventas</SelectItem>
                                    <SelectItem value="purchases">Libro de Compras</SelectItem>
                                    <SelectItem value="balance">Balance General</SelectItem>
                                </SelectContent>
                            </Select>
                        </div>

                        <div className="flex gap-4 pt-4">
                            <PDFDownloadButton
                                document={<GenericReportPDF title={getReportTitle()} data={reportData} />}
                                fileName={`${reportType}_report.pdf`}
                                className="w-full"
                            >
                                {({ loading }: { loading: boolean }) => (
                                    <Button className="w-full" disabled={loading || isLoading}>
                                        <FileDown className="mr-2 h-4 w-4" />
                                        {loading || isLoading ? 'Generando...' : 'Descargar PDF'}
                                    </Button>
                                )}
                            </PDFDownloadButton>

                            <Button 
                                variant="outline" 
                                className="w-full" 
                                onClick={() => handleDownloadExcel(reportType)}
                                disabled={reportType !== 'sales'}
                            >
                                <FileSpreadsheet className="mr-2 h-4 w-4" />
                                Exportar Excel
                            </Button>
                        </div>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}
