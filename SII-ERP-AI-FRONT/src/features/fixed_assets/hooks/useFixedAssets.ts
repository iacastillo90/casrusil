import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { fixedAssetsService } from '../services/fixedAssets.service';

export const useFixedAssets = () => useQuery({ queryKey: ['fixedAssets'], queryFn: fixedAssetsService.getAssets });

export const useRunDepreciation = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: fixedAssetsService.runDepreciation,
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['fixedAssets'] })
    });
};