import { useQuery } from '@tanstack/react-query';
import { complianceService } from '../services/compliance.service';

export const useComplianceDashboard = (period?: string) => {
    return useQuery({
        queryKey: ['compliance', period],
        queryFn: () => complianceService.getDashboard(period),
    });
};
