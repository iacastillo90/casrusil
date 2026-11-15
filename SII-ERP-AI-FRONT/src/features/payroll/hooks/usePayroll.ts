import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '@/lib/axios';

export interface Employee {
    id: string;
    name: string;
    rut: string;
    base: number;
    afp: string;
    isapre: string;
}

    export const usePayroll = () => {
    const queryClient = useQueryClient();

    const { data: employees, isLoading, error } = useQuery({
        queryKey: ['payroll', 'employees'],
        queryFn: async () => {
            const { data } = await api.get('/payroll/employees');
            return data as Employee[];
        },
    });

    const processPrevired = useMutation({
        mutationFn: async (payload: { year: number; month: number }) => {
            const { data } = await api.post('/payroll/previred/export', payload, { responseType: 'blob' });
            return data;
        }
    });

    return {
        employees: employees || [],
        isLoading,
        error,
        processPrevired
    };
};
