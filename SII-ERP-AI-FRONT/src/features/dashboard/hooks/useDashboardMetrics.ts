import { useQuery } from '@tanstack/react-query';
import api from '@/lib/axios';

export interface MonthlySalesPoint {
    name: string;
    total: number;
}

export interface DashboardMetrics {
    currentMonthSales: number;
    currentMonthVat: number;
    currentMonthCount: number;
    previousMonthSales: number;
    salesDeltaPct: number | null;
    pendingCount: number;
    pendingTotal: number;
    averageTicket: number;
    monthlySales: MonthlySalesPoint[];
}

interface DashboardMetricsApiResponse {
    currentMonthSales: number;
    currentMonthVat: number;
    currentMonthCount: number;
    previousMonthSales: number;
    salesDeltaPct: number | null;
    pendingCount: number;
    pendingTotal: number;
    averageTicket: number;
    monthlySales: MonthlySalesPoint[];
}

const fetchDashboardMetrics = async (): Promise<DashboardMetricsApiResponse> => {
    const { data } = await api.get('/dashboard/finance/metrics');
    return data;
};

export const useDashboardMetrics = () => {
    const query = useQuery<DashboardMetricsApiResponse>({
        queryKey: ['dashboard-metrics'],
        queryFn: fetchDashboardMetrics,
    });

    return {
        ...query,
        metrics: query.data,
    };
};