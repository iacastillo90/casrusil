import React from 'react';

export const AccountingEntryCard = ({ entry, isLoading }: { entry: any, isLoading: boolean }) => {
    if (isLoading) return <div>Loading...</div>;
    return <div>AccountingEntryCard</div>;
};
