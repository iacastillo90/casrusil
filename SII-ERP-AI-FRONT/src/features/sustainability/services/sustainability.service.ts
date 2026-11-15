import { api } from '@/lib/api';

export interface MonthlyComparison {
    month: string;
    total: number;
    sustainable: number;
}

export interface GreenProvider {
    id: string;
    name: string;
    ecoScore: number;
    totalPurchased: number;
    category?: string;
}

export interface GreenPassport {
    // Métricas de salud (compatibilidad con consumidores previos)
    financialHealth?: number;
    environmentalHealth?: number;
    carbonFootprint?: number;
    greenSuppliers?: number;
    riskLevel?: string;

    // Métricas del dashboard de sostenibilidad
    scoreLevel: string;
    ecoScore: number;
    certifications: string[];
    lastUpdated: string;
    totalPurchases: number;
    sustainablePurchases: number;
    sustainablePercentage: number;
    monthlyComparison: MonthlyComparison[];
    topGreenProviders: GreenProvider[];
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

export interface InvoiceSustainability {
    ecoScore: number;
    carbonFootprint: number;
    isGreenProvider: boolean;
    recommendations: string[];
}

const emptyGreenPassport: GreenPassport = {
    scoreLevel: 'Sin datos',
    ecoScore: 0,
    certifications: [],
    lastUpdated: new Date().toISOString(),
    totalPurchases: 0,
    sustainablePurchases: 0,
    sustainablePercentage: 0,
    monthlyComparison: [],
    topGreenProviders: [],
};

export const sustainabilityService = {
    getGreenPassport: async (): Promise<GreenPassport> => {
        try {
            const response = await api.get<Partial<GreenPassport>>('/sustainability/score');
            // Se combinan los valores del backend con los valores por defecto para
            // garantizar que todos los campos requeridos estén presentes en la UI.
            return { ...emptyGreenPassport, ...response.data };
        } catch {
            return emptyGreenPassport;
        }
    },
    getEsgReport: async (): Promise<EsgReport> => {
        const response = await api.get<EsgReport>('/sustainability/esg-report');
        return response.data;
    },
    getCorfoSimulation: async (): Promise<CorfoSimulation> => {
        const response = await api.get<CorfoSimulation>('/sustainability/corfo-simulator');
        return response.data;
    },
};
