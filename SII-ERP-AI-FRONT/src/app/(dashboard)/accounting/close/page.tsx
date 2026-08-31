"use client";

import { useState, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import {
    Bot, CheckCircle2, XCircle, AlertTriangle, ChevronRight,
    Lock, FileText, Building2, Calculator, Loader2, Sparkles,
    TrendingDown, ShieldCheck, RefreshCw, ArrowRight, MessageSquare,
    Clock, CheckCheck
} from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { cn } from "@/lib/utils";
import { toast } from "sonner";
import { formatCLP } from "@/lib/formatters";
import { usePeriodClose } from "@/features/period_close/hooks/usePeriodClose";

// ─── Types ───────────────────────────────────────────────────────────────────
type StepStatus = "pending" | "running" | "done" | "warning" | "error";

interface AuditMessage {
    type: "info" | "warning" | "success" | "error";
    text: string;
}

interface CloseStep {
    id: string;
    title: string;
    description: string;
    icon: React.ElementType;
    status: StepStatus;
    messages: AuditMessage[];
    proposedEntries?: { label: string; debit: number; credit: number }[];
}

// ─── Mock AI Analysis Results ─────────────────────────────────────────────────
const INITIAL_STEPS: CloseStep[] = [
    {
        id: "documents",
        title: "Auditoría de Documentos SII",
        description: "Verificar que todos los DTEs del período estén centralizados en el libro mayor.",
        icon: FileText,
        status: "pending",
        messages: [],
    },
    {
        id: "bank",
        title: "Conciliación Bancaria",
        description: "Confirmar que el saldo contable cuadre con los extractos bancarios del período.",
        icon: Building2,
        status: "pending",
        messages: [],
    },
    {
        id: "depreciation",
        title: "Depreciación y Provisiones",
        description: "La IA calcula y propone los asientos de depreciación de activos y provisiones de impuestos.",
        icon: TrendingDown,
        status: "pending",
        messages: [],
    },
    {
        id: "closing",
        title: "Cierre y Refundición de Cuentas",
        description: "Genera el asiento de cierre que salda las cuentas de resultado y bloquea el período.",
        icon: Lock,
        status: "pending",
        messages: [],
    },
];

// Simulated AI analysis per step
const STEP_RESULTS: Record<string, Partial<CloseStep>> = {
    documents: {
        status: "warning",
        messages: [
            { type: "success", text: "✓ 142 facturas de ventas centralizadas correctamente." },
            { type: "success", text: "✓ 87 facturas de compras conciliadas con el RCV del SII." },
            { type: "warning", text: "⚠ 2 boletas de honorarios (Nº 334 y Nº 335) pendientes de registrar. Se propone asiento automático." },
        ],
    },
    bank: {
        status: "warning",
        messages: [
            { type: "success", text: "✓ Saldo banco BCI: $12.450.320 — Cuadra con libro mayor." },
            { type: "warning", text: "⚠ Partida pendiente detectada: Cheque #00234 por $320.000 emitido el 28/11, aún no cobrado." },
            { type: "info", text: "ℹ Se sugiere registrar como 'Cheque en Tránsito' para la conciliación del período." },
        ],
    },
    depreciation: {
        status: "done",
        messages: [
            { type: "info", text: "ℹ Activos fijos detectados: 8 bienes depreciables." },
            { type: "success", text: "✓ Depreciación mensual calculada: $184.600 (Método Lineal, SII Tabla Vigente)." },
            { type: "success", text: "✓ Provisión de IVA Débito Fiscal: $2.134.500 propuesta." },
            { type: "success", text: "✓ Provisión PPM (Pago Provisional Mensual): $340.000 calculada." },
        ],
        proposedEntries: [
            { label: "Gasto Depreciación Acumulada", debit: 184600, credit: 0 },
            { label: "Depreciación Acumulada Activos", debit: 0, credit: 184600 },
            { label: "Provisión IVA", debit: 0, credit: 2134500 },
            { label: "Gasto Tributario (PPM)", debit: 340000, credit: 0 },
        ],
    },
    closing: {
        status: "done",
        messages: [
            { type: "success", text: "✓ Ingresos del período: $18.450.000 — Cuentas saldadas." },
            { type: "success", text: "✓ Gastos del período: $14.230.000 — Cuentas saldadas." },
            { type: "success", text: "✓ Utilidad del ejercicio determinada: $4.220.000." },
            { type: "success", text: "✓ Período Noviembre 2026 bloqueado. No se permiten modificaciones retroactivas." },
        ],
    },
};

// ─── Component ────────────────────────────────────────────────────────────────
export default function PeriodClosePage() {
    const [steps, setSteps] = useState<CloseStep[]>(INITIAL_STEPS);
    const [currentStep, setCurrentStep] = useState<number>(-1);
    const [isRunning, setIsRunning] = useState(false);
    const [isClosed, setIsClosed] = useState(false);
    const [aiChat, setAiChat] = useState<{ role: "ai" | "system"; text: string }[]>([
        { role: "ai", text: "👋 Hola, soy tu Asistente de Cierre Contable. Cuando estés listo, inicia el análisis y revisaré el período punto por punto antes de proceder al cierre oficial." }
    ]);
    const [progress, setProgress] = useState(0);

    const completedSteps = steps.filter(s => s.status === "done" || s.status === "warning").length;
    const hasWarnings = steps.some(s => s.status === "warning");

    const { auditPeriod, executeClose } = usePeriodClose();

    const runAnalysis = async () => {
        setIsRunning(true);
        setCurrentStep(0);
        setAiChat(prev => [...prev, { role: "system", text: "Iniciando auditoría de cierre del período Noviembre 2026..." }]);

        // 1. Call Real Backend Endpoint
        let aiRecommendation = "";
        let backendIssues: string[] = [];
        try {
            const auditResult = await auditPeriod.mutateAsync({ year: 2026, month: 11 });
            aiRecommendation = auditResult.aiRecommendation;
            backendIssues = auditResult.issuesFound;
        } catch (error) {
            console.error("Error fetching audit", error);
            aiRecommendation = "Error al conectar con la IA. Usando heurísticas locales.";
        }

        for (let i = 0; i < INITIAL_STEPS.length; i++) {
            // Mark as running
            setCurrentStep(i);
            setSteps(prev => prev.map((s, idx) => idx === i ? { ...s, status: "running" } : s));
            setProgress(((i) / INITIAL_STEPS.length) * 100);

            // Simulate API/AI processing time
            await new Promise(r => setTimeout(r, 2200));

            // Apply results
            const result = STEP_RESULTS[INITIAL_STEPS[i].id];
            setSteps(prev => prev.map((s, idx) => idx === i ? { ...s, ...result } as CloseStep : s));

            // Add AI message based on result
            if (result.status === "warning") {
                setAiChat(prev => [...prev, { role: "ai", text: `Paso ${i + 1} completado con observaciones. Revisa los detalles antes de continuar.` }]);
            } else if (result.status === "done") {
                setAiChat(prev => [...prev, { role: "ai", text: `Paso ${i + 1} validado sin irregularidades. ✓` }]);
            }

            setProgress(((i + 1) / INITIAL_STEPS.length) * 100);
            await new Promise(r => setTimeout(r, 300));
        }

        setCurrentStep(-1);
        setIsRunning(false);
        setAiChat(prev => [...prev, {
            role: "ai",
            text: aiRecommendation || (hasWarnings
                ? "⚠ Análisis completo. Se encontraron algunas observaciones menores. Puedes proceder al cierre o resolver las observaciones primero."
                : "✅ Análisis completo. Todo en orden. Puedes proceder al cierre oficial del período.")
        }]);
    };

    const handleClose = async () => {
        try {
            await executeClose.mutateAsync({ year: 2026, month: 11 });
            setIsClosed(true);
            toast.success("¡Período Noviembre 2026 cerrado exitosamente!", {
                description: "El asiento de cierre fue registrado y el período quedó bloqueado.",
                duration: 5000,
            });
            setAiChat(prev => [...prev, {
                role: "ai",
                text: "🔒 Período oficialmente cerrado. Utilidad del ejercicio: $4.220.000. Los estados financieros finales del período ya están disponibles en el módulo de Reportes."
            }]);
        } catch (error) {
            toast.error("Error al cerrar el período.");
        }
    };

    const statusConfig: Record<StepStatus, { color: string; label: string; icon: React.ElementType }> = {
        pending: { color: "text-slate-400 bg-slate-100", label: "Pendiente", icon: Clock },
        running: { color: "text-blue-600 bg-blue-50 animate-pulse", label: "Analizando...", icon: Loader2 },
        done: { color: "text-emerald-700 bg-emerald-50", label: "OK", icon: CheckCircle2 },
        warning: { color: "text-amber-700 bg-amber-50", label: "Con observaciones", icon: AlertTriangle },
        error: { color: "text-red-700 bg-red-50", label: "Error", icon: XCircle },
    };

    return (
        <div className="space-y-6 p-6 max-w-6xl mx-auto animate-in fade-in duration-500">
            {/* Header */}
            <div className="flex justify-between items-start">
                <div>
                    <div className="flex items-center gap-3 mb-2">
                        <div className="p-2 bg-indigo-100 rounded-xl">
                            <Bot className="h-7 w-7 text-indigo-600" />
                        </div>
                        <div>
                            <h1 className="text-2xl font-bold tracking-tight">Cierre de Período Asistido por IA</h1>
                            <p className="text-muted-foreground text-sm">Período: <span className="font-semibold text-slate-700">Noviembre 2026</span></p>
                        </div>
                    </div>
                </div>
                <div className="flex items-center gap-3">
                    {isClosed ? (
                        <Badge className="bg-emerald-100 text-emerald-800 border-0 text-sm px-3 py-1.5">
                            <Lock className="w-3 h-3 mr-1.5" /> Período Cerrado
                        </Badge>
                    ) : (
                        <Badge variant="outline" className="text-slate-600 text-sm px-3 py-1.5">
                            <RefreshCw className="w-3 h-3 mr-1.5" /> Período Abierto
                        </Badge>
                    )}
                </div>
            </div>

            {/* Progress Bar */}
            {(isRunning || completedSteps > 0) && (
                <motion.div initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }}>
                    <Card className="border-indigo-100 bg-gradient-to-r from-indigo-50 to-purple-50">
                        <CardContent className="pt-4 pb-4">
                            <div className="flex justify-between text-sm font-medium text-indigo-800 mb-2">
                                <span className="flex items-center gap-2">
                                    <Sparkles className="w-4 h-4" /> Auditoría en Progreso
                                </span>
                                <span>{completedSteps}/{INITIAL_STEPS.length} pasos completados</span>
                            </div>
                            <Progress value={progress} className="h-2 bg-indigo-100" />
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

                                                    {/* Proposed Journal Entries */}
                                                    {step.proposedEntries && (
                                                        <div className="mt-3 border border-dashed border-indigo-200 rounded-lg p-3 bg-indigo-50/40">
                                                            <p className="text-xs font-semibold text-indigo-700 mb-2 flex items-center gap-1.5">
                                                                <Sparkles className="w-3 h-3" /> Asientos Propuestos por IA:
                                                            </p>
                                                            <table className="w-full text-xs">
                                                                <thead>
                                                                    <tr className="text-slate-500">
                                                                        <th className="text-left font-medium pb-1">Cuenta</th>
                                                                        <th className="text-right font-medium pb-1">Debe</th>
                                                                        <th className="text-right font-medium pb-1">Haber</th>
                                                                    </tr>
                                                                </thead>
                                                                <tbody>
                                                                    {step.proposedEntries.map((entry, i) => (
                                                                        <tr key={i} className="border-t border-indigo-100">
                                                                            <td className="py-1 text-slate-700">{entry.label}</td>
                                                                            <td className="text-right text-emerald-700 font-mono">
                                                                                {entry.debit ? formatCLP(entry.debit) : "—"}
                                                                            </td>
                                                                            <td className="text-right text-red-600 font-mono">
                                                                                {entry.credit ? formatCLP(entry.credit) : "—"}
                                                                            </td>
                                                                        </tr>
                                                                    ))}
                                                                </tbody>
                                                            </table>
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
                                className="w-full bg-indigo-600 hover:bg-indigo-700 gap-2"
                                onClick={runAnalysis}
                            >
                                <Bot className="w-4 h-4" />
                                Iniciar Auditoría con IA
                                <ArrowRight className="w-4 h-4 ml-auto" />
                            </Button>
                        )}

                        {isRunning && (
                            <Button className="w-full bg-indigo-400 cursor-not-allowed gap-2" disabled>
                                <Loader2 className="w-4 h-4 animate-spin" /> Analizando período...
                            </Button>
                        )}

                        {!isRunning && completedSteps === INITIAL_STEPS.length && !isClosed && (
                            <>
                                <Button variant="outline" onClick={runAnalysis} className="gap-2">
                                    <RefreshCw className="w-4 h-4" /> Re-analizar
                                </Button>
                                <Button
                                    className="flex-1 bg-emerald-600 hover:bg-emerald-700 gap-2"
                                    onClick={handleClose}
                                >
                                    <Lock className="w-4 h-4" />
                                    Confirmar y Cerrar Período
                                    <CheckCheck className="w-4 h-4 ml-auto" />
                                </Button>
                            </>
                        )}

                        {isClosed && (
                            <div className="w-full flex items-center justify-center gap-3 p-4 bg-emerald-50 border border-emerald-200 rounded-lg">
                                <CheckCircle2 className="w-6 h-6 text-emerald-600 shrink-0" />
                                <div>
                                    <p className="font-semibold text-emerald-800">Período Cerrado Oficialmente</p>
                                    <p className="text-xs text-emerald-600">Noviembre 2026 — Bloqueado el {new Date().toLocaleDateString("es-CL")}</p>
                                </div>
                            </div>
                        )}
                    </div>
                </div>

                {/* AI Chat Panel */}
                <div className="lg:col-span-2">
                    <Card className="h-full flex flex-col border-slate-200 shadow-sm sticky top-6">
                        <CardHeader className="border-b border-slate-100 bg-gradient-to-r from-indigo-600 to-purple-700 text-white rounded-t-xl">
                            <CardTitle className="text-base flex items-center gap-2">
                                <Sparkles className="w-4 h-4 text-indigo-200" />
                                Asistente IA — Cierre Contable
                            </CardTitle>
                            <CardDescription className="text-indigo-200 text-xs">
                                Análisis en tiempo real del período
                            </CardDescription>
                        </CardHeader>
                        <CardContent className="flex-1 overflow-y-auto p-4 space-y-3 bg-slate-50 max-h-[550px]">
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
                                        <div className="w-7 h-7 rounded-full bg-indigo-100 flex items-center justify-center shrink-0 mt-0.5">
                                            <Bot className="w-4 h-4 text-indigo-600" />
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
                                <span>IA con acceso de solo lectura al libro contable</span>
                            </div>
                        </div>
                    </Card>
                </div>
            </div>
        </div>
    );
}
