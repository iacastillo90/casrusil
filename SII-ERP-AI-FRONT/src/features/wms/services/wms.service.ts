import { api } from '@/lib/api';

export interface PickingItem {
    productId: string;
    locationCode: string;
    quantity: number;
}

export interface PickingTask {
    id: string;
    orderId: string;
    status: string;
    items: PickingItem[];
}

export interface WarehouseLocation {
    id: string;
    code: string;
    zone: string;
    description: string;
}

export const wmsService = {
    getLocations: async (): Promise<WarehouseLocation[]> => {
        const response = await api.get('/wms/locations');
        return response.data;
    },
    getPickingTasks: async (): Promise<PickingTask[]> => {
        const response = await api.get('/wms/picking');
        return response.data;
    },
    completePickingTask: async (id: string): Promise<void> => {
        await api.post(`/wms/picking/${id}/complete`);
    }
};
