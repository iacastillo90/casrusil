import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { mrpService } from '../services/mrp.service';

export const useBoms = () => useQuery({ queryKey: ['mrp', 'boms'], queryFn: mrpService.getBoms });
export const useWorkOrders = () => useQuery({ queryKey: ['mrp', 'orders'], queryFn: mrpService.getWorkOrders });

export const useCompleteWorkOrder = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: mrpService.completeWorkOrder,
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['mrp', 'orders'] })
    });
};