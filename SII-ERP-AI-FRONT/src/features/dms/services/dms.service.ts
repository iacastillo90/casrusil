import { api } from '@/lib/api';

export const dmsService = {
    getDocuments: async () => {
        const response = await api.get('/dms');
        return response.data;
    },
    analyzeDocument: async (id: string) => {
        const response = await api.post(`/dms/${id}/analyze`);
        return response.data;
    }
};