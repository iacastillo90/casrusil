export interface Invoice {
    id: string;
    folio: number;
    status: string;
    issuerRut?: string;
    recipientName: string;
    recipientRut?: string;
    lines?: any[];
    netAmount?: number;
    taxAmount?: number;
    totalAmount: string | number;
    issueDate: string;
}
