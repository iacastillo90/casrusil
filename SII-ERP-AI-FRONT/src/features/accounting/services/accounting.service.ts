import api from '@/lib/axios';

/**
 * Contratos de la API de contabilidad y tributario.
 *
 * Todo lo que hay acá está verificado contra los controllers del backend
 * (`AccountingController`, `F29AlertController`, `SiiAuditController`,
 * `PeriodCloseController`). Si un endpoint no existe, no se declara.
 */

/** Estado de Resultados (GET /reports/income-statement). */
export interface IncomeStatement {
    totalRevenue: number;
    cogs: number;
    grossProfit: number;
    adminExpenses: number;
    netIncome: number;
    netMargin: number;
}

interface IncomeStatementApiResponse {
    totalRevenue: number;
    totalCostOfSales: number;
    grossProfit: number;
    totalOperatingExpenses: number;
    netIncome: number;
    netIncomeMargin: number;
}

export interface AccountingEntryLine {
    id: string;
    accountCode: string;
    accountName: string;
    debit: number;
    credit: number;
}

/** Asiento contable devuelto por GET /accounting/entries. */
export interface AccountingEntry {
    id: string;
    date: string;
    description: string;
    referenceType?: string | null;
    referenceId?: string | null;
    documentNumber?: string | null;
    status?: string | null;
    lines: AccountingEntryLine[];
    totalDebit: number;
    totalCredit: number;
}

export interface AuditTrailEntry {
    id: string;
    timestamp: string;
    action: string;
    user: string;
    detail?: string;
}

/** Balance General (GET /accounting/balance-sheet?date=yyyy-MM-dd). */
export interface BalanceSheet {
    date: string;
    totalAssets: number;
    totalLiabilities: number;
    totalEquity: number;
    assetAccounts: Record<string, number>;
    liabilityAccounts: Record<string, number>;
    equityAccounts: Record<string, number>;
    isBalanced: boolean;
}

/** Línea del F29 (código SII, concepto y monto). */
export interface F29Line {
    code: string;
    description: string;
    amount: number;
}

/** Formulario 29 calculado (GET /accounting/f29?period=yyyy-MM). */
export interface F29Report {
    period: string;
    totalSalesTaxable: number;
    totalSalesExempt: number;
    totalPurchasesTaxable: number;
    totalPurchasesExempt: number;
    vatDebit: number;
    vatCredit: number;
    vatPayable: number;
    vatRecoverable: number;
    feeWithholding: number;
    totalPayable: number;
    details: F29Line[];
}

/**
 * Alerta de F29 desactualizado (GET /compliance/f29/alerts):
 * un asiento cayó en un período ya declarado y requiere rectificatoria.
 */
export interface F29Alert {
    id: string;
    periodYear: number;
    periodMonth: number;
    invoiceFolio: number | null;
    vatDifference: number;
    reason: string | null;
    createdAt: string;
    resolved: boolean;
}

export type DiscrepancyType = 'MISSING_IN_ERP' | 'MISSING_IN_SII' | 'AMOUNT_MISMATCH';

export interface SiiAuditDiscrepancy {
    documentType: string;
    folio: number | null;
    rutCounterpart: string | null;
    date: string | null;
    amountSii: number;
    amountErp: number;
    type: DiscrepancyType;
    severity: 'CRITICAL' | 'WARNING';
}

export interface SiiAuditSummary {
    netAmount: number;
    iva: number;
    documentCount: number;
}

/** Espejo SII vs ERP (GET /accounting/audit/sii-mirror?month&year). */
export interface SiiAuditReport {
    siiTotal: SiiAuditSummary;
    erpTotal: SiiAuditSummary;
    discrepancies: SiiAuditDiscrepancy[];
    isMatch: boolean;
}

export interface PeriodStatus {
    period: string;
    isClosed: boolean;
}

export interface ClosedPeriod {
    id: string;
    period: string;
    closedAt: string;
    profitLoss: number;
}

interface AccountingEntryLineApiDto {
    accountCode: string;
    accountName: string;
    debit: number | null;
    credit: number | null;
}

interface AccountingEntryApiDto {
    id: string;
    entryDate: string;
    description: string;
    referenceType?: string | null;
    referenceId?: string | null;
    documentNumber?: string | null;
    status?: string | null;
    lines: AccountingEntryLineApiDto[] | null;
}

interface F29ApiResponse {
    period: string;
    totalSalesTaxable: number | null;
    totalSalesExempt: number | null;
    totalPurchasesTaxable: number | null;
    totalPurchasesExempt: number | null;
    vatDebit: number | null;
    vatCredit: number | null;
    vatPayable: number | null;
    vatRecoverable: number | null;
    feeWithholding: number | null;
    totalPayable: number | null;
    details: F29Line[] | null;
}

interface BalanceSheetApiResponse {
    date: string;
    totalAssets: number | null;
    totalLiabilities: number | null;
    totalEquity: number | null;
    assetAccounts: Record<string, number> | null;
    liabilityAccounts: Record<string, number> | null;
    equityAccounts: Record<string, number> | null;
    isBalanced: boolean;
}

const num = (value: number | null | undefined): number => (value == null ? 0 : Number(value));

