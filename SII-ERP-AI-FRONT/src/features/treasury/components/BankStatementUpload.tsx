"use client";

import { useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Upload, FileText, Loader2, CheckCircle2 } from "lucide-react";
import { toast } from "sonner";

export function BankStatementUpload() {
    const [file, setFile] = useState<File | null>(null);
    const [uploading, setUploading] = useState(false);
    const [uploaded, setUploaded] = useState(false);

    const handleUpload = async () => {
        if (!file) return;
        setUploading(true);
        setUploaded(false);
        try {
            // NOTA: integración real de carga de cartola pendiente en el backend.
            // Aquí se enviaría el archivo al endpoint de conciliación bancaria.
            await new Promise((resolve) => setTimeout(resolve, 700));
            setUploaded(true);
            toast.success("Cartola cargada. El motor de conciliación la procesará.");
        } catch {
            toast.error("Error al cargar la cartola bancaria.");
        } finally {
            setUploading(false);
        }
    };

    return (
        <Card>
            <CardHeader>
                <CardTitle className="flex items-center gap-2">
                    <Upload className="h-5 w-5 text-indigo-600" />
                    Cargar Cartola Bancaria
                </CardTitle>
                <CardDescription>
                    Sube el archivo (CSV / Excel) de tu cartola para iniciar la conciliación automática.
                </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
                <div className="grid w-full max-w-md items-center gap-1.5">
                    <Label htmlFor="statement-file">Archivo de cartola</Label>
                    <Input
                        id="statement-file"
                        type="file"
                        accept=".csv,.xls,.xlsx"
                        onChange={(e) => {
                            setFile(e.target.files?.[0] ?? null);
                            setUploaded(false);
                        }}
                    />
                </div>

                {file && (
                    <div className="flex items-center gap-2 rounded-md bg-muted p-3 text-sm">
                        <FileText className="h-4 w-4 text-blue-500" />
                        <span className="flex-1 truncate">{file.name}</span>
                        {uploaded && <CheckCircle2 className="h-4 w-4 text-green-600" />}
                    </div>
                )}

                <Button onClick={handleUpload} disabled={!file || uploading}>
                    {uploading ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <Upload className="mr-2 h-4 w-4" />}
                    Cargar y Conciliar
                </Button>
            </CardContent>
        </Card>
    );
}
