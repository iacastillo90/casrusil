import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '@/lib/axios';

/** Empleado expuesto por `GET /payroll/employees` (EmployeeResponse). */
export interface Employee {
    id: number;
    name: string;
    rut: string;
    base: number;
    afp: string;
    isapre: string;
}

/** Liquidación devuelta por `POST /payroll/calculate` (PayslipResponse). */
export interface Payslip {
    id: number;
    employeeName: string;
    rut: string;
    period: string;
    taxableIncome: number;
    nonTaxableIncome: number;
    totalDiscounts: number;
    netSalary: number;
}

export interface PayrollPeriod {
    year: number;
    month: number;
}

export const usePayroll = () => {
    const queryClient = useQueryClient();

    const employeesQuery = useQuery({
        queryKey: ['payroll', 'employees'],
        queryFn: async () => {
            const { data } = await api.get<Employee[]>('/payroll/employees');
            return data ?? [];
        },
    });

    /** Calcula la nómina del período: crea/actualiza las liquidaciones. */
    const calculatePayroll = useMutation({
        mutationFn: async (period: PayrollPeriod) => {
            const { data } = await api.post<Payslip[]>('/payroll/calculate', period);
            return data ?? [];
        },
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['payroll'] }),
    });

    /** Descarga el archivo Previred del período como texto plano. */
    const exportPrevired = useMutation({
        mutationFn: async (period: PayrollPeriod) => {
            const response = await api.post('/payroll/previred/export', period, {
                responseType: 'blob',
            });
            const blob = new Blob([response.data], { type: 'text/plain;charset=utf-8' });
            const url = URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.download = `previred_${period.year}_${String(period.month).padStart(2, '0')}.txt`;
            document.body.appendChild(link);
            link.click();
            link.remove();
            URL.revokeObjectURL(url);
        },
    });

    return {
        employees: employeesQuery.data ?? [],
        isLoading: employeesQuery.isLoading,
        isFetching: employeesQuery.isFetching,
        error: employeesQuery.error,
        refetch: employeesQuery.refetch,
        calculatePayroll,
        exportPrevired,
    };
};
