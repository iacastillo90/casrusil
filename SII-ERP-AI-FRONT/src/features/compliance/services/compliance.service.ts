import api from '@/lib/axios';

export type AnomalySeverity = 'INFO' | 'WARNING' | 'CRITICAL';

export interface AnomalyWarning {
    code: string;
    message: string;
    severity: AnomalySeverity;
}

export interface SiiAuditStatus {
    totalErp: number;
    totalSii: number;
    isBalanced: boolean;
    missingInErp: number;
}

export interface F29Preview {
    period: string;
    vatDebit: number;
    vatCredit: number;
    vatPayable: number;
    vatRecoverable: number;
    feeWithholding: number;
    totalPayable: number;
}

export interface TaxParameters {
    id: string;
    utmValue: number;
    ufValue: number;
    bheRetentionRate: number;
}

export interface ComplianceDashboard {
    siiStatus: SiiAuditStatus;
    f29Preview: F29Preview;
    aiAlerts: AnomalyWarning[];
    acuseReciboAlerts: AnomalyWarning[];
    taxParameters: TaxParameters | null;
    globalHealthScore: number;
}

export const complianceService = {
    /**
     * Dashboard tributario unificado (`GET /api/v1/compliance/dashboard`).
     * La empresa se resuelve desde el JWT en el backend.
     */
    getDashboard: async (period?: string): Promise<ComplianceDashboard> => {
        const { data } = await api.get<ComplianceDashboard>('/compliance/dashboard', {
            params: period ? { period } : undefined,
        });
        return data;
    },
};
