import api from '@/lib/axios';
import { EnergyStation, EnergyMetrics, EnergyAlert, ChargeSession } from '../types/energy.types';

export const energyService = {
    getStations: async (): Promise<EnergyStation[]> => {
        const { data } = await api.get<EnergyStation[]>('/energy/stations');
        return data;
    },

    getMetrics: async (): Promise<EnergyMetrics> => {
        const { data } = await api.get<EnergyMetrics>('/energy/metrics');
        return data;
    },

    getAlerts: async (): Promise<EnergyAlert[]> => {
        const { data } = await api.get<EnergyAlert[]>('/energy/alerts');
        return data;
    },

    getRecentCharges: async (limit?: number): Promise<ChargeSession[]> => {
        const params = limit ? { params: { limit } } : {};
        const { data } = await api.get<ChargeSession[]>('/energy/charges', params);
        return data;
    },
};