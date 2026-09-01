import api from '@/lib/axios';

export interface CafFolioRange {
    start: number;
    end: number;
}

export interface CafUploadResult {
    folioRange: CafFolioRange;
}

export interface SiiSyncStatus {
    id: string;
    documentType: string;
    lastSync: string;
    status: string;
    pendingCount: number;
}

export const siiService = {
    /**
     * Estado de sincronización con el SII por tipo de documento.
     * NOTA: integración real con el backend pendiente.
     */
    getSyncStatus: async (): Promise<SiiSyncStatus[]> => {
        try {
            const { data } = await api.get<SiiSyncStatus[]>('/sii/sync-status');
            return data ?? [];
        } catch {
            return [];
        }
    },

    /**
     * Sube un archivo CAF (XML) al backend y devuelve el rango de folios cargado.
     */
    uploadCAF: async (file: File): Promise<CafUploadResult> => {
        const formData = new FormData();
        formData.append('file', file);
        const { data } = await api.post<CafUploadResult>('/sii/caf', formData, {
            headers: { 'Content-Type': 'multipart/form-data' },
        });
        return data;
    },
};
