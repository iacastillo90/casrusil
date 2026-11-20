import { api } from '@/lib/api';

export interface DmsDocument {
    id: string;
    documentType: string;
    title: string;
    aiSummary?: string | null;
    expirationDate?: string | null;
}

export interface CreateDmsDocumentRequest {
    title: string;
    documentType: string;
    storageUrl: string;
}

export const dmsService = {
    createDocument: async (payload: CreateDmsDocumentRequest): Promise<DmsDocument> => {
        const response = await api.post('/dms', payload);
        return response.data;
    },
    getDocuments: async (): Promise<DmsDocument[]> => {
        const response = await api.get('/dms');
        return response.data;
    },
    analyzeDocument: async (id: string): Promise<DmsDocument> => {
        const response = await api.post(`/dms/${id}/analyze`);
        return response.data;
    }
};
