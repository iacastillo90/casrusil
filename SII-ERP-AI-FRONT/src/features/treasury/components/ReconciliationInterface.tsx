"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { CheckCircle2, XCircle, ArrowLeftRight, Inbox, RefreshCw } from "lucide-react";
import { formatCLP, formatDate } from "@/lib/formatters";
import { toast } from "sonner";
import {
    useBankTransactions,
    useIgnoreTransaction,
    useReconcileTransaction,
} from "@/features/banking/hooks/useBanking";

const getErrorMessage = (error: unknown): string =>
    error instanceof Error ? error.message : "Inténtalo nuevamente";

export function ReconciliationInterface() {
    const {
        data: transactions = [],
        isLoading,
        isError,
        error,
        refetch,
        isFetching,
    } = useBankTransactions();
    const reconcileTransaction = useReconcileTransaction();
    const ignoreTransaction = useIgnoreTransaction();

    const pending = transactions.filter((transaction) => transaction.status === "UNRECONCILED");
    const isMutating = reconcileTransaction.isPending || ignoreTransaction.isPending;

    const handleReconcile = (id: string) => {
        reconcileTransaction.mutate(id, {
            onSuccess: () => toast.success("Movimiento conciliado."),
            onError: (mutationError) =>
                toast.error("No se pudo conciliar el movimiento.", {
                    description: getErrorMessage(mutationError),
                }),
        });
    };

    const handleIgnore = (id: string) => {
        ignoreTransaction.mutate(id, {
            onSuccess: () => toast.success("Movimiento ignorado."),
            onError: (mutationError) =>
                toast.error("No se pudo ignorar el movimiento.", {
                    description: getErrorMessage(mutationError),
                }),
        });
    };

    return (
        <Card>
            <CardHeader>
                <CardTitle className="flex items-center justify-between text-lg">
                    <span className="flex items-center gap-2">
                        <ArrowLeftRight className="h-5 w-5 text-indigo-600" />
                        Conciliación de Movimientos
                    </span>
                    <div className="flex items-center gap-2">
                        <Badge variant="secondary">{pending.length} por conciliar</Badge>
                        <Button
                            variant="outline"
                            size="icon"
                            aria-label="Actualizar movimientos"
                            onClick={() => refetch()}
                            disabled={isFetching}
                        >
                            <RefreshCw className={`h-4 w-4 ${isFetching ? "animate-spin" : ""}`} />
                        </Button>
                    </div>
                </CardTitle>
            </CardHeader>
            <CardContent>
                {isLoading ? (
                    <div className="space-y-2">
                        {Array.from({ length: 3 }).map((_, index) => (
                            <Skeleton key={index} className="h-16 w-full" />
                        ))}
                    </div>
                ) : isError ? (
                    <div className="flex flex-col items-center justify-center py-12 text-center text-muted-foreground">
                        <p className="font-medium">No se pudieron cargar los movimientos bancarios.</p>
                        <p className="text-sm">
                            {error instanceof Error ? error.message : "Error desconocido"}
                        </p>
                        <Button variant="outline" size="sm" className="mt-3" onClick={() => refetch()}>
                            Reintentar
                        </Button>
                    </div>
                ) : pending.length === 0 ? (
                    <div className="flex flex-col items-center justify-center py-12 text-center text-muted-foreground">
                        <Inbox className="mb-2 h-10 w-10 opacity-50" />
                        <p className="font-medium">No hay movimientos por conciliar</p>
                        <p className="text-sm">Sincroniza tus bancos para traer movimientos nuevos.</p>
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
                                    <p className="text-sm text-muted-foreground">
                                        {formatDate(movement.transactionDate)} · {movement.bankName}
                                    </p>
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
                                            aria-label={`Conciliar ${movement.description}`}
                                            onClick={() => handleReconcile(movement.id)}
                                            disabled={isMutating}
                                        >
                                            <CheckCircle2 className="h-5 w-5" />
                                        </Button>
                                        <Button
                                            variant="outline"
                                            size="icon"
                                            className="text-red-600"
                                            aria-label={`Ignorar ${movement.description}`}
                                            onClick={() => handleIgnore(movement.id)}
                                            disabled={isMutating}
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
