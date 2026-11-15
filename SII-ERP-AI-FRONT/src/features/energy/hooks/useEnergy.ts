import { useQuery } from '@tanstack/react-query';
import { energyService } from '../services/energy.service';
import { EnergyStation, EnergyMetrics, EnergyAlert, ChargeSession } from '../types/energy.types';

export const useEnergyStations = () => {
    return useQuery({
        queryKey: ['energy-stations'],
        queryFn: () => energyService.getStations(),
    });
};

export const useEnergyMetrics = () => {
    return useQuery({
        queryKey: ['energy-metrics'],
        queryFn: () => energyService.getMetrics(),
    });
};

export const useEnergyAlerts = (enabled: boolean = true) => {
    return useQuery({
        queryKey: ['energy-alerts'],
        queryFn: () => energyService.getAlerts(),
        enabled,
    });
};

export const useRecentCharges = (limit: number = 10) => {
    return useQuery({
        queryKey: ['energy-charges', limit],
        queryFn: () => energyService.getRecentCharges(limit),
    });
};