import { api } from '@/lib/api';

export interface BomItem {
    componentProductId: string;
    quantity: number;
}

export interface Bom {
    id: string;
    name: string;
    items: BomItem[];
}

export interface WorkOrder {
    id: string;
    targetQuantity: number;
    bomId: string;
    status: string;
}

export const mrpService = {
    getBoms: async (): Promise<Bom[]> => {
        const response = await api.get('/mrp/bom');
        return response.data;
    },
    getWorkOrders: async (): Promise<WorkOrder[]> => {
        const response = await api.get('/mrp/work-order');
        return response.data;
    },
    completeWorkOrder: async (id: string): Promise<void> => {
        await api.post(`/mrp/work-order/${id}/complete`);
    }
};
