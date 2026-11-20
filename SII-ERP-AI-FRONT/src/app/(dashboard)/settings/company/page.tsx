import type { Metadata } from 'next';
import { PageHeader } from '@/features/accounting/components/PageHeader';
import { CompanyProfile } from '@/features/settings/components/CompanyProfile';

export const metadata: Metadata = {
    title: 'Empresa | SII ERP AI',
    description: 'Datos fiscales y de contacto de la empresa',
};

/**
 * Página de perfil de empresa. Reutiliza el formulario real de Configuración
 * (GET/PUT /company/profile) en lugar del formulario vacío anterior.
 */
export default function CompanySettingsPage() {
    return (
        <div className="space-y-6">
            <PageHeader
                title="Empresa"
                description="Razón social, RUT y datos de contacto que aparecen en tus documentos tributarios."
            />
            <CompanyProfile />
        </div>
    );
}
