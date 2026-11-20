"use client";

import { useState } from "react";
import { Controller, useFieldArray, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "sonner";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import {
    AlertCircle,
    ArrowLeft,
    ArrowRight,
    Check,
    FileText,
    Plus,
    Receipt,
    Trash2,
} from "lucide-react";

import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";
import { Skeleton } from "@/components/ui/skeleton";
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table";
import { RutInput } from "@/components/shared/RutInput";
import {
    computeInvoiceTotals,
    cleanRut,
    isValidRutFormat,
    invoiceService,
    toCreateInvoiceRequest,
    toUpdateInvoiceRequest,
    type InvoiceFormModel,
} from "@/features/invoicing/services/invoice.service";
import type { Invoice } from "@/features/invoicing/types/invoice.types";
import { settingsService } from "@/features/settings/services/settings.service";
import { siiService } from "@/features/sii/services/sii.service";
import { formatCLP } from "@/lib/formatters";
import { cn } from "@/lib/utils";

const lineSchema = z.object({
    description: z.string().trim().min(1, "La descripción es obligatoria"),
    quantity: z.coerce.number().positive("La cantidad debe ser mayor a 0"),
    unitPrice: z.coerce.number().positive("El precio debe ser mayor a $0"),
});

const invoiceFormSchema = z.object({
    tipoDte: z.union([z.literal(33), z.literal(39)]),
    rutReceptor: z
        .string()
        .min(1, "Ingresa el RUT del receptor")
        .refine((value) => isValidRutFormat(value), "Formato de RUT inválido (ej: 12.345.678-9)"),
    fechaEmision: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, "Selecciona una fecha válida"),
    lines: z.array(lineSchema).min(1, "Agrega al menos una línea"),
});

type InvoiceFormValues = z.infer<typeof invoiceFormSchema>;

const STEPS = [
    { title: "Receptor", sub: "Tipo y cliente" },
    { title: "Líneas", sub: "Productos y servicios" },
    { title: "Totales", sub: "IVA 19%" },
    { title: "Revisar y emitir", sub: "Confirmación" },
] as const;

const todayIso = (): string => {
    const now = new Date();
    const local = new Date(now.getTime() - now.getTimezoneOffset() * 60_000);
    return local.toISOString().slice(0, 10);
};

const getErrorMessage = (error: unknown): string => {
    const err = error as {
        response?: { data?: { message?: string; error?: string } };
    } | null;
    return (
        err?.response?.data?.message ??
        err?.response?.data?.error ??
        (error instanceof Error ? error.message : "Inténtalo nuevamente")
    );
};

interface InvoiceFormProps {
    invoice?: Invoice;
    onClose?: () => void;
}

