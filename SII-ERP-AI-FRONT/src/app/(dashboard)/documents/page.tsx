'use client';

import { useState } from 'react';
import { CalendarDays, FilePlus2, FileText, FolderOpen, Sparkles } from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { PageHeader } from '@/features/accounting/components/PageHeader';
import { EmptyState, ErrorState, TableSkeleton } from '@/features/accounting/components/DataStates';
import {
    useAnalyzeDocument,
    useDocuments,
    useUploadDocumentFile,
} from '@/features/dms/hooks/useDms';
import { DMS_ALLOWED_TYPES, DMS_MAX_SIZE_BYTES } from '@/features/dms/services/dms.service';
import { formatDate } from '@/lib/formatters';

export default function DmsPage() {
    const [open, setOpen] = useState(false);
    const [form, setForm] = useState({ title: '', documentType: '' });
    const [file, setFile] = useState<File | null>(null);
    const [fileError, setFileError] = useState<string | null>(null);

    const documents = useDocuments();
    const uploadDocument = useUploadDocumentFile();
    const analyze = useAnalyzeDocument();

    const validateFile = (candidate: File | null): string | null => {
        if (!candidate) return 'Selecciona un archivo';
        if (candidate.size === 0) return 'El archivo está vacío';
        if (candidate.size > DMS_MAX_SIZE_BYTES) {
            return `El archivo supera el máximo de 10 MB (${(candidate.size / (1024 * 1024)).toFixed(1)} MB)`;
        }
        const type = candidate.type || 'application/octet-stream';
        if (!(DMS_ALLOWED_TYPES as readonly string[]).includes(type)) {
            return `Tipo no permitido (${type}). Usa PDF, PNG, JPEG, TXT, DOCX o XLSX`;
        }
        return null;
    };

    const handleCreate = async (event: React.FormEvent) => {
        event.preventDefault();
        const validationError = validateFile(file);
        if (validationError) {
            setFileError(validationError);
            return;
        }
        if (!form.documentType.trim()) {
            toast.error('Indica el tipo de documento (por ejemplo CONTRATO)');
            return;
        }
        try {
            await uploadDocument.mutateAsync({
                file: file as File,
                title: form.title.trim() || undefined,
                documentType: form.documentType.trim(),
            });
            toast.success('Documento subido');
            setOpen(false);
            setForm({ title: '', documentType: '' });
            setFile(null);
            setFileError(null);
        } catch (error) {
            toast.error('No se pudo subir el documento', {
                description: error instanceof Error ? error.message : undefined,
            });
        }
    };

    const handleAnalyze = (id: string) => {
        analyze.mutate(id, {
            onSuccess: () => toast.success('Documento analizado con IA'),
            onError: (error) =>
                toast.error('No se pudo analizar el documento', {
                    description: error instanceof Error ? error.message : undefined,
                }),
        });
    };

    return (
        <div className="space-y-6">
            <PageHeader
                title="Gestor documental"
                description="Contratos y documentos de la empresa, con resumen y fecha de vencimiento extraídos por IA."
                actions={
                    <Button onClick={() => setOpen(true)}>
                        <FilePlus2 aria-hidden="true" className="mr-2 h-4 w-4" />
                        Subir documento
                    </Button>
                }
            />

            {documents.error ? (
                <ErrorState
                    title="No se pudieron cargar los documentos"
                    message={
                        documents.error instanceof Error
                            ? documents.error.message
                            : 'Error desconocido'
                    }
                    onRetry={() => documents.refetch()}
                    retrying={documents.isFetching}
                />
            ) : documents.isLoading ? (
                <TableSkeleton rows={4} cols={3} />
            ) : !documents.data || documents.data.length === 0 ? (
                <EmptyState
                    icon={FolderOpen}
                    title="No hay documentos registrados"
                    description="Registra un contrato o certificado para que la IA extraiga las fechas clave."
                    action={
                        <Button variant="outline" onClick={() => setOpen(true)}>
                            <FilePlus2 aria-hidden="true" className="mr-2 h-4 w-4" />
                            Subir documento
                        </Button>
                    }
                />
            ) : (
                <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                    {documents.data.map((document) => (
                        <Card key={document.id}>
                            <CardHeader className="flex flex-row items-start gap-3 pb-2">
                                <FileText
                                    aria-hidden="true"
                                    className="mt-0.5 h-5 w-5 shrink-0 text-muted-foreground"
                                />
                                <div>
                                    <CardTitle className="text-base font-semibold">
                                        {document.title}
                                    </CardTitle>
                                    <p className="text-xs text-muted-foreground">
                                        {document.documentType}
                                    </p>
                                </div>
                            </CardHeader>
                            <CardContent className="space-y-3">
                                {document.aiSummary ? (
                                    <>
                                        <div className="rounded-lg border border-info-subtle-foreground/20 bg-info-subtle p-3 text-sm text-info-subtle-foreground">
                                            <p className="mb-1 flex items-center gap-2 font-medium">
                                                <Sparkles aria-hidden="true" className="h-4 w-4" />
                                                Resumen IA
                                            </p>
                                            <p>{document.aiSummary}</p>
                                        </div>
                                        {document.expirationDate ? (
                                            <div className="flex items-center gap-2 rounded-lg border border-warning-subtle-foreground/30 bg-warning-subtle p-2 text-sm text-warning-subtle-foreground">
                                                <CalendarDays aria-hidden="true" className="h-4 w-4" />
                                                Vence el {formatDate(document.expirationDate)}
                                            </div>
                                        ) : null}
                                    </>
                                ) : (
                                    <Button
                                        variant="secondary"
                                        className="w-full"
                                        onClick={() => handleAnalyze(document.id)}
                                        loading={analyze.isPending && analyze.variables === document.id}
                                    >
                                        <Sparkles aria-hidden="true" className="mr-2 h-4 w-4" />
                                        Analizar con IA
                                    </Button>
                                )}
                            </CardContent>
                        </Card>
                    ))}
                </div>
            )}

            <Dialog open={open} onOpenChange={setOpen}>
                <DialogContent>
                    <form onSubmit={handleCreate} className="space-y-4">
                        <DialogHeader>
                            <DialogTitle>Subir documento</DialogTitle>
                            <DialogDescription>
                                Se sube el archivo al almacenamiento del sistema (máx. 10 MB): PDF,
                                PNG, JPEG, TXT, DOCX o XLSX.
                            </DialogDescription>
                        </DialogHeader>

                        <div className="space-y-3">
                            <div className="space-y-1.5">
                                <Label htmlFor="doc-title">Título</Label>
                                <Input
                                    id="doc-title"
                                    value={form.title}
                                    onChange={(event) =>
                                        setForm({ ...form, title: event.target.value })
                                    }
                                    placeholder="Contrato de arriendo oficina"
                                    required
                                />
                            </div>
                            <div className="space-y-1.5">
                                <Label htmlFor="doc-type">Tipo de documento</Label>
                                <Input
                                    id="doc-type"
                                    value={form.documentType}
                                    onChange={(event) =>
                                        setForm({ ...form, documentType: event.target.value })
                                    }
                                    placeholder="CONTRATO"
                                    required
                                />
                            </div>
                            <div className="space-y-1.5">
                                <Label htmlFor="doc-file">Archivo</Label>
                                <Input
                                    id="doc-file"
                                    type="file"
                                    accept={DMS_ALLOWED_TYPES.join(',')}
                                    onChange={(event) => {
                                        const selected = event.target.files?.[0] ?? null;
                                        setFile(selected);
                                        setFileError(validateFile(selected));
                                    }}
                                />
                                {file ? (
                                    <p className="text-xs text-muted-foreground">
                                        {file.name} · {(file.size / 1024).toFixed(0)} KB
                                    </p>
                                ) : null}
                                {fileError ? (
                                    <p className="text-xs text-destructive">{fileError}</p>
                                ) : null}
                            </div>
                        </div>

                        <DialogFooter>
                            <Button
                                type="button"
                                variant="outline"
                                onClick={() => {
                                    setOpen(false);
                                    setFile(null);
                                    setFileError(null);
                                }}
                                disabled={uploadDocument.isPending}
                            >
                                Cancelar
                            </Button>
                            <Button
                                type="submit"
                                loading={uploadDocument.isPending}
                                disabled={!file || Boolean(fileError)}
                            >
                                Subir documento
                            </Button>
                        </DialogFooter>
                    </form>
                </DialogContent>
            </Dialog>
        </div>
    );
}
