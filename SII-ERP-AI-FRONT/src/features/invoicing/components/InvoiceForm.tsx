"use client";

import { useState, useEffect } from "react";
import { useForm, useFieldArray, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "sonner";
import { Loader2, Plus, Trash2, FileText } from "lucide-react";
import Link from "next/link";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { invoiceService } from "@/features/invoicing/services/invoice.service";
import { Invoice, InvoiceLine } from "@/features/invoicing/types/invoice.types";
import { useRouter } from "next/navigation";

const invoiceFormSchema = z.object({
    type: z.enum(["FACTURA", "BOLETA"]),
    clientRut: z.string().min(1, "El RUT del cliente es obligatorio"),
    lines: z.array(z.object({
        id: z.string().optional(),
        description: z.string().min(1, "La descripción es obligatoria"),
        quantity: z.number().int().min(1, "La cantidad debe ser al menos 1"),
        unitPrice: z.number().min(0, "El precio debe ser mayor o igual a 0"),
    })).min(1, "Debe agregar al menos una línea"),
});

type InvoiceFormData = z.infer<typeof invoiceFormSchema>;

interface InvoiceFormProps {
    invoice?: Invoice;
    onClose?: () => void;
}

export function InvoiceForm({ invoice, onClose }: InvoiceFormProps) {
    const [isSubmitting, setIsSubmitting] = useState(false);
    const router = useRouter();
    const isEditMode = !!invoice;

    const {
        control,
        handleSubmit,
        reset,
        formState: { errors },
        watch,
    } = useForm<InvoiceFormData>({
        resolver: zodResolver(invoiceFormSchema),
        defaultValues: {
            type: "FACTURA",
            clientRut: invoice?.recipientRut ?? "",
            lines: invoice?.lines?.length ? invoice.lines.map(l => ({
                id: l.id,
                description: l.description,
                quantity: l.quantity,
                unitPrice: l.unitPrice,
            })) : [{ description: "", quantity: 1, unitPrice: 0 }],
        },
    });

    const { fields, append, remove } = useFieldArray({
        control,
        name: "lines",
    });

    // Reset when modal closes (only for new invoices)
    useEffect(() => {
        if (!isEditMode) {
            reset({
                type: "FACTURA",
                clientRut: "",
                lines: [{ description: "", quantity: 1, unitPrice: 0 }],
            });
        }
    }, [isEditMode, reset]);

    const onSubmit = async (data: InvoiceFormData) => {
        setIsSubmitting(true);
        try {
            const lines: InvoiceLine[] = data.lines.map((line, index) => ({
                id: line.id ?? `line-${Date.now()}-${index}`,
                description: line.description,
                quantity: line.quantity,
                unitPrice: line.unitPrice,
                lineTotal: line.quantity * line.unitPrice,
            }));

            const netAmount = lines.reduce((sum, l) => sum + l.lineTotal, 0);
            const taxAmount = netAmount * 0.19; // IVA 19%
            const totalAmount = netAmount + taxAmount;

            const invoiceData = {
                type: data.type,
                clientRut: data.clientRut,
                lines,
                netAmount,
                taxAmount,
                totalAmount,
                issueDate: new Date().toISOString(),
                folio: 0, // Se asigna en el backend
                status: "DRAFT",
                issuerRut: "", // Se asigna en el backend
                recipientName: "",
            };

            if (isEditMode && invoice) {
                await invoiceService.updateInvoice(invoice.id, invoiceData);
                toast.success("Factura actualizada");
            } else {
                await invoiceService.createInvoice(invoiceData);
                toast.success("Factura creada");
            }

            if (onClose) {
                onClose();
            } else {
                router.push("/invoices");
            }
        } catch (error: unknown) {
            const err = error as { response?: { status?: number } } | null;
            if (err?.response?.status === 409) {
                toast.error("No hay folios disponibles", {
                    description: (
                        <div className="flex flex-col gap-1">
                            <p>No hay folios disponibles en el CAF.</p>
                            <Link href="/sii/caf" className="text-primary underline hover:no-underline">
                                Ir a cargar CAF <FileText className="inline h-4 w-4 ml-1" />
                            </Link>
                        </div>
                    ),
                });
            } else {
                toast.error("Error al guardar la factura", {
                    description: error instanceof Error ? error.message : "Inténtalo nuevamente",
                });
            }
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleAddLine = () => {
        append({ description: "", quantity: 1, unitPrice: 0 });
    };

    const handleRemoveLine = (index: number) => {
        if (fields.length > 1) {
            remove(index);
        } else {
            // Reset single line instead of removing
            reset(prev => ({
                ...prev,
                lines: [{ description: "", quantity: 1, unitPrice: 0 }],
            }));
        }
    };

    const watchedLines = watch("lines");
    const netAmount = watchedLines.reduce((sum: number, line: InvoiceFormData['lines'][0]) => sum + (Number(line.quantity) * Number(line.unitPrice)), 0);
    const taxAmount = netAmount * 0.19;
    const totalAmount = netAmount + taxAmount;

    if (isEditMode && !invoice) {
        return null;
    }

    return (
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
            <Card>
                <CardHeader>
                    <CardTitle>{isEditMode ? "Editar Factura" : "Crear Factura"}</CardTitle>
                    <CardDescription>
                        {isEditMode ? "Modifica los datos de la factura existente" : "Completa los datos para crear una nueva factura"}
                    </CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                    {/* Tipo DTE */}
                    <div className="space-y-2">
                        <Label htmlFor="type">Tipo de Documento</Label>
                        <Controller
                            name="type"
                            control={control}
                            render={({ field }) => (
                                <Select
                                    value={field.value}
                                    onValueChange={field.onChange}
                                    disabled={isEditMode || isSubmitting}
                                >
                                    <SelectTrigger>
                                        <SelectValue placeholder="Selecciona el tipo..." />
                                    </SelectTrigger>
                                    <SelectContent>
                                        <SelectItem value="FACTURA">Factura Electrónica (33)</SelectItem>
                                        <SelectItem value="BOLETA">Boleta Electrónica (39)</SelectItem>
                                    </SelectContent>
                                </Select>
                            )}
                        />
                        {errors.type && (
                            <p className="text-sm text-destructive">{errors.type.message}</p>
                        )}
                    </div>

                    {/* RUT Cliente */}
                    <div className="space-y-2">
                        <Label htmlFor="clientRut">RUT del Cliente</Label>
                        <Input
                            id="clientRut"
                            placeholder="Ej: 12345678-9"
                            {...control.register("clientRut")}
                            defaultValue=""
                            disabled={isEditMode || isSubmitting}
                        />
                        {errors.clientRut && (
                            <p className="text-sm text-destructive">{errors.clientRut.message}</p>
                        )}
                    </div>

                    {/* Líneas de Producto */}
                    <div className="space-y-4">
                        <div className="flex justify-between items-center">
                            <h3 className="text-lg font-medium">Líneas</h3>
                            <Button type="button" variant="outline" size="sm" onClick={handleAddLine} disabled={isSubmitting}>
                                <Plus className="h-4 w-4 mr-1" />
                                Agregar línea
                            </Button>
                        </div>

                        <div className="space-y-4">
                            {fields.map((field, index) => {
                                const watchedLine = watch(`lines.${index}`);
                                return (
                                    <Card key={field.id} className="p-4">
                                        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                                            {/* Descripción */}
                                            <div className="md:col-span-2">
                                                <Label htmlFor={`lines.${index}.description`}>
                                                    Descripción
                                                </Label>
                                                <Input
                                                    id={`lines.${index}.description`}
                                                    {...control.register(`lines.${index}.description` as const)}
                                                    defaultValue={watchedLine?.description ?? ""}
                                                    disabled={isEditMode || isSubmitting}
                                                />
                                                {errors.lines?.[index]?.description && (
                                                    <p className="text-sm text-destructive mt-1">
                                                        {errors.lines[index]!.description?.message}
                                                    </p>
                                                )}
                                            </div>

                                            {/* Cantidad */}
                                            <div>
                                                <Label htmlFor={`lines.${index}.quantity`}>
                                                    Cantidad
                                                </Label>
                                                <Input
                                                    id={`lines.${index}.quantity`}
                                                    type="number"
                                                    min={1}
                                                    step={1}
                                                    {...control.register(`lines.${index}.quantity` as const)}
                                                    defaultValue={watchedLine?.quantity ?? 1}
                                                    disabled={isEditMode || isSubmitting}
                                                />
                                                {errors.lines?.[index]?.quantity && (
                                                    <p className="text-sm text-destructive mt-1">
                                                        {errors.lines[index]!.quantity?.message}
                                                    </p>
                                                )}
                                            </div>

                                            {/* Precio Unitario */}
                                            <div>
                                                <Label htmlFor={`lines.${index}.unitPrice`}>
                                                    Precio Unitario
                                                </Label>
                                                <Input
                                                    id={`lines.${index}.unitPrice`}
                                                    type="number"
                                                    min={0}
                                                    step={0.01}
                                                    {...control.register(`lines.${index}.unitPrice` as const)}
                                                    defaultValue={watchedLine?.unitPrice ?? 0}
                                                    disabled={isEditMode || isSubmitting}
                                                />
                                                {errors.lines?.[index]?.unitPrice && (
                                                    <p className="text-sm text-destructive mt-1">
                                                        {errors.lines[index]!.unitPrice?.message}
                                                    </p>
                                                )}
                                            </div>

                                            {/* Acción eliminar */}
                                            <div className="items-end">
                                                <Button
                                                    type="button"
                                                    variant="ghost"
                                                    size="icon"
                                                    onClick={() => handleRemoveLine(index)}
                                                    disabled={isSubmitting}
                                                >
                                                    <Trash2 className="h-4 w-4" />
                                                </Button>
                                            </div>
                                        </div>
                                    </Card>
                                );
                            })}
                        </div>

                        {errors.lines && (
                            <p className="text-sm text-destructive">{errors.lines.message}</p>
                        )}
                    </div>

                    {/* Resumen financiero */}
                    <div className="bg-muted p-4 rounded-lg space-y-2">
                        <h4 className="font-medium">Resumen</h4>
                        <div className="flex justify-between">
                            <span>Subtotal:</span>
                            <span className="font-mono">${netAmount.toLocaleString('es-CL')}</span>
                        </div>
                        <div className="flex justify-between">
                            <span>IVA (19%):</span>
                            <span className="font-mono">${taxAmount.toLocaleString('es-CL')}</span>
                        </div>
                        <div className="flex justify-between border-t pt-2 font-bold">
                            <span>Total:</span>
                            <span className="font-mono text-lg">${totalAmount.toLocaleString('es-CL')}</span>
                        </div>
                    </div>
                </CardContent>
            </Card>

            <div className="flex justify-end gap-2">
                {onClose && (
                    <Button type="button" variant="outline" onClick={onClose} disabled={isSubmitting}>
                        Cancelar
                    </Button>
                )}
                <Button type="submit" disabled={isSubmitting || (isEditMode && !invoice)}>
                    {isSubmitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                    {isEditMode ? "Actualizar" : "Crear"}
                </Button>
            </div>
        </form>
    );
}