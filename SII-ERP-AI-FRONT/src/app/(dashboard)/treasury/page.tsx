"use client";

import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { BankStatementUpload } from "@/features/treasury/components/BankStatementUpload";
import { ReconciliationInterface } from "@/features/treasury/components/ReconciliationInterface";
import { CashFlowChart } from "@/features/treasury/components/CashFlowChart";
import { LiquidityKPIs } from "@/features/treasury/components/LiquidityKPIs";
import { CashEntryModal } from "@/features/treasury/components/CashEntryModal";
import { AiPredictiveChart } from "@/features/treasury/components/AiPredictiveChart";
import { MultiCurrencyPanel } from "@/features/treasury/components/MultiCurrencyPanel";

export default function TreasuryPage() {
    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between">
                <h1 className="text-3xl font-bold tracking-tight">Tesorería y Bancos</h1>
                <CashEntryModal />
            </div>

            <Tabs defaultValue="ai-forecast" className="space-y-4">
                <TabsList>
                    <TabsTrigger value="ai-forecast">Proyección IA (Avanzado)</TabsTrigger>
                    <TabsTrigger value="reconciliation">Conciliación Bancaria</TabsTrigger>
                    <TabsTrigger value="cashflow">Flujo de Caja Clásico</TabsTrigger>
                </TabsList>

                <TabsContent value="ai-forecast" className="space-y-4">
                    <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
                        <div className="xl:col-span-2">
                            <AiPredictiveChart />
                        </div>
                        <div className="space-y-6">
                            <MultiCurrencyPanel />
                            {/* We can add another advanced panel here later */}
                        </div>
                    </div>
                </TabsContent>

                <TabsContent value="reconciliation" className="space-y-4">
                    <BankStatementUpload />
                    <ReconciliationInterface />
                </TabsContent>

                <TabsContent value="cashflow" className="space-y-4">
                    <LiquidityKPIs />
                    <CashFlowChart />
                </TabsContent>
            </Tabs>
        </div>
    );
}
