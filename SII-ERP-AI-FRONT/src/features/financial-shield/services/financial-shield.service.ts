import api from '@/lib/axios';

export type HealthStatus = 'HEALTHY' | 'WARNING' | 'CRITICAL';
export type RiskSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export interface RiskFactor {
    description: string;
    severity: RiskSeverity;
}

export interface GreenScore {
    score: number;
    level: string;
    benefits: string[];
}

export interface CashFlowHealth {
    status: HealthStatus;
    projectedBalance30Days: number;
    runwayEnd: string | null;
    riskFactors: RiskFactor[];
    greenScore: GreenScore;
    offers: unknown[];
}

export const financialShieldService = {
    /**
     * Diagnóstico de salud financiera (`GET /api/v1/financial-shield/health`).
     */
    getHealth: async (): Promise<CashFlowHealth> => {
        const { data } = await api.get<CashFlowHealth>('/financial-shield/health');
        return data;
    },
};
