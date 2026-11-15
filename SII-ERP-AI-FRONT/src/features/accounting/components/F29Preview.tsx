"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { 
    FileText, 
    Calendar as CalendarIcon,
    Calculator,
    Send,
    CheckCircle2,
    AlertCircle,
    RefreshCw
} from "lucide-react";
import { accountingService, F29PreviewData } from "../services/accounting.service";
import { toast } from "sonner";

interface F29PreviewProps {
    period?: string;
    precalculatedData?: F29PreviewData;
    report?: never;  // No usar 'report' para evitar confusión con el nombre de la variable
}

const PERIODS = [
    { value: '2024-01', label: 'Enero 2024' },
    { value: '2024-02', label: 'Febrero 2024' },
    { value: '2024-03', label: 'Marzo 2024' },
    { value: '2024-04', label: 'Abril 2024' },
    { value: '2024-05', label: 'Mayo 2024' },
    { value: '2024-06', label: 'Junio 2024' },
    { value: '2024-07', label: 'Julio 2024' },
    { value: '2024-08', label: 'Agosto 2024' },
    { value: '2024-09', label: 'Septiembre 2024' },
    { value: '2024-10', label: 'Octubre 2024' },
    { value: '2024-11', label: 'Noviembre 2024' },
    { value: '2024-12', label: 'Diciembre 2024' },
];

