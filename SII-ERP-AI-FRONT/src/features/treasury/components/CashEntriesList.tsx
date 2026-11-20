"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { RefreshCw, Wallet } from "lucide-react";
import { formatCLP, formatDate } from "@/lib/formatters";
import { useCashEntries } from "../hooks/useTreasury";

export function CashEntriesList() {
    const { data: entries = [], isLoading, isError, error, refetch, isFetching } = useCashEntries();

    return (
        <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0">
                <CardTitle className="flex items-center gap-2 text-base font-semibold">
                    <Wallet className="h-5 w-5 text-indigo-600" />
                    Movimientos de caja
                </CardTitle>
                <Button variant="outline" size="sm" onClick={() => refetch()} disabled={isFetching}>
                    <RefreshCw className={`mr-2 h-4 w-4 ${isFetching ? "animate-spin" : ""}`} />
                    Actualizar
                </Button>
            </CardHeader>
            <CardContent>
                {isLoading ? (
                    <div className="space-y-2">
                        {Array.from({ length: 3 }).map((_, index) => (
                            <Skeleton key={index} className="h-12 w-full" />
                        ))}
                    </div>
                ) : isError ? (
                    <div className="py-8 text-center text-sm text-muted-foreground">
                        <p>No se pudieron cargar los movimientos de caja.</p>
                        <p className="mt-1 text-xs">
                            {error instanceof Error ? error.message : "Error desconocido"}
                        </p>
                        <Button variant="outline" size="sm" className="mt-3" onClick={() => refetch()}>
                            Reintentar
                        </Button>
                    </div>
                ) : entries.length === 0 ? (
                    <div className="py-8 text-center text-sm text-muted-foreground">
                        No hay movimientos de caja registrados.
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full text-sm">
                            <thead>
                                <tr className="border-b text-xs uppercase text-muted-foreground">
                                    <th className="px-3 py-2 text-left font-medium">Fecha</th>
                                    <th className="px-3 py-2 text-left font-medium">Descripción</th>
                                    <th className="px-3 py-2 text-left font-medium">Categoría</th>
                                    <th className="px-3 py-2 text-right font-medium">Monto</th>
                                </tr>
                            </thead>
                            <tbody>
                                {entries.map((entry) => (
                                    <tr key={entry.id} className="border-b last:border-0">
                                        <td className="px-3 py-2 text-muted-foreground">{formatDate(entry.date)}</td>
                                        <td className="px-3 py-2 font-medium">{entry.description}</td>
                                        <td className="px-3 py-2 text-muted-foreground">{entry.category || "—"}</td>
                                        <td
                                            className={`px-3 py-2 text-right font-semibold ${
                                                entry.amount >= 0 ? "text-green-600" : "text-red-600"
                                            }`}
                                        >
                                            {formatCLP(entry.amount)}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </CardContent>
        </Card>
    );
}
