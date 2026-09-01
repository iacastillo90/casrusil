import { useQuery } from '@tanstack/react-query';
import { invoiceService, InvoiceListParams } from '../services/invoice.service';

export const useInvoices = (params?: InvoiceListParams) => {
    return useQuery({
        queryKey: ['invoices', params ?? {}],
        queryFn: () => invoiceService.getInvoices(params),
    });
};

export const useInvoice = (id: string) => {
    return useQuery({
        queryKey: ['invoices', id],
        queryFn: () => invoiceService.getInvoice(id),
        enabled: !!id,
    });
};
