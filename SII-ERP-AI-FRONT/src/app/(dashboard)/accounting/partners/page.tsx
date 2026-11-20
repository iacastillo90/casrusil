import type { Metadata } from 'next';
import { PageHeader } from '@/features/accounting/components/PageHeader';
import { PartnerLedger } from '@/features/accounting/components/partner-ledger';

export const metadata: Metadata = {
    title: 'Cuentas corrientes | SII ERP AI',
    description: 'Saldos por cliente y proveedor',
};

export default function PartnerLedgerPage() {
    return (
        <div className="space-y-6">
            <PageHeader
                title="Cuentas corrientes"
                description="Saldos por cliente y proveedor calculados desde los asientos contables."
            />
            <PartnerLedger />
        </div>
    );
}
