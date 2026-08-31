import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { crmService, CreateLeadRequest, CreateQuoteRequest, LeadStatus } from '../services/crm.service';

export const useLeads = () => {
    return useQuery({
        queryKey: ['crm-leads'],
        queryFn: crmService.getLeads
    });
};

export const useCreateLead = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (data: CreateLeadRequest) => crmService.createLead(data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['crm-leads'] });
        }
    });
};

export const useUpdateLeadStatus = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: ({ id, status }: { id: string, status: LeadStatus }) => crmService.updateLeadStatus(id, status),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['crm-leads'] });
        }
    });
};

export const useQuotes = () => {
    return useQuery({
        queryKey: ['crm-quotes'],
        queryFn: crmService.getQuotes
    });
};

export const useCreateQuote = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (data: CreateQuoteRequest) => crmService.createQuote(data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['crm-quotes'] });
        }
    });
};

export const useAcceptQuote = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id: string) => crmService.acceptQuote(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['crm-quotes'] });
        }
    });
};

export const useRejectQuote = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id: string) => crmService.rejectQuote(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['crm-quotes'] });
        }
    });
};
