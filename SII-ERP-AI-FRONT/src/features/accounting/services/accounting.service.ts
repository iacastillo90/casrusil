import api from '@/lib/axios';

/**
 * Estado de Resultados (Income Statement) para un período.
 */
export interface IncomeStatement {
    totalRevenue: number;
    cogs: number;
    grossProfit: number;
    adminExpenses: number;
    netIncome: number;
    netMargin: number;
}

/**
 * Línea de un asiento contable (partida doble).
 */
export interface AccountingEntryLine {
    id: string;
    accountCode: string;
    accountName: string;
    debit: number;
    credit: number;
}

/**
 * Asiento contable asociado a un documento (ej. factura).
 */
export interface AccountingEntry {
    id: string;
    date: string;
    description: string;
    lines: AccountingEntryLine[];
    totalDebit: number;
    totalCredit: number;
}

/**
 * Evento de trazabilidad/auditoría de un documento.
 */
export interface AuditTrailEntry {
    id: string;
    timestamp: string;
    action: string;
    user: string;
    detail?: string;
}

export interface BalanceSheet {
    totalAssets: number;
    totalLiabilities: number;
    totalEquity: number;
}

const emptyIncomeStatement: IncomeStatement = {
    totalRevenue: 0,
    cogs: 0,
    grossProfit: 0,
    adminExpenses: 0,
    netIncome: 0,
    netMargin: 0,
};

const emptyBalanceSheet: BalanceSheet = {
    totalAssets: 0,
    totalLiabilities: 0,
    totalEquity: 0,
};

export const accountingService = {
    /**
     * Estado de resultados para el rango [from, to].
     * NOTA: el endpoint backend `/accounting/income-statement` está pendiente;
     * mientras tanto se intenta la llamada real y se retorna un objeto tipado
     * vacío como respaldo para no romper la UI.
     */
    getIncomeStatement: async (from: Date, to: Date): Promise<IncomeStatement> => {
        try {
            const { data } = await api.get<IncomeStatement>('/accounting/income-statement', {
                params: { from: from.toISOString(), to: to.toISOString() },
            });
            return data ?? emptyIncomeStatement;
        } catch {
            return emptyIncomeStatement;
        }
    },

    /**
     * Asiento contable generado por una factura.
     * NOTA: integración real con el backend pendiente.
     */
    getInvoiceAccountingEntry: async (id: string): Promise<AccountingEntry | null> => {
        try {
            const { data } = await api.get<AccountingEntry>(`/accounting/entries/by-invoice/${id}`);
            return data ?? null;
        } catch {
            return null;
        }
    },

    /**
     * Trazabilidad/auditoría de una factura.
     * NOTA: integración real con el backend pendiente.
     */
    getInvoiceAuditTrail: async (id: string): Promise<AuditTrailEntry[]> => {
        try {
            const { data } = await api.get<AuditTrailEntry[]>(`/invoices/${id}/audit-trail`);
            return data ?? [];
        } catch {
            return [];
        }
    },

    getBalanceSheet: async (dateTo: Date): Promise<BalanceSheet> => {
        try {
            const { data } = await api.get<BalanceSheet>('/accounting/balance-sheet', {
                params: { dateTo: dateTo.toISOString() },
            });
            return data ?? emptyBalanceSheet;
        } catch {
            return emptyBalanceSheet;
        }
    },
};
