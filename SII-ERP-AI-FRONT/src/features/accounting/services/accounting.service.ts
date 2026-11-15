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
 * Respuesta del backend (IncomeStatementReportDTO).
 */
interface IncomeStatementApiResponse {
    totalRevenue: number;
    totalCostOfSales: number;
    grossProfit: number;
    totalOperatingExpenses: number;
    netIncome: number;
    netIncomeMargin: number;
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

/**
 * Nodo del árbol de cuentas para Balance Sheet
 */
export interface AccountNode {
    code: string;
    name: string;
    type: 'ASSET' | 'LIABILITY' | 'EQUITY' | 'REVENUE' | 'EXPENSE';
    children?: AccountNode[];
    balance?: number;
    isLeaf: boolean;
}

interface AccountingEntryLineApiDto {
    accountCode: string;
    accountName: string;
    debit: number;
    credit: number;
}

interface AccountingEntryApiDto {
    id: string;
    entryDate: string;
    description: string;
    lines: AccountingEntryLineApiDto[];
}

const mapAccountingEntry = (entry: AccountingEntryApiDto): AccountingEntry => {
    const lines = (entry.lines ?? []).map((line, index) => ({
        id: `${entry.id}-${index}`,
        accountCode: line.accountCode,
        accountName: line.accountName,
        debit: Number(line.debit ?? 0),
        credit: Number(line.credit ?? 0),
    }));

    return {
        id: entry.id,
        date: entry.entryDate,
        description: entry.description,
        lines,
        totalDebit: lines.reduce((sum, line) => sum + line.debit, 0),
        totalCredit: lines.reduce((sum, line) => sum + line.credit, 0),
    };
};

const toIsoDate = (date: Date) => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
};

/**
 * Conciliación tributaria para impuestos (IVA, PPM, etc.)
 */
export interface TaxReconciliation {
    period: string;
    vatDebit: number;      // IVA débito
    vatCredit: number;     // IVA crédito
    ppmDebit: number;      // PPM débito
    ppmCredit: number;     // PPM crédito
    difference: number;    // Diferencia
    status: 'RESOLVED' | 'PENDING' | 'OVERPAID';
    taxableSales: number;
    taxablePurchases: number;
}

/**
 * Vista previa del F29
 */
export interface F29PreviewData {
    period: string;
    vatDebit: number;
    vatCredit: number;
    vatPayable: number;
    ppmDebit: number;
    ppmCredit: number;
    ppmPayable: number;
    totalPayable: number;
    taxableSales: number;
    taxablePurchases: number;
    salesWithVat: number;
    purchasesWithVat: number;
}

/**
 * Respuesta del endpoint F29
 */
interface F29Response {
    period: string;
    vatDebit: number;
    vatCredit: number;
    ppmDebit: number;
    ppmCredit: number;
    taxableSales: number;
    taxablePurchases: number;
}

export const accountingService = {
    /**
     * Estado de resultados para el rango [from, to].
     */
    getIncomeStatement: async (from: Date, to: Date): Promise<IncomeStatement> => {
        const { data } = await api.get<IncomeStatementApiResponse>(
            '/reports/income-statement',
            { params: { from: toIsoDate(from), to: toIsoDate(to) } },
        );

        return {
            totalRevenue: data.totalRevenue ?? 0,
            cogs: data.totalCostOfSales ?? 0,
            grossProfit: data.grossProfit ?? 0,
            adminExpenses: data.totalOperatingExpenses ?? 0,
            netIncome: data.netIncome ?? 0,
            netMargin: data.netIncomeMargin ?? 0,
        };
    },

    /**
     * Asiento contable generado por una factura.
     */
    getInvoiceAccountingEntry: async (id: string): Promise<AccountingEntry | null> => {
        const { data } = await api.get<AccountingEntryApiDto[]>(`/accounting/entries/by-invoice/${id}`);
        const first = data?.[0];
        return first ? mapAccountingEntry(first) : null;
    },

    /**
     * Trazabilidad/auditoría de una factura.
     */
    getInvoiceAuditTrail: async (id: string): Promise<AuditTrailEntry[]> => {
        const { data } = await api.get<AuditTrailEntry[]>(`/invoices/${id}/audit-trail`);
        return data ?? [];
    },

    getBalanceSheet: async (dateTo: Date): Promise<BalanceSheet> => {
        const { data } = await api.get<BalanceSheet>('/accounting/balance-sheet', {
            params: { date: toIsoDate(dateTo) },
        });
        return data;
    },

    /**
     * Conciliación de impuestos para un período
     */
    getTaxReconciliation: async (period: string): Promise<TaxReconciliation> => {
        const { data } = await api.get<F29Response>(`/accounting/f29?period=${period}`);
        
        const vatDifference = data.vatDebit - data.vatCredit;
        const ppmDifference = data.ppmDebit - data.ppmCredit;
        const totalDifference = vatDifference + ppmDifference;

        return {
            period,
            vatDebit: data.vatDebit,
            vatCredit: data.vatCredit,
            ppmDebit: data.ppmDebit,
            ppmCredit: data.ppmCredit,
            difference: totalDifference,
            status: totalDifference === 0 ? 'RESOLVED' : totalDifference < 0 ? 'OVERPAID' : 'PENDING',
            taxableSales: data.taxableSales,
            taxablePurchases: data.taxablePurchases,
        };
    },

    /**
     * Vista previa de F29
     */
    getF29Preview: async (period: string): Promise<F29PreviewData> => {
        const { data } = await api.get<F29Response>(`/accounting/f29?period=${period}`);
        
        const vatPayable = Math.max(0, data.vatDebit - data.vatCredit);
        const ppmPayable = Math.max(0, data.ppmDebit - data.ppmCredit);
        
        return {
            period,
            vatDebit: data.vatDebit,
            vatCredit: data.vatCredit,
            vatPayable,
            ppmDebit: data.ppmDebit,
            ppmCredit: data.ppmCredit,
            ppmPayable,
            totalPayable: vatPayable + ppmPayable,
            taxableSales: data.taxableSales,
            taxablePurchases: data.taxablePurchases,
            salesWithVat: data.taxableSales * 1.19,
            purchasesWithVat: data.taxablePurchases * 1.19,
        };
    },

    /**
     * Enviar F29
     */
    submitF29: async (period: string): Promise<{ success: boolean; message?: string }> => {
        const { data } = await api.post<{ success: boolean; message?: string }>(
            '/reports/f29/submit',
            { period }
        );
        return data;
    },

    /**
     * Obtener árbol de cuentas para Balance Sheet
     */
    getBalanceSheetTree: async (date: Date): Promise<AccountNode[]> => {
        const { data } = await api.get<AccountNode[]>('/accounting/accounts/tree', {
            params: { date: toIsoDate(date) },
        });
        return data;
    },
};