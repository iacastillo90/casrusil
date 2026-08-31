import { useQuery } from '@tanstack/react-query';
import { treasuryService } from '../services/treasury.service';

export const useAiForecast = (days: number = 30) => {
    return useQuery({
        queryKey: ['treasury-ai-forecast', days],
        queryFn: () => treasuryService.getAiForecast(days)
    });
};
