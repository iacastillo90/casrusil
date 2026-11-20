import api from '@/lib/axios';
import type { Invoice } from '../types/invoice.types';

export interface InvoiceListParams {
    page?: number;
    pageSize?: number;
    status?: string;
}

export interface InvoiceListResponse {
    invoices: Invoice[];
    total: number;
    page: number;
    pageSize: number;
}

interface InvoiceApiDto {
    id: string;
    folio: number;
    status: string;
    issuerRut: string;
    receiverRut: string;
    businessName: string;
    date: string;
    dueDate?: string;
    netAmount: number;
    taxAmount: number;
    totalAmount: number;
    transactionType?: 'SALE' | 'PURCHASE';
    items?: InvoiceLineApiDto[];
}

interface InvoiceLineApiDto {
    lineNumber?: number;
    itemName?: string;
    itemCode?: string | null;
    quantity?: number;
    price?: number;
    amount?: number;
    unit?: string;
}

/**
 * Modelo que produce el formulario de emisión (pasos Receptor → Líneas →
 * Totales → Revisar). Los mapeadores `toCreateInvoiceRequest` y
 * `toUpdateInvoiceRequest` lo traducen al contrato exacto del backend.
 */
export interface InvoiceFormModel {
    tipoDte: 33 | 39;
    rutEmisor: string;
    razonSocialEmisor: string;
    rutReceptor: string;
    /** Fecha en formato yyyy-MM-dd. */
    fechaEmision: string;
    lines: InvoiceFormLine[];
}

export interface InvoiceFormLine {
    description: string;
    quantity: number;
    unitPrice: number;
}

/** Contrato de POST /api/v1/invoices (InvoiceController.CreateInvoiceRequest). */
export interface CreateInvoiceRequest {
    tipoDte: number;
    rutEmisor: string;
    rutReceptor: string;
    razonSocialEmisor: string;
    fechaEmision: string;
    montoNeto: number;
    montoIva: number;
    montoTotal: number;
    lines: {
        description: string;
        quantity: number;
        unitPrice: number;
    }[];
}

/** Contrato de PUT /api/v1/invoices/{id} (UpdateInvoiceRequest). */
export interface UpdateInvoiceRequest {
    type: 'FACTURA' | 'BOLETA';
    clientRut: string;
    lines: {
        description: string;
        quantity: number;
        unitPrice: number;
        lineTotal: number;
    }[];
    netAmount: number;
    taxAmount: number;
    totalAmount: number;
    issueDate: string;
    folio: number;
    status: string;
    issuerRut: string;
    recipientName: string;
}

export interface InvoiceTotals {
    neto: number;
    iva: number;
    total: number;
}

export const IVA_RATE = 0.19;

/**
 * Deja el RUT en el formato que valida el backend (`^\d{1,8}-[\dkK]$`):
 * quita puntos y espacios, conserva el guión.
 */
export const cleanRut = (rut: string): string =>
    (rut ?? '').replace(/\./g, '').replace(/\s+/g, '').trim();

/** Mismo patrón que `@Pattern` del backend. */
export const isValidRutFormat = (rut: string): boolean => /^\d{1,8}-[\dkK]$/.test(cleanRut(rut));

/** Redondea al peso chileno (CLP) y calcula IVA 19%. */
export const computeInvoiceTotals = (lines: InvoiceFormLine[]): InvoiceTotals => {
    const neto = Math.round(
        lines.reduce((sum, line) => sum + Number(line.quantity) * Number(line.unitPrice), 0),
    );
    const iva = Math.round(neto * IVA_RATE);
    return { neto, iva, total: neto + iva };
};

/**
 * Traduce el formulario al request EXACTO de creación.
 * No incluye `folio`: el backend lo asigna desde el CAF.
 */
