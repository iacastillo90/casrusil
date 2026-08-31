import React from 'react';

export const SustainabilityCard = ({ data, isLoading }: { data: any, isLoading: boolean }) => {
    if (isLoading) return <div>Loading...</div>;
    return <div>SustainabilityCard</div>;
};
