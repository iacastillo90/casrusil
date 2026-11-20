'use client';

import Link from 'next/link';
import { PageHeader } from '@/features/accounting/components/PageHeader';
import { TaxReconciliationTable } from '@/features/accounting/components/TaxReconciliationTable';

/**
 * Auditoría SII: espejo del RCV contra la contabilidad interna.
 * Requiere una conexión SII autenticada; si no existe, la tabla muestra el
 * error real del backend (409 "SII Token not found").
 */
export default function TaxAuditPage() {
    return (
        <div className="space-y-6">
            <PageHeader
                title="Auditoría SII"
                description="Comparación del Registro de Compra y Venta (RCV) del SII contra la contabilidad interna, documento por documento."
                actions={
                    <Link
                        href="/sii"
                        className="text-sm font-medium text-primary underline-offset-4 hover:underline"
                    >
                        Estado de la conexión SII
                    </Link>
                }
            />
            <TaxReconciliationTable />
        </div>
    );
}
