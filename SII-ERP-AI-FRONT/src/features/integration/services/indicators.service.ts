import api from '@/lib/axios';

export interface EconomicIndicators {
    uf: number;
    utm: number;
    usd: number;
    source?: string;
    date?: string;
}

const emptyIndicators: EconomicIndicators = { uf: 0, utm: 0, usd: 0 };

export const indicatorsService = {
    /**
     * Indicadores económicos genéricos.
     */
    getIndicators: async (): Promise<EconomicIndicators> => {
        try {
            const { data } = await api.get<EconomicIndicators>('/indicators');
            return data ?? emptyIndicators;
        } catch {
            return emptyIndicators;
        }
    },

    /**
     * Indicadores económicos del día (UF, UTM, USD).
     * NOTA: la fuente real (ej. Mindicador.cl vía backend) queda pendiente de
     * integración; se intenta la llamada real y se propaga el error para que el
     * widget muestre el estado de "no disponible".
     */
    getTodayIndicators: async (): Promise<EconomicIndicators> => {
        const { data } = await api.get<EconomicIndicators>('/indicators/today');
        return data;
    },
};
