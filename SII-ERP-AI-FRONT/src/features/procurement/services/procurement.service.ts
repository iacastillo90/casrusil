import { api } from '@/lib/api';

export interface PurchaseOrderLine {
    id?: string;
    productId: string;
    description: string;
    quantity: number;
    unitPrice: number;
    totalAmount?: number;
}

export interface PurchaseOrder {
    id: string;
    companyId: string;
    supplierRut: string;
    supplierName: string;
    orderDate: string;
    expectedDeliveryDate: string;
    status: 'DRAFT' | 'SENT' | 'PARTIALLY_RECEIVED' | 'RECEIVED' | 'INVOICED' | 'CANCELLED';
    totalAmount: number;
    lines: PurchaseOrderLine[];
}

export interface CreatePurchaseOrderRequest {
    supplierRut: string;
    supplierName: string;
    expectedDeliveryDate: string;
    lines: {
        /** null = línea libre sin producto de inventario (el backend lo acepta). */
        productId: string | null;
        description: string;
        quantity: number;
        unitPrice: number;
    }[];
}

export const procurementService = {
    getPurchaseOrders: async (): Promise<PurchaseOrder[]> => {
        const response = await api.get('/procurement/orders');
        return response.data;
    },
    
    getPurchaseOrder: async (id: string): Promise<PurchaseOrder> => {
        const response = await api.get(`/procurement/orders/${id}`);
        return response.data;
    },
    
    createPurchaseOrder: async (data: CreatePurchaseOrderRequest): Promise<PurchaseOrder> => {
        const response = await api.post('/procurement/orders', data);
        return response.data;
    },
    
    markAsSent: async (id: string): Promise<PurchaseOrder> => {
        const response = await api.post(`/procurement/orders/${id}/sent`);
        return response.data;
    },
    
    markAsReceived: async (id: string): Promise<PurchaseOrder> => {
        const response = await api.post(`/procurement/orders/${id}/received`);
        return response.data;
    }
};
