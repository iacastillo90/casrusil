import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '@/lib/axios';

export interface Employee {
    id: string;
    name: string;
    rut: string;
    role: string;
    base: number;
    type: string;
    afp: string;
    isapre: string;
    status: string;
}

const MOCK_EMPLOYEES: Employee[] = [
    { id: "EMP-01", name: "Juan Pérez", rut: "15.444.333-2", role: "Desarrollador Senior", base: 2500000, type: "Indefinido", afp: "Habitat", isapre: "Colmena", status: "Processed" },
    { id: "EMP-02", name: "María González", rut: "17.222.111-K", role: "Diseñadora UX", base: 1800000, type: "Indefinido", afp: "Capital", isapre: "Consalud", status: "Processed" },
    { id: "EMP-03", name: "Carlos Soto", rut: "19.888.777-5", role: "Soporte TI", base: 950000, type: "Plazo Fijo", afp: "Modelo", isapre: "Fonasa", status: "Pending" },
    { id: "EMP-04", name: "Ana Rojas", rut: "12.333.444-1", role: "Gerente Finanzas", base: 3200000, type: "Indefinido", afp: "Cuprum", isapre: "Banmédica", status: "Processed" },
];

export const usePayroll = () => {
    const queryClient = useQueryClient();

    const { data: employees, isLoading, error } = useQuery({
        queryKey: ['payroll', 'employees'],
        queryFn: async () => {
            try {
                // Try fetching from backend if endpoint exists
                const { data } = await api.get('/payroll/employees');
                return data as Employee[];
            } catch (err) {
                // Fallback to mock if endpoint doesn't exist yet
                return MOCK_EMPLOYEES;
            }
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
