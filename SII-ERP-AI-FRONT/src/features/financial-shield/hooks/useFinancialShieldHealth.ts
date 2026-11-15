import { useQuery } from '@tanstack/react-query';
import { financialShieldService } from '../services/financial-shield.service';

export const useFinancialShieldHealth = () => {
    return useQuery({
        queryKey: ['financial-shield', 'health'],
        queryFn: () => financialShieldService.getHealth(),
    });
};
