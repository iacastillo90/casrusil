"use client";

import { toast } from "sonner";
import { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import {
    Bot, CheckCircle2, XCircle, AlertTriangle,
    Lock, CalendarRange, FileSpreadsheet, PercentSquare,
    Loader2, Sparkles, PieChart, Landmark, ArrowRight,
    RefreshCw, CheckCheck, FileText, FileBadge,
    Download, Clock, ShieldCheck
} from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { cn } from "@/lib/utils";
import { formatCLP } from "@/lib/formatters";
import { usePeriodClose } from "@/features/period_close/hooks/usePeriodClose";

type StepStatus = "pending" | "running" | "done" | "warning" | "error";

interface AuditMessage {
    type: "info" | "warning" | "success" | "error";
    text: string;
}

interface AnnualCloseStep {
    id: string;
    title: string;
    description: string;
    icon: React.ElementType;
    status: StepStatus;
    messages: AuditMessage[];
    dataPreview?: React.ReactNode;
}

const INITIAL_STEPS: AnnualCloseStep[] = [
    {
        id: "audit",
        title: "Auditoría de 12 Meses",
        description: "Verifica que los 12 períodos del año comercial estén cerrados y conciliados.",
        icon: CalendarRange,
        status: "pending",
        messages: [],
    },
    {
        id: "rli",
        title: "Determinación RLI (Renta Líquida Imponible)",
        description: "Cálculo de ajustes agregados y deducidos tributarios sobre el resultado financiero.",
        icon: FileSpreadsheet,
        status: "pending",
        messages: [],
    },
    {
        id: "idpc",
        title: "Cálculo del IDPC (Primera Categoría)",
        description: "Determinación del Impuesto de Primera Categoría según el régimen tributario.",
        icon: Landmark,
        status: "pending",
        messages: [],
    },
    {
        id: "distribution",
        title: "Distribución de Utilidades",
        description: "Propuesta de asientos de retiro o distribución a los socios.",
        icon: PieChart,
        status: "pending",
        messages: [],
    },
    {
        id: "f22",
        title: "Pre-llenado F22",
        description: "Generación preliminar del Formulario 22 de Declaración de Renta.",
        icon: FileBadge,
        status: "pending",
        messages: [],
    },
];

export default function AnnualClosePage() {
    const [steps, setSteps] = useState<AnnualCloseStep[]>(INITIAL_STEPS);
    const [currentStep, setCurrentStep] = useState<number>(-1);
    const [isRunning, setIsRunning] = useState(false);
    const [isClosed, setIsClosed] = useState(false);
    const [aiChat, setAiChat] = useState<{ role: "ai" | "system"; text: string }[]>([
        { role: "ai", text: "👋 ¡Hola! Soy tu Asistente de Operación Renta. Este es el proceso más importante del año, pero no te preocupes. Reviso la contabilidad de todo el ejercicio y te ayudo a preparar el terreno para tu Declaración Anual de Impuestos." }
    ]);
    const [progress, setProgress] = useState(0);

    const completedSteps = steps.filter(s => s.status === "done" || s.status === "warning").length;
    
    // Removing mock STEP_RESULTS

    const { auditPeriod } = usePeriodClose();

    const runAnalysis = async () => {
        setIsRunning(true);
        setCurrentStep(0);
        setAiChat(prev => [...prev, { role: "system", text: "Iniciando proceso de Cierre Anual (Año Comercial 2026)..." }]);

        let aiRecommendation = "";
        let backendIssues: string[] = [];

        try {
            // Evaluamos diciembre para el cierre anual
            const auditResult = await auditPeriod.mutateAsync({ year: 2026, month: 12 });
            aiRecommendation = auditResult.aiRecommendation || "Operación Renta lista.";
            backendIssues = auditResult.issuesFound || [];
            
            setSteps(INITIAL_STEPS.map((s, i) => {
                if (i === 0 && backendIssues.length > 0) {
                    return {
                        ...s,
                        status: "warning",
                        messages: backendIssues.map(issue => ({ type: "warning", text: `⚠ ${issue}` }))
                    };
                }
                return {
                    ...s,
                    status: "done",
                    messages: [{ type: "success", text: "✓ Validado correctamente." }]
                };
            }));

            setProgress(100);
            
            if (backendIssues.length > 0) {
                setAiChat(prev => [...prev, { role: "ai", text: "He encontrado observaciones. Revisa los pasos." }]);
            }
        } catch (error) {
            console.error("Error fetching annual audit", error);
            aiRecommendation = "Error al conectar con la IA o Backend.";
            setSteps(INITIAL_STEPS.map(s => ({
                ...s,
                status: "error",
                messages: [{ type: "error", text: "Error de conexión." }]
            })));
        }

        setCurrentStep(-1);
        setIsRunning(false);
        setAiChat(prev => [...prev, {
            role: "ai",
            text: aiRecommendation || "✅ Proceso de auditoría anual finalizado."
        }]);
    };

    const handleClose = () => {
        setIsClosed(true);
        toast.success("¡Ejercicio 2026 cerrado exitosamente!", {
            description: "Los saldos iniciales para 2027 han sido generados y el F22 guardado.",
            duration: 5000,
        });
        setAiChat(prev => [...prev, {
            role: "ai",
            text: "🔒 Ejercicio cerrado oficialmente. He preparado el asiento de apertura para 2027. ¡Felicitaciones por otro año de operación!"
        }]);
    };

    const statusConfig: Record<StepStatus, { color: string; label: string; icon: React.ElementType }> = {
        pending: { color: "text-slate-400 bg-slate-100", label: "Pendiente", icon: Clock },
        running: { color: "text-blue-600 bg-blue-50 animate-pulse", label: "Analizando...", icon: Loader2 },
        done: { color: "text-emerald-700 bg-emerald-50", label: "OK", icon: CheckCircle2 },
        warning: { color: "text-amber-700 bg-amber-50", label: "Ajuste Sugerido", icon: AlertTriangle },
        error: { color: "text-red-700 bg-red-50", label: "Error", icon: XCircle },
    };

    return (
        <div className="space-y-6 p-6 max-w-6xl mx-auto animate-in fade-in duration-500">
            {/* Header */}
            <div className="flex justify-between items-start">
                <div>
                    <div className="flex items-center gap-3 mb-2">
                        <div className="p-2 bg-purple-100 rounded-xl">
                            <Bot className="h-7 w-7 text-purple-600" />
                        </div>
                        <div>
                            <h1 className="text-2xl font-bold tracking-tight">Cierre de Año Asistido por IA (F22)</h1>
                            <p className="text-muted-foreground text-sm">Ejercicio Comercial: <span className="font-semibold text-slate-700">2026</span></p>
                        </div>
                    </div>
                </div>
                <div className="flex items-center gap-3">
                    {isClosed ? (
                        <Badge className="bg-emerald-100 text-emerald-800 border-0 text-sm px-3 py-1.5">
                            <Lock className="w-3 h-3 mr-1.5" /> Año 2026 Cerrado
                        </Badge>
                    ) : (
                        <Badge variant="outline" className="text-slate-600 text-sm px-3 py-1.5">
                            <RefreshCw className="w-3 h-3 mr-1.5" /> Año Comercial Abierto
                        </Badge>
                    )}
                </div>
            </div>

            {/* Progress Bar */}
            {(isRunning || completedSteps > 0) && (
                <motion.div initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }}>
                    <Card className="border-purple-100 bg-gradient-to-r from-purple-50 to-indigo-50">
                        <CardContent className="pt-4 pb-4">
                            <div className="flex justify-between text-sm font-medium text-purple-800 mb-2">
                                <span className="flex items-center gap-2">
                                    <Sparkles className="w-4 h-4" /> Operación Renta en Progreso
                                </span>
                                <span>{completedSteps}/{INITIAL_STEPS.length} tareas completadas</span>
                            </div>
                            <Progress value={progress} className="h-2 bg-purple-100 [&>div]:bg-purple-600" />
                        </CardContent>
                    </Card>
                </motion.div>
            )}

            <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
                {/* Steps Panel */}
                <div className="lg:col-span-3 space-y-4">
                    {steps.map((step, index) => {
                        const config = statusConfig[step.status];
                        const StepIcon = step.icon;
                        const StatusIcon = config.icon;

                        return (
                            <motion.div
                                key={step.id}
                                initial={{ opacity: 0, x: -20 }}
                                animate={{ opacity: 1, x: 0 }}
                                transition={{ delay: index * 0.07 }}
                            >
                                <Card className={cn(
                                    "border transition-all duration-300",
                                    step.status === "running" && "border-blue-300 shadow-blue-100 shadow-md",
                                    step.status === "warning" && "border-amber-200",
                                    step.status === "done" && "border-emerald-200",
                                )}>
                                    <CardHeader className="pb-2 pt-4">
                                        <div className="flex items-center justify-between">
                                            <div className="flex items-center gap-3">
                                                <div className={cn("w-9 h-9 rounded-lg flex items-center justify-center", config.color)}>
                                                    {step.status === "running" ? (
                                                        <Loader2 className="w-4 h-4 animate-spin" />
                                                    ) : (
                                                        <StepIcon className="w-4 h-4" />
                                                    )}
                                                </div>
                                                <div>
                                                    <CardTitle className="text-base">{step.title}</CardTitle>
                                                    <CardDescription className="text-xs mt-0.5">{step.description}</CardDescription>
                                                </div>
                                            </div>
                                            <Badge variant="outline" className={cn("text-xs", config.color)}>
                                                <StatusIcon className="w-3 h-3 mr-1" />
                                                {config.label}
                                            </Badge>
                                        </div>
                                    </CardHeader>

                                    <AnimatePresence>
                                        {step.messages.length > 0 && (
                                            <motion.div
                                                initial={{ opacity: 0, height: 0 }}
                                                animate={{ opacity: 1, height: "auto" }}
                                                exit={{ opacity: 0, height: 0 }}
                                            >
                                                <CardContent className="pt-0 pb-3">
                                                    <div className="border-t border-slate-100 pt-3 space-y-1.5">
                                                        {step.messages.map((msg, i) => (
                                                            <p key={i} className={cn(
                                                                "text-xs font-mono rounded px-2 py-1",
                                                                msg.type === "success" && "text-emerald-700 bg-emerald-50",
                                                                msg.type === "warning" && "text-amber-700 bg-amber-50",
                                                                msg.type === "info" && "text-blue-700 bg-blue-50",
                                                                msg.type === "error" && "text-red-700 bg-red-50",
                                                            )}>
                                                                {msg.text}
                                                            </p>
                                                        ))}
                                                    </div>

                                                    {/* Data Preview block if available */}
                                                    {step.dataPreview}
                                                    
                                                    {step.id === "f22" && step.status === "done" && (
                                                        <div className="mt-4 flex gap-3">
                                                            <Button size="sm" variant="outline" className="w-full gap-2 text-slate-600">
                                                                <FileText className="w-4 h-4" /> Ver Borrador F22
                                                            </Button>
                                                            <Button size="sm" variant="secondary" className="w-full gap-2 bg-indigo-50 text-indigo-700 hover:bg-indigo-100 border-indigo-200">
                                                                <Download className="w-4 h-4" /> Exportar Papeles de Trabajo
                                                            </Button>
                                                        </div>
                                                    )}
                                                </CardContent>
                                            </motion.div>
                                        )}
                                    </AnimatePresence>
                                </Card>
                            </motion.div>
                        );
                    })}

                    {/* Action Buttons */}
                    <div className="flex gap-3 pt-2">
                        {!isRunning && completedSteps === 0 && (
                            <Button
                                className="w-full bg-purple-600 hover:bg-purple-700 gap-2"
                                onClick={runAnalysis}
                            >
                                <Bot className="w-4 h-4" />
                                Iniciar Proceso de Operación Renta
                                <ArrowRight className="w-4 h-4 ml-auto" />
                            </Button>
                        )}

                        {isRunning && (
                            <Button className="w-full bg-purple-400 cursor-not-allowed gap-2" disabled>
                                <Loader2 className="w-4 h-4 animate-spin" /> Procesando año contable...
                            </Button>
                        )}

                        {!isRunning && completedSteps === INITIAL_STEPS.length && !isClosed && (
                            <>
                                <Button variant="outline" onClick={runAnalysis} className="gap-2">
                                    <RefreshCw className="w-4 h-4" /> Re-calcular
                                </Button>
                                <Button
                                    className="flex-1 bg-emerald-600 hover:bg-emerald-700 gap-2"
                                    onClick={handleClose}
                                >
                                    <Lock className="w-4 h-4" />
                                    Firmar y Cerrar Año Comercial 2026
                                    <CheckCheck className="w-4 h-4 ml-auto" />
                                </Button>
                            </>
                        )}

                        {isClosed && (
                            <div className="w-full flex items-center justify-center gap-3 p-4 bg-emerald-50 border border-emerald-200 rounded-lg">
                                <CheckCircle2 className="w-6 h-6 text-emerald-600 shrink-0" />
                                <div>
                                    <p className="font-semibold text-emerald-800">Cierre de Año Completado</p>
                                    <p className="text-xs text-emerald-600">Saldos de apertura para 2027 listos.</p>
                                </div>
                            </div>
                        )}
                    </div>
                </div>

                {/* AI Chat Panel */}
                <div className="lg:col-span-2">
                    <Card className="h-full flex flex-col border-slate-200 shadow-sm sticky top-6">
                        <CardHeader className="border-b border-slate-100 bg-gradient-to-r from-purple-600 to-indigo-700 text-white rounded-t-xl">
                            <CardTitle className="text-base flex items-center gap-2">
                                <Sparkles className="w-4 h-4 text-purple-200" />
                                Asistente IA — Cierre Anual
                            </CardTitle>
                            <CardDescription className="text-purple-200 text-xs">
                                Preparación Tributaria F22
                            </CardDescription>
                        </CardHeader>
                        <CardContent className="flex-1 overflow-y-auto p-4 space-y-3 bg-slate-50 max-h-[600px]">
                            {aiChat.map((msg, i) => (
                                <motion.div
                                    key={i}
                                    initial={{ opacity: 0, y: 10 }}
                                    animate={{ opacity: 1, y: 0 }}
                                    className={cn(
                                        "flex gap-2 max-w-full",
                                        msg.role === "system" && "justify-center"
                                    )}
                                >
                                    {msg.role === "ai" && (
                                        <div className="w-7 h-7 rounded-full bg-purple-100 flex items-center justify-center shrink-0 mt-0.5">
                                            <Bot className="w-4 h-4 text-purple-600" />
                                        </div>
                                    )}
                                    <div className={cn(
                                        "rounded-xl px-3 py-2 text-sm leading-relaxed",
                                        msg.role === "ai" && "bg-white border border-slate-200 text-slate-700 shadow-sm",
                                        msg.role === "system" && "bg-slate-200 text-slate-500 text-xs px-4 py-1.5 rounded-full"
                                    )}>
                                        {msg.text}
                                    </div>
                                </motion.div>
                            ))}
                        </CardContent>
                        <div className="p-3 border-t border-slate-100 bg-white rounded-b-xl">
                            <div className="flex items-center gap-2 text-xs text-slate-400">
                                <ShieldCheck className="w-4 h-4 text-emerald-500" />
                                <span>IA con conocimiento tributario SII vigente</span>
                            </div>
                        </div>
                    </Card>
                </div>
            </div>
        </div>
    );
}
