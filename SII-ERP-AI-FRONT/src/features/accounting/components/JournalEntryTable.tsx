"use client";

import React, { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { 
    Search, 
    Calendar as CalendarIcon,
    FileText,
    RefreshCw,
    ChevronLeft,
    ChevronRight,
    Download
} from "lucide-react";
import { format, parseISO } from "date-fns";
import { es } from "date-fns/locale";
import { accountingService, AccountingEntry } from "../services/accounting.service";
import { toast } from "sonner";

interface JournalEntryTableProps {
    invoiceId?: string;
    dateRange?: { from: Date; to: Date };
    isLoading?: boolean;
    entries?: AccountingEntry[];
}

export const JournalEntryTable = ({ 
    invoiceId,
    dateRange,
    isLoading: externalLoading,
    entries: externalEntries 
}: JournalEntryTableProps = {}) => {
    const [selectedInvoice, setSelectedInvoice] = useState<string>(invoiceId || '');
    const [searchTerm, setSearchTerm] = useState('');
    const [currentPage, setCurrentPage] = useState(1);
    const [selectedDateRange, setSelectedDateRange] = useState(dateRange);

    // Fetch entries
    const { 
        data: entries,
        isLoading: queryLoading,
        error,
        refetch 
    } = useQuery({
        queryKey: ['accounting-entries', selectedInvoice, currentPage, searchTerm],
        queryFn: async (): Promise<AccountingEntry[]> => {
            // Si hay un invoiceId específico, usar ese
            if (selectedInvoice) {
                const entry = await accountingService.getInvoiceAccountingEntry(selectedInvoice);
                return entry ? [entry] : [];
            }
            
            // Si hay rango de fechas, buscar en ese rango
            if (selectedDateRange) {
                // TODO: Implementar endpoint para buscar asientos por rango de fechas
                return [] as AccountingEntry[];
            }
            
            // Por defecto, devolver vacío
            return [] as AccountingEntry[];
        },
        enabled: !!selectedInvoice,
        staleTime: 30000,
    });

    const isLoading = externalLoading || queryLoading || (!entries && !externalEntries);
    const displayEntries = externalEntries || entries || [];

    const filteredEntries = displayEntries.filter(entry => 
        entry.description.toLowerCase().includes(searchTerm.toLowerCase()) ||
        entry.id.toLowerCase().includes(searchTerm.toLowerCase())
    );

    const totalDebit = filteredEntries.reduce((sum: number, e: AccountingEntry) => sum + e.totalDebit, 0);
    const totalCredit = filteredEntries.reduce((sum: number, e: AccountingEntry) => sum + e.totalCredit, 0);

    const handleRefresh = () => {
        refetch();
        toast.success("Asientos actualizados");
    };

    const handleExport = () => {
        // Exportar a CSV
        const csvContent = [
            ['ID', 'Fecha', 'Descripción', 'Débito', 'Crédito', 'Diferencia'],
            ...filteredEntries.map((e: AccountingEntry) => [
                e.id,
                e.date,
                e.description,
                e.totalDebit.toString(),
                e.totalCredit.toString(),
                (e.totalDebit - e.totalCredit).toString()
            ])
        ].map(row => row.join(',')).join('\n');

        const blob = new Blob([csvContent], { type: 'text/csv' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'asientos-contables.csv';
        a.click();
        URL.revokeObjectURL(url);
    };

    if (isLoading) {
        return (
            <Card>
                <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                        <FileText className="h-5 w-5" />
                        Asientos Contables
                    </CardTitle>
                </CardHeader>
                <CardContent>
                    <div className="animate-pulse space-y-4">
                        <div className="h-10 bg-gray-200 rounded" />
                        <div className="h-4 bg-gray-200 rounded w-1/3" />
                        <div className="space-y-3">
                            {[...Array(5)].map((_, i) => (
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
                    Asientos Contables (Partida Doble)
                </CardTitle>
                <CardDescription>
                    Detalle de débitos y créditos por cuenta
                </CardDescription>
            </CardHeader>

            <CardContent className="space-y-4">
                {/* Filtros y búsqueda */}
                <div className="flex flex-col sm:flex-row gap-3">
                    <div className="relative flex-1">
                        <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                        <Input
                            placeholder="Buscar por descripción o ID..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="pl-10"
                        />
                    </div>
                    
                    {error && (
                        <div className="text-red-600 text-sm">
                            Error al cargar asientos: {error instanceof Error ? error.message : 'Error desconocido'}
                        </div>
                    )}

                    <Button variant="outline" onClick={handleRefresh} size="sm">
                        <RefreshCw className="h-4 w-4 mr-2" />
                        Actualizar
                    </Button>
                </div>

                {/* Totales y estado de partida doble */}
                <div className="bg-gray-50 rounded-lg p-4 grid grid-cols-3 gap-4 text-center">
                    <div>
                        <p className="text-xs text-muted-foreground">Total Débitos</p>
                        <p className="text-lg font-semibold text-red-600">
                            {totalDebit > 0 ? `-${totalDebit.toLocaleString('es-CL')}` : '0'}
                        </p>
                    </div>
                    <div>
                        <p className="text-xs text-muted-foreground">Total Créditos</p>
                        <p className="text-lg font-semibold text-green-600">
                            {totalCredit.toLocaleString('es-CL')}
                        </p>
                    </div>
                    <div>
                        <p className="text-xs text-muted-foreground">Diferencia</p>
                        <p className={`text-lg font-semibold ${(totalDebit - totalCredit) === 0 ? 'text-green-600' : 'text-red-600'}`}>
                            {(totalDebit - totalCredit).toLocaleString('es-CL')}
                        </p>
                    </div>
                </div>

                {/* Estado de partida doble */}
                {(totalDebit - totalCredit) !== 0 && (
                    <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-800">
                        <p className="font-medium">✗ La partida no está equilibrada</p>
                        <p>Por favor, corrige la diferencia antes de continuar</p>
                    </div>
                )}

                {/* Tabla de asientos */}
                {filteredEntries.length > 0 ? (
                    <>
                        <div className="overflow-x-auto">
                            <table className="w-full text-sm">
                                <thead>
                                    <tr className="border-b bg-gray-50">
                                        <th className="px-4 py-2 text-left font-medium">ID</th>
                                        <th className="px-4 py-2 text-left font-medium">Fecha</th>
                                        <th className="px-4 py-2 text-left font-medium">Cuenta</th>
                                        <th className="px-4 py-2 text-right font-medium">Débito</th>
                                        <th className="px-4 py-2 text-right font-medium">Crédito</th>
                                        <th className="px-4 py-2 text-left font-medium">Descripción</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {filteredEntries.map((entry: AccountingEntry) => (
                                        <React.Fragment key={entry.id}>
                                            {/* Asiento principal */}
                                            <tr className="border-b hover:bg-gray-50">
                                                <td className="px-4 py-2 font-mono text-xs bg-gray-100">
                                                    {entry.id}
                                                </td>
                                                <td className="px-4 py-2">
                                                    {format(parseISO(entry.date), 'dd/MM/yyyy', { locale: es })}
                                                </td>
                                                <td className="px-4 py-2 font-mono">
                                                    {entry.lines[0]?.accountCode || '-'}
                                                </td>
                                                <td className="px-4 py-2 text-right text-red-600">
                                                    {entry.lines[0]?.debit?.toLocaleString('es-CL') || '0'}
                                                </td>
                                                <td className="px-4 py-2 text-right text-green-600">
                                                    {entry.lines[0]?.credit?.toLocaleString('es-CL') || '0'}
                                                </td>
                                                <td className="px-4 py-2">{entry.description}</td>
                                            </tr>
                                            
                                            {/* Líneas adicionales */}
                                            {entry.lines.slice(1).map((line, idx) => (
                                                <tr key={`${entry.id}-line-${idx}`} className="border-b hover:bg-gray-50 bg-gray-50/50">
                                                    <td colSpan={5} className="px-4 py-1 text-sm pl-12">
                                                        <div className="flex items-center gap-2">
                                                            <span className="font-mono text-xs bg-gray-100 px-2 py-0.5 rounded">
                                                                {line.accountCode}
                                                            </span>
                                                            <span className="text-muted-foreground">{line.accountName}</span>
                                                        </div>
                                                    </td>
                                                    <td className="px-4 py-1 text-right">
                                                        {line.debit > 0 && (
                                                            <span className="text-red-600">{line.debit.toLocaleString('es-CL')}</span>
                                                        )}
                                                        {line.credit > 0 && (
                                                            <span className="text-green-600">{line.credit.toLocaleString('es-CL')}</span>
                                                        )}
                                                    </td>
                                                </tr>
                                            ))}
                                        </React.Fragment>
                                    ))}
                                </tbody>
                            </table>
                        </div>

                        {/* Paginación */}
                        <div className="flex justify-between items-center mt-4">
                            <p className="text-sm text-muted-foreground">
                                {filteredEntries.length} asientos mostrados
                            </p>
                            <div className="flex gap-2">
                                <Button 
                                    variant="outline" 
                                    size="sm"
                                    onClick={() => setCurrentPage(p => Math.max(p - 1, 1))}
                                    disabled={currentPage === 1}
                                >
                                    <ChevronLeft className="h-4 w-4" />
                                    Anterior
                                </Button>
                                <span className="text-sm px-2 self-center">Página {currentPage}</span>
                                <Button 
                                    variant="outline" 
                                    size="sm"
                                    onClick={() => setCurrentPage(p => p + 1)}
                                    disabled={filteredEntries.length < 10}
                                >
                                    Siguiente
                                    <ChevronRight className="h-4 w-4" />
                                </Button>
                            </div>
                        </div>
                    </>
                ) : (
                    <div className="text-center py-8 text-muted-foreground">
                        <FileText className="h-8 w-8 mx-auto mb-2" />
                        <p>No se encontraron asientos contables</p>
                        <p className="text-sm">
                            {selectedInvoice ? 'Introduce un ID de factura válido' : 'Selecciona o ingresa un ID de documento'}
                        </p>
                    </div>
                )}
            </CardContent>
        </Card>
    );
};

export default JournalEntryTable;