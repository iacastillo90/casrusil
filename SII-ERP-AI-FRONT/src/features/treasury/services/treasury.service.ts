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
    }
};
