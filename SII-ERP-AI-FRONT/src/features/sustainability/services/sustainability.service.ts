import { api } from '@/lib/api';

export interface GreenPassport {
    financialHealth: number;
    environmentalHealth: number;
    carbonFootprint: number;
    greenSuppliers: number;
    riskLevel: string;
}

export interface EsgReport {
    scope1Emissions: number;
    scope2Emissions: number;
    scope3Emissions: number;
    totalEmissions: number;
    aiRecommendations: string[];
}

export interface CorfoSimulation {
    eligibilityStatus: string;
    taxRebatePercentage: number;
    preferentialCreditRate: number;
    nextLevelRequirement: string;
}

export const sustainabilityService = {
    getGreenPassport: async (): Promise<GreenPassport> => {
        const response = await api.get('/sustainability/score');
        return response.data;
    },
    getEsgReport: async (): Promise<EsgReport> => {
        const response = await api.get('/sustainability/esg-report');
        return response.data;
    },
    getCorfoSimulation: async (): Promise<CorfoSimulation> => {
        const response = await api.get('/sustainability/corfo-simulator');
        return response.data;
    }
};