const mapAccountingEntry = (entry: AccountingEntryApiDto): AccountingEntry => {
    const lines = (entry.lines ?? []).map((line, index) => ({
        id: `${entry.id}-${index}`,
        accountCode: line.accountCode ?? '',
        accountName: line.accountName ?? '',
        debit: num(line.debit),
        credit: num(line.credit),
    }));

    return {
        id: entry.id,
        date: entry.entryDate,
        description: entry.description ?? '',
        referenceType: entry.referenceType ?? null,
        referenceId: entry.referenceId ?? null,
        documentNumber: entry.documentNumber ?? null,
        status: entry.status ?? null,
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

const mapF29 = (data: F29ApiResponse): F29Report => ({
    period: data.period,
    totalSalesTaxable: num(data.totalSalesTaxable),
    totalSalesExempt: num(data.totalSalesExempt),
    totalPurchasesTaxable: num(data.totalPurchasesTaxable),
    totalPurchasesExempt: num(data.totalPurchasesExempt),
    vatDebit: num(data.vatDebit),
    vatCredit: num(data.vatCredit),
    vatPayable: num(data.vatPayable),
    vatRecoverable: num(data.vatRecoverable),
    feeWithholding: num(data.feeWithholding),
    totalPayable: num(data.totalPayable),
    details: data.details ?? [],
});

export const accountingService = {
    /** Estado de resultados para el rango [from, to]. */
    getIncomeStatement: async (from: Date, to: Date): Promise<IncomeStatement> => {
        const { data } = await api.get<IncomeStatementApiResponse>(
            '/reports/income-statement',
            { params: { from: toIsoDate(from), to: toIsoDate(to) } },
        );

        return {
            totalRevenue: num(data.totalRevenue),
            cogs: num(data.totalCostOfSales),
            grossProfit: num(data.grossProfit),
            adminExpenses: num(data.totalOperatingExpenses),
            netIncome: num(data.netIncome),
            netMargin: num(data.netIncomeMargin),
        };
    },

    /** Todos los asientos de la empresa (GET /accounting/entries). */
    getEntries: async (): Promise<AccountingEntry[]> => {
        const { data } = await api.get<AccountingEntryApiDto[]>('/accounting/entries');
        return (data ?? []).map(mapAccountingEntry);
    },

    /** Asiento generado por una factura. */
    getInvoiceAccountingEntry: async (id: string): Promise<AccountingEntry | null> => {
        const { data } = await api.get<AccountingEntryApiDto[]>(
            `/accounting/entries/by-invoice/${id}`,
        );
        const first = data?.[0];
        return first ? mapAccountingEntry(first) : null;
    },

    /** Trazabilidad/auditoría de un documento. */
    getInvoiceAuditTrail: async (id: string): Promise<AuditTrailEntry[]> => {
        const { data } = await api.get<AuditTrailEntry[]>(`/invoices/${id}/audit-trail`);
        return data ?? [];
    },

    /** Balance General a una fecha de corte. */
    getBalanceSheet: async (dateTo: Date): Promise<BalanceSheet> => {
        const { data } = await api.get<BalanceSheetApiResponse>('/accounting/balance-sheet', {
            params: { date: toIsoDate(dateTo) },
        });

        return {
            date: data.date,
            totalAssets: num(data.totalAssets),
            totalLiabilities: num(data.totalLiabilities),
            totalEquity: num(data.totalEquity),
            assetAccounts: data.assetAccounts ?? {},
            liabilityAccounts: data.liabilityAccounts ?? {},
            equityAccounts: data.equityAccounts ?? {},
            isBalanced: Boolean(data.isBalanced),
        };
    },

    /** F29 calculado para un período `yyyy-MM`. */
    getF29: async (period: string): Promise<F29Report> => {
        const { data } = await api.get<F29ApiResponse>('/accounting/f29', { params: { period } });
        return mapF29(data);
    },

    /** Alertas de aceptaciones tardías en períodos ya declarados. */
    getF29Alerts: async (): Promise<F29Alert[]> => {
        const { data } = await api.get<F29Alert[]>('/compliance/f29/alerts');
        return (data ?? []).map((alert) => ({
            ...alert,
            vatDifference: num(alert.vatDifference),
        }));
    },

    /** Espejo SII vs ERP para un mes/año. */
    getSiiMirror: async (year: number, month: number): Promise<SiiAuditReport> => {
        const { data } = await api.get<SiiAuditReport>('/accounting/audit/sii-mirror', {
            params: { year, month },
        });
        return {
            siiTotal: {
                netAmount: num(data.siiTotal?.netAmount),
                iva: num(data.siiTotal?.iva),
                documentCount: num(data.siiTotal?.documentCount),
            },
            erpTotal: {
                netAmount: num(data.erpTotal?.netAmount),
                iva: num(data.erpTotal?.iva),
                documentCount: num(data.erpTotal?.documentCount),
            },
            discrepancies: data.discrepancies ?? [],
            isMatch: Boolean(data.isMatch),
        };
    },

    /** Estado de un período contable (abierto/cerrado). */
    getPeriodStatus: async (period: string): Promise<PeriodStatus> => {
        const { data } = await api.get<PeriodStatus>(`/accounting/periods/${period}/status`);
        return data;
    },

    /** Períodos cerrados de la empresa. */
    getClosedPeriods: async (): Promise<ClosedPeriod[]> => {
        const { data } = await api.get<ClosedPeriod[]>('/accounting/periods/closed');
        return data ?? [];
    },
};
