import React from 'react';

export const InvoiceAuditTrail = ({ auditTrail, isLoading }: { auditTrail: any, isLoading: boolean }) => {
    if (isLoading) return <div>Loading...</div>;
    return <div>InvoiceAuditTrail</div>;
};