export const F29Preview = ({ period, precalculatedData }: F29PreviewProps = {}) => {
    const [selectedPeriod, setSelectedPeriod] = useState<string>(period || '');
    const [isSubmitting, setIsSubmitting] = useState(false);

    const { 
        data: report,
        isLoading,
        error,
        refetch 
    } = useQuery({
        queryKey: ['f29-preview', selectedPeriod],
        queryFn: () => accountingService.getF29Preview(selectedPeriod),
        enabled: !!selectedPeriod,
        staleTime: 60000,
    });

    // Si se proporciona precalculatedData, usarla directamente
    const displayReport = precalculatedData || report;

    const handleRefresh = () => {
        refetch();
        toast.success("Reporte F29 actualizado");
    };

    const handleSendF29 = async () => {
        if (!selectedPeriod) {
            toast.error("Selecciona un período primero");
            return;
        }

        setIsSubmitting(true);
        try {
            await accountingService.submitF29(selectedPeriod);
            toast.success("F29 enviado correctamente");
        } catch (error) {
            toast.error("Error al enviar F29", {
                description: error instanceof Error ? error.message : "Inténtalo nuevamente"
            });
        } finally {
            setIsSubmitting(false);
        }
    };

    const calculateTotalPayable = () => {
        if (!displayReport) return 0;
        return displayReport.vatPayable + displayReport.ppmPayable;
    };

    if (isLoading) {
        return (
            <Card>
                <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                        <FileText className="h-5 w-5" />
                        Vista Previa F29
                    </CardTitle>
                </CardHeader>
                <CardContent>
                    <div className="animate-pulse space-y-4">
                        <div className="h-10 bg-gray-200 rounded" />
                        <div className="h-4 bg-gray-200 rounded w-1/3" />
                        <div className="space-y-3">
                            {[...Array(6)].map((_, i) => (
                                <div key={i} className="h-16 bg-gray-200 rounded" />
                            ))}
                        </div>
                    </div>
                </CardContent>
            </Card>
        );
    }

    return (
        <Card className="w-full">
            <CardHeader>
                <CardTitle className="flex items-center gap-2">
                    <FileText className="h-5 w-5" />
                    Vista Previa F29
                </CardTitle>
                <CardDescription>
                    Declaración de Impuesto Especial sobre Combustibles y Otros tributos
                </CardDescription>
            </CardHeader>

            <CardContent className="space-y-4">
                {/* Selector de período */}
                <div className="flex flex-col sm:flex-row gap-3">
                    <Select
                        value={selectedPeriod}
                        onValueChange={setSelectedPeriod}
                    >
                        <SelectTrigger className="w-[180px]">
                            <SelectValue placeholder="Selecciona un período" />
                        </SelectTrigger>
                        <SelectContent>
                            {PERIODS.map(p => (
                                <SelectItem key={p.value} value={p.value}>
                                    {p.label}
                                </SelectItem>
                            ))}
                        </SelectContent>
                    </Select>

                    {error && (
                        <div className="text-red-600 text-sm">
                            Error al cargar reporte: {error instanceof Error ? error.message : 'Error desconocido'}
                        </div>
                    )}

                    <Button variant="outline" onClick={handleRefresh} size="sm">
                        <RefreshCw className="h-4 w-4 mr-2" />
                        Actualizar
                    </Button>
                </div>

                {/* Reporte F29 */}
                {displayReport ? (
                    <div className="space-y-4">
                        {/* Resumen financiero */}
                        <div className="bg-gray-50 rounded-lg p-4 grid grid-cols-2 md:grid-cols-4 gap-4">
                            <div className="text-center">
                                <p className="text-xs text-muted-foreground mb-1">Período</p>
                                <p className="font-semibold">{displayReport.period}</p>
                            </div>
                            <div className="text-center">
                                <p className="text-xs text-muted-foreground mb-1">IVA Débito</p>
                                <p className="font-semibold text-red-600">
                                    -${displayReport.vatDebit?.toLocaleString('es-CL') || '0'}
                                </p>
                            </div>
                            <div className="text-center">
                                <p className="text-xs text-muted-foreground mb-1">IVA Crédito</p>
                                <p className="font-semibold text-green-600">
                                    ${displayReport.vatCredit?.toLocaleString('es-CL') || '0'}
                                </p>
                            </div>
                            <div className="text-center">
                                <p className="text-xs text-muted-foreground mb-1">IVA a Pagar</p>
                                <p className={`font-semibold ${displayReport.vatPayable > 0 ? 'text-red-600' : 'text-green-600'}`}>
                                    {displayReport.vatPayable > 0 ? '-' : ''}${Math.abs(displayReport.vatPayable)?.toLocaleString('es-CL') || '0'}
                                </p>
                            </div>
                        </div>

                        {/* Tabla de detalle */}
                        <div className="border rounded-lg overflow-hidden">
                            <table className="w-full text-sm">
                                <thead>
                                    <tr className="bg-gray-50 border-b">
                                        <th className="px-4 py-3 text-left font-medium">Concepto</th>
                                        <th className="px-4 py-3 text-right font-medium">Valor Base</th>
                                        <th className="px-4 py-3 text-right font-medium">Base Imponible</th>
                                        <th className="px-4 py-3 text-right font-medium">Impuesto</th>
                                        <th className="px-4 py-3 text-right font-medium">A Pagar</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr className="border-b hover:bg-gray-50">
                                        <td className="px-4 py-3 font-medium">Ventas Afectas</td>
                                        <td className="px-4 py-3 text-right">
                                            {displayReport.salesWithVat?.toLocaleString('es-CL') || '0'}
                                        </td>
                                        <td className="px-4 py-3 text-right">
                                            {displayReport.taxableSales?.toLocaleString('es-CL') || '0'}
                                        </td>
                                        <td className="px-4 py-3 text-right text-red-600">
                                            {displayReport.vatPayable > 0 ? '-' : ''}{Math.abs(displayReport.vatPayable)?.toLocaleString('es-CL') || '0'}
                                        </td>
                                        <td className="px-4 py-3 text-right font-medium">
                                            {displayReport.vatPayable > 0 ? '-' : ''}{Math.abs(displayReport.vatPayable)?.toLocaleString('es-CL') || '0'}
                                        </td>
                                    </tr>
                                    <tr className="border-b hover:bg-gray-50">
                                        <td className="px-4 py-3 font-medium">Compras Afectas</td>
                                        <td className="px-4 py-3 text-right">
                                            {displayReport.purchasesWithVat?.toLocaleString('es-CL') || '0'}
                                        </td>
                                        <td className="px-4 py-3 text-right">
                                            {displayReport.taxablePurchases?.toLocaleString('es-CL') || '0'}
                                        </td>
                                        <td className="px-4 py-3 text-right text-green-600">
                                            {Math.abs(displayReport.vatCredit)?.toLocaleString('es-CL') || '0'}
                                        </td>
                                        <td className="px-4 py-3 text-right font-medium text-green-600">
                                            -{Math.abs(displayReport.vatCredit)?.toLocaleString('es-CL') || '0'}
                                        </td>
                                    </tr>
                                    <tr className="border-b hover:bg-gray-50">
                                        <td className="px-4 py-3 font-medium">PPM (Propiedad)</td>
                                        <td colSpan={2} className="px-4 py-3 text-right">
                                            -
                                        </td>
                                        <td className="px-4 py-3 text-right text-red-600">
                                            {displayReport.ppmPayable > 0 ? '-' : ''}{Math.abs(displayReport.ppmPayable)?.toLocaleString('es-CL') || '0'}
                                        </td>
                                        <td className="px-4 py-3 text-right font-medium">
                                            {displayReport.ppmPayable > 0 ? '-' : ''}{Math.abs(displayReport.ppmPayable)?.toLocaleString('es-CL') || '0'}
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>

                        {/* Total a pagar */}
                        <div className="border-t pt-4">
                            <div className="bg-gray-50 rounded-lg p-4">
                                <div className="flex justify-between items-center">
                                    <div>
                                        <p className="font-medium">Total a Pagar (F29)</p>
                                        <p className="text-sm text-muted-foreground">
                                            IVA + PPM + Otros tributos
                                        </p>
                                    </div>
                                    <div className="text-right">
                                        <p className="text-2xl font-bold">
                                            {calculateTotalPayable() > 0 ? '-' : ''}{Math.abs(calculateTotalPayable())?.toLocaleString('es-CL') || '0'}
                                        </p>
                                        <p className="text-xs text-muted-foreground">CLP</p>
                                    </div>
                                </div>
                            </div>
                        </div>

                        {/* Botón de envío */}
                        <div className="flex justify-end gap-3 pt-2">
                            <Button 
                                variant="outline" 
                                size="sm"
                                disabled={isSubmitting}
                            >
                                <FileText className="h-4 w-4 mr-2" />
                                Exportar PDF
                            </Button>
                            <Button 
                                onClick={handleSendF29}
                                disabled={isSubmitting || calculateTotalPayable() > 0}
                                size="sm"
                            >
                                {isSubmitting ? (
                                    <>
                                        <RefreshCw className="h-4 w-4 mr-2 animate-spin" />
                                        Enviando...
                                    </>
                                ) : (
                                    <>
                                        <Send className="h-4 w-4 mr-2" />
                                        Enviar F29
                                    </>
                                )}
                            </Button>
                        </div>
                    </div>
                ) : (
                    <div className="text-center py-8 text-muted-foreground">
                        <CalendarIcon className="h-8 w-8 mx-auto mb-2" />
                        <p>Selecciona un período para ver el preview del F29</p>
                    </div>
                )}
            </CardContent>
        </Card>
    );
};

export default F29Preview;