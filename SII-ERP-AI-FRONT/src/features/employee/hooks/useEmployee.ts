import { useQuery } from '@tanstack/react-query';
import { employeeService } from '../services/employee.service';

export const useMyPayslips = () => {
    return useQuery({
        queryKey: ['employee-payslips'],
        queryFn: employeeService.getMyPayslips
    });
};
