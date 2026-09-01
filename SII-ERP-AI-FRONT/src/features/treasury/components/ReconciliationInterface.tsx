"use client";

import { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { CheckCircle2, XCircle, ArrowLeftRight, Inbox } from "lucide-react";
import { formatCLP, formatDate } from "@/lib/formatters";
import { toast } from "sonner";

interface BankMovement {
    id: string;
    date: string;
    description: string;
    amount: number;
    status: "PENDING" | "RECONCILED" | "IGNORED";
}

export function ReconciliationInterface() {
    // NOTA: integración real con el backend pendiente. La interfaz opera sobre
    // el estado local; al conectar el endpoint se poblará con movimientos reales.
    const [movements, setMovements] = useState<BankMovement[]>([]);

    const pending = movements.filter((m) => m.status === "PENDING");

    const updateStatus = (id: string, status: BankMovement["status"]) => {
        setMovements((prev) => prev.map((m) => (m.id === id ? { ...m, status } : m)));
        toast.success(status === "RECONCILED" ? "Movimiento conciliado" : "Movimiento ignorado");
    };

    return (
        <Card>
            <CardHeader>
                <CardTitle className="flex items-center justify-between text-lg">
                    <span className="flex items-center gap-2">
                        <ArrowLeftRight className="h-5 w-5 text-indigo-600" />
                        Conciliación de Movimientos
                    </span>
                    <Badge variant="secondary">{pending.length} por conciliar</Badge>
                </CardTitle>
            </CardHeader>
            <CardContent>
                {pending.length === 0 ? (
                    <div className="flex flex-col items-center justify-center py-12 text-center text-muted-foreground">
                        <Inbox className="mb-2 h-10 w-10 opacity-50" />
                        <p className="font-medium">No hay movimientos por conciliar</p>
                        <p className="text-sm">Carga una cartola bancaria para comenzar.</p>
                    </div>
                ) : (
                    <ul className="space-y-3">
                        {pending.map((movement) => (
                            <li
                                key={movement.id}
                                className="flex items-center justify-between rounded-lg border p-4 hover:bg-muted/40"
                            >
                                <div>
                                    <p className="font-medium">{movement.description}</p>
                                    <p className="text-sm text-muted-foreground">{formatDate(movement.date)}</p>
                                </div>
                                <div className="flex items-center gap-4">
                                    <span
                                        className={`font-semibold ${movement.amount >= 0 ? "text-green-600" : "text-red-600"}`}
                                    >
                                        {formatCLP(movement.amount)}
                                    </span>
                                    <div className="flex gap-2">
                                        <Button
                                            variant="outline"
                                            size="icon"
                                            className="text-green-600"
                                            onClick={() => updateStatus(movement.id, "RECONCILED")}
                                        >
                                            <CheckCircle2 className="h-5 w-5" />
                                        </Button>
                                        <Button
                                            variant="outline"
                                            size="icon"
                                            className="text-red-600"
                                            onClick={() => updateStatus(movement.id, "IGNORED")}
                                        >
                                            <XCircle className="h-5 w-5" />
                                        </Button>
                                    </div>
                                </div>
                            </li>
                        ))}
                    </ul>
                )}
            </CardContent>
        </Card>
    );
}
