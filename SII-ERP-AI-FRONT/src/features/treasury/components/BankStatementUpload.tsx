"use client";

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { RefreshCw, Info } from "lucide-react";
import { toast } from "sonner";
import { useSyncBankTransactions } from "@/features/banking/hooks/useBanking";

const getErrorMessage = (error: unknown): string =>
    error instanceof Error ? error.message : "Inténtalo nuevamente";

export function BankStatementUpload() {
    const syncTransactions = useSyncBankTransactions();

    const handleSync = () => {
        syncTransactions.mutate(undefined, {
            onSuccess: () => toast.success("Movimientos bancarios sincronizados."),
            onError: (error) =>
                toast.error("No se pudieron sincronizar los movimientos bancarios.", {
                    description: getErrorMessage(error),
                }),
        });
    };

    return (
        <Card>
            <CardHeader>
                <CardTitle className="flex items-center gap-2">
                    <RefreshCw className="h-5 w-5 text-indigo-600" />
                    Sincronización Bancaria
                </CardTitle>
                <CardDescription>
                    Sincroniza los movimientos de tus bancos conectados para conciliarlos.
                </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
                <div className="flex items-start gap-2 rounded-lg border border-amber-200 bg-amber-50 p-3 text-sm text-amber-900">
                    <Info className="mt-0.5 h-4 w-4 shrink-0" />
                    <p>
                        La importación de cartolas CSV/Excel está pendiente de definición de formato.
                        Por ahora puedes sincronizar los movimientos de los bancos que tengas conectados.
                    </p>
                </div>

                <Button onClick={handleSync} disabled={syncTransactions.isPending}>
                    <RefreshCw className={`mr-2 h-4 w-4 ${syncTransactions.isPending ? "animate-spin" : ""}`} />
                    {syncTransactions.isPending ? "Sincronizando..." : "Sincronizar movimientos"}
                </Button>
            </CardContent>
        </Card>
    );
}
