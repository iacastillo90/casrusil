import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { accountingService, BalanceSheet, AccountingEntry, TaxReconciliation } from '../services/accounting.service';
import type { AuditAlert } from '../components/AuditAlertsList';

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

/**
 * Hook para obtener activos fijos
 */
export const useFixedAssets = () => {
    return useQuery<FixedAsset[]>({
        queryKey: ['fixed-assets'],
        queryFn: async () => {
            const response = await fetch('/api/v1/fixed-assets');
            if (!response.ok) throw new Error('Error al cargar activos fijos');
            return response.json();
        },
    });
};

/**
 * Hook para ejecutar depreciación
 */
export const useRunDepreciation = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: async () => {
            const response = await fetch('/api/v1/fixed-assets/run-depreciation', {
                method: 'POST',
            });
            if (!response.ok) throw new Error('Error al ejecutar depreciación');
            return response.json();
        },
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['fixed-assets'] }),
    });
};

/**
 * Hook para calcular depreciación
 */
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
        
        const monthsElapsed = Math.max(0, 
            (current.getFullYear() - acquisition.getFullYear()) * 12 + 
            (current.getMonth() - acquisition.getMonth())
        );

        if (method === 'LINEAL') {
            const monthlyDepreciation = initialValue / usefulLifeMonths;
            const accumulatedDepreciation = monthlyDepreciation * monthsElapsed;
            
            return {
                period: currentPeriod,
                monthlyDepreciation,
                accumulatedDepreciation,
                remainingValue: initialValue - accumulatedDepreciation,
            };
        } else {
            // Declining balance with 2x rate
            const rate = 2 / usefulLifeMonths;
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
        }
    };

    return { calculateDepreciation };
};

/**
 * Hook para obtener el balance sheet
 */
export const useBalanceSheet = (date: Date) => {
    return useQuery<BalanceSheet>({
        queryKey: ['balance-sheet', date.toISOString().split('T')[0]],
        queryFn: () => accountingService.getBalanceSheet(date),
    });
};

/**
 * Hook para obtener asientos contables
 */
export const useJournalEntry = (invoiceId: string) => {
    return useQuery<AccountingEntry | null>({
        queryKey: ['accounting-entry', invoiceId],
        queryFn: () => accountingService.getInvoiceAccountingEntry(invoiceId),
    });
};

/**
 * Hook para obtener conciliación de impuestos
 */
export const useTaxReconciliation = (period: string) => {
    return useQuery<TaxReconciliation>({
        queryKey: ['tax-reconciliation', period],
        queryFn: () => accountingService.getTaxReconciliation(period),
    });
};