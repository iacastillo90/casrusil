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
        const response = await api.get('/payroll/employee/payslips');
        return response.data;
    }
};
