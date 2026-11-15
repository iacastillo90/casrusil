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
    year?: number;
}

export const feeService = {
    /**
     * Lista las boletas de honorarios de la empresa.
     */
    getFees: async (params: GetFeesParams): Promise<Fee[]> => {
        const { data } = await api.get<Fee[]>('/fees', { params });
        return data ?? [];
    },
};
