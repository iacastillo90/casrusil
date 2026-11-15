"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { 
    Calculator, 
    Calendar as CalendarIcon,
    FileText,
    RefreshCw,
    CheckCircle2,
    AlertCircle,
    Search
} from "lucide-react";
import { format, parseISO } from "date-fns";
import { es } from "date-fns/locale";
import { accountingService, TaxReconciliation } from "../services/accounting.service";
import { toast } from "sonner";

interface TaxReconciliationTableProps {
    period?: string;
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

export const TaxReconciliationTable = ({ period }: TaxReconciliationTableProps = {}) => {
    const [selectedPeriod, setSelectedPeriod] = useState<string>(period || '');
    const [searchTerm, setSearchTerm] = useState('');

    const { 
        data: reconciliation,
        isLoading,
        error,
        refetch 
    } = useQuery({
        queryKey: ['tax-reconciliation', selectedPeriod],
        queryFn: () => accountingService.getTaxReconciliation(selectedPeriod),
        enabled: !!selectedPeriod,
        staleTime: 60000,
    });

    const handleRefresh = () => {
        refetch();
        toast.success("Conciliación actualizada");
    };

    const getStatusBadge = (status: string) => {
        switch (status) {
            case 'RESOLVED':
                return (
                    <span className="inline-flex items-center gap-1 px-2 py-0.5 text-xs font-medium bg-green-100 text-green-800 rounded-full">
                        <CheckCircle2 className="h-3 w-3" />
                        Conciliado
                    </span>
                );
            case 'PENDING':
                return (
                    <span className="inline-flex items-center gap-1 px-2 py-0.5 text-xs font-medium bg-amber-100 text-amber-800 rounded-full">
                        <AlertCircle className="h-3 w-3" />
                        Pendiente
                    </span>
                );
            case 'OVERPAID':
                return (
                    <span className="inline-flex items-center gap-1 px-2 py-0.5 text-xs font-medium bg-blue-100 text-blue-800 rounded-full">
                        <CheckCircle2 className="h-3 w-3" />
                        Sobrepagado
                    </span>
                );
            default:
                return (
                    <span className="inline-flex items-center gap-1 px-2 py-0.5 text-xs font-medium bg-gray-100 text-gray-800 rounded-full">
                        -
                    </span>
                );
        }
    };

    if (isLoading) {
        return (
            <Card>
                <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                        <Calculator className="h-5 w-5" />
                        Conciliación Tributaria
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
                    <Calculator className="h-5 w-5" />
                    Conciliación de Impuestos
                </CardTitle>
                <CardDescription>
                    Detalle de IVA débito, crédito y diferencia por período
                </CardDescription>
            </CardHeader>

            <CardContent className="space-y-4">
                {/* Filtros */}
                <div className="flex flex-col sm:flex-row gap-3">
                    <Select
                        value={selectedPeriod}
                        onValueChange={setSelectedPeriod}
                    >
                        <SelectTrigger className="w-[180px]">
                            <SelectValue placeholder="Selecciona un período" />
                        </SelectTrigger>
                        <SelectContent>
                            {PERIODS.map(period => (
                                <SelectItem key={period.value} value={period.value}>
                                    {period.label}
                                </SelectItem>
                            ))}
                        </SelectContent>
                    </Select>

                    {error && (
                        <div className="text-red-600 text-sm">
                            Error al cargar conciliación: {error instanceof Error ? error.message : 'Error desconocido'}
                        </div>
                    )}

                    <Button variant="outline" onClick={handleRefresh} size="sm">
                        <RefreshCw className="h-4 w-4 mr-2" />
                        Actualizar
                    </Button>
                </div>

                {/* Estado */}
                {!selectedPeriod && (
                    <div className="text-center py-8 text-muted-foreground">
                        <Search className="h-8 w-8 mx-auto mb-2" />
                        <p>Selecciona un período para ver la conciliación</p>
                    </div>
                )}

                {reconciliation && (
                    <div className="space-y-4">
                        {/* Resumen financiero */}
                        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                            <div className="border rounded-lg p-4 text-center">
                                <p className="text-xs text-muted-foreground mb-1">Período</p>
                                <p className="font-semibold">{reconciliation.period}</p>
                            </div>
                            <div className="border rounded-lg p-4 text-center">
                                <p className="text-xs text-muted-foreground mb-1">Venta Afectas</p>
                                <p className="font-semibold text-green-600">
                                    {reconciliation.taxableSales?.toLocaleString('es-CL') || '0'}
                                </p>
                            </div>
                            <div className="border rounded-lg p-4 text-center">
                                <p className="text-xs text-muted-foreground mb-1">Compras Afectas</p>
                                <p className="font-semibold text-red-600">
                                    {reconciliation.taxablePurchases?.toLocaleString('es-CL') || '0'}
                                </p>
                            </div>
                            <div className="border rounded-lg p-4 text-center">
                                <p className="text-xs text-muted-foreground mb-1">Estado</p>
                                {getStatusBadge(reconciliation.status)}
                            </div>
                        </div>

                        {/* Tabla de conciliación */}
                        <div className="border rounded-lg overflow-hidden">
                            <table className="w-full text-sm">
                                <thead>
                                    <tr className="bg-gray-50 border-b">
                                        <th className="px-4 py-3 text-left font-medium">Concepto</th>
                                        <th className="px-4 py-3 text-right font-medium">Débito</th>
                                        <th className="px-4 py-3 text-right font-medium">Crédito</th>
                                        <th className="px-4 py-3 text-right font-medium">Diferencia</th>
                                        <th className="px-4 py-3 text-center font-medium">Estado</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {/* Fila de IVA */}
                                    <tr className="border-b hover:bg-gray-50">
                                        <td className="px-4 py-3 font-medium">IVA 19%</td>
                                        <td className="px-4 py-3 text-right">
                                            {reconciliation.vatDebit?.toLocaleString('es-CL') || '0'}
                                        </td>
                                        <td className="px-4 py-3 text-right">
                                            {reconciliation.vatCredit?.toLocaleString('es-CL') || '0'}
                                        </td>
                                        <td className="px-4 py-3 text-right font-medium">
                                            {(reconciliation.vatDebit - reconciliation.vatCredit)?.toLocaleString('es-CL') || '0'}
                                        </td>
                                        <td className="px-4 py-3 text-center">
                                            {Math.abs(reconciliation.vatDebit - reconciliation.vatCredit) < 1 ? 
                                                <CheckCircle2 className="h-5 w-5 text-green-600 mx-auto" /> : 
                                                <AlertCircle className="h-5 w-5 text-amber-600 mx-auto" />
                                            }
                                        </td>
                                    </tr>

                                    {/* Fila de PPM */}
                                    <tr className="border-b hover:bg-gray-50">
                                        <td className="px-4 py-3 font-medium">PPM (Impuesto a la Propiedad)</td>
                                        <td className="px-4 py-3 text-right">
                                            {reconciliation.ppmDebit?.toLocaleString('es-CL') || '0'}
                                        </td>
                                        <td className="px-4 py-3 text-right">
                                            {reconciliation.ppmCredit?.toLocaleString('es-CL') || '0'}
                                        </td>
                                        <td className="px-4 py-3 text-right font-medium">
                                            {(reconciliation.ppmDebit - reconciliation.ppmCredit)?.toLocaleString('es-CL') || '0'}
                                        </td>
                                        <td className="px-4 py-3 text-center">
                                            {Math.abs(reconciliation.ppmDebit - reconciliation.ppmCredit) < 1 ? 
                                                <CheckCircle2 className="h-5 w-5 text-green-600 mx-auto" /> : 
                                                <AlertCircle className="h-5 w-5 text-amber-600 mx-auto" />
                                            }
                                        </td>
                                    </tr>

                                    {/* Total */}
                                    <tr className="font-semibold bg-gray-50">
                                        <td className="px-4 py-3">Total Impuesto a Pagar</td>
                                        <td colSpan={2} className="px-4 py-3 text-right">
                                            -
                                        </td>
                                        <td className="px-4 py-3 text-right">
                                            {reconciliation.difference?.toLocaleString('es-CL') || '0'}
                                        </td>
                                        <td className="px-4 py-3 text-center">
                                            {getStatusBadge(reconciliation.status)}
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>

                        {/* Acciones */}
                        <div className="flex justify-end gap-3 pt-2">
                            <Button variant="outline" size="sm">
                                <FileText className="h-4 w-4 mr-2" />
                                Exportar a Excel
                            </Button>
                            <Button size="sm" disabled={reconciliation.status === 'RESOLVED'}>
                                Enviar Declaración
                            </Button>
                        </div>
                    </div>
                )}
            </CardContent>
        </Card>
    );
};

export default TaxReconciliationTable;