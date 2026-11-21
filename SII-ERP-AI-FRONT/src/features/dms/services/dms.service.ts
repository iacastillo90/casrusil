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

/** Tipos aceptados por el backend (10 MB máximo). */
export const DMS_ALLOWED_TYPES = [
    'application/pdf',
    'image/png',
    'image/jpeg',
    'text/plain',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
] as const;

export const DMS_MAX_SIZE_BYTES = 10 * 1024 * 1024;

export const dmsService = {
    uploadDocumentFile: async (
        file: File,
        metadata: { title?: string; documentType: string },
    ): Promise<DmsDocument> => {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('documentType', metadata.documentType);
        if (metadata.title) {
            formData.append('title', metadata.title);
        }
        const response = await api.post('/dms', formData, {
            headers: { 'Content-Type': 'multipart/form-data' },
        });
        return response.data;
    },
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
