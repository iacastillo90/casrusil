import { api } from '@/lib/api';

export interface AiForecastReport {
    currentLiquidity: number;
    predictedLiquidity: number;
    trend: 'POSITIVE' | 'NEGATIVE';
    riskAlerts: string[];
    dailyForecast: { date: string, estimatedAmount: number }[];
}

export const treasuryService = {
    getAiForecast: async (days: number = 30): Promise<AiForecastReport> => {
        const response = await api.get(`/treasury/ai-forecast?days=${days}`);
        return response.data;
    },
    getExchangeRates: async (): Promise<Record<string, number>> => {
        try {
            const response = await api.get(`/treasury/exchange-rates`);
            return response.data;
        } catch {
            return { 'USD_CLP': 945.50, 'EUR_CLP': 1020.30, 'UF_CLP': 37500.20 };
        }
    }
};
