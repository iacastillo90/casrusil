import { useQuery } from '@tanstack/react-query';
import api from '@/lib/axios';

export interface FleetMetrics {
    activeVehicles: number;
    totalVehicles: number;
    enRoute: number;
    efficiencyPercent: number;
    maintenanceScheduled: number;
}

const MOCK_FLEET_METRICS: FleetMetrics = {
    activeVehicles: 12,
    totalVehicles: 15,
    enRoute: 8,
    efficiencyPercent: 94,
    maintenanceScheduled: 2,
};

export const useFleet = () => {
    const { data: metrics, isLoading, error } = useQuery({
        queryKey: ['fleet', 'metrics'],
        queryFn: async () => {
            try {
                // Try fetching from backend if endpoint exists
                const { data } = await api.get('/fleet/metrics');
                return data as FleetMetrics;
            } catch (err) {
                // Fallback to mock if endpoint doesn't exist yet
                return MOCK_FLEET_METRICS;
            }
        },
    });

    return {
        metrics,
        isLoading,
        error
    };
};
