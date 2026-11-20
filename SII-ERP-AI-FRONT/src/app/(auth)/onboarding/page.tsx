"use client";

import { useEffect, useMemo, useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { AlertCircle, ArrowRight, Building2, CheckCircle2, Key, ShieldCheck } from "lucide-react";
import { useRouter } from "next/navigation";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { StatusBadge } from "@/components/shared/StatusBadge";
import { api } from "@/lib/api";
import {
    MISSING_FIELD_TO_FORM,
    type CertificateInfo,
    type CompanyOnboardingData,
    type OnboardingStatus,
    saveCompanyData,
    uploadCertificate,
} from "@/features/onboarding/services/onboarding.service";

const STEPS = [
    { icon: Building2, label: "Empresa" },
    { icon: Key, label: "Certificado" },
    { icon: ShieldCheck, label: "Listo" },
] as const;

const EMPTY_FORM: CompanyOnboardingData = {
    razonSocial: "",
    email: "",
    direccion: "",
    giro: "",
    acteco: "",
    comuna: "",
};

interface CompanyProfile {
    razonSocial?: string;
    email?: string;
    commercialAddress?: string;
    giro?: string;
    acteco?: string;
    comuna?: string;
}

export default function OnboardingPage() {
    const router = useRouter();
    const [step, setStep] = useState(1);
    const [form, setForm] = useState<CompanyOnboardingData>(EMPTY_FORM);
    const [status, setStatus] = useState<OnboardingStatus | null>(null);
    const [certificate, setCertificate] = useState<CertificateInfo | null>(null);
    const [file, setFile] = useState<File | null>(null);
    const [password, setPassword] = useState("");
    const [error, setError] = useState<string | null>(null);
    const [saving, setSaving] = useState(false);
    const [uploading, setUploading] = useState(false);

    useEffect(() => {
        api.get<CompanyProfile>("/company/profile")
            .then(({ data }) => {
                setForm((current) => ({
                    razonSocial: current.razonSocial || data.razonSocial || "",
                    email: current.email || data.email || "",
                    direccion: current.direccion || data.commercialAddress || "",
                    giro: current.giro || data.giro || "",
                    acteco: current.acteco || data.acteco || "",
                    comuna: current.comuna || data.comuna || "",
                }));
            })
            .catch(() => {
                // Sin perfil previo: el formulario arranca vacío.
            });
    }, []);

    const missingByField = useMemo(() => {
        const map: Partial<Record<keyof CompanyOnboardingData, string>> = {};
        for (const missing of status?.missingFields ?? []) {
            const key = MISSING_FIELD_TO_FORM[missing.toLowerCase()];
            if (key) {
                map[key] = "Obligatorio para emitir";
            }
        }
        return map;
    }, [status]);

    const update = (key: keyof CompanyOnboardingData, value: string) => {
        setForm((current) => ({ ...current, [key]: value }));
    };

    const handleSaveCompany = async () => {
        setSaving(true);
        setError(null);
        try {
            const result = await saveCompanyData(form);
            setStatus(result);
            if (result.emissionReady) {
                toast.success("Datos de la empresa guardados");
                setStep(2);
            } else {
                toast.warning("Faltan datos para poder emitir");
            }
        } catch (err) {
            setError(err instanceof Error ? err.message : "No se pudieron guardar los datos");
        } finally {
            setSaving(false);
        }
    };

    const handleUploadCertificate = async () => {
        if (!file) {
            setError("Selecciona el archivo .p12 de tu certificado");
            return;
        }
        if (!password) {
            setError("Ingresa la contraseña del certificado");
            return;
        }

        setUploading(true);
        setError(null);
        try {
            const info = await uploadCertificate(file, password);
            setCertificate(info);
            // La contraseña no se conserva en el estado del cliente.
            setPassword("");
            setFile(null);
            toast.success("Certificado cargado y validado");
            setStep(3);
        } catch (err) {
            setError(err instanceof Error ? err.message : "No se pudo cargar el certificado");
        } finally {
            setUploading(false);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-slate-50 p-4">
            <div className="w-full max-w-xl">
                <div className="mb-8 relative">
                    <div className="absolute top-1/2 left-0 w-full h-1 bg-slate-200 -z-10 -translate-y-1/2 rounded-full" />
                    <div
                        className="absolute top-1/2 left-0 h-1 bg-primary -z-10 -translate-y-1/2 rounded-full transition-all duration-500 ease-in-out"
                        style={{ width: `${((step - 1) / (STEPS.length - 1)) * 100}%` }}
                    />
                    <div className="flex justify-between">
                        {STEPS.map((item, index) => (
                            <div key={item.label} className="flex flex-col items-center">
                                <div
                                    className={`w-10 h-10 rounded-full flex items-center justify-center transition-colors duration-300 ${
                                        step > index + 1
                                            ? "bg-primary text-primary-foreground"
                                            : step === index + 1
                                              ? "bg-primary/10 text-primary border-2 border-primary"
                                              : "bg-white text-slate-400 border-2 border-slate-200"
                                    }`}
                                >
                                    {step > index + 1 ? (
                                        <CheckCircle2 className="w-5 h-5" />
                                    ) : (
                                        <item.icon className="w-5 h-5" />
                                    )}
                                </div>
                                <span className="text-xs font-medium mt-2 text-slate-600">{item.label}</span>
                            </div>
                        ))}
                    </div>
                </div>

                {error && (
                    <Alert variant="destructive" className="mb-4">
                        <AlertCircle className="h-4 w-4" />
                        <AlertTitle>No se pudo completar el paso</AlertTitle>
                        <AlertDescription>{error}</AlertDescription>
                    </Alert>
                )}

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
                                        <CardTitle>Datos de la empresa</CardTitle>
                                        <CardDescription>
                                            Estos datos aparecen en cada DTE y son requisitos del SII.
                                        </CardDescription>
                                    </CardHeader>
                                    <CardContent className="space-y-4">
                                        <div className="space-y-2">
                                            <Label htmlFor="razonSocial">Razón social</Label>
                                            <Input
                                                id="razonSocial"
                                                value={form.razonSocial}
                                                error={missingByField.razonSocial}
                                                onChange={(event) => update("razonSocial", event.target.value)}
                                                placeholder="Mi Empresa SpA"
                                            />
                                        </div>
                                        <div className="space-y-2">
                                            <Label htmlFor="email">Email de contacto</Label>
                                            <Input
                                                id="email"
                                                type="email"
                                                value={form.email}
                                                onChange={(event) => update("email", event.target.value)}
                                                placeholder="contacto@miempresa.cl"
                                            />
                                        </div>
                                        <div className="space-y-2">
                                            <Label htmlFor="direccion">Dirección</Label>
                                            <Input
                                                id="direccion"
                                                value={form.direccion}
                                                error={missingByField.direccion}
                                                onChange={(event) => update("direccion", event.target.value)}
                                                placeholder="Av. Providencia 1234"
                                            />
                                        </div>
                                        <div className="grid grid-cols-2 gap-4">
                                            <div className="space-y-2">
                                                <Label htmlFor="giro">Giro</Label>
                                                <Input
                                                    id="giro"
                                                    value={form.giro}
                                                    error={missingByField.giro}
                                                    onChange={(event) => update("giro", event.target.value)}
                                                    placeholder="Servicios de informática"
                                                />
                                            </div>
                                            <div className="space-y-2">
                                                <Label htmlFor="acteco">Acteco</Label>
                                                <Input
                                                    id="acteco"
                                                    value={form.acteco}
                                                    error={missingByField.acteco}
                                                    onChange={(event) => update("acteco", event.target.value)}
                                                    placeholder="620100"
                                                    helpText="Código de actividad económica del SII"
                                                />
                                            </div>
                                        </div>
                                        <div className="space-y-2">
                                            <Label htmlFor="comuna">Comuna</Label>
                                            <Input
                                                id="comuna"
                                                value={form.comuna}
                                                error={missingByField.comuna}
                                                onChange={(event) => update("comuna", event.target.value)}
                                                placeholder="Providencia"
                                            />
                                        </div>
                                    </CardContent>
                                    <CardFooter className="flex justify-end">
                                        <Button onClick={handleSaveCompany} loading={saving}>
                                            Siguiente <ArrowRight className="w-4 h-4 ml-2" />
                                        </Button>
                                    </CardFooter>
                                </>
                            )}

                            {step === 2 && (
                                <>
                                    <CardHeader>
                                        <CardTitle>Certificado digital</CardTitle>
                                        <CardDescription>
                                            Sube tu certificado .p12. Se guarda cifrado y se valida contra el RUT de la empresa.
                                        </CardDescription>
                                    </CardHeader>
                                    <CardContent className="space-y-4">
                                        <div className="space-y-2">
                                            <Label htmlFor="sii-cert">Certificado (.p12)</Label>
                                            <Input
                                                id="sii-cert"
                                                type="file"
                                                accept=".p12,.pfx"
                                                onChange={(event) => setFile(event.target.files?.[0] ?? null)}
                                            />
                                        </div>
                                        <div className="space-y-2">
                                            <Label htmlFor="sii-pass">Contraseña del certificado</Label>
                                            <Input
                                                id="sii-pass"
                                                type="password"
                                                autoComplete="off"
                                                value={password}
                                                onChange={(event) => setPassword(event.target.value)}
                                                helpText="No se guarda en el navegador: sólo se envía al backend para abrir el certificado."
                                            />
                                        </div>
                                        <div className="p-4 bg-primary/5 text-primary rounded-lg text-sm border border-primary/10 flex items-start gap-3">
                                            <Key className="w-5 h-5 shrink-0 mt-0.5" />
                                            <p>
                                                El archivo y la contraseña se cifran en reposo en el servidor y nunca se
                                                devuelven en las respuestas.
                                            </p>
                                        </div>
                                    </CardContent>
                                    <CardFooter className="flex justify-between">
                                        <Button variant="outline" onClick={() => setStep(1)}>
                                            Atrás
                                        </Button>
                                        <Button onClick={handleUploadCertificate} loading={uploading}>
                                            Cargar certificado <ArrowRight className="w-4 h-4 ml-2" />
                                        </Button>
                                    </CardFooter>
                                </>
                            )}

                            {step === 3 && (
                                <>
                                    <CardHeader>
                                        <CardTitle>Todo listo</CardTitle>
                                        <CardDescription>
                                            Revisa el estado antes de empezar a emitir documentos.
                                        </CardDescription>
                                    </CardHeader>
                                    <CardContent className="space-y-4">
                                        <div className="flex items-center justify-between rounded-lg border p-4">
                                            <div>
                                                <p className="text-sm font-medium">Estado de emisión</p>
                                                <p className="text-xs text-muted-foreground">
                                                    {status?.emissionReady
                                                        ? "La empresa tiene todos los datos que exige el SII."
                                                        : "Faltan datos obligatorios para emitir."}
                                                </p>
                                            </div>
                                            <StatusBadge status={status?.emissionReady ? "ACCEPTED" : "REJECTED"} />
                                        </div>

                                        {!status?.emissionReady && (status?.missingFields?.length ?? 0) > 0 && (
                                            <Alert>
                                                <AlertCircle className="h-4 w-4" />
                                                <AlertTitle>Falta completar</AlertTitle>
                                                <AlertDescription>
                                                    <ul className="mt-2 list-disc pl-4 space-y-1">
                                                        {status?.missingFields.map((missing) => (
                                                            <li key={missing}>{missing}</li>
                                                        ))}
                                                    </ul>
                                                    <Button
                                                        variant="link"
                                                        className="mt-2 h-auto p-0"
                                                        onClick={() => setStep(1)}
                                                    >
                                                        Volver al paso de datos de la empresa
                                                    </Button>
                                                </AlertDescription>
                                            </Alert>
                                        )}

                                        {certificate && (
                                            <div className="rounded-lg border p-4 text-sm">
                                                <p className="font-medium">Certificado cargado</p>
                                                <p className="text-muted-foreground">
                                                    RUT {certificate.rut}
                                                    {certificate.validUntil
                                                        ? ` · válido hasta ${certificate.validUntil}`
                                                        : ""}
                                                </p>
                                            </div>
                                        )}
                                    </CardContent>
                                    <CardFooter className="flex justify-between">
                                        <Button variant="outline" onClick={() => setStep(2)}>
                                            Cambiar certificado
                                        </Button>
                                        <Button onClick={() => router.push("/dashboard")}>
                                            Ir al dashboard <ArrowRight className="w-4 h-4 ml-2" />
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
