import { api } from '@/lib/api';

export interface Payslip {
    id: number;
    taxableIncome: number;
    nonTaxableIncome: number;
    totalDiscounts: number;
    netSalary: number;
}

export const employeeService = {
    getMyPayslips: async (): Promise<Payslip[]> => {
        const response = await api.get('/payroll/employee/payslips', {
            headers: {
                'X-Employee-Id': '11111111-1111-1111-1111-111111111111' // Mock ID for now
            }
        });
        return response.data;
    }
};
