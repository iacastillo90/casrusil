import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { posService, RegisterSaleRequest } from '../services/pos.service';

export const usePosTransactions = () => {
    return useQuery({
        queryKey: ['pos-transactions'],
        queryFn: posService.getTransactions
    });
};

export const useRegisterSale = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (data: RegisterSaleRequest) => posService.registerSale(data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['pos-transactions'] });
        }
    });
};

export const useSyncTransactions = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: () => posService.syncTransactions(),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['pos-transactions'] });
        }
    });
};
