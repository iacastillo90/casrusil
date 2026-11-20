import { useMutation, useQuery } from '@tanstack/react-query';
import api from '@/lib/axios';
import { accountingService } from '@/features/accounting/services/accounting.service';

export interface ReportItem {
    date: string;
    description: string;
    amount: number;
}

export type ReportType = 'sales' | 'purchases' | 'balance';

interface InvoiceApiResponse {
    folio?: number | string;
    date?: string;
    receiverRut?: string;
    totalAmount?: number;
    transactionType?: 'SALE' | 'PURCHASE';
}

const fetchInvoices = async (kind: 'SALE' | 'PURCHASE'): Promise<ReportItem[]> => {
    const { data } = await api.get<InvoiceApiResponse[]>('/invoices');
    return (data ?? [])
        .filter((invoice) =>
            kind === 'PURCHASE'
                ? invoice.transactionType === 'PURCHASE'
                : invoice.transactionType !== 'PURCHASE',
        )
        .map((invoice) => ({
            date: invoice.date ?? '',
            description: `${kind === 'PURCHASE' ? 'Compra' : 'Venta'} folio #${invoice.folio ?? 'S/N'}${
                invoice.receiverRut ? ` — ${invoice.receiverRut}` : ''
            }`,
            amount: Number(invoice.totalAmount ?? 0),
        }));
};

/**
 * Datos de los reportes descargables. Cada tipo se consulta solo cuando está
 * seleccionado; el balance usa el reporte real del backend.
 */
export const useReports = (reportType: ReportType) => {
    const sales = useQuery({
        queryKey: ['reports', 'sales'],
        queryFn: () => fetchInvoices('SALE'),
        enabled: reportType === 'sales',
    });

    const purchases = useQuery({
        queryKey: ['reports', 'purchases'],
        queryFn: () => fetchInvoices('PURCHASE'),
        enabled: reportType === 'purchases',
    });

    const balance = useQuery({
        queryKey: ['reports', 'balance'],
        queryFn: async (): Promise<ReportItem[]> => {
            const sheet = await accountingService.getBalanceSheet(new Date());
            return [
                { date: sheet.date, description: 'Total activos', amount: sheet.totalAssets },
                { date: sheet.date, description: 'Total pasivos', amount: sheet.totalLiabilities },
                { date: sheet.date, description: 'Patrimonio', amount: sheet.totalEquity },
            ];
        },
        enabled: reportType === 'balance',
    });

    const active =
        reportType === 'sales' ? sales : reportType === 'purchases' ? purchases : balance;

    const downloadExcel = useMutation({
        mutationFn: async () => {
            const response = await api.get('/reports/sales/excel', { responseType: 'blob' });
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', 'libro_ventas.xlsx');
            document.body.appendChild(link);
            link.click();
            link.remove();
            window.URL.revokeObjectURL(url);
        },
    });

    return {
        data: active.data ?? [],
        isLoading: active.isLoading,
        error: active.error,
        refetch: active.refetch,
        isFetching: active.isFetching,
        downloadExcel,
    };
};
