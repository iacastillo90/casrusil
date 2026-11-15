import React from 'react';
import { Document, Page, Text, View, StyleSheet } from '@react-pdf/renderer';
import { formatCLP, formatDate } from '@/lib/formatters';
import { Invoice } from '@/features/invoicing/types/invoice.types';
import { DTE_STATUS } from '@/lib/constants';

// Styles for the PDF
const styles = StyleSheet.create({
    page: {
        flexDirection: 'column',
        padding: 30,
        fontFamily: 'Helvetica, sans-serif',
        backgroundColor: '#ffffff',
    },
    header: {
        borderBottomWidth: 2,
        borderBottomColor: '#000000',
        paddingBottom: 15,
        marginBottom: 20,
    },
    title: {
        fontSize: 24,
        fontWeight: 700,
        marginBottom: 5,
    },
    subtitle: {
        fontSize: 12,
        color: '#666666',
    },
    invoiceInfo: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        marginBottom: 30,
    },
    invoiceDetails: {
        width: '50%',
    },
    invoiceMeta: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        marginTop: 10,
    },
    metaLabel: {
        fontSize: 10,
        color: '#666666',
    },
    metaValue: {
        fontSize: 10,
        fontWeight: 500,
    },
    section: {
        marginBottom: 20,
    },
    sectionTitle: {
        fontSize: 14,
        fontWeight: 600,
        marginBottom: 10,
        paddingBottom: 5,
        borderBottomWidth: 1,
        borderBottomColor: '#e0e0e0',
    },
    tableHeader: {
        flexDirection: 'row',
        backgroundColor: '#f5f5f5',
    },
    tableRow: {
        flexDirection: 'row',
        borderStyle: 'solid',
        borderWidth: 1,
        borderLeftWidth: 0,
        borderBottomWidth: 0,
    },
    tableCol: {
        borderStyle: 'solid',
        borderWidth: 1,
        borderLeftWidth: 0,
        borderBottomWidth: 0,
        padding: 8,
    },
    descriptionCol: {
        width: '40%',
    },
    quantityCol: {
        width: '15%',
    },
    priceCol: {
        width: '15%',
    },
    totalCol: {
        width: '15%',
    },
    tableCell: {
        fontSize: 10,
    },
    tableCellBold: {
        fontSize: 10,
        fontWeight: 500,
    },
    footer: {
        marginTop: 30,
        paddingTop: 20,
        borderTopWidth: 2,
        borderTopColor: '#000000',
    },
    summaryRow: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        paddingVertical: 5,
    },
    summaryLabel: {
        fontSize: 12,
        color: '#666666',
    },
    summaryValue: {
        fontSize: 12,
        fontWeight: 500,
    },
    totalRow: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        paddingVertical: 10,
        borderTopWidth: 1,
        borderTopColor: '#000000',
    },
    totalLabel: {
        fontSize: 14,
        fontWeight: 700,
    },
    totalValue: {
        fontSize: 16,
        fontWeight: 700,
        color: '#000000',
    },
    footerText: {
        fontSize: 10,
        color: '#999999',
        textAlign: 'center',
        marginTop: 20,
    },
});

// Status colors
const getStatusColor = (status: string): string => {
    switch (status) {
        case DTE_STATUS.DRAFT:
            return '#6B7280'; // gray
        case DTE_STATUS.SIGNED:
            return '#3B82F6'; // blue
        case DTE_STATUS.SENT:
            return '#8B5CF6'; // purple
        case DTE_STATUS.ACCEPTED:
            return '#10B981'; // green
        case DTE_STATUS.REJECTED:
            return '#EF4444'; // red
        default:
            return '#6B7280';
    }
};

const getStatusLabel = (status: string): string => {
    switch (status) {
        case DTE_STATUS.DRAFT:
            return 'Borrador';
        case DTE_STATUS.SIGNED:
            return 'Firmado';
        case DTE_STATUS.SENT:
            return 'Enviado';
        case DTE_STATUS.ACCEPTED:
            return 'Aceptado';
        case DTE_STATUS.REJECTED:
            return 'Rechazado';
        default:
            return status;
    }
};

interface InvoicePDFProps {
    invoice: Invoice;
}

