import { useQuery } from '@tanstack/react-query';
import api from '@/lib/axios';

export interface ActivityItem {
    id: string;
    description: string;
    amount: number;
    user: string;
    timestamp: string;
}

export const useRecentActivity = () => {
    return useQuery({
        queryKey: ['recent-activity'],
        queryFn: async () => {
            const { data } = await api.get<ActivityItem[]>('/dashboard/recent-activity');
            return data;
        },
    });
};
