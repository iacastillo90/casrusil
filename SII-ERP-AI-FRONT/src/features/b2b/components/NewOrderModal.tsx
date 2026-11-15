"use client";

import { useMemo, useState } from "react";
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
import { Plus, Trash2, Loader2, ShoppingCart } from "lucide-react";
import { toast } from "sonner";
import { useCreateOrder } from "../hooks/useB2b";
import { formatCLP } from "@/lib/formatters";

interface DraftItem {
    key: number;
    productName: string;
    quantity: string;
    unitPrice: string;
}

let nextKey = 1;

const emptyItem = (): DraftItem => ({
    key: nextKey++,
    productName: "",
    quantity: "1",
    unitPrice: "",
});

export function NewOrderModal() {
    const [open, setOpen] = useState(false);
    const [items, setItems] = useState<DraftItem[]>([emptyItem()]);
    const { mutate: createOrder, isPending } = useCreateOrder();

    const total = useMemo(
        () => items.reduce((acc, item) => {
            const quantity = Number(item.quantity);
            const unitPrice = Number(item.unitPrice);
            if (!Number.isFinite(quantity) || !Number.isFinite(unitPrice)) return acc;
            return acc + quantity * unitPrice;
        }, 0),
        [items]
    );

    const reset = () => setItems([emptyItem()]);

    const updateItem = (key: number, patch: Partial<Omit<DraftItem, "key">>) => {
        setItems(current => current.map(item => (item.key === key ? { ...item, ...patch } : item)));
    };

    const addItem = () => setItems(current => [...current, emptyItem()]);

    const removeItem = (key: number) => {
        setItems(current => (current.length === 1 ? current : current.filter(item => item.key !== key)));
    };

    const handleSubmit = () => {
        const payloadItems = items.map(item => ({
            productName: item.productName.trim(),
            quantity: Number(item.quantity),
            unitPrice: Number(item.unitPrice),
        }));

        const hasInvalidItem = payloadItems.some(
            item => !item.productName || !Number.isInteger(item.quantity) || item.quantity < 1
                || !Number.isFinite(item.unitPrice) || item.unitPrice < 0
        );

        if (hasInvalidItem) {
            toast.error("Revisá los productos: nombre obligatorio, cantidad entera mayor a 0 y precio mayor o igual a 0.");
            return;
        }

        createOrder({ items: payloadItems }, {
            onSuccess: (order) => {
                toast.success(`Pedido creado por ${formatCLP(order.totalAmount)}.`);
                reset();
                setOpen(false);
            },
            onError: () => toast.error("No se pudo crear el pedido. Intentá nuevamente."),
        });
    };

    return (
        <Dialog open={open} onOpenChange={(next) => { setOpen(next); if (!next) reset(); }}>
            <DialogTrigger asChild>
                <Button>
                    <ShoppingCart className="mr-2 h-4 w-4" /> Nuevo Pedido
                </Button>
            </DialogTrigger>
            <DialogContent className="max-w-2xl">
                <DialogHeader>
                    <DialogTitle>Nuevo Pedido</DialogTitle>
                    <DialogDescription>
                        Cargá los productos que querés pedir. El pedido queda pendiente de pago.
                    </DialogDescription>
                </DialogHeader>

                <div className="grid gap-4 py-2">
                    {items.map((item, index) => (
                        <div key={item.key} className="grid grid-cols-12 items-end gap-2">
                            <div className="col-span-6 grid gap-2">
                                <Label htmlFor={`product-${item.key}`}>Producto {index + 1}</Label>
                                <Input
                                    id={`product-${item.key}`}
                                    placeholder="Nombre del producto"
                                    value={item.productName}
                                    onChange={event => updateItem(item.key, { productName: event.target.value })}
                                />
                            </div>
                            <div className="col-span-2 grid gap-2">
                                <Label htmlFor={`quantity-${item.key}`}>Cantidad</Label>
                                <Input
                                    id={`quantity-${item.key}`}
                                    type="number"
                                    min={1}
                                    step={1}
                                    value={item.quantity}
                                    onChange={event => updateItem(item.key, { quantity: event.target.value })}
                                />
                            </div>
                            <div className="col-span-3 grid gap-2">
                                <Label htmlFor={`price-${item.key}`}>Precio unitario</Label>
                                <Input
                                    id={`price-${item.key}`}
                                    type="number"
                                    min={0}
                                    step={1}
                                    placeholder="CLP"
                                    value={item.unitPrice}
                                    onChange={event => updateItem(item.key, { unitPrice: event.target.value })}
                                />
                            </div>
                            <div className="col-span-1 flex justify-end">
                                <Button
                                    type="button"
                                    variant="ghost"
                                    size="icon"
                                    disabled={items.length === 1}
                                    onClick={() => removeItem(item.key)}
                                    aria-label={`Quitar producto ${index + 1}`}
                                >
                                    <Trash2 className="h-4 w-4" />
                                </Button>
                            </div>
                        </div>
                    ))}

                    <Button type="button" variant="outline" className="w-fit" onClick={addItem}>
                        <Plus className="mr-2 h-4 w-4" /> Agregar producto
                    </Button>

                    <div className="flex items-center justify-between border-t pt-4">
                        <span className="text-sm text-muted-foreground">Total</span>
                        <span className="text-lg font-semibold">{formatCLP(total)}</span>
                    </div>
                </div>

                <DialogFooter>
                    <Button variant="outline" onClick={() => setOpen(false)} disabled={isPending}>
                        Cancelar
                    </Button>
                    <Button onClick={handleSubmit} disabled={isPending}>
                        {isPending ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : null}
                        Crear pedido
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
