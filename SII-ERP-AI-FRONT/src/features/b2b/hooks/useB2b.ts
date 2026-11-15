import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { b2bService } from '../services/b2b.service';

const ordersKey = ['b2b', 'orders'] as const;

export const useCustomerOrders = () => useQuery({
    queryKey: ordersKey,
    queryFn: () => b2bService.getCustomerOrders(),
});

export const useCreateOrder = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: b2bService.createOrder,
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ordersKey })
    });
};

export const usePayOrder = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: b2bService.payOrder,
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ordersKey })
    });
};