export const toCreateInvoiceRequest = (form: InvoiceFormModel): CreateInvoiceRequest => {
    const totals = computeInvoiceTotals(form.lines);
    return {
        tipoDte: form.tipoDte,
        rutEmisor: cleanRut(form.rutEmisor),
        rutReceptor: cleanRut(form.rutReceptor),
        razonSocialEmisor: form.razonSocialEmisor.trim(),
        fechaEmision: form.fechaEmision,
        montoNeto: totals.neto,
        montoIva: totals.iva,
        montoTotal: totals.total,
        lines: form.lines.map((line) => ({
            description: line.description.trim(),
            quantity: Number(line.quantity),
            unitPrice: Number(line.unitPrice),
        })),
    };
};

/** Traduce el formulario al request EXACTO de actualización. */
export const toUpdateInvoiceRequest = (
    form: InvoiceFormModel,
    original: Invoice,
): UpdateInvoiceRequest => {
    const totals = computeInvoiceTotals(form.lines);
    return {
        type: form.tipoDte === 39 ? 'BOLETA' : 'FACTURA',
        clientRut: cleanRut(form.rutReceptor),
        lines: form.lines.map((line) => ({
            description: line.description.trim(),
            quantity: Number(line.quantity),
            unitPrice: Number(line.unitPrice),
            lineTotal: Math.round(Number(line.quantity) * Number(line.unitPrice)),
        })),
        netAmount: totals.neto,
        taxAmount: totals.iva,
        totalAmount: totals.total,
        issueDate: form.fechaEmision,
        folio: original.folio,
        status: original.status,
        issuerRut: original.issuerRut,
        recipientName: original.recipientName,
    };
};

const mapInvoice = (dto: InvoiceApiDto): Invoice => ({
    id: dto.id,
    folio: dto.folio,
    status: dto.status,
    issuerRut: dto.issuerRut,
    issuerName: dto.businessName,
    recipientRut: dto.receiverRut,
    // `businessName` corresponde al emisor, no al receptor: el backend aún no
    // persiste la razón social del receptor.
    recipientName: '',
    lines: (dto.items ?? []).map((item) => ({
        id: item.lineNumber != null ? String(item.lineNumber) : '',
        description: item.itemName ?? '',
        quantity: item.quantity ?? 0,
        unitPrice: item.price ?? 0,
        lineTotal: item.amount ?? 0,
    })),
    netAmount: dto.netAmount,
    taxAmount: dto.taxAmount,
    totalAmount: dto.totalAmount,
    issueDate: dto.date,
    dueDate: dto.dueDate,
    transactionType: dto.transactionType,
});

export const invoiceService = {
    /**
     * Lista de facturas de la empresa.
     * El backend devuelve un arreglo plano; se normaliza al modelo del frontend.
     */
    getInvoices: async (params?: InvoiceListParams): Promise<InvoiceListResponse> => {
        const { data } = await api.get<InvoiceApiDto[]>('/invoices', { params });
        const invoices = (data ?? []).map(mapInvoice);

        return {
            invoices,
            total: invoices.length,
            page: params?.page ?? 1,
            pageSize: params?.pageSize ?? invoices.length,
        };
    },

    /**
     * Detalle de una factura por id.
     */
    getInvoice: async (id: string): Promise<Invoice | null> => {
        const { data } = await api.get<InvoiceApiDto | null>(`/invoices/${id}`);
        return data ? mapInvoice(data) : null;
    },

    /**
     * Envía la factura por email al destinatario indicado.
     */
    sendInvoice: async (id: string, email: string): Promise<void> => {
        await api.post(`/invoices/${id}/send`, null, { params: { email } });
    },

    /**
     * Crea una nueva factura con el contrato exacto del backend.
     */
    createInvoice: async (request: CreateInvoiceRequest): Promise<Invoice> => {
        const { data } = await api.post<InvoiceApiDto>('/invoices', request);
        return mapInvoice(data);
    },

    /**
     * Actualiza una factura existente con el contrato exacto del backend.
     */
    updateInvoice: async (id: string, request: UpdateInvoiceRequest): Promise<Invoice> => {
        const { data } = await api.put<InvoiceApiDto>(`/invoices/${id}`, request);
        return mapInvoice(data);
    },
};
