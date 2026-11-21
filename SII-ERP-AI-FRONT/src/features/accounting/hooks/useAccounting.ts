import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '@/lib/axios';
import {
    accountingService,
    type AccountingEntry,
    type BalanceSheet,
    type ClosedPeriod,
    type F29Alert,
    type F29Report,
    type PeriodStatus,
    type SiiAuditReport,
} from '../services/accounting.service';
import type { AuditAlert } from '../components/AuditAlertsList';
import { useAuthStore } from '@/features/auth/stores/auth.store';

interface FixedAsset {
    id: string;
    name: string;
    code: string;
    initialValue: number;
    accumulatedDepreciation: number;
    currentValue: number;
    usefulLifeMonths: number;
    depreciationMethod: 'LINEAL' | 'DECILONAL';
    acquisitionDate: string;
    status: 'ACTIVE' | 'DEPRECIATED' | 'DISPOSED';
    monthlyDepreciation: number;
}

interface DepreciationCalculation {
    period: string;
    monthlyDepreciation: number;
    accumulatedDepreciation: number;
    remainingValue: number;
}

/** Asientos contables de la empresa (GET /accounting/entries). */
export const useLedger = () => {
    return useQuery<AccountingEntry[]>({
        queryKey: ['accounting-entries'],
        queryFn: () => accountingService.getEntries(),
    });
};

/** F29 calculado de un período `yyyy-MM`. */
export const useF29 = (period: string) => {
    return useQuery<F29Report>({
        queryKey: ['f29', period],
        queryFn: () => accountingService.getF29(period),
        enabled: Boolean(period),
    });
};

/** Alertas de F29 desactualizado. */
export const useF29Alerts = () => {
    return useQuery<F29Alert[]>({
        queryKey: ['f29-alerts'],
        queryFn: () => accountingService.getF29Alerts(),
    });
};

/** Espejo SII vs ERP de un período. */
export const useSiiMirror = (year: number, month: number) => {
    return useQuery<SiiAuditReport>({
        queryKey: ['sii-mirror', year, month],
        queryFn: () => accountingService.getSiiMirror(year, month),
    });
};

/** Estado de un período contable. */
export const usePeriodStatus = (period: string) => {
    return useQuery<PeriodStatus>({
        queryKey: ['period-status', period],
        queryFn: () => accountingService.getPeriodStatus(period),
        enabled: Boolean(period),
    });
};

/** Períodos contables cerrados. */
export const useClosedPeriods = () => {
    return useQuery<ClosedPeriod[]>({
        queryKey: ['closed-periods'],
        queryFn: () => accountingService.getClosedPeriods(),
    });
};

interface AuditAlertApiDto {
    id: string;
    type?: string;
    severity: string;
    title: string;
    description?: string;
}

interface AuditAlertsApiResponse {
    alerts?: AuditAlertApiDto[];
}

/**
 * Alertas de auditoría para el menú de notificaciones.
 *
 * El endpoint `GET /audit/alerts` exige rol ADMIN/AUDITOR, por eso la consulta
 * se deshabilita para otros roles. El backend no expone `createdAt` en este
 * DTO; la fecha se omite en la UI en vez de inventarla.
 */
export const useAuditAlerts = (): { data: AuditAlert[]; isLoading: boolean; error: unknown } => {
    const role = useAuthStore((state) => state.user?.role);
    const canReadAudit = role === 'ADMIN' || role === 'AUDITOR';

    const { data, isLoading, error } = useQuery<AuditAlert[]>({
        queryKey: ['audit-alerts'],
        queryFn: async () => {
            const { data } = await api.get<AuditAlertsApiResponse>('/audit/alerts');
            return (data.alerts ?? []).map((alert) => ({
                id: alert.id,
                severity: alert.severity as AuditAlert['severity'],
                title: alert.title,
                message: alert.description ?? '',
                createdAt: '',
            }));
        },
        enabled: canReadAudit,
        retry: false,
        staleTime: 60_000,
    });

    return { data: data ?? [], isLoading: canReadAudit ? isLoading : false, error };
};

/** Activos fijos de la empresa. */
export const useFixedAssets = () => {
    return useQuery<FixedAsset[]>({
        queryKey: ['fixed-assets'],
        queryFn: async () => {
            const { data } = await api.get<FixedAsset[]>('/fixed-assets');
            return data ?? [];
        },
    });
};

/** Ejecuta el cálculo de depreciación mensual. */
export const useRunDepreciation = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: async () => {
            await api.post('/fixed-assets/run-depreciation');
        },
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['fixed-assets'] }),
    });
};

/** Calculadora local de depreciación (lineal / decilonal). */
export const useDepreciationCalculator = () => {
    const calculateDepreciation = (
        initialValue: number,
        usefulLifeMonths: number,
        method: 'LINEAL' | 'DECILONAL',
        acquisitionDate: string,
        currentPeriod: string
    ): DepreciationCalculation => {
        const acquisition = new Date(acquisitionDate);
        const current = new Date(currentPeriod);

        const monthsElapsed = Math.max(
            0,
            (current.getFullYear() - acquisition.getFullYear()) * 12 +
                (current.getMonth() - acquisition.getMonth()),
        );

        if (method === 'LINEAL') {
            const monthlyDepreciation = usefulLifeMonths > 0 ? initialValue / usefulLifeMonths : 0;
            const accumulatedDepreciation = monthlyDepreciation * monthsElapsed;

            return {
                period: currentPeriod,
                monthlyDepreciation,
                accumulatedDepreciation,
                remainingValue: Math.max(0, initialValue - accumulatedDepreciation),
            };
        }

        const rate = usefulLifeMonths > 0 ? 2 / usefulLifeMonths : 0;
        let bookValue = initialValue;
        let accumulated = 0;

        for (let i = 0; i < monthsElapsed; i++) {
            const monthly = bookValue * rate;
            accumulated += monthly;
            bookValue = initialValue - accumulated;
        }

        return {
            period: currentPeriod,
            monthlyDepreciation: bookValue * rate,
            accumulatedDepreciation: accumulated,
            remainingValue: Math.max(0, initialValue - accumulated),
        };
    };

    return { calculateDepreciation };
};

/** Balance General a una fecha. */
export const useBalanceSheet = (date: Date) => {
    return useQuery<BalanceSheet>({
        queryKey: ['balance-sheet', date.toISOString().split('T')[0]],
        queryFn: () => accountingService.getBalanceSheet(date),
    });
};

/** Asiento contable de una factura. */
export const useJournalEntry = (invoiceId: string) => {
    return useQuery<AccountingEntry | null>({
        queryKey: ['accounting-entry', invoiceId],
        queryFn: () => accountingService.getInvoiceAccountingEntry(invoiceId),
        enabled: Boolean(invoiceId),
    });
};
