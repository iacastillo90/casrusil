import { api } from '@/lib/api';

export const fixedAssetsService = {
    getAssets: async () => {
        const response = await api.get('/fixed-assets');
        return response.data;
    },
    runDepreciation: async () => {
        const response = await api.post('/fixed-assets/run-depreciation');
        return response.data;
    }
};