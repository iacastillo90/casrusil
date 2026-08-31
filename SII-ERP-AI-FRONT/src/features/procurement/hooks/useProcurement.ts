import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { procurementService, CreatePurchaseOrderRequest } from '../services/procurement.service';

export const usePurchaseOrders = () => {
    return useQuery({
        queryKey: ['purchase-orders'],
        queryFn: procurementService.getPurchaseOrders
    });
};

export const usePurchaseOrder = (id: string) => {
    return useQuery({
        queryKey: ['purchase-orders', id],
        queryFn: () => procurementService.getPurchaseOrder(id),
        enabled: !!id
    });
};

export const useCreatePurchaseOrder = () => {
    const queryClient = useQueryClient();
    
    return useMutation({
        mutationFn: (data: CreatePurchaseOrderRequest) => procurementService.createPurchaseOrder(data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['purchase-orders'] });
        }
    });
};

export const useMarkAsSent = () => {
    const queryClient = useQueryClient();
    
    return useMutation({
        mutationFn: (id: string) => procurementService.markAsSent(id),
        onSuccess: (_, id) => {
            queryClient.invalidateQueries({ queryKey: ['purchase-orders'] });
            queryClient.invalidateQueries({ queryKey: ['purchase-orders', id] });
        }
    });
};

export const useMarkAsReceived = () => {
    const queryClient = useQueryClient();
    
    return useMutation({
        mutationFn: (id: string) => procurementService.markAsReceived(id),
        onSuccess: (_, id) => {
            queryClient.invalidateQueries({ queryKey: ['purchase-orders'] });
            queryClient.invalidateQueries({ queryKey: ['purchase-orders', id] });
        }
    });
};
