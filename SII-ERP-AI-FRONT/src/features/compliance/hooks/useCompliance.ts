import { useQuery } from '@tanstack/react-query';
import axios from 'axios';

export const useComplianceDashboard = (period?: string) => {
    return useQuery({
        queryKey: ['compliance', period],
        queryFn: async () => {
            const url = period ? `/api/v1/compliance/dashboard?period=${period}` : '/api/v1/compliance/dashboard';
            const { data } = await axios.get(url, { headers: { 'X-Tenant-ID': '77.777.777-7' } });
            return data;
        }
    });
};