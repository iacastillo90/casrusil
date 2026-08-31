import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { b2bService } from '../services/b2b.service';

export const useCustomerOrders = (customerId: string) => useQuery({ 
    queryKey: ['b2b', 'orders', customerId], 
    queryFn: () => b2bService.getCustomerOrders(customerId),
    enabled: !!customerId
});

export const usePayOrder = (customerId: string) => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: b2bService.payOrder,
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['b2b', 'orders', customerId] })
    });
};