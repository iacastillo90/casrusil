import api from '@/lib/axios';
import type { Invoice } from '../types/invoice.types';

export interface InvoiceListParams {
    page?: number;
    pageSize?: number;
    status?: string;
}

export interface InvoiceListResponse {
    invoices: Invoice[];
    total: number;
    page: number;
    pageSize: number;
}

interface InvoiceApiDto {
    id: string;
    folio: number;
    status: string;
    issuerRut: string;
    receiverRut: string;
    businessName: string;
    date: string;
    dueDate?: string;
    netAmount: number;
    taxAmount: number;
    totalAmount: number;
    transactionType?: 'SALE' | 'PURCHASE';
    items?: InvoiceLineApiDto[];
}

interface InvoiceLineApiDto {
    lineNumber?: number;
    itemName?: string;
    itemCode?: string | null;
    quantity?: number;
    price?: number;
    amount?: number;
    unit?: string;
}

const mapInvoice = (dto: InvoiceApiDto): Invoice => ({
    id: dto.id,
    folio: dto.folio,
    status: dto.status,
    issuerRut: dto.issuerRut,
    recipientRut: dto.receiverRut,
    recipientName: dto.businessName,
    lines: (dto.items ?? []).map((item) => ({
        id: item.lineNumber != null ? String(item.lineNumber) : '',
        description: item.itemName ?? '',
        quantity: item.quantity ?? 0,
        unitPrice: item.price ?? 0,
        lineTotal: item.amount ?? 0,
    })),
    netAmount: dto.netAmount,
    taxAmount: dto.taxAmount,
    totalAmount: dto.totalAmount,
    issueDate: dto.date,
    dueDate: dto.dueDate,
    transactionType: dto.transactionType,
});

export const invoiceService = {
    /**
     * Lista de facturas de la empresa.
     * El backend devuelve un arreglo plano; se normaliza al modelo del frontend.
     */
    getInvoices: async (params?: InvoiceListParams): Promise<InvoiceListResponse> => {
        const { data } = await api.get<InvoiceApiDto[]>('/invoices', { params });
        const invoices = (data ?? []).map(mapInvoice);

        return {
            invoices,
            total: invoices.length,
            page: params?.page ?? 1,
            pageSize: params?.pageSize ?? invoices.length,
        };
    },

    /**
     * Detalle de una factura por id.
     */
    getInvoice: async (id: string): Promise<Invoice | null> => {
        const { data } = await api.get<InvoiceApiDto | null>(`/invoices/${id}`);
        return data ? mapInvoice(data) : null;
    },

    /**
     * Envía la factura por email al destinatario indicado.
     */
    sendInvoice: async (id: string, email: string): Promise<void> => {
        await api.post(`/invoices/${id}/send`, null, { params: { email } });
    },

    /**
     * Crea una nueva factura.
     */
    createInvoice: async (invoice: Partial<Invoice> & { lines: InvoiceLineApiDto[] }): Promise<Invoice> => {
        const { data } = await api.post<InvoiceApiDto>('/invoices', invoice);
        return mapInvoice(data);
    },

    /**
     * Actualiza una factura existente.
     */
    updateInvoice: async (id: string, invoice: Partial<Invoice> & { lines: InvoiceLineApiDto[] }): Promise<Invoice> => {
        const { data } = await api.put<InvoiceApiDto>(`/invoices/${id}`, invoice);
        return mapInvoice(data);
    },
};
