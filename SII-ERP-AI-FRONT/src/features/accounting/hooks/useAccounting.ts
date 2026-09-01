import type { AuditAlert } from '../components/AuditAlertsList';

export const useLedger = (startDate: Date, endDate: Date, someParam: number) => {
    return { data: [], isLoading: false };
};

export const useCleanupNovember = () => {
    return { mutate: () => {}, isPending: false };
};
export const useF29 = (period?: string) => {
    return { data: null, isLoading: false, error: null };
};

export const useAuditAlerts = (): { data: AuditAlert[]; isLoading: boolean; error: unknown } => {
    // NOTA: integración real con el backend pendiente; retorno tipado vacío.
    return { data: [], isLoading: false, error: null };
};
