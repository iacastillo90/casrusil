"use client";

import React, { useState } from 'react';
import { useLeads, useUpdateLeadStatus } from '@/features/crm/hooks/useCrm';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Plus, GripVertical } from 'lucide-react';
import { Lead, LeadStatus } from '@/features/crm/services/crm.service';

const COLUMNS: { id: LeadStatus, title: string }[] = [
    { id: 'NEW', title: 'Nuevos' },
    { id: 'CONTACTED', title: 'Contactados' },
    { id: 'QUALIFIED', title: 'Calificados' },
    { id: 'PROPOSAL_SENT', title: 'Propuesta' },
    { id: 'WON', title: 'Ganados' },
    { id: 'LOST', title: 'Perdidos' }
];

export default function CrmLeadsPage() {
    const { data: leads, isLoading } = useLeads();
    const updateStatus = useUpdateLeadStatus();

    const [draggedLead, setDraggedLead] = useState<Lead | null>(null);

    const handleDragStart = (lead: Lead) => {
        setDraggedLead(lead);
    };

    const handleDrop = (e: React.DragEvent, newStatus: LeadStatus) => {
        e.preventDefault();
        if (draggedLead && draggedLead.status !== newStatus) {
            updateStatus.mutate({ id: draggedLead.id, status: newStatus });
        }
        setDraggedLead(null);
    };

    const handleDragOver = (e: React.DragEvent) => {
        e.preventDefault();
    };

    if (isLoading) {
        return <div className="p-8 text-center text-gray-500">Cargando pipeline...</div>;
    }

    return (
        <div className="space-y-6 h-[calc(100vh-8rem)] flex flex-col">
            <div className="flex justify-between items-center shrink-0">
                <div>
                    <h2 className="text-2xl font-bold tracking-tight">CRM - Pipeline Comercial</h2>
                    <p className="text-muted-foreground">Arrastra los leads para avanzar en el embudo de ventas.</p>
                </div>
                <Button>
                    <Plus className="mr-2 h-4 w-4" />
                    Nuevo Lead
                </Button>
            </div>

            <div className="flex gap-4 overflow-x-auto pb-4 flex-1">
                {COLUMNS.map(col => {
                    const columnLeads = leads?.filter(l => l.status === col.id) || [];
                    
                    return (
                        <div 
                            key={col.id} 
                            className="bg-gray-100 rounded-lg p-4 w-80 min-w-[320px] flex flex-col"
                            onDrop={(e) => handleDrop(e, col.id)}
                            onDragOver={handleDragOver}
                        >
                            <div className="flex justify-between items-center mb-4">
                                <h3 className="font-semibold text-gray-700">{col.title}</h3>
                                <span className="bg-gray-200 text-gray-600 text-xs py-1 px-2 rounded-full">
                                    {columnLeads.length}
                                </span>
                            </div>

                            <div className="flex-1 overflow-y-auto space-y-3">
                                {columnLeads.map(lead => (
                                    <div 
                                        key={lead.id} 
                                        draggable
                                        onDragStart={() => handleDragStart(lead)}
                                        className="bg-white p-3 rounded shadow-sm border border-gray-200 cursor-grab active:cursor-grabbing hover:border-blue-300"
                                    >
                                        <div className="flex justify-between items-start">
                                            <div>
                                                <h4 className="font-medium text-sm">{lead.contactName}</h4>
                                                <p className="text-xs text-gray-500">{lead.companyName}</p>
                                            </div>
                                            <GripVertical className="h-4 w-4 text-gray-400" />
                                        </div>
                                        <div className="mt-3 text-xs text-gray-400">
                                            {lead.email}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    );
                })}
            </div>
        </div>
    );
}
