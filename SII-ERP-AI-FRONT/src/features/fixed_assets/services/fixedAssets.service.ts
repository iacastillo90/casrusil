import { api } from '@/lib/api';

export interface FixedAsset {
    id: string;
    name: string;
    code: string;
    initialValue: number;
    accumulatedDepreciation: number;
    currentValue: number;
    usefulLifeMonths: number;
    depreciationMethod: 'LINEAL' | 'DECILONAL';
    acquisitionDate: string;
    status: 'ACTIVE' | 'DEPRECIATED' | 'DISPOSED';
    monthlyDepreciation: number;
}

export interface DepreciationResult {
    success: boolean;
    message?: string;
    processedAssets: number;
}

export const fixedAssetsService = {
    getAssets: async (): Promise<FixedAsset[]> => {
        const response = await api.get('/fixed-assets');
        return response.data;
    },
    
    getAssetById: async (id: string): Promise<FixedAsset> => {
        const response = await api.get(`/fixed-assets/${id}`);
        return response.data;
    },

    createAsset: async (asset: Partial<FixedAsset>): Promise<FixedAsset> => {
        const response = await api.post('/fixed-assets', asset);
        return response.data;
    },

    updateAsset: async (id: string, asset: Partial<FixedAsset>): Promise<FixedAsset> => {
        const response = await api.put(`/fixed-assets/${id}`, asset);
        return response.data;
    },

    deleteAsset: async (id: string): Promise<void> => {
        await api.delete(`/fixed-assets/${id}`);
    },

    runDepreciation: async (): Promise<DepreciationResult> => {
        const response = await api.post('/fixed-assets/run-depreciation');
        return response.data;
    },
};