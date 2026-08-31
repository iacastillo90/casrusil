"use client";

import { useInvoice } from "@/features/invoicing/hooks/useInvoices";
import { useParams } from "next/navigation";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { formatCLP, formatDate, formatRut } from "@/lib/formatters";
import { Badge } from "@/components/ui/badge";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import dynamic from "next/dynamic";
const PDFDownloadButton = dynamic(
    () => import("@/components/ui/pdf-download-button"),
    { ssr: false, loading: () => <Button variant="outline" disabled>Generando PDF...</Button> }
);
import { InvoicePDF } from "@/features/invoicing/components/InvoicePDF";
import { DTEStatusTracker } from "@/features/invoicing/components/DTEStatusTracker";
import { Button } from "@/components/ui/button";
import { Download, FileText, Leaf, History, Calculator } from "lucide-react";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { AccountingEntryCard } from "@/features/accounting/components/AccountingEntryCard";
import { SustainabilityCard } from "@/features/sustainability/components/SustainabilityCard";
import { InvoiceAuditTrail } from "@/features/invoicing/components/InvoiceAuditTrail";
import { accountingService } from "@/features/accounting/services/accounting.service";
import { sustainabilityService } from "@/features/sustainability/services/sustainability.service";
import { useQuery } from "@tanstack/react-query";

