import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { treasuryService, CreateCashEntryRequest } from '../services/treasury.service';

export const useAiForecast = (days: number = 30) => {
    return useQuery({
        queryKey: ['treasury-ai-forecast', days],
        queryFn: () => treasuryService.getAiForecast(days)
    });
};

export const useCashEntries = () => {
    return useQuery({
        queryKey: ['treasury-cash-entries'],
        queryFn: treasuryService.getCashEntries
    });
};

export const useCreateCashEntry = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (payload: CreateCashEntryRequest) => treasuryService.createCashEntry(payload),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['treasury-cash-entries'] });
        }
    });
};
