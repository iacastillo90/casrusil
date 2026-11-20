export type DteStatus = 'DRAFT' | 'SIGNED' | 'SENT' | 'ACCEPTED' | 'REJECTED';

export interface InvoiceLine {
    id: string;
    description: string;
    quantity: number;
    unitPrice: number;
    lineTotal: number;
}

export interface Invoice {
    id: string;
    folio: number;
    status: string;
    issuerRut: string;
    /** Razón social del emisor (`businessName` en el backend). */
    issuerName?: string;
    /**
     * El backend todavía no persiste la razón social del receptor: la columna
     * `business_name` guarda la del emisor. Se mantiene vacío y la UI usa
     * `issuerName` para el emisor.
     */
    recipientName: string;
    recipientRut: string;
    lines: InvoiceLine[];
    netAmount: number;
    taxAmount: number;
    totalAmount: number;
    issueDate: string;
    dueDate?: string;
    transactionType?: 'SALE' | 'PURCHASE';
}

/**
 * Fila genérica usada por los reportes PDF (GenericReportPDF).
 * Se expone desde este módulo porque los reportes comparten el dominio
 * de facturación/contabilidad.
 */
export interface ReportItem {
    date: string;
    description: string;
    amount: number;
}
