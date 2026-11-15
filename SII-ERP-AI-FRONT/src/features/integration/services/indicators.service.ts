import api from '@/lib/axios';

export interface EconomicIndicators {
    uf: number;
    utm: number;
    usd: number;
    source?: string;
    date?: string;
}

export const indicatorsService = {
    /**
     * Indicadores económicos del día (UF, UTM, USD).
     * Fuente: backend `GET /api/v1/integration/indicators/today`.
     */
    getIndicators: async (): Promise<EconomicIndicators> => {
        const { data } = await api.get<EconomicIndicators>('/integration/indicators/today');
        return data;
    },

    /**
     * Alias explícito de `getIndicators` para el widget del layout.
     */
    getTodayIndicators: async (): Promise<EconomicIndicators> => {
        const { data } = await api.get<EconomicIndicators>('/integration/indicators/today');
        return data;
    },
};
