import { api } from '@/lib/api';

/** Datos fiscales que exige la emisión de DTE. */
export interface CompanyOnboardingData {
    razonSocial: string;
    email: string;
    direccion: string;
    giro: string;
    acteco: string;
    comuna: string;
}

/** Resultado del guardado: qué falta para poder emitir. */
export interface OnboardingStatus {
    razonSocial: string;
    missingFields: string[];
    emissionReady: boolean;
}

/** Datos públicos del certificado cargado (nunca incluye la contraseña). */
export interface CertificateInfo {
    rut: string;
    validUntil: string | null;
}

/** Error de negocio devuelto por el backend (400/422). */
interface ApiErrorBody {
    message?: string;
    missingFields?: string[];
}

function messageFrom(error: unknown): string {
    const body = (error as { response?: { data?: ApiErrorBody } })?.response?.data;
    return body?.message ?? 'No se pudo completar la operación. Intenta nuevamente.';
}

/**
 * Guarda los datos fiscales de la empresa.
 *
 * @param data razón social, email, dirección, giro, acteco y comuna
 * @returns campos faltantes y si la empresa ya puede emitir
 */
export async function saveCompanyData(data: CompanyOnboardingData): Promise<OnboardingStatus> {
    try {
        const { data: status } = await api.post<OnboardingStatus>('/company/onboarding', data);
        return status;
    } catch (error) {
        throw new Error(messageFrom(error));
    }
}

/**
 * Sube el certificado .p12 cifrado en tránsito y validado por el backend.
 *
 * <p>La contraseña sólo viaja en este request: no se persiste en el cliente.</p>
 *
 * @param file archivo PKCS#12
 * @param password contraseña del certificado
 * @returns RUT del titular y fecha de vencimiento
 */
export async function uploadCertificate(file: File, password: string): Promise<CertificateInfo> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('password', password);

    try {
        const { data } = await api.post<CertificateInfo>('/company/onboarding/certificate', formData, {
            headers: { 'Content-Type': 'multipart/form-data' },
        });
        return data;
    } catch (error) {
        throw new Error(messageFrom(error));
    }
}

/** Campos del backend que corresponden a cada input del formulario. */
export const MISSING_FIELD_TO_FORM: Record<string, keyof CompanyOnboardingData> = {
    'razón social': 'razonSocial',
    'giro': 'giro',
    'acteco': 'acteco',
    'comuna': 'comuna',
    'dirección': 'direccion',
};
