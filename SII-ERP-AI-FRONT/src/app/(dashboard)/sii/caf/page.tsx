"use client";

import { useState, useEffect } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { FileUp, FileText, CheckCircle, AlertTriangle, Loader2, Eye, AlertCircle } from "lucide-react";
import { siiService } from "@/features/sii/services/sii.service";
import { toast } from "sonner";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { StatusBadge } from "@/components/shared/StatusBadge";
import { Badge } from "@/components/ui/badge";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";

export default function CAFPage() {
    const [file, setFile] = useState<File | null>(null);
    const [uploading, setUploading] = useState(false);
    const [lastUploadResult, setLastUploadResult] = useState<{ start: number; end: number } | null>(null);
    const [cafs, setCafs] = useState<Array<{
        id: string;
        tipoDte: string;
        folioDesde: number;
        folioHasta: number;
        folioActual: number;
        disponibles: number;
        fechaAutorizacion: string;
        fechaVencimiento: string | null;
        estado: 'VIGENTE' | 'POR_AGOTARSE' | 'AGOTADO' | 'VENCIDO';
    }>>([]);
    const [loadingCafs, setLoadingCafs] = useState(true);
    const [errorCafs, setErrorCafs] = useState<string | null>(null);

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files[0]) {
            setFile(e.target.files[0]);
        }
    };

    const handleUpload = async () => {
        if (!file) return;

        setUploading(true);
        try {
            const response = await siiService.uploadCAF(file);
            toast.success("Archivo CAF subido correctamente");
            setLastUploadResult(response.folioRange);
            setFile(null);
            // Recargar lista de CAFs
            await loadCafs();
        } catch (error: any) {
            console.error(error);
            if (error?.response?.status === 409) {
                toast.error("No hay folios disponibles. Verifique los CAFs cargados.");
            } else {
                toast.error("Error al subir archivo CAF");
            }
        } finally {
            setUploading(false);
        }
    };

    const loadCafs = async () => {
        try {
            setLoadingCafs(true);
            setErrorCafs(null);
            const data = await siiService.getCafs();
            setCafs(data);
        } catch (error) {
            console.error(error);
            setErrorCafs("No se pudieron cargar los CAFs");
        } finally {
            setLoadingCafs(false);
        }
    };

    useEffect(() => {
        loadCafs();
    }, []);

    const formatDate = (dateStr: string | null) => {
        if (!dateStr) return '—';
        try {
            return new Date(dateStr).toLocaleDateString('es-CL');
        } catch {
            return dateStr;
        }
    };

    return (
        <div className="space-y-6">
            <h1 className="text-3xl font-bold tracking-tight">Administración de Folios (CAF)</h1>

            <div className="grid gap-6 md:grid-cols-2">
                <Card>
                    <CardHeader>
                        <CardTitle className="flex items-center gap-2">
                            <FileUp className="h-5 w-5" />
                            Cargar Nuevos Folios
                        </CardTitle>
                        <CardDescription>
                            Sube el archivo XML descargado desde el portal del SII.
                        </CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-4">
                        <div className="grid w-full max-w-sm items-center gap-1.5">
                            <Label htmlFor="caf-file">Archivo XML</Label>
                            <Input id="caf-file" type="file" accept=".xml" onChange={handleFileChange} />
                        </div>

                        {file && (
                            <div className="bg-muted p-3 rounded-md flex items-center gap-2 text-sm">
                                <FileText className="h-4 w-4 text-blue-500" />
                                <span className="truncate flex-1">{file.name}</span>
                            </div>
                        )}

                        <Button onClick={handleUpload} loading={uploading} disabled={!file}>
                            {file ? 'Cargar CAF' : 'Seleccionar archivo'}
                        </Button>

                        {lastUploadResult && (
                            <Alert className="bg-green-50 border-green-200">
                                <CheckCircle className="h-4 w-4 text-green-600" />
                                <AlertTitle className="text-green-800">Carga Exitosa</AlertTitle>
                                <AlertDescription className="text-green-700">
                                    Se han cargado los folios desde el <strong>{lastUploadResult.start}</strong> al <strong>{lastUploadResult.end}</strong>.
                                </AlertDescription>
                            </Alert>
                        )}
                    </CardContent>
                </Card>

                <div className="space-y-6">
                    <Card>
                        <CardHeader>
                            <CardTitle>Estado de Folios</CardTitle>
                        </CardHeader>
                        <CardContent>
                            {lastUploadResult ? (
                                <Alert className="bg-green-50 border-green-200">
                                    <CheckCircle className="h-4 w-4 text-green-600" />
                                    <AlertTitle className="text-green-800">Última Carga Exitosa</AlertTitle>
                                    <AlertDescription className="text-green-700">
                                        Se han cargado los folios desde el <strong>{lastUploadResult.start}</strong> al <strong>{lastUploadResult.end}</strong>.
                                    </AlertDescription>
                                </Alert>
                            ) : (
                                <Alert>
                                    <AlertTriangle className="h-4 w-4" />
                                    <AlertTitle>Información</AlertTitle>
                                    <AlertDescription>
                                        No se ha realizado carga de folios recientemente en esta sesión.
                                    </AlertDescription>
                                </Alert>
                            )}
                        </CardContent>
                    </Card>
                </div>
            </div>

            {/* Lista de CAFs cargados */}
            <Card>
                <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                        <FileText className="h-5 w-5" />
                        CAFs Cargados
                    </CardTitle>
                    <CardDescription>
                        Lista de todos los archivos CAF cargados con su estado actual.
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    {loadingCafs ? (
                        <div className="animate-pulse space-y-4">
                            <div className="h-4 bg-muted rounded w-1/4"></div>
                            <div className="space-y-3">
                                {[...Array(3)].map((_, i) => (
                                    <div key={i} className="h-12 bg-muted rounded"></div>
                                ))}
                            </div>
                        </div>
                    ) : errorCafs ? (
                        <Alert variant="destructive">
                            <AlertCircle className="h-4 w-4" />
                            <AlertTitle>Error</AlertTitle>
                            <AlertDescription>{errorCafs}</AlertDescription>
                        </Alert>
                    ) : cafs.length === 0 ? (
                        <div className="text-center py-8">
                            <FileText className="h-12 w-12 text-muted-foreground/50 mx-auto mb-4" />
                            <p className="text-muted-foreground">No hay CAFs cargados</p>
                            <p className="text-sm text-muted-foreground/70 mt-1">
                                Cargue un archivo CAF XML para comenzar
                            </p>
                        </div>
                    ) : (
                        <div className="overflow-x-auto">
                            <Table>
                                <TableHeader>
                                    <TableRow>
                                        <TableHead>Tipo DTE</TableHead>
                                        <TableHead>Rango</TableHead>
                                        <TableHead>Actual</TableHead>
                                        <TableHead>Disponibles</TableHead>
                                        <TableHead>Autorización</TableHead>
                                        <TableHead>Vencimiento</TableHead>
                                        <TableHead>Estado</TableHead>
                                    </TableRow>
                                </TableHeader>
                                <TableBody>
                                    {cafs.map((caf) => (
                                        <TableRow key={caf.id}>
                                            <TableCell>
                                                <Badge variant="outline">{caf.tipoDte}</Badge>
                                            </TableCell>
                                            <TableCell>
                                                {caf.folioDesde} – {caf.folioHasta}
                                            </TableCell>
                                            <TableCell>{caf.folioActual}</TableCell>
                                            <TableCell>
                                                <Badge variant={caf.disponibles === 0 ? "destructive" : "default"}>
                                                    {caf.disponibles}
                                                </Badge>
                                            </TableCell>
                                            <TableCell>{new Date(caf.fechaAutorizacion).toLocaleDateString('es-CL')}</TableCell>
                                            <TableCell>{caf.fechaVencimiento ? new Date(caf.fechaVencimiento).toLocaleDateString('es-CL') : '—'}</TableCell>
                                            <TableCell>
                                                <StatusBadge status={caf.estado} />
                                            </TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                        </div>
                    )}
                </CardContent>
            </Card>
        </div>
    );
}