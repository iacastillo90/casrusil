"use client";

import { useState } from "react";
import { Users, FileText, Download, UploadCloud, CheckCircle2, Building, Clock, ChevronRight } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { formatCLP } from "@/lib/formatters";
import { toast } from "sonner";

// Mock Data
const MOCK_EMPLOYEES = [
    { id: "EMP-01", name: "Juan Pérez", rut: "15.444.333-2", role: "Desarrollador Senior", base: 2500000, type: "Indefinido", afp: "Habitat", isapre: "Colmena", status: "Processed" },
    { id: "EMP-02", name: "María González", rut: "17.222.111-K", role: "Diseñadora UX", base: 1800000, type: "Indefinido", afp: "Capital", isapre: "Consalud", status: "Processed" },
    { id: "EMP-03", name: "Carlos Soto", rut: "19.888.777-5", role: "Soporte TI", base: 950000, type: "Plazo Fijo", afp: "Modelo", isapre: "Fonasa", status: "Pending" },
    { id: "EMP-04", name: "Ana Rojas", rut: "12.333.444-1", role: "Gerente Finanzas", base: 3200000, type: "Indefinido", afp: "Cuprum", isapre: "Banmédica", status: "Processed" },
];

export default function PayrollPage() {
    const [isProcessing, setIsProcessing] = useState(false);

    const totalBase = MOCK_EMPLOYEES.reduce((acc, emp) => acc + emp.base, 0);
    const estimatedLeyesSociales = totalBase * 0.22; // Roughly 22% for demo
    const totalCompanyCost = totalBase + estimatedLeyesSociales;

    const handleProcessPrevired = () => {
        setIsProcessing(true);
        setTimeout(() => {
            setIsProcessing(false);
            toast.success("Nómina enviada a Previred exitosamente");
        }, 2000);
    };

    return (
        <div className="space-y-6 p-6 animate-in fade-in duration-500">
            <div className="flex justify-between items-center">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight flex items-center gap-2">
                        <Users className="h-8 w-8 text-indigo-600" />
                        Remuneraciones y RRHH
                    </h1>
                    <p className="text-muted-foreground mt-1">Gestión de nómina, leyes sociales y centralización contable.</p>
                </div>
                <div className="flex gap-3">
                    <Button variant="outline" className="gap-2 bg-white">
                        <FileText className="w-4 h-4" /> Emitir Liquidaciones
                    </Button>
                    <Button 
                        className="gap-2 bg-emerald-600 hover:bg-emerald-700" 
                        onClick={handleProcessPrevired}
                        disabled={isProcessing}
                    >
                        <UploadCloud className="w-4 h-4" /> 
                        {isProcessing ? "Procesando..." : "Declarar Previred"}
                    </Button>
                </div>
            </div>

            {/* KPIs */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                <Card className="shadow-sm">
                    <CardContent className="pt-6">
                        <div className="flex justify-between items-start">
                            <div className="space-y-2">
                                <p className="text-sm font-medium text-slate-500">Dotación Activa</p>
                                <p className="text-3xl font-bold">{MOCK_EMPLOYEES.length}</p>
                            </div>
                            <div className="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center text-blue-600">
                                <Building className="w-5 h-5" />
                            </div>
                        </div>
                        <p className="text-xs text-slate-500 mt-4">Empleados con contrato vigente</p>
                    </CardContent>
                </Card>

                <Card className="shadow-sm">
                    <CardContent className="pt-6">
                        <div className="flex justify-between items-start">
                            <div className="space-y-2">
                                <p className="text-sm font-medium text-slate-500">Total Sueldos Base</p>
                                <p className="text-2xl font-bold text-slate-900">{formatCLP(totalBase)}</p>
                            </div>
                        </div>
                        <p className="text-xs text-slate-500 mt-4">Período: Mes actual</p>
                    </CardContent>
                </Card>

                <Card className="shadow-sm">
                    <CardContent className="pt-6">
                        <div className="flex justify-between items-start">
                            <div className="space-y-2">
                                <p className="text-sm font-medium text-slate-500">Leyes Sociales (Est.)</p>
                                <p className="text-2xl font-bold text-amber-600">{formatCLP(estimatedLeyesSociales)}</p>
                            </div>
                        </div>
                        <p className="text-xs text-slate-500 mt-4">AFP, Isapre/Fonasa, Mutual, AFC</p>
                    </CardContent>
                </Card>

                <Card className="shadow-sm border-indigo-200 bg-indigo-50/30">
                    <CardContent className="pt-6">
                        <div className="flex justify-between items-start">
                            <div className="space-y-2">
                                <p className="text-sm font-medium text-indigo-800">Costo Empresa Total</p>
                                <p className="text-2xl font-black text-indigo-700">{formatCLP(totalCompanyCost)}</p>
                            </div>
                        </div>
                        <p className="text-xs text-indigo-600 mt-4">Impacto directo en P&L</p>
                    </CardContent>
                </Card>
            </div>

            {/* Employee List */}
            <Card className="shadow-sm border-slate-200">
                <CardHeader className="bg-slate-50/50 border-b border-slate-100 pb-4">
                    <CardTitle className="text-lg flex justify-between items-center">
                        Detalle de Nómina
                        <Badge variant="secondary" className="font-normal text-xs">Período Noviembre 2026</Badge>
                    </CardTitle>
                </CardHeader>
                <div className="overflow-x-auto">
                    <table className="w-full text-sm text-left">
                        <thead className="text-xs text-slate-500 uppercase bg-white border-b border-slate-100">
                            <tr>
                                <th className="px-6 py-4 font-semibold">Empleado</th>
                                <th className="px-6 py-4 font-semibold">RUT</th>
                                <th className="px-6 py-4 font-semibold">Instituciones</th>
                                <th className="px-6 py-4 font-semibold text-right">Sueldo Base</th>
                                <th className="px-6 py-4 font-semibold text-center">Estado Cálculo</th>
                                <th className="px-6 py-4 font-semibold text-right">Acción</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100 bg-white">
                            {MOCK_EMPLOYEES.map((emp) => (
                                <tr key={emp.id} className="hover:bg-slate-50 transition-colors group">
                                    <td className="px-6 py-4">
                                        <div className="font-medium text-slate-900">{emp.name}</div>
                                        <div className="text-xs text-slate-500 mt-0.5">{emp.role}</div>
                                    </td>
                                    <td className="px-6 py-4 font-mono text-xs text-slate-600">{emp.rut}</td>
                                    <td className="px-6 py-4">
                                        <div className="flex gap-2">
                                            <Badge variant="outline" className="text-[10px] bg-slate-50">{emp.afp}</Badge>
                                            <Badge variant="outline" className="text-[10px] bg-slate-50">{emp.isapre}</Badge>
                                        </div>
                                    </td>
                                    <td className="px-6 py-4 text-right font-semibold text-slate-700">
                                        {formatCLP(emp.base)}
                                    </td>
                                    <td className="px-6 py-4 text-center">
                                        {emp.status === "Processed" ? (
                                            <Badge className="bg-emerald-100 text-emerald-800 hover:bg-emerald-100 border-0">
                                                <CheckCircle2 className="w-3 h-3 mr-1" /> Listo
                                            </Badge>
                                        ) : (
                                            <Badge className="bg-amber-100 text-amber-800 hover:bg-amber-100 border-0">
                                                <Clock className="w-3 h-3 mr-1" /> Pendiente
                                            </Badge>
                                        )}
                                    </td>
                                    <td className="px-6 py-4 text-right">
                                        <Button variant="ghost" size="sm" className="opacity-0 group-hover:opacity-100 transition-opacity">
                                            Ver Detalle <ChevronRight className="w-4 h-4 ml-1" />
                                        </Button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </Card>
        </div>
    );
}
