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

export const invoiceService = {
    /**
     * Lista paginada de facturas.
     * NOTA: se intenta la llamada real al backend y se retorna una respuesta
     * tipada vacía como respaldo.
     */
    getInvoices: async (params?: InvoiceListParams): Promise<InvoiceListResponse> => {
        try {
            const { data } = await api.get<InvoiceListResponse>('/invoices', { params });
            return data;
        } catch {
            return {
                invoices: [],
                total: 0,
                page: params?.page ?? 1,
                pageSize: params?.pageSize ?? 20,
            };
        }
    },

    /**
     * Detalle de una factura por id.
     */
    getInvoice: async (id: string): Promise<Invoice | null> => {
        try {
            const { data } = await api.get<Invoice>(`/invoices/${id}`);
            return data ?? null;
        } catch {
            return null;
        }
    },

    /**
     * Envía la factura al SII.
     */
    sendInvoice: async (id: string): Promise<void> => {
        await api.post(`/invoices/${id}/send`);
    },
};
