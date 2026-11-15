import { api } from '@/lib/api';

export type B2bOrderStatus = 'PENDING' | 'PAID' | 'CANCELLED';

export interface B2bOrderItem {
    productName: string;
    quantity: number;
    unitPrice: number;
    subtotal: number;
}

export interface B2bOrder {
    id: string;
    status: B2bOrderStatus;
    totalAmount: number;
    items: B2bOrderItem[];
    createdAt: string;
}

export interface CreateOrderItemPayload {
    productName: string;
    quantity: number;
    unitPrice: number;
}

export interface CreateOrderPayload {
    items: CreateOrderItemPayload[];
}

export const b2bService = {
    getCustomerOrders: async (): Promise<B2bOrder[]> => {
        const { data } = await api.get<B2bOrder[]>('/b2b/orders/my-orders');
        return data ?? [];
    },
    createOrder: async (payload: CreateOrderPayload): Promise<B2bOrder> => {
        const { data } = await api.post<B2bOrder>('/b2b/orders', payload);
        return data;
    },
    payOrder: async (id: string): Promise<B2bOrder> => {
        const { data } = await api.post<B2bOrder>(`/b2b/orders/${id}/pay`);
        return data;
    },
};
