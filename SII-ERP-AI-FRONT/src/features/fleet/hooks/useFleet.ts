import { useQuery } from '@tanstack/react-query';
import api from '@/lib/axios';

export interface FleetMetrics {
    activeVehicles: number;
    totalVehicles: number;
    enRoute: number;
    efficiencyPercent: number;
    maintenanceScheduled: number;
}

    export const useFleet = () => {
    const { data: metrics, isLoading, isFetching, error, refetch } = useQuery({
        queryKey: ['fleet', 'metrics'],
        queryFn: async () => {
            const { data } = await api.get('/fleet/metrics');
            return data as FleetMetrics;
        },
    });

    return {
        metrics,
        isLoading,
        isFetching,
        error,
        refetch
    };
};