export const InvoicePDF: React.FC<InvoicePDFProps> = ({ invoice }) => {
    // Calculate totals
    const netAmount = invoice.lines.reduce((sum, line) => sum + (line.lineTotal || line.quantity * line.unitPrice), 0);
    const taxAmount = netAmount * 0.19; // IVA 19%
    const totalAmount = netAmount + taxAmount;

    // Format CLP without currency symbol for better PDF layout
    const formatCLPNoSymbol = (amount: number): string => {
        return new Intl.NumberFormat('es-CL', {
            style: 'decimal',
        }).format(amount);
    };

    return (
        <Document>
            <Page size="A4" style={styles.page}>
                {/* Header with Title */}
                <View style={styles.header}>
                    <Text style={styles.title}>FACTURA ELECTRÓNICA</Text>
                    {invoice.status !== DTE_STATUS.DRAFT && (
                        <View style={{ marginTop: 5 }}>
                            <Text style={styles.subtitle}>
                                Estado: {getStatusLabel(invoice.status)}
                            </Text>
                        </View>
                    )}
                </View>

                {/* Invoice Metadata */}
                <View style={styles.invoiceInfo}>
                    {/* Emisor */}
                    <View style={styles.invoiceDetails}>
                        <Text style={styles.sectionTitle}>Emisor</Text>
                        <View style={styles.invoiceMeta}>
                            <View>
                                <Text style={styles.metaLabel}>RUT Emisor:</Text>
                                <Text style={styles.metaValue}>{invoice.issuerRut || 'N/A'}</Text>
                            </View>
                        </View>
                    </View>

                    {/* Receptor */}
                    <View style={styles.invoiceDetails}>
                        <Text style={styles.sectionTitle}>Receptor</Text>
                        <View style={styles.invoiceMeta}>
                            <View>
                                <Text style={styles.metaLabel}>Razón Social:</Text>
                                <Text style={styles.metaValue}>{invoice.recipientName || 'N/A'}</Text>
                            </View>
                            <View style={{ marginTop: 5 }}>
                                <Text style={styles.metaLabel}>RUT:</Text>
                                <Text style={styles.metaValue}>{invoice.recipientRut || 'N/A'}</Text>
                            </View>
                        </View>
                    </View>
                </View>

                {/* Invoice Details */}
                <View style={styles.invoiceInfo}>
                    <View style={styles.invoiceDetails}>
                        <Text style={styles.metaLabel}>Folio:</Text>
                        <Text style={styles.metaValue}>{invoice.folio || 'Por generar'}</Text>
                    </View>
                    <View style={styles.invoiceDetails}>
                        <Text style={styles.metaLabel}>Fecha de Emisión:</Text>
                        <Text style={styles.metaValue}>{invoice.issueDate ? formatDate(invoice.issueDate) : 'N/A'}</Text>
                    </View>
                    {invoice.dueDate && (
                        <View style={styles.invoiceDetails}>
                            <Text style={styles.metaLabel}>Fecha de Vencimiento:</Text>
                            <Text style={styles.metaValue}>{invoice.dueDate ? formatDate(invoice.dueDate) : 'N/A'}</Text>
                        </View>
                    )}
                </View>

                {/* Line Items Table */}
                <View style={styles.section}>
                    <Text style={styles.sectionTitle}>Detalle de Líneas</Text>
                    <View style={styles.tableHeader}>
                        <View style={styles.descriptionCol}>
                            <Text style={styles.tableCellBold}>Descripción</Text>
                        </View>
                        <View style={styles.quantityCol}>
                            <Text style={styles.tableCellBold}>Cant.</Text>
                        </View>
                        <View style={styles.priceCol}>
                            <Text style={styles.tableCellBold}>Precio</Text>
                        </View>
                        <View style={styles.totalCol}>
                            <Text style={styles.tableCellBold}>Total</Text>
                        </View>
                    </View>
                    {invoice.lines.map((line, index) => (
                        <View key={line.id || index} style={styles.tableRow}>
                            <View style={styles.descriptionCol}>
                                <Text style={styles.tableCell}>{line.description}</Text>
                            </View>
                            <View style={styles.quantityCol}>
                                <Text style={{ ...styles.tableCell, textAlign: 'right' }}>
                                    {line.quantity}
                                </Text>
                            </View>
                            <View style={styles.priceCol}>
                                <Text style={styles.tableCell}>
                                    ${formatCLPNoSymbol(line.unitPrice)}
                                </Text>
                            </View>
                            <View style={styles.totalCol}>
                                <Text style={styles.tableCell}>
                                    ${formatCLPNoSymbol(line.lineTotal || line.quantity * line.unitPrice)}
                                </Text>
                            </View>
                        </View>
                    ))}
                </View>

                {/* Summary/Totals */}
                <View style={styles.footer}>
                    <View style={styles.summaryRow}>
                        <Text style={styles.summaryLabel}>Neto:</Text>
                        <Text style={styles.summaryValue}>${formatCLPNoSymbol(netAmount)}</Text>
                    </View>
                    <View style={styles.summaryRow}>
                        <Text style={styles.summaryLabel}>IVA (19%):</Text>
                        <Text style={styles.summaryValue}>${formatCLPNoSymbol(taxAmount)}</Text>
                    </View>
                    <View style={styles.totalRow}>
                        <Text style={styles.totalLabel}>Total:</Text>
                        <Text style={styles.totalValue}>
                            ${formatCLP(totalAmount)}
                        </Text>
                    </View>
                </View>

                {/* Footer */}
                <Text style={styles.footerText}>
                    Esta factura es un documento electrónico válido. No es necesario imprimirla.
                </Text>
                <Text style={styles.footerText}>
                    Generado el: {formatDate(new Date().toISOString())}
                </Text>
            </Page>
        </Document>
    );
};

export default InvoicePDF;