import api from '@/lib/axios';

export interface CafFolioRange {
    start: number;
    end: number;
}

export interface CafUploadResult {
    folioRange: CafFolioRange;
}

/**
 * Estado de sincronización SII
 */
export interface SiiSyncStatus {
    lastSync: string;
    nextSync: string;
    modules: {
        sii: 'SYNCED' | 'PENDING' | 'ERROR';
        caf: 'SYNCED' | 'PENDING' | 'ERROR';
        dte: 'SYNCED' | 'PENDING' | 'ERROR';
    };
    lastError?: string;
}

/**
 * Estado de un DTE
 */
export interface DteStatus {
    tipo: string;
    folio: number;
    rutEmisor: string;
    rutReceptor: string;
    estado: 'PENDIENTE' | 'ENVIADO' | 'ACEPTADO' | 'RECHAZADO';
    fechaEmision: string;
    monto: number;
}

export const siiService = {
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

    /**
     * Obtener estado de sincronización SII
     */
    getSyncStatus: async (): Promise<SiiSyncStatus> => {
        const { data } = await api.get<SiiSyncStatus>('/sii/status');
        return data;
    },

    /**
     * Forzar sincronización SII
     */
    syncSii: async (): Promise<{ success: boolean; message?: string }> => {
        const { data } = await api.post<{ success: boolean; message?: string }>('/sii/sync');
        return data;
    },

    /**
     * Obtener estado de DTE
     */
    getDteStatus: async (tipo: string, folio: number): Promise<DteStatus> => {
        const { data } = await api.get<DteStatus>(`/sii/dte/${tipo}/${folio}`);
        return data;
    },

    /**
     * Enviar DTE
     */
    sendDte: async (dteId: string): Promise<{ success: boolean; message?: string }> => {
        const { data } = await api.post<{ success: boolean; message?: string }>(`/sii/dte/${dteId}/send`);
        return data;
    },
};