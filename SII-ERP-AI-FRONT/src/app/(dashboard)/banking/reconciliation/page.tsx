"use client";

import React, { useState } from 'react';
import { 
    useBankConnections, 
    useBankTransactions, 
    useSyncBankTransactions,
    useConnectBank,
    useReconcileTransaction,
    useIgnoreTransaction
} from '@/features/banking/hooks/useBanking';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Building2, RefreshCw, CheckCircle2, XCircle, Clock } from 'lucide-react';
import { BankConnection, BankTransaction } from '@/features/banking/services/banking.service';

export default function BankingReconciliationPage() {
    const { data: connections, isLoading: isLoadingConns } = useBankConnections();
    const { data: transactions, isLoading: isLoadingTx } = useBankTransactions();
    const syncTransactions = useSyncBankTransactions();
    const connectBank = useConnectBank();
    const reconcileTransaction = useReconcileTransaction();
    const ignoreTransaction = useIgnoreTransaction();

    const [bankName, setBankName] = useState('');
    const [accountNumber, setAccountNumber] = useState('');

    const handleConnect = (e: React.FormEvent) => {
        e.preventDefault();
        connectBank.mutate({ bankName, accountNumber }, {
            onSuccess: () => {
                setBankName('');
                setAccountNumber('');
            }
        });
    };

    if (isLoadingConns || isLoadingTx) {
        return <div className="p-8 text-center text-gray-500">Cargando módulo de conciliación bancaria...</div>;
    }

    const unreconciledTx = transactions?.filter(t => t.status === 'UNRECONCILED') || [];

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <div>
                    <h2 className="text-2xl font-bold tracking-tight">Conciliación Bancaria</h2>
                    <p className="text-muted-foreground">Sincroniza tus cuentas y concilia movimientos automáticamente (Open Banking).</p>
                </div>
                <Button 
                    onClick={() => syncTransactions.mutate()} 
                    disabled={syncTransactions.isPending}
                >
                    <RefreshCw className={`mr-2 h-4 w-4 ${syncTransactions.isPending ? 'animate-spin' : ''}`} />
                    Sincronizar Bancos
                </Button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                
                {/* Bank Connections Widget */}
                <Card className="md:col-span-1">
                    <CardHeader>
                        <CardTitle className="text-lg flex items-center">
                            <Building2 className="mr-2 h-5 w-5 text-indigo-600" />
                            Bancos Conectados
                        </CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-4">
                        {connections?.map((conn: BankConnection) => (
                            <div key={conn.id} className="p-3 border rounded-lg flex justify-between items-center">
                                <div>
                                    <p className="font-semibold">{conn.bankName}</p>
                                    <p className="text-sm text-gray-500">CTA: {conn.accountNumber}</p>
                                    <p className="text-xs text-gray-400 mt-1 flex items-center">
                                        <Clock className="h-3 w-3 mr-1"/> Sync: {conn.lastSync}
                                    </p>
                                </div>
                                <span className="flex h-3 w-3 rounded-full bg-green-500"></span>
                            </div>
                        ))}

                        <form onSubmit={handleConnect} className="mt-4 pt-4 border-t space-y-3">
                            <p className="text-sm font-medium">Agregar Conexión (Fintoc)</p>
                            <Input 
                                placeholder="Nombre del Banco" 
                                value={bankName} 
                                onChange={e => setBankName(e.target.value)} 
                                required 
                            />
                            <Input 
                                placeholder="Número de Cuenta" 
                                value={accountNumber} 
                                onChange={e => setAccountNumber(e.target.value)} 
                                required 
                            />
                            <Button type="submit" className="w-full" disabled={connectBank.isPending}>
                                Conectar Banco
                            </Button>
                        </form>
                    </CardContent>
                </Card>

                {/* Reconciliation Panel */}
                <Card className="md:col-span-2">
                    <CardHeader>
                        <CardTitle className="text-lg">Movimientos por Conciliar ({unreconciledTx.length})</CardTitle>
                    </CardHeader>
                    <CardContent>
                        {unreconciledTx.length === 0 ? (
                            <div className="text-center py-8 text-muted-foreground">
                                No hay movimientos pendientes de conciliación.
                            </div>
                        ) : (
                            <div className="space-y-3">
                                {unreconciledTx.map((tx: BankTransaction) => (
                                    <div key={tx.id} className="p-4 border rounded-lg flex justify-between items-center hover:bg-gray-50">
                                        <div>
                                            <p className="font-medium text-gray-900">{tx.description}</p>
                                            <div className="flex gap-4 text-sm text-gray-500 mt-1">
                                                <span>{new Date(tx.transactionDate).toLocaleDateString()}</span>
                                                <span>Ref: {tx.transactionId.substring(0,8)}</span>
                                            </div>
                                        </div>
                                        <div className="flex items-center gap-4">
                                            <span className={`font-semibold text-lg ${tx.amount > 0 ? 'text-green-600' : 'text-red-600'}`}>
                                                ${Math.abs(tx.amount).toLocaleString('es-CL')}
                                            </span>
                                            <div className="flex gap-2">
                                                <Button 
                                                    variant="outline" 
                                                    size="icon" 
                                                    className="text-green-600 hover:text-green-700 hover:bg-green-50"
                                                    onClick={() => reconcileTransaction.mutate(tx.id)}
                                                    disabled={reconcileTransaction.isPending}
                                                >
                                                    <CheckCircle2 className="h-5 w-5" />
                                                </Button>
                                                <Button 
                                                    variant="outline" 
                                                    size="icon" 
                                                    className="text-red-600 hover:text-red-700 hover:bg-red-50"
                                                    onClick={() => ignoreTransaction.mutate(tx.id)}
                                                    disabled={ignoreTransaction.isPending}
                                                >
                                                    <XCircle className="h-5 w-5" />
                                                </Button>
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </CardContent>
                </Card>

            </div>
        </div>
    );
}