export default function InvoiceDetailPage() {
    const params = useParams();
    const id = params.id as string;
    const { data: invoice, isLoading } = useInvoice(id);

    // Fetch accounting entry for this invoice
    const { data: accountingEntry, isLoading: isLoadingEntry } = useQuery({
        queryKey: ['invoice-accounting-entry', id],
        queryFn: () => accountingService.getInvoiceAccountingEntry(id),
        enabled: !!id,
    });

    // Fetch sustainability data for this invoice
    const { data: sustainabilityData, isLoading: isLoadingSustainability } = useQuery({
        queryKey: ['invoice-sustainability', id],
        queryFn: () => sustainabilityService.getInvoiceSustainability(id),
        enabled: !!id,
    });

    // Fetch audit trail for this invoice
    const { data: auditTrail, isLoading: isLoadingAudit } = useQuery({
        queryKey: ['invoice-audit-trail', id],
        queryFn: () => accountingService.getInvoiceAuditTrail(id),
        enabled: !!id,
    });

    if (isLoading) {
        return (
            <div className="flex items-center justify-center min-h-[400px]">
                <div className="text-center">
                    <div className="h-8 w-8 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto mb-4" />
                    <p className="text-muted-foreground">Cargando factura...</p>
                </div>
            </div>
        );
    }

    if (!invoice) {
        return (
            <div className="flex items-center justify-center min-h-[400px]">
                <div className="text-center">
                    <FileText className="h-16 w-16 text-muted-foreground mx-auto mb-4 opacity-50" />
                    <h2 className="text-2xl font-bold mb-2">Factura no encontrada</h2>
                    <p className="text-muted-foreground">La factura solicitada no existe o no tienes permisos para verla.</p>
                </div>
            </div>
        );
    }

    return (
        <div className="space-y-6 max-w-6xl mx-auto">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Factura #{invoice.folio}</h1>
                    <p className="text-muted-foreground mt-1">
                        Vista 360° con trazabilidad completa
                    </p>
                </div>
                <div className="flex gap-2">
                    <PDFDownloadButton
                        document={<InvoicePDF invoice={invoice} />}
                        fileName={`factura-${invoice.folio}.pdf`}
                    >
                        {({ loading }: { loading: boolean }) => (
                            <Button variant="outline" disabled={loading}>
                                <Download className="mr-2 h-4 w-4" />
                                {loading ? 'Generando PDF...' : 'Descargar PDF'}
                            </Button>
                        )}
                    </PDFDownloadButton>
                </div>
            </div>

            <DTEStatusTracker status={invoice.status} />

            {/* Tabbed Interface */}
            <Tabs defaultValue="general" className="w-full">
                <TabsList className="grid w-full grid-cols-4 lg:w-auto">
                    <TabsTrigger value="general" className="gap-2">
                        <FileText className="h-4 w-4" />
                        <span className="hidden sm:inline">General</span>
                    </TabsTrigger>
                    <TabsTrigger value="contabilidad" className="gap-2">
                        <Calculator className="h-4 w-4" />
                        <span className="hidden sm:inline">Contabilidad</span>
                    </TabsTrigger>
                    <TabsTrigger value="sostenibilidad" className="gap-2">
                        <Leaf className="h-4 w-4" />
                        <span className="hidden sm:inline">Sostenibilidad</span>
                    </TabsTrigger>
                    <TabsTrigger value="trazabilidad" className="gap-2">
                        <History className="h-4 w-4" />
                        <span className="hidden sm:inline">Trazabilidad</span>
                    </TabsTrigger>
                </TabsList>

                {/* General Tab */}
                <TabsContent value="general" className="space-y-6 mt-6">
                    <div className="grid gap-6 md:grid-cols-2">
                        <Card>
                            <CardHeader>
                                <CardTitle>Emisor</CardTitle>
                            </CardHeader>
                            <CardContent>
                                <dl className="grid grid-cols-3 gap-2 text-sm">
                                    <dt className="font-medium text-muted-foreground">RUT:</dt>
                                    <dd className="col-span-2">{formatRut(invoice.issuerRut)}</dd>
                                </dl>
                            </CardContent>
                        </Card>
                        <Card>
                            <CardHeader>
                                <CardTitle>Receptor</CardTitle>
                            </CardHeader>
                            <CardContent>
                                <dl className="grid grid-cols-3 gap-2 text-sm">
                                    <dt className="font-medium text-muted-foreground">Razón Social:</dt>
                                    <dd className="col-span-2">{invoice.recipientName}</dd>
                                    <dt className="font-medium text-muted-foreground">RUT:</dt>
                                    <dd className="col-span-2">{formatRut(invoice.recipientRut)}</dd>
                                </dl>
                            </CardContent>
                        </Card>
                    </div>

                    <Card>
                        <CardHeader>
                            <CardTitle>Detalle</CardTitle>
                        </CardHeader>
                        <CardContent>
                            <Table>
                                <TableHeader>
                                    <TableRow>
                                        <TableHead>Descripción</TableHead>
                                        <TableHead className="text-right">Cant.</TableHead>
                                        <TableHead className="text-right">Precio Unit.</TableHead>
                                        <TableHead className="text-right">Total</TableHead>
                                    </TableRow>
                                </TableHeader>
                                <TableBody>
                                    {invoice.lines.map((line: any) => (
                                        <TableRow key={line.id}>
                                            <TableCell>{line.description}</TableCell>
                                            <TableCell className="text-right">{line.quantity}</TableCell>
                                            <TableCell className="text-right">{formatCLP(line.unitPrice)}</TableCell>
                                            <TableCell className="text-right">{formatCLP(line.lineTotal)}</TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>

                            <div className="mt-6 flex flex-col items-end space-y-2">
                                <div className="flex justify-between w-48">
                                    <span className="text-muted-foreground">Neto:</span>
                                    <span className="font-medium">{formatCLP(invoice.netAmount)}</span>
                                </div>
                                <div className="flex justify-between w-48">
                                    <span className="text-muted-foreground">IVA:</span>
                                    <span className="font-medium">{formatCLP(invoice.taxAmount)}</span>
                                </div>
                                <div className="flex justify-between w-48 border-t pt-2">
                                    <span className="font-bold">Total:</span>
                                    <span className="font-bold text-lg">{formatCLP(invoice.totalAmount)}</span>
                                </div>
                            </div>
                        </CardContent>
                    </Card>
                </TabsContent>

                {/* Contabilidad Tab */}
                <TabsContent value="contabilidad" className="mt-6">
                    <AccountingEntryCard
                        entry={accountingEntry ?? null}
                        isLoading={isLoadingEntry}
                    />
                </TabsContent>

                {/* Sostenibilidad Tab */}
                <TabsContent value="sostenibilidad" className="mt-6">
                    <SustainabilityCard
                        data={sustainabilityData}
                        isLoading={isLoadingSustainability}
                    />
                </TabsContent>

                {/* Trazabilidad Tab */}
                <TabsContent value="trazabilidad" className="mt-6">
                    <InvoiceAuditTrail
                        auditTrail={auditTrail}
                        isLoading={isLoadingAudit}
                    />
                </TabsContent>
            </Tabs>
        </div>
    );
}
