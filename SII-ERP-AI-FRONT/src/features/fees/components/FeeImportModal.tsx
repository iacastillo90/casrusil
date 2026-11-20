"use client";

import { useState } from "react";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Upload, FileSpreadsheet, Loader2 } from "lucide-react";
import { toast } from "sonner";
import { useQueryClient } from "@tanstack/react-query";
import api from "@/lib/axios";
import { useAuthStore } from "@/features/auth/stores/auth.store";

interface FeeImportResult {
    successCount: number;
    errorCount: number;
    message?: string;
}

const getErrorMessage = (error: unknown): string =>
    error instanceof Error ? error.message : "Inténtalo nuevamente";

export function FeeImportModal() {
    const [open, setOpen] = useState(false);
    const [file, setFile] = useState<File | null>(null);
    const [importing, setImporting] = useState(false);
    const companyId = useAuthStore((state) => state.companyId);
    const queryClient = useQueryClient();

    const handleImport = async () => {
        if (!file) return;

        if (!companyId) {
            toast.error("No se pudo identificar la empresa. Vuelve a iniciar sesión.");
            return;
        }

        setImporting(true);
        try {
            const formData = new FormData();
            formData.append("file", file);

            // El backend actual exige companyId como request param en POST /fees/import.
            const { data } = await api.post<FeeImportResult>("/fees/import", formData, {
                params: { companyId },
            });

            if (data.successCount > 0) {
                toast.success(`Se importaron ${data.successCount} boletas.`, {
                    description:
                        data.errorCount > 0
                            ? `${data.errorCount} filas no se pudieron procesar.`
                            : undefined,
                });
                await queryClient.invalidateQueries({ queryKey: ["fees"] });
                setFile(null);
                setOpen(false);
            } else {
                toast.error("No se importaron boletas.", {
                    description: data.message || "Revisa el formato del archivo e inténtalo nuevamente.",
                });
            }
        } catch (error) {
            toast.error("Error al importar el archivo de honorarios.", {
                description: getErrorMessage(error),
            });
        } finally {
            setImporting(false);
        }
    };

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                <Button className="gap-2">
                    <Upload className="h-4 w-4" />
                    Importar Boletas
                </Button>
            </DialogTrigger>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle className="flex items-center gap-2">
                        <FileSpreadsheet className="h-5 w-5" />
                        Importar Boletas de Honorarios
                    </DialogTitle>
                    <DialogDescription>
                        Sube el archivo CSV exportado desde el portal del SII con las boletas recibidas.
                    </DialogDescription>
                </DialogHeader>
                <div className="grid gap-2 py-2">
                    <Label htmlFor="fee-file">Archivo CSV</Label>
                    <Input
                        id="fee-file"
                        type="file"
                        accept=".csv"
                        onChange={(e) => setFile(e.target.files?.[0] ?? null)}
                    />
                    {file && <p className="text-sm text-muted-foreground truncate">{file.name}</p>}
                </div>
                <DialogFooter>
                    <Button variant="outline" onClick={() => setOpen(false)} disabled={importing}>
                        Cancelar
                    </Button>
                    <Button onClick={handleImport} disabled={!file || importing}>
                        {importing ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <Upload className="mr-2 h-4 w-4" />}
                        Importar
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
