import React from 'react';

export const JournalEntryTable = ({ entries, isLoading }: { entries: any[], isLoading: boolean }) => {
    if (isLoading) return <div>Loading...</div>;
    return <div>JournalEntryTable</div>;
};
