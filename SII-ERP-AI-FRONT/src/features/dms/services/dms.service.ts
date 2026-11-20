import { api } from '@/lib/api';

export interface DmsDocument {
    id: string;
    documentType: string;
    title: string;
    aiSummary?: string | null;
    expirationDate?: string | null;
}

export const dmsService = {
    getDocuments: async (): Promise<DmsDocument[]> => {
        const response = await api.get('/dms');
        return response.data;
    },
    analyzeDocument: async (id: string): Promise<DmsDocument> => {
        const response = await api.post(`/dms/${id}/analyze`);
        return response.data;
    }
};
