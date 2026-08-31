import { useQuery } from '@tanstack/react-query';
import api from '@/lib/axios';

export interface ReportItem {
    date: string;
    description: string;
    amount: number;
}

export const useReports = () => {
    const { data: salesData, isLoading, error } = useQuery({
        queryKey: ['invoices'],
        queryFn: async () => {
            const { data } = await api.get('/invoices');
            return data.map((inv: any) => ({
                date: inv.date || inv.fechaEmision || new Date().toISOString().split('T')[0],
                description: `Venta Folio #${inv.folio || 'S/N'} - ${inv.issuerRut || ''}`,
                amount: inv.totalAmount || inv.montoTotal || 0
            })) as ReportItem[];
        },
    });

    const handleDownloadExcel = async (reportType: string) => {
        if (reportType === 'sales') {
            try {
                // Descargar Excel generado en backend
                const response = await api.get('/reports/sales/excel', {
                    responseType: 'blob'
                });
                
                const url = window.URL.createObjectURL(new Blob([response.data]));
                const link = document.createElement('a');
                link.href = url;
                link.setAttribute('download', 'sales_report.xlsx');
                document.body.appendChild(link);
                link.click();
                link.remove();
            } catch (err) {
                console.error("Error downloading excel", err);
            }
        }
    };

    return {
        salesData,
        isLoading,
        error,
        handleDownloadExcel
    };
};
