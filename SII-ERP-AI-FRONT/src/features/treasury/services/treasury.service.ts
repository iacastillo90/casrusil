import { api } from '@/lib/api';

export interface AiForecastReport {
    currentLiquidity: number;
    predictedLiquidity: number;
    trend: 'POSITIVE' | 'NEGATIVE';
    riskAlerts: string[];
    dailyForecast: { date: string, estimatedAmount: number }[];
}

export interface CashEntry {
    id: string;
    date: string;
    description: string;
    amount: number;
    reference?: string | null;
    category?: string | null;
    currency?: string;
    exchangeRate?: number;
}

export interface CreateCashEntryRequest {
    date: string;
    description: string;
    amount: number;
    reference?: string | null;
    category?: string | null;
    currency?: string;
    exchangeRate?: number;
}

export const treasuryService = {
    getAiForecast: async (days: number = 30): Promise<AiForecastReport> => {
        const response = await api.get(`/treasury/ai-forecast?days=${days}`);
        return response.data;
    },
    getCashEntries: async (): Promise<CashEntry[]> => {
        const response = await api.get('/treasury/cash-entries');
        return response.data ?? [];
    },
    createCashEntry: async (payload: CreateCashEntryRequest): Promise<void> => {
        await api.post('/treasury/cash-entries', payload);
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
