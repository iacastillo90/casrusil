import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { bankingService, ConnectBankRequest } from '../services/banking.service';

export const useBankConnections = () => {
    return useQuery({
        queryKey: ['bank-connections'],
        queryFn: bankingService.getConnections
    });
};

export const useConnectBank = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (data: ConnectBankRequest) => bankingService.connectBank(data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['bank-connections'] });
        }
    });
};

export const useBankTransactions = () => {
    return useQuery({
        queryKey: ['bank-transactions'],
        queryFn: bankingService.getTransactions
    });
};

export const useSyncBankTransactions = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: () => bankingService.syncTransactions(),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['bank-transactions'] });
        }
    });
};

export const useReconcileTransaction = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id: string) => bankingService.reconcileTransaction(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['bank-transactions'] });
        }
    });
};

export const useIgnoreTransaction = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id: string) => bankingService.ignoreTransaction(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['bank-transactions'] });
        }
    });
};
