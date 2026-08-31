import { api } from '@/lib/api';

export const b2bService = {
    getCustomerOrders: async (customerId: string) => {
        const response = await api.get(`/b2b/orders/${customerId}`);
        return response.data;
    },
    payOrder: async (id: string) => {
        const response = await api.post(`/b2b/orders/${id}/pay`);
        return response.data;
    }
};