import { api } from '@/lib/api';

export type LeadStatus = 'NEW' | 'CONTACTED' | 'QUALIFIED' | 'PROPOSAL_SENT' | 'WON' | 'LOST';

export interface Lead {
    id: string;
    companyId: string;
    contactName: string;
    companyName: string;
    email: string;
    phone: string;
    status: LeadStatus;
    createdAt: string;
}

export interface QuoteLine {
    id?: string;
    description: string;
    quantity: number;
    unitPrice: number;
    totalAmount?: number;
}

export interface Quote {
    id: string;
    companyId: string;
    leadId?: string;
    clientRut: string;
    clientName: string;
    quoteDate: string;
    expirationDate: string;
    status: 'DRAFT' | 'SENT' | 'ACCEPTED' | 'REJECTED';
    totalAmount: number;
    lines: QuoteLine[];
}

export interface CreateLeadRequest {
    contactName: string;
    companyName: string;
    email: string;
    phone: string;
}

export interface CreateQuoteRequest {
    leadId?: string;
    clientRut: string;
    clientName: string;
    expirationDate: string;
    lines: QuoteLine[];
}

export const crmService = {
    // --- LEADS ---
    getLeads: async (): Promise<Lead[]> => {
        const response = await api.get('/crm/leads');
        return response.data;
    },
    
    createLead: async (data: CreateLeadRequest): Promise<Lead> => {
        const response = await api.post('/crm/leads', data);
        return response.data;
    },
    
    updateLeadStatus: async (id: string, status: LeadStatus): Promise<Lead> => {
        const response = await api.put(`/crm/leads/${id}/status`, { status });
        return response.data;
    },

    // --- QUOTES ---
    getQuotes: async (): Promise<Quote[]> => {
        const response = await api.get('/crm/quotes');
        return response.data;
    },
    
    createQuote: async (data: CreateQuoteRequest): Promise<Quote> => {
        const response = await api.post('/crm/quotes', data);
        return response.data;
    },
    
    acceptQuote: async (id: string): Promise<Quote> => {
        const response = await api.post(`/crm/quotes/${id}/accept`);
        return response.data;
    },

    rejectQuote: async (id: string): Promise<Quote> => {
        const response = await api.post(`/crm/quotes/${id}/reject`);
        return response.data;
    }
};
