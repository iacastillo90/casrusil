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

export type BookType = "SALE" | "PURCHASE";

interface CsvImportModalProps {
    defaultBookType?: BookType;
}

interface InvoiceImportResult {
    successCount: number;
    errorCount: number;
    message?: string;
}

const getErrorMessage = (error: unknown): string =>
    error instanceof Error ? error.message : "Inténtalo nuevamente";

export function CsvImportModal({ defaultBookType = "SALE" }: CsvImportModalProps) {
    const [open, setOpen] = useState(false);
    const [file, setFile] = useState<File | null>(null);
    const [bookType, setBookType] = useState<BookType>(defaultBookType);
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

            const { data } = await api.post<InvoiceImportResult>("/invoices/import", formData, {
                params: { companyId, bookType },
            });

            if (data.successCount > 0) {
                toast.success(`Se importaron ${data.successCount} documentos.`, {
                    description:
                        data.errorCount > 0
                            ? `${data.errorCount} filas no se pudieron procesar.`
                            : undefined,
                });
                await Promise.all([
                    queryClient.invalidateQueries({ queryKey: ["invoices"] }),
                    queryClient.invalidateQueries({ queryKey: ["reports"] }),
                ]);
                setFile(null);
                setOpen(false);
            } else {
                toast.error("No se importaron documentos.", {
                    description:
                        data.message || "Revisa el formato del CSV del SII e inténtalo nuevamente.",
                });
            }
        } catch (error) {
            toast.error("Error al importar el CSV.", {
                description: getErrorMessage(error),
            });
        } finally {
            setImporting(false);
        }
    };

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                <Button variant="outline" className="gap-2">
                    <FileSpreadsheet className="h-4 w-4" />
                    Importar CSV
                </Button>
            </DialogTrigger>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle className="flex items-center gap-2">
                        <FileSpreadsheet className="h-5 w-5" />
                        Importar documentos desde CSV
                    </DialogTitle>
                    <DialogDescription>
                        Carga masiva de documentos al libro de {bookType === "SALE" ? "ventas" : "compras"}.
                    </DialogDescription>
                </DialogHeader>
                <div className="grid gap-4 py-2">
                    <div className="grid gap-2">
                        <Label>Tipo de libro</Label>
                        <div className="flex gap-2">
                            <Button
                                type="button"
                                variant={bookType === "SALE" ? "default" : "outline"}
                                size="sm"
                                onClick={() => setBookType("SALE")}
                            >
                                Ventas
                            </Button>
                            <Button
                                type="button"
                                variant={bookType === "PURCHASE" ? "default" : "outline"}
                                size="sm"
                                onClick={() => setBookType("PURCHASE")}
                            >
                                Compras
                            </Button>
                        </div>
                    </div>
                    <div className="grid gap-2">
                        <Label htmlFor="csv-file">Archivo CSV</Label>
                        <Input
                            id="csv-file"
                            type="file"
                            accept=".csv"
                            onChange={(e) => setFile(e.target.files?.[0] ?? null)}
                        />
                        {file && <p className="text-sm text-muted-foreground truncate">{file.name}</p>}
                    </div>
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
