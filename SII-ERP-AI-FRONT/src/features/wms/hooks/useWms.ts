import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { wmsService } from '../services/wms.service';

export const useLocations = () => useQuery({ queryKey: ['wms', 'locations'], queryFn: wmsService.getLocations });
export const usePickingTasks = () => useQuery({ queryKey: ['wms', 'picking'], queryFn: wmsService.getPickingTasks });

export const useCompletePickingTask = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: wmsService.completePickingTask,
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['wms', 'picking'] })
    });
};