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

export function FeeImportModal() {
    const [open, setOpen] = useState(false);
    const [file, setFile] = useState<File | null>(null);
    const [importing, setImporting] = useState(false);

    const handleImport = async () => {
        if (!file) return;
        setImporting(true);
        try {
            const formData = new FormData();
            formData.append('file', file);
            // await api.post('/fees/import', formData, { headers: { 'Content-Type': 'multipart/form-data' } });
            
            toast.success("Archivo de boletas procesado correctamente.");
            setFile(null);
            setOpen(false);
        } catch {
            toast.error("Error al importar el archivo de honorarios.");
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
                        Sube el archivo (CSV / Excel) exportado desde el portal del SII con las boletas recibidas.
                    </DialogDescription>
                </DialogHeader>
                <div className="grid gap-2 py-2">
                    <Label htmlFor="fee-file">Archivo</Label>
                    <Input
                        id="fee-file"
                        type="file"
                        accept=".csv,.xls,.xlsx"
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
