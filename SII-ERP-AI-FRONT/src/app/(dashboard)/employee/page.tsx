"use client";

import React from 'react';
import { useMyPayslips } from '@/features/employee/hooks/useEmployee';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { UnavailableAction } from '@/components/shared/UnavailableAction';
import { Download, FileText, CalendarClock } from 'lucide-react';
import { Payslip } from '@/features/employee/services/employee.service';

export default function EmployeePortalPage() {
    const { data: payslips, isLoading } = useMyPayslips();

    if (isLoading) {
        return <div className="p-8 text-center text-gray-500">Cargando portal del empleado...</div>;
    }

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <div>
                    <h2 className="text-2xl font-bold tracking-tight">Mi Portal</h2>
                    <p className="text-muted-foreground">Revisa tus liquidaciones y solicita vacaciones.</p>
                </div>
                <UnavailableAction reason="Solicitud de vacaciones no disponible aún">
                    <Button variant="outline" disabled aria-disabled="true">
                        <CalendarClock className="mr-2 h-4 w-4" />
                        Solicitar Vacaciones
                    </Button>
                </UnavailableAction>
            </div>

            <Card>
                <CardHeader>
                    <CardTitle className="text-lg flex items-center">
                        <FileText className="mr-2 h-5 w-5 text-blue-600" />
                        Mis Liquidaciones de Sueldo
                    </CardTitle>
                </CardHeader>
                <CardContent>
                    {(!payslips || payslips.length === 0) ? (
                        <div className="text-center py-8 text-muted-foreground">
                            No tienes liquidaciones disponibles.
                        </div>
                    ) : (
                        <div className="space-y-4">
                            {payslips.map((payslip: Payslip) => (
                                <div key={payslip.id} className="flex justify-between items-center p-4 border rounded-lg hover:bg-gray-50">
                                    <div>
                                        <div className="font-semibold">Liquidación - Período ID {payslip.id}</div>
                                        <div className="text-sm text-gray-500">
                                            Sueldo Líquido: <span className="font-medium text-gray-900">${payslip.netSalary.toLocaleString('es-CL')}</span>
                                        </div>
                                    </div>
                                    <UnavailableAction reason="PDF de liquidación pendiente de definición">
                                        <Button variant="ghost" size="icon" disabled aria-disabled="true">
                                            <Download className="h-5 w-5 text-blue-600" />
                                        </Button>
                                    </UnavailableAction>
                                </div>
                            ))}
                        </div>
                    )}
                </CardContent>
            </Card>
        </div>
    );
}
