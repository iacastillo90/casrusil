import { useQuery } from '@tanstack/react-query';
import { sustainabilityService } from '../services/sustainability.service';

export const useGreenPassport = () => {
    return useQuery({
        queryKey: ['sustainability', 'passport'],
        queryFn: () => sustainabilityService.getGreenPassport()
    });
};

export const useEsgReport = () => {
    return useQuery({
        queryKey: ['sustainability', 'esg'],
        queryFn: () => sustainabilityService.getEsgReport()
    });
};

export const useCorfoSimulation = () => {
    return useQuery({
        queryKey: ['sustainability', 'corfo'],
        queryFn: () => sustainabilityService.getCorfoSimulation()
    });
};
