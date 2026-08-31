import { api } from '@/lib/api';

export interface PosTransactionItem {
    id?: string;
    productId: string;
    productName: string;
    quantity: number;
    unitPrice: number;
    subtotal?: number;
}

export interface PosTransaction {
    id: string;
    companyId: string;
    posTerminalId: string;
    cashierName: string;
    transactionDate: string;
    totalAmount: number;
    paymentMethod: 'CASH' | 'CARD' | 'TRANSFER';
    status: 'COMPLETED' | 'VOIDED' | 'PENDING_SYNC';
    items: PosTransactionItem[];
}

export interface RegisterSaleRequest {
    posTerminalId: string;
    cashierName: string;
    paymentMethod: string;
    items: PosTransactionItem[];
}

export const posService = {
    getTransactions: async (): Promise<PosTransaction[]> => {
        const response = await api.get('/pos/transactions');
        return response.data;
    },
    
    registerSale: async (data: RegisterSaleRequest): Promise<PosTransaction> => {
        const response = await api.post('/pos/transactions', data);
        return response.data;
    },
    
    syncTransactions: async (): Promise<void> => {
        await api.post('/pos/sync');
    }
};
