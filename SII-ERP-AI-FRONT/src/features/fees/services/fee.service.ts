import api from '@/lib/axios';

/**
 * Boleta de Honorarios Electrónica (BHE) recibida.
 */
export interface Fee {
    id: string;
    folio: number;
    issuerRut: string;
    issuerName: string;
    issueDate: string;
    grossAmount: number;
    retentionAmount: number;
    netAmount: number;
    status: string;
}

export interface GetFeesParams {
    companyId: string;
}

export const feeService = {
    /**
     * Lista las boletas de honorarios de la empresa.
     * NOTA: integración real con el backend pendiente; se intenta la llamada
     * real y se retorna un arreglo tipado vacío como respaldo.
     */
    getFees: async (params: GetFeesParams): Promise<Fee[]> => {
        try {
            const { data } = await api.get<Fee[]>('/fees', { params });
            return data ?? [];
        } catch {
            return [];
        }
    },
};
