import { useMutation } from '@tanstack/react-query';
import api from '@/lib/axios';

export interface PeriodCloseRequest {
    year: number;
    month: number;
}

export interface PeriodAuditResponse {
    period: string;
    issuesFound: string[];
    readyToClose: boolean;
    aiRecommendation: string;
}

export const usePeriodClose = () => {
    const auditPeriod = useMutation({
        mutationFn: async (request: PeriodCloseRequest) => {
            const { data } = await api.post<PeriodAuditResponse>('/period-close/audit', request);
            return data;
        },
    });

    const executeClose = useMutation({
        mutationFn: async (request: PeriodCloseRequest) => {
            await api.post('/period-close/execute', request);
        },
    });

    return {
        auditPeriod,
        executeClose,
    };
};
