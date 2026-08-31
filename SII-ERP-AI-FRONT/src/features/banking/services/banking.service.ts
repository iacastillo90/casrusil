import { api } from '@/lib/api';

export interface BankConnection {
    id: string;
    companyId: string;
    bankName: string;
    accountNumber: string;
    status: 'ACTIVE' | 'ERROR' | 'DISCONNECTED';
    lastSync: string;
}

export interface BankTransaction {
    id: string;
    companyId: string;
    bankName: string;
    accountNumber: string;
    transactionId: string;
    transactionDate: string;
    description: string;
    amount: number;
    status: 'UNRECONCILED' | 'RECONCILED' | 'IGNORED';
}

export interface ConnectBankRequest {
    bankName: string;
    accountNumber: string;
}

export const bankingService = {
    getConnections: async (): Promise<BankConnection[]> => {
        const response = await api.get('/banking/connections');
        return response.data;
    },
    
    connectBank: async (data: ConnectBankRequest): Promise<BankConnection> => {
        const response = await api.post('/banking/connections', data);
        return response.data;
    },
    
    syncTransactions: async (): Promise<void> => {
        await api.post('/banking/sync');
    },

    getTransactions: async (): Promise<BankTransaction[]> => {
        const response = await api.get('/banking/transactions');
        return response.data;
    },
    
    reconcileTransaction: async (id: string): Promise<void> => {
        await api.post(`/banking/transactions/${id}/reconcile`);
    },

    ignoreTransaction: async (id: string): Promise<void> => {
        await api.post(`/banking/transactions/${id}/ignore`);
    }
};
