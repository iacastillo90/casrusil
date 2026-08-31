import { api } from '@/lib/api';

export const wmsService = {
    getLocations: async () => {
        const response = await api.get('/wms/locations');
        return response.data;
    },
    getPickingTasks: async () => {
        const response = await api.get('/wms/picking');
        return response.data;
    },
    completePickingTask: async (id: string) => {
        const response = await api.post(`/wms/picking/${id}/complete`);
        return response.data;
    }
};