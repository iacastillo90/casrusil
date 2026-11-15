"use client";

import { useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Progress } from "@/components/ui/progress";
import { Badge } from "@/components/ui/badge";
import { FileText, Calendar, AlertCircle, CheckCircle2, Loader2 } from "lucide-react";
import { toast } from "sonner";
import { siiService } from "../services/sii.service";

interface CAFUploadProps {
    onSuccess?: (folioRange: { start: number; end: number }) => void;
}

export const CAFUpload = ({ onSuccess }: CAFUploadProps) => {
    const [selectedFile, setSelectedFile] = useState<File | null>(null);
    const [uploadProgress, setUploadProgress] = useState<number>(0);
    const [uploadedFolios, setUploadedFolios] = useState<{ start: number; end: number } | null>(null);
    const [isUploading, setIsUploading] = useState(false);

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (file) {
            // Validar que sea XML
            if (!file.name.endsWith('.xml')) {
                toast.error("El archivo debe ser en formato XML", {
                    description: "Los archivos CAF son archivos XML",
                });
                return;
            }
            setSelectedFile(file);
            setUploadedFolios(null);
            setUploadProgress(0);
        }
    };

    const handleUpload = async () => {
        if (!selectedFile) {
            toast.error("Selecciona un archivo CAF válido");
            return;
        }

        setIsUploading(true);
        setUploadProgress(0);
        let progressInterval: ReturnType<typeof setInterval> | undefined;

        try {
            // Simular progreso de upload
            progressInterval = setInterval(() => {
                setUploadProgress(prev => Math.min(prev + 10, 90));
            }, 150);

            const result = await siiService.uploadCAF(selectedFile);
            
            if (progressInterval) clearInterval(progressInterval);
            setUploadProgress(100);
            
            setUploadedFolios(result.folioRange);
            onSuccess?.(result.folioRange);
            
            toast.success("Archivo CAF subido correctamente", {
                description: `Folios ${result.folioRange.start} a ${result.folioRange.end}`,
            });

        } catch (error) {
            if (progressInterval) clearInterval(progressInterval);
            setUploadProgress(0);
            toast.error("Error al subir archivo CAF", {
                description: error instanceof Error ? error.message : "Inténtalo nuevamente",
            });
        } finally {
            setIsUploading(false);
        }
    };

    const reset = () => {
        setSelectedFile(null);
        setUploadedFolios(null);
        setUploadProgress(0);
    };

    return (
        <Card className="w-full">
            <CardHeader>
                <CardTitle className="flex items-center gap-2">
                    <FileText className="h-5 w-5" />
                    Subida de Archivo CAF
                </CardTitle>
                <CardDescription>
                    Sube archivos CAF en formato XML para sincronizar folios con el SII
                </CardDescription>
            </CardHeader>
            
            <CardContent className="space-y-4">
                {/* Upload area */}
                {!uploadedFolios && (
                    <div className="space-y-4">
                        <div className="grid w-full items-center gap-1.5">
                            <Label htmlFor="caf-file">Archivo CAF (XML)</Label>
                            <Input
                                id="caf-file"
                                type="file"
                                accept=".xml"
                                onChange={handleFileChange}
                                disabled={isUploading}
                            />
                        </div>

                        {selectedFile && (
                            <div className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg border">
                                <FileText className="h-4 w-4 text-blue-500" />
                                <div className="flex-1">
                                    <p className="font-medium text-sm">{selectedFile.name}</p>
                                    <p className="text-xs text-muted-foreground">
                                        {(selectedFile.size / 1024).toFixed(1)} KB
                                    </p>
                                </div>
                                <Badge variant="outline">XML</Badge>
                            </div>
                        )}

                        {/* Progress bar */}
                        {isUploading && (
                            <div className="space-y-2">
                                <div className="flex justify-between text-sm">
                                    <span>Procesando archivo...</span>
                                    <span>{uploadProgress}%</span>
                                </div>
                                <Progress value={uploadProgress} className="w-full" />
                            </div>
                        )}

                        {/* Botones de acción */}
                        <div className="flex gap-3 pt-2">
                            <Button 
                                variant="outline" 
                                onClick={reset}
                                disabled={isUploading}
                            >
                                Limpiar
                            </Button>
                            <Button
                                onClick={handleUpload}
                                disabled={!selectedFile || isUploading}
                            >
                                {isUploading ? (
                                    <>
                                        <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                                        Procesando...
                                    </>
                                ) : (
                                    <>
                                        Subir CAF
                                    </>
                                )}
                            </Button>
                        </div>
                    </div>
                )}

                {/* Preview de folios */}
                {uploadedFolios && (
                    <div className="space-y-3">
                        <div className="flex items-center gap-3 p-4 bg-green-50 rounded-lg border border-green-200">
                            <CheckCircle2 className="h-5 w-5 text-green-600" />
                            <div className="flex-1">
                                <h4 className="font-medium text-green-900">CAF procesado correctamente</h4>
                                <p className="text-sm text-green-700">
                                    Folios disponibles: {uploadedFolios.start} - {uploadedFolios.end}
                                </p>
                            </div>
                        </div>
                        
                        <div className="grid grid-cols-3 gap-4 text-center">
                            <div className="p-3 bg-gray-50 rounded-lg">
                                <p className="text-xs text-muted-foreground">Folio Inicial</p>
                                <p className="font-semibold">{uploadedFolios.start}</p>
                            </div>
                            <div className="p-3 bg-gray-50 rounded-lg">
                                <p className="text-xs text-muted-foreground">Folio Final</p>
                                <p className="font-semibold">{uploadedFolios.end}</p>
                            </div>
                            <div className="p-3 bg-gray-50 rounded-lg">
                                <p className="text-xs text-muted-foreground">Total</p>
                                <p className="font-semibold">
                                    {uploadedFolios.end - uploadedFolios.start + 1}
                                </p>
                            </div>
                        </div>

                        <Button onClick={() => {
                            setSelectedFile(null);
                            setUploadedFolios(null);
                        }} disabled={isUploading}>
                            Subir otro archivo
                        </Button>
                    </div>
                )}
            </CardContent>
        </Card>
    );
};

export default CAFUpload;