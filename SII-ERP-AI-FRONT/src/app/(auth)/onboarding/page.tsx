"use client";

import { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Building2, Key, Settings, CheckCircle2, ArrowRight, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { useRouter } from "next/navigation";
import { toast } from "sonner";

export default function OnboardingPage() {
    const [step, setStep] = useState(1);
    const [isLoading, setIsLoading] = useState(false);
    const router = useRouter();

    const nextStep = () => setStep((s) => Math.min(s + 1, 3));
    const prevStep = () => setStep((s) => Math.max(s - 1, 1));

    const handleComplete = async () => {
        setIsLoading(true);
        try {
            // Lógica de llamada al servicio de configuración (ej: await settingsService.saveConfig(...))
            setIsLoading(false);
            toast.success("¡Configuración completada con éxito!");
            router.push("/dashboard");
        } catch (error) {
            setIsLoading(false);
            toast.error("Error al configurar empresa.");
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-slate-50 p-4">
            <div className="w-full max-w-xl">
                {/* Stepper Header */}
                <div className="mb-8 relative">
                    <div className="absolute top-1/2 left-0 w-full h-1 bg-slate-200 -z-10 -translate-y-1/2 rounded-full" />
                    <div
                        className="absolute top-1/2 left-0 h-1 bg-indigo-600 -z-10 -translate-y-1/2 rounded-full transition-all duration-500 ease-in-out"
                        style={{ width: `${((step - 1) / 2) * 100}%` }}
                    />
                    <div className="flex justify-between">
                        {[
                            { icon: Building2, label: "Empresa" },
                            { icon: Key, label: "SII" },
                            { icon: Settings, label: "Preferencias" }
                        ].map((s, i) => (
                            <div key={i} className="flex flex-col items-center">
                                <div className={`w-10 h-10 rounded-full flex items-center justify-center transition-colors duration-300 ${step > i ? "bg-indigo-600 text-white" : step === i + 1 ? "bg-indigo-100 text-indigo-600 border-2 border-indigo-600" : "bg-white text-slate-400 border-2 border-slate-200"}`}>
                                    {step > i + 1 ? <CheckCircle2 className="w-5 h-5" /> : <s.icon className="w-5 h-5" />}
                                </div>
                                <span className="text-xs font-medium mt-2 text-slate-600">{s.label}</span>
                            </div>
                        ))}
                    </div>
                </div>

                {/* Form Content */}
                <AnimatePresence mode="wait">
                    <motion.div
                        key={step}
                        initial={{ opacity: 0, x: 20 }}
                        animate={{ opacity: 1, x: 0 }}
                        exit={{ opacity: 0, x: -20 }}
                        transition={{ duration: 0.3 }}
                    >
                        <Card className="shadow-lg border-0">
                            {step === 1 && (
                                <>
                                    <CardHeader>
                                        <CardTitle>Detalles de la Empresa</CardTitle>
                                        <CardDescription>Ingresa la información básica de tu organización.</CardDescription>
                                    </CardHeader>
                                    <CardContent className="space-y-4">
                                        <div className="space-y-2">
                                            <Label htmlFor="rut">RUT de Empresa</Label>
                                            <Input id="rut" placeholder="76.000.000-K" />
                                        </div>
                                        <div className="space-y-2">
                                            <Label htmlFor="name">Razón Social</Label>
                                            <Input id="name" placeholder="Mi Empresa SpA" />
                                        </div>
                                        <div className="space-y-2">
                                            <Label htmlFor="industry">Industria</Label>
                                            <Input id="industry" placeholder="Ej: Tecnología, Retail, Servicios" />
                                        </div>
                                    </CardContent>
                                    <CardFooter className="flex justify-end">
                                        <Button onClick={nextStep}>
                                            Siguiente <ArrowRight className="w-4 h-4 ml-2" />
                                        </Button>
                                    </CardFooter>
                                </>
                            )}

                            {step === 2 && (
                                <>
                                    <CardHeader>
                                        <CardTitle>Conexión al SII</CardTitle>
                                        <CardDescription>Configura las credenciales para sincronizar automáticamente el RCV y DTEs.</CardDescription>
                                    </CardHeader>
                                    <CardContent className="space-y-4">
                                        <div className="space-y-2">
                                            <Label htmlFor="sii-cert">Certificado Digital (.p12)</Label>
                                            <Input id="sii-cert" type="file" />
                                        </div>
                                        <div className="space-y-2">
                                            <Label htmlFor="sii-pass">Contraseña del Certificado</Label>
                                            <Input id="sii-pass" type="password" />
                                        </div>
                                        <div className="p-4 bg-indigo-50 text-indigo-800 rounded-lg text-sm border border-indigo-100 flex items-start gap-3">
                                            <Key className="w-5 h-5 shrink-0 mt-0.5 text-indigo-600" />
                                            <p>Tus credenciales se encriptan de extremo a extremo y solo se utilizan para la sincronización oficial con el Servicio de Impuestos Internos de Chile.</p>
                                        </div>
                                    </CardContent>
                                    <CardFooter className="flex justify-between">
                                        <Button variant="outline" onClick={prevStep}>Atrás</Button>
                                        <Button onClick={nextStep}>
                                            Siguiente <ArrowRight className="w-4 h-4 ml-2" />
                                        </Button>
                                    </CardFooter>
                                </>
                            )}

                            {step === 3 && (
                                <>
                                    <CardHeader>
                                        <CardTitle>Preferencias del ERP</CardTitle>
                                        <CardDescription>Ajusta el comportamiento inicial del sistema.</CardDescription>
                                    </CardHeader>
                                    <CardContent className="space-y-4">
                                        <div className="space-y-2">
                                            <Label htmlFor="currency">Moneda Principal</Label>
                                            <Input id="currency" value="CLP - Peso Chileno" disabled />
                                        </div>
                                        <div className="space-y-2">
                                            <Label htmlFor="timezone">Zona Horaria</Label>
                                            <Input id="timezone" value="America/Santiago" disabled />
                                        </div>
                                        <div className="space-y-2">
                                            <Label htmlFor="ai-level">Asistencia IA (Modo Dios)</Label>
                                            <select id="ai-level" className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50">
                                                <option value="proactive">Proactiva (Recomendado)</option>
                                                <option value="reactive">Reactiva (Solo a petición)</option>
                                                <option value="off">Desactivada</option>
                                            </select>
                                        </div>
                                    </CardContent>
                                    <CardFooter className="flex justify-between">
                                        <Button variant="outline" onClick={prevStep}>Atrás</Button>
                                        <Button onClick={handleComplete} disabled={isLoading} className="bg-emerald-600 hover:bg-emerald-700">
                                            {isLoading ? <Loader2 className="w-4 h-4 mr-2 animate-spin" /> : <CheckCircle2 className="w-4 h-4 mr-2" />}
                                            {isLoading ? "Configurando..." : "Finalizar Configuración"}
                                        </Button>
                                    </CardFooter>
                                </>
                            )}
                        </Card>
                    </motion.div>
                </AnimatePresence>
            </div>
        </div>
    );
}
