export const useInvoices = () => {
    return { data: { invoices: [] }, isLoading: false };
};

export const useInvoice = (id: string) => {
    return { 
        data: { 
            id, 
            folio: 1, 
            status: 'PENDING', 
            issuerRut: '1-9', 
            recipientName: 'Test', 
            recipientRut: '2-7', 
            lines: [], 
            netAmount: 0, 
            taxAmount: 0, 
            totalAmount: 0 
        }, 
        isLoading: false 
    };
};
