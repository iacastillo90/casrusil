export const complianceService = {
    getDashboard: async (period: string) => {
        return {
            globalHealthScore: 85,
            aiAlerts: [],
            siiStatus: { totalErp: 0, totalSii: 0, isBalanced: true, missingInErp: 0 },
            f29Preview: { vatDebit: 0, vatCredit: 0, totalTax: 0 },
            accountingStatus: { unbalancedEntries: 0, pendingClassification: 0 }
        };
    }
};