export function InvoiceForm({ invoice, onClose }: InvoiceFormProps) {
    const router = useRouter();
    const isEditMode = Boolean(invoice);
    const [step, setStep] = useState(0);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const companyQuery = useQuery({
        queryKey: ["company-profile"],
        queryFn: () => settingsService.getCompanyProfile(),
        staleTime: 5 * 60_000,
    });

    const cafsQuery = useQuery({
        queryKey: ["sii-cafs"],
        queryFn: () => siiService.getCafs(),
        retry: false,
    });

    const {
        control,
        handleSubmit,
        register,
        trigger,
        watch,
        formState: { errors },
    } = useForm<InvoiceFormValues>({
        resolver: zodResolver(invoiceFormSchema),
        mode: "onBlur",
        defaultValues: {
            tipoDte: 33,
            rutReceptor: invoice?.recipientRut ?? "",
            fechaEmision: invoice?.issueDate?.slice(0, 10) ?? todayIso(),
            lines: invoice?.lines?.length
                ? invoice.lines.map((line) => ({
                      description: line.description,
                      quantity: line.quantity,
                      unitPrice: line.unitPrice,
                  }))
                : [{ description: "", quantity: 1, unitPrice: 0 }],
        },
    });

    const { fields, append, remove } = useFieldArray({ control, name: "lines" });

    const watchedLines = watch("lines");
    // Sin memo: react-hook-form puede devolver la misma referencia de array
    // entre renders y useMemo dejaría los totales congelados en el valor inicial.
    const totals = computeInvoiceTotals(
        (watchedLines ?? []).map((line) => ({
            description: line?.description ?? "",
            quantity: Number(line?.quantity) || 0,
            unitPrice: Number(line?.unitPrice) || 0,
        })),
    );

    const tipoDte = watch("tipoDte");
    const cafForTipo = (cafsQuery.data ?? []).find(
        (caf) => String(caf.tipoDte) === String(tipoDte),
    );

    const canSendToSii = invoice
        ? !["SENT", "ACCEPTED", "ACCEPTED_WITH_OBJECTIONS", "REJECTED", "CANCELLED"].includes(
              invoice.status,
          )
        : false;

    const goNext = async () => {
        if (step === 0) {
            const valid = await trigger(["tipoDte", "rutReceptor", "fechaEmision"]);
            if (!valid) return;
        }
        if (step === 1) {
            const valid = await trigger("lines");
            if (!valid) return;
        }
        setStep((current) => Math.min(current + 1, STEPS.length - 1));
    };

    const goBack = () => setStep((current) => Math.max(current - 1, 0));

    const onSubmit = async (values: InvoiceFormValues) => {
        const company = companyQuery.data;
        if (!company?.rut || !company.name) {
            toast.error("No se pudo cargar el emisor", {
                description: "Revisa los datos de la empresa en Configuración antes de emitir.",
            });
            return;
        }

        const model: InvoiceFormModel = {
            tipoDte: values.tipoDte,
            rutEmisor: company.rut,
            razonSocialEmisor: company.name,
            rutReceptor: values.rutReceptor,
            fechaEmision: values.fechaEmision,
            lines: values.lines.map((line) => ({
                description: line.description,
                quantity: Number(line.quantity),
                unitPrice: Number(line.unitPrice),
            })),
        };

        setIsSubmitting(true);
        try {
            if (isEditMode && invoice) {
                const updated = await invoiceService.updateInvoice(
                    invoice.id,
                    toUpdateInvoiceRequest(model, invoice),
                );
                toast.success(`Factura #${updated.folio} actualizada`);
                if (onClose) onClose();
                else router.push(`/invoices/${updated.id}`);
            } else {
                const created = await invoiceService.createInvoice(toCreateInvoiceRequest(model));
                toast.success(`Documento emitido con folio N° ${created.folio}`, {
                    description: `Total ${formatCLP(created.totalAmount)}.`,
                });
                router.push(`/invoices/${created.id}`);
            }
        } catch (error: unknown) {
            const status = (error as { response?: { status?: number } } | null)?.response?.status;
            if (status === 409) {
                toast.error("No hay folios disponibles", {
                    description: (
                        <div className="flex flex-col gap-1">
                            <p>No hay folios disponibles en el CAF para este tipo de documento.</p>
                            <Link href="/sii/caf" className="text-primary underline hover:no-underline">
                                Ir a cargar CAF
                            </Link>
                        </div>
                    ),
                });
            } else {
                toast.error("No se pudo emitir el documento", {
                    description: getErrorMessage(error),
                });
            }
        } finally {
            setIsSubmitting(false);
        }
    };

    const companyLoading = companyQuery.isLoading;
    const companyError = companyQuery.isError;

    return (
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
            {/* Stepper */}
            <ol className="flex items-center gap-2" aria-label="Pasos de emisión">
                {STEPS.map((item, index) => (
                    <li key={item.title} className="flex flex-1 items-center gap-2">
                        <span
                            className={cn(
                                "flex h-8 w-8 shrink-0 items-center justify-center rounded-full border text-sm font-medium",
                                index < step && "border-primary bg-primary text-primary-foreground",
                                index === step && "border-primary text-primary",
                                index > step && "text-muted-foreground",
                            )}
                            aria-current={index === step ? "step" : undefined}
                        >
                            {index < step ? <Check className="h-4 w-4" /> : index + 1}
                        </span>
                        <div className="hidden sm:block">
                            <p
                                className={cn(
                                    "text-sm font-medium leading-tight",
                                    index <= step ? "text-foreground" : "text-muted-foreground",
                                )}
                            >
                                {item.title}
                            </p>
                            <p className="text-xs text-muted-foreground">{item.sub}</p>
                        </div>
                        {index < STEPS.length - 1 && <div className="h-px flex-1 bg-border" />}
                    </li>
                ))}
            </ol>

            {/* Paso 0: Receptor */}
            {step === 0 && (
                <Card>
                    <CardHeader>
                        <CardTitle>Tipo de documento y receptor</CardTitle>
                        <CardDescription>
                            El folio lo asigna el sistema desde el CAF al emitir.
                        </CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-4">
                        <div className="grid gap-4 md:grid-cols-2">
                            <div className="space-y-2">
                                <Label htmlFor="tipoDte">Tipo de documento</Label>
                                <Controller
                                    name="tipoDte"
                                    control={control}
                                    render={({ field }) => (
                                        <Select
                                            value={String(field.value)}
                                            onValueChange={(value) => field.onChange(Number(value))}
                                            disabled={isEditMode || isSubmitting}
                                        >
                                            <SelectTrigger id="tipoDte">
                                                <SelectValue placeholder="Selecciona el tipo" />
                                            </SelectTrigger>
                                            <SelectContent>
                                                <SelectItem value="33">Factura electrónica (33)</SelectItem>
                                                <SelectItem value="39">Boleta electrónica (39)</SelectItem>
                                            </SelectContent>
                                        </Select>
                                    )}
                                />
                                {errors.tipoDte && (
                                    <p className="text-sm text-destructive">{errors.tipoDte.message}</p>
                                )}
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="fechaEmision">Fecha de emisión</Label>
                                <Input
                                    id="fechaEmision"
                                    type="date"
                                    {...register("fechaEmision")}
                                    disabled={isSubmitting}
                                    aria-invalid={Boolean(errors.fechaEmision)}
                                />
                                {errors.fechaEmision && (
                                    <p className="text-sm text-destructive">{errors.fechaEmision.message}</p>
                                )}
                            </div>
                        </div>

                        {/* Folios disponibles */}
                        {cafsQuery.isLoading && <Skeleton className="h-9 w-full" />}
                        {cafsQuery.isSuccess && !cafForTipo && (
                            <Alert variant="destructive">
                                <AlertCircle className="h-4 w-4" />
                                <AlertTitle>No hay folios para este tipo de documento</AlertTitle>
                                <AlertDescription className="flex flex-col gap-2 sm:flex-row sm:items-center">
                                    <span>Solicita un CAF en el sitio del SII y cárgalo para poder emitir.</span>
                                    <Button asChild size="sm" variant="outline">
                                        <Link href="/sii/caf">Ir a Folios (CAF)</Link>
                                    </Button>
                                </AlertDescription>
                            </Alert>
                        )}
                        {cafForTipo && cafForTipo.disponibles < 100 && (
                            <Alert>
                                <AlertCircle className="h-4 w-4" />
                                <AlertTitle>Quedan {cafForTipo.disponibles} folios</AlertTitle>
                                <AlertDescription>
                                    Solicita un nuevo CAF pronto para no interrumpir la emisión.
                                </AlertDescription>
                            </Alert>
                        )}

                        <div className="space-y-2">
                            <Label htmlFor="rutReceptor">RUT del receptor</Label>
                            <Controller
                                name="rutReceptor"
                                control={control}
                                render={({ field }) => (
                                    <RutInput
                                        id="rutReceptor"
                                        name={field.name}
                                        ref={field.ref}
                                        value={field.value}
                                        onValueChange={field.onChange}
                                        onBlur={field.onBlur}
                                        disabled={isSubmitting}
                                        aria-invalid={Boolean(errors.rutReceptor)}
                                        placeholder="12.345.678-9"
                                    />
                                )}
                            />
                            {errors.rutReceptor ? (
                                <p className="text-sm text-destructive">{errors.rutReceptor.message}</p>
                            ) : (
                                <p className="text-xs text-muted-foreground">
                                    Se enviará al SII como {cleanRut(watch("rutReceptor")) || "—"}.
                                </p>
                            )}
                        </div>

                        <div className="rounded-lg border p-4">
                            <p className="text-sm font-medium">Emisor</p>
                            {companyLoading && <Skeleton className="mt-2 h-10 w-full" />}
                            {companyError && (
                                <Alert variant="destructive" className="mt-2">
                                    <AlertCircle className="h-4 w-4" />
                                    <AlertTitle>No se pudo cargar la empresa</AlertTitle>
                                    <AlertDescription className="flex items-center gap-2">
                                        <span>Revisa tu conexión o el perfil de empresa.</span>
                                        <Button
                                            type="button"
                                            size="sm"
                                            variant="outline"
                                            onClick={() => companyQuery.refetch()}
                                        >
                                            Reintentar
                                        </Button>
                                    </AlertDescription>
                                </Alert>
                            )}
                            {companyQuery.data && (
                                <div className="mt-2 text-sm text-muted-foreground">
                                    <p className="font-medium text-foreground">{companyQuery.data.name}</p>
                                    <p className="font-mono text-xs">{companyQuery.data.rut || "Sin RUT configurado"}</p>
                                </div>
                            )}
                        </div>
                    </CardContent>
                </Card>
            )}

            {/* Paso 1: Líneas */}
            {step === 1 && (
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0">
                        <div>
                            <CardTitle>Líneas del documento</CardTitle>
                            <CardDescription>Agrega al menos una línea con cantidad y precio.</CardDescription>
                        </div>
                        <Button
                            type="button"
                            variant="outline"
                            size="sm"
                            onClick={() => append({ description: "", quantity: 1, unitPrice: 0 })}
                            disabled={isSubmitting}
                        >
                            <Plus className="mr-1 h-4 w-4" />
                            Agregar línea
                        </Button>
                    </CardHeader>
                    <CardContent className="space-y-4">
                        {fields.map((field, index) => (
                            <div key={field.id} className="rounded-lg border p-4">
                                <div className="grid grid-cols-1 gap-4 md:grid-cols-12">
                                    <div className="md:col-span-6">
                                        <Label htmlFor={`lines.${index}.description`}>Descripción</Label>
                                        <Input
                                            id={`lines.${index}.description`}
                                            placeholder="Producto o servicio"
                                            {...register(`lines.${index}.description`)}
                                            disabled={isSubmitting}
                                            aria-invalid={Boolean(errors.lines?.[index]?.description)}
                                        />
                                        {errors.lines?.[index]?.description && (
                                            <p className="mt-1 text-sm text-destructive">
                                                {errors.lines[index]?.description?.message}
                                            </p>
                                        )}
                                    </div>
                                    <div className="md:col-span-2">
                                        <Label htmlFor={`lines.${index}.quantity`}>Cantidad</Label>
                                        <Input
                                            id={`lines.${index}.quantity`}
                                            type="number"
                                            min={1}
                                            step={1}
                                            {...register(`lines.${index}.quantity`)}
                                            disabled={isSubmitting}
                                            aria-invalid={Boolean(errors.lines?.[index]?.quantity)}
                                        />
                                        {errors.lines?.[index]?.quantity && (
                                            <p className="mt-1 text-sm text-destructive">
                                                {errors.lines[index]?.quantity?.message}
                                            </p>
                                        )}
                                    </div>
                                    <div className="md:col-span-3">
                                        <Label htmlFor={`lines.${index}.unitPrice`}>Precio unitario</Label>
                                        <Input
                                            id={`lines.${index}.unitPrice`}
                                            type="number"
                                            min={0}
                                            step={1}
                                            {...register(`lines.${index}.unitPrice`)}
                                            disabled={isSubmitting}
                                            aria-invalid={Boolean(errors.lines?.[index]?.unitPrice)}
                                        />
                                        {errors.lines?.[index]?.unitPrice && (
                                            <p className="mt-1 text-sm text-destructive">
                                                {errors.lines[index]?.unitPrice?.message}
                                            </p>
                                        )}
                                    </div>
                                    <div className="flex items-end justify-end md:col-span-1">
                                        <Button
                                            type="button"
                                            variant="ghost"
                                            size="icon"
                                            onClick={() => remove(index)}
                                            disabled={isSubmitting || fields.length === 1}
                                            aria-label={`Quitar línea ${index + 1}`}
                                        >
                                            <Trash2 className="h-4 w-4" />
                                        </Button>
                                    </div>
                                </div>
                            </div>
                        ))}
                        {errors.lines?.message && (
                            <p className="text-sm text-destructive">{errors.lines.message}</p>
                        )}
                    </CardContent>
                </Card>
            )}

            {/* Paso 2: Totales con IVA */}
            {step === 2 && (
                <Card>
                    <CardHeader>
                        <CardTitle>Totales</CardTitle>
                        <CardDescription>El IVA (19%) se calcula sobre el monto neto y se redondea al peso.</CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-6">
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead>Descripción</TableHead>
                                    <TableHead className="text-right">Cant.</TableHead>
                                    <TableHead className="text-right">Precio unit.</TableHead>
                                    <TableHead className="text-right">Subtotal</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {(watchedLines ?? []).map((line, index) => (
                                    <TableRow key={index}>
                                        <TableCell>{line?.description || "—"}</TableCell>
                                        <TableCell className="text-right">{Number(line?.quantity) || 0}</TableCell>
                                        <TableCell className="text-right">
                                            {formatCLP(Number(line?.unitPrice) || 0)}
                                        </TableCell>
                                        <TableCell className="text-right">
                                            {formatCLP(
                                                (Number(line?.quantity) || 0) * (Number(line?.unitPrice) || 0),
                                            )}
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>

                        <div className="ml-auto w-full max-w-xs space-y-2">
                            <div className="flex justify-between text-sm">
                                <span className="text-muted-foreground">Monto neto</span>
                                <span className="font-medium">{formatCLP(totals.neto)}</span>
                            </div>
                            <div className="flex justify-between text-sm">
                                <span className="text-muted-foreground">IVA 19%</span>
                                <span className="font-medium">{formatCLP(totals.iva)}</span>
                            </div>
                            <div className="flex justify-between border-t pt-2 text-base">
                                <span className="font-bold">Total</span>
                                <span className="font-bold">{formatCLP(totals.total)}</span>
                            </div>
                        </div>
                    </CardContent>
                </Card>
            )}

            {/* Paso 3: Revisar y emitir */}
            {step === 3 && (
                <Card>
                    <CardHeader>
                        <CardTitle>Revisar y emitir</CardTitle>
                        <CardDescription>
                            Revisa receptor, montos y fecha. Al emitir se asigna el folio y no podrás editar el
                            documento.
                        </CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-6">
                        <div className="grid gap-4 md:grid-cols-2">
                            <div className="rounded-lg border p-4">
                                <p className="text-sm font-medium">Emisor</p>
                                <p className="mt-1 text-sm">{companyQuery.data?.name ?? "—"}</p>
                                <p className="font-mono text-xs text-muted-foreground">
                                    {companyQuery.data?.rut ? cleanRut(companyQuery.data.rut) : "—"}
                                </p>
                            </div>
                            <div className="rounded-lg border p-4">
                                <p className="text-sm font-medium">Receptor</p>
                                <p className="mt-1 font-mono text-sm">{cleanRut(watch("rutReceptor")) || "—"}</p>
                                <p className="text-xs text-muted-foreground">
                                    {tipoDte === 39 ? "Boleta electrónica (39)" : "Factura electrónica (33)"} ·{" "}
                                    {watch("fechaEmision")}
                                </p>
                            </div>
                        </div>

                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead>Descripción</TableHead>
                                    <TableHead className="text-right">Cant.</TableHead>
                                    <TableHead className="text-right">Precio unit.</TableHead>
                                    <TableHead className="text-right">Subtotal</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {(watchedLines ?? []).map((line, index) => (
                                    <TableRow key={index}>
                                        <TableCell>{line?.description || "—"}</TableCell>
                                        <TableCell className="text-right">{Number(line?.quantity) || 0}</TableCell>
                                        <TableCell className="text-right">
                                            {formatCLP(Number(line?.unitPrice) || 0)}
                                        </TableCell>
                                        <TableCell className="text-right">
                                            {formatCLP(
                                                (Number(line?.quantity) || 0) * (Number(line?.unitPrice) || 0),
                                            )}
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>

                        <div className="ml-auto w-full max-w-xs space-y-2">
                            <div className="flex justify-between text-sm">
                                <span className="text-muted-foreground">Monto neto</span>
                                <span className="font-medium">{formatCLP(totals.neto)}</span>
                            </div>
                            <div className="flex justify-between text-sm">
                                <span className="text-muted-foreground">IVA 19%</span>
                                <span className="font-medium">{formatCLP(totals.iva)}</span>
                            </div>
                            <div className="flex justify-between border-t pt-2 text-base">
                                <span className="font-bold">Total</span>
                                <span className="font-bold">{formatCLP(totals.total)}</span>
                            </div>
                        </div>

                        {invoice && (
                            <Alert>
                                <FileText className="h-4 w-4" />
                                <AlertTitle>Edición del documento</AlertTitle>
                                <AlertDescription>
                                    Se conservará el folio #{invoice.folio} del documento original.
                                    {canSendToSii ? " Podrás enviarlo al SII desde el detalle." : ""}
                                </AlertDescription>
                            </Alert>
                        )}
                    </CardContent>
                </Card>
            )}

            {/* Navegación */}
            <div className="flex items-center justify-between">
                <div>
                    {step > 0 && (
                        <Button type="button" variant="outline" onClick={goBack} disabled={isSubmitting}>
                            <ArrowLeft className="mr-2 h-4 w-4" />
                            Atrás
                        </Button>
                    )}
                </div>
                <div className="flex gap-2">
                    {onClose && (
                        <Button type="button" variant="ghost" onClick={onClose} disabled={isSubmitting}>
                            Cancelar
                        </Button>
                    )}
                    {step < STEPS.length - 1 ? (
                        <Button type="button" onClick={goNext} disabled={isSubmitting}>
                            Siguiente
                            <ArrowRight className="ml-2 h-4 w-4" />
                        </Button>
                    ) : (
                        <Button type="submit" loading={isSubmitting}>
                            <Receipt className="mr-2 h-4 w-4" />
                            {isEditMode ? "Guardar cambios" : "Emitir documento"}
                        </Button>
                    )}
                </div>
            </div>
        </form>
    );
}
