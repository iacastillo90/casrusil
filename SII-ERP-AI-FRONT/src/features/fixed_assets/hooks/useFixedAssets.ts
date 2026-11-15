import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { fixedAssetsService, FixedAsset } from '../services/fixedAssets.service';

interface UseFixedAssetsResult {
    data: FixedAsset[] | undefined;
    isLoading: boolean;
    error: Error | null;
}

export const useFixedAssets = (): UseFixedAssetsResult => {
    return useQuery({
        queryKey: ['fixed-assets'],
        queryFn: () => fixedAssetsService.getAssets(),
    });
};

/**
 * Hook para ejecutar depreciación
 * Compatible con ambos patrones:
 * - useRunDepreciation().mutate()
 * - useRunDepreciation({ onSuccess: ... }).mutate()
 */
export const useRunDepreciation = () => {
    const queryClient = useQueryClient();
    
    return useMutation({
        mutationFn: fixedAssetsService.runDepreciation,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['fixed-assets'] });
        },
    });
};

interface UseFixedAssetParams {
    assetId: string;
}

export const useFixedAsset = ({ assetId }: UseFixedAssetParams) => {
    return useQuery({
        queryKey: ['fixed-asset', assetId],
        queryFn: () => fixedAssetsService.getAssetById(assetId),
        enabled: !!assetId,
    });
};