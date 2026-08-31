export const accountingService = {
    getInvoiceAccountingEntry: async (id: string) => {
        return null;
    },
    getInvoiceAuditTrail: async (id: string) => {
        return [];
    },
    getBalanceSheet: async (dateTo: Date): Promise<any> => {
        return { totalAssets: 0, totalLiabilities: 0, totalEquity: 0 };
    }
};
