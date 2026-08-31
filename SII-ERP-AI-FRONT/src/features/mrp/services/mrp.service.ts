import { api } from '@/lib/api';

export const mrpService = {
    getBoms: async () => {
        const response = await api.get('/mrp/bom');
        return response.data;
    },
    getWorkOrders: async () => {
        const response = await api.get('/mrp/work-order');
        return response.data;
    },
    completeWorkOrder: async (id: string) => {
        const response = await api.post(`/mrp/work-order/${id}/complete`);
        return response.data;
    }
};