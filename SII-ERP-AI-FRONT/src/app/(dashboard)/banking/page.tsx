"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { useBankConnections, useBankTransactions } from "@/features/banking/hooks/useBanking";
import { formatCLP } from "@/lib/formatters";
import { Loader2, Plus, RefreshCw } from "lucide-react";

export default function BankingPage() {
    const { data: connections, isLoading: connectionsLoading } = useBankConnections();
    const { data: transactions, isLoading: transactionsLoading } = useBankTransactions();

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h1 className="text-3xl font-bold tracking-tight">Tesorería y Bancos</h1>
                <Button>
                    <Plus className="mr-2 h-4 w-4" />
                    Conectar Banco
                </Button>
            </div>

            <Card>
                <CardHeader>
                    <CardTitle>Conexiones Bancarias</CardTitle>
                </CardHeader>
                <CardContent>
                    {connectionsLoading ? (
                        <Loader2 className="h-6 w-6 animate-spin" />
                    ) : (
                        <div className="space-y-4">
                            {connections?.map(conn => (
                                <div key={conn.id} className="flex justify-between p-4 border rounded-lg">
                                    <span>{conn.bankName} - {conn.accountNumber}</span>
                                    <span>{conn.status}</span>
                                </div>
                            ))}
                        </div>
                    )}
                </CardContent>
            </Card>
            
            <Card>
                <CardHeader>
                    <CardTitle>Transacciones Pendientes de Conciliar</CardTitle>
                </CardHeader>
                <CardContent>
                    {transactionsLoading ? (
                        <Loader2 className="h-6 w-6 animate-spin" />
                    ) : (
                        <div className="space-y-4">
                            {transactions?.filter(t => t.status === 'UNRECONCILED').map(t => (
                                <div key={t.id} className="flex justify-between p-4 border rounded-lg">
                                    <span>{t.description}</span>
                                    <span>{formatCLP(t.amount)}</span>
                                </div>
                            ))}
                        </div>
                    )}
                </CardContent>
            </Card>
        </div>
    );
}
