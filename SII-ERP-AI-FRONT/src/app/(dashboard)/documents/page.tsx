"use client";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { FileText, Sparkles, UploadCloud, CalendarAlert } from "lucide-react";
import { useDocuments, useAnalyzeDocument } from "@/features/dms/hooks/useDms";
import { toast } from "sonner";

export default function DmsPage() {
    const { data: documents, isLoading } = useDocuments();
    const { mutate: analyze, isPending } = useAnalyzeDocument();

    const handleAnalyze = (id: string) => {
        analyze(id, {
            onSuccess: () => toast.success("Documento analizado con IA correctamente.")
        });
    };

    const getDocIcon = (type: string) => {
        return <FileText className="h-8 w-8 text-blue-500" />;
    };

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Gestor Documental Inteligente (DMS)</h1>
                    <p className="text-muted-foreground mt-1">Sube tus contratos y deja que la IA extraiga fechas clave.</p>
                </div>
                <Button><UploadCloud className="mr-2 h-4 w-4" /> Subir Documento</Button>
            </div>

            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                {isLoading ? <p>Cargando documentos...</p> : documents?.length === 0 ? <p>No hay documentos subidos.</p> : documents?.map((doc: any) => (
                    <Card key={doc.id} className="shadow-sm">
                        <CardHeader className="flex flex-row items-start justify-between pb-2">
                            <div className="flex items-center gap-3">
                                {getDocIcon(doc.documentType)}
                                <div>
                                    <CardTitle className="text-base">{doc.title}</CardTitle>
                                    <p className="text-xs text-muted-foreground">{doc.documentType}</p>
                                </div>
                            </div>
                        </CardHeader>
                        <CardContent className="pt-4">
                            {doc.aiSummary ? (
                                <div className="space-y-3">
                                    <div className="bg-blue-50 border border-blue-100 p-3 rounded-md text-sm text-blue-900">
                                        <div className="flex items-center gap-2 font-semibold mb-1">
                                            <Sparkles className="h-4 w-4 text-blue-600" /> Resumen IA
                                        </div>
                                        {doc.aiSummary}
                                    </div>
                                    <div className="flex items-center gap-2 text-sm font-medium text-red-600 bg-red-50 p-2 rounded-md border border-red-100">
                                        <CalendarAlert className="h-4 w-4" />
                                        Vence el: {doc.expirationDate}
                                    </div>
                                </div>
                            ) : (
                                <div className="text-center py-4">
                                    <Button variant="secondary" className="w-full text-indigo-700 bg-indigo-50 hover:bg-indigo-100" onClick={() => handleAnalyze(doc.id)} disabled={isPending}>
                                        <Sparkles className="mr-2 h-4 w-4" /> Analizar con IA
                                    </Button>
                                </div>
                            )}
                        </CardContent>
                    </Card>
                ))}
            </div>
        </div>
    );
}