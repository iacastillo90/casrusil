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
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Plus, Loader2 } from "lucide-react";
import { toast } from "sonner";
import { useCreateCashEntry } from "../hooks/useTreasury";

type MovementType = "INCOME" | "EXPENSE";

const getErrorMessage = (error: unknown): string =>
    error instanceof Error ? error.message : "Inténtalo nuevamente";

export function CashEntryModal() {
    const [open, setOpen] = useState(false);
    const [type, setType] = useState<MovementType>("INCOME");
    const [amount, setAmount] = useState("");
    const [description, setDescription] = useState("");
    const [date, setDate] = useState(() => new Date().toISOString().slice(0, 10));
    const createCashEntry = useCreateCashEntry();

    const reset = () => {
        setType("INCOME");
        setAmount("");
        setDescription("");
        setDate(new Date().toISOString().slice(0, 10));
    };

    const handleSave = async () => {
        const parsedAmount = Number(amount);
        if (!Number.isFinite(parsedAmount) || parsedAmount <= 0 || !description.trim()) {
            toast.error("Completa un monto mayor a 0 y una descripción.");
            return;
        }

        // El backend guarda montos con signo: positivo = ingreso, negativo = egreso.
        const signedAmount = type === "INCOME" ? parsedAmount : -parsedAmount;

        try {
            await createCashEntry.mutateAsync({
                date,
                description: description.trim(),
                amount: signedAmount,
                reference: null,
                // Categoría genérica: el backend la mapea a "Otros ingresos/gastos".
                category: "OTHER",
                currency: "CLP",
                exchangeRate: 1,
            });
            toast.success(`Movimiento de ${type === "INCOME" ? "ingreso" : "egreso"} registrado.`);
            reset();
            setOpen(false);
        } catch (error) {
            toast.error("No se pudo registrar el movimiento de caja.", {
                description: getErrorMessage(error),
            });
        }
    };

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                <Button className="gap-2">
                    <Plus className="h-4 w-4" />
                    Registrar Movimiento
                </Button>
            </DialogTrigger>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Nuevo Movimiento de Caja</DialogTitle>
                    <DialogDescription>Registra un ingreso o egreso manual de tesorería.</DialogDescription>
                </DialogHeader>
                <div className="grid gap-4 py-2">
                    <div className="grid gap-2">
                        <Label>Tipo</Label>
                        <Select value={type} onValueChange={(v) => setType(v as MovementType)}>
                            <SelectTrigger>
                                <SelectValue />
                            </SelectTrigger>
                            <SelectContent>
                                <SelectItem value="INCOME">Ingreso</SelectItem>
                                <SelectItem value="EXPENSE">Egreso</SelectItem>
                            </SelectContent>
                        </Select>
                    </div>
                    <div className="grid gap-2">
                        <Label htmlFor="cash-amount">Monto (CLP)</Label>
                        <Input
                            id="cash-amount"
                            type="number"
                            min={1}
                            value={amount}
                            onChange={(e) => setAmount(e.target.value)}
                            placeholder="0"
                        />
                    </div>
                    <div className="grid gap-2">
                        <Label htmlFor="cash-desc">Descripción</Label>
                        <Input
                            id="cash-desc"
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            placeholder="Ej: Pago a proveedor"
                        />
                    </div>
                    <div className="grid gap-2">
                        <Label htmlFor="cash-date">Fecha</Label>
                        <Input id="cash-date" type="date" value={date} onChange={(e) => setDate(e.target.value)} />
                    </div>
                </div>
                <DialogFooter>
                    <Button variant="outline" onClick={() => setOpen(false)} disabled={createCashEntry.isPending}>
                        Cancelar
                    </Button>
                    <Button onClick={handleSave} disabled={createCashEntry.isPending}>
                        {createCashEntry.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                        Guardar
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
