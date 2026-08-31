export const useLedger = (startDate: Date, endDate: Date, someParam: number) => {
    return { data: [], isLoading: false };
};

export const useCleanupNovember = () => {
    return { mutate: () => {}, isPending: false };
};
export const useF29 = (period?: string) => {
    return { data: null, isLoading: false, error: null };
};

export const useAuditAlerts = () => {
    return { data: [], isLoading: false, error: null };
};
