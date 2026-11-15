"use client";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { useFixedAssets, useRunDepreciation } from "@/features/fixed_assets/hooks/useFixedAssets";
import { Calculator, Monitor, Truck, Cog } from "lucide-react";
import { formatCLP } from "@/lib/formatters";
import { toast } from "sonner";

export default function FixedAssetsPage() {
    const { data: assets, isLoading } = useFixedAssets();
    const runDepreciation = useRunDepreciation();

    const handleRunDepreciation = () => {
        runDepreciation.mutate(undefined, {
            onSuccess: () => toast.success("Depreciación mensual calculada y guardada.")
        });
    };

    const getIcon = (category: string) => {
        switch(category) {
            case 'VEHICLE': return <Truck className="h-5 w-5 text-blue-500" />;
            case 'IT': return <Monitor className="h-5 w-5 text-purple-500" />;
            default: return <Cog className="h-5 w-5 text-gray-500" />;
        }
    }

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Activos Fijos y Depreciación</h1>
                    <p className="text-muted-foreground mt-1">Control de ciclo de vida y valor residual de activos.</p>
                </div>
                <Button onClick={handleRunDepreciation} disabled={runDepreciation.isPending}>
                    <Calculator className="mr-2 h-4 w-4" /> Ejecutar Depreciación Mensual
                </Button>
            </div>

            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                {isLoading ? <p>Cargando activos...</p> : assets?.length === 0 ? <p>No hay activos registrados.</p> : assets?.map((asset: any) => (
                    <Card key={asset.id} className="overflow-hidden">
                        <div className="bg-slate-50 border-b p-3 flex justify-between items-center">
                            <div className="flex items-center gap-2">
                                {getIcon(asset.category)}
                                <span className="font-semibold text-sm">{asset.category}</span>
                            </div>
                            <Badge variant="outline">{asset.purchaseDate}</Badge>
                        </div>
                        <CardHeader className="pb-2">
                            <CardTitle className="text-lg">{asset.name}</CardTitle>
                        </CardHeader>
                        <CardContent>
                            <div className="space-y-3 mt-2">
                                <div className="flex justify-between text-sm">
                                    <span className="text-muted-foreground">Valor de Compra:</span>
                                    <span className="font-medium">{formatCLP(asset.purchaseValue)}</span>
                                </div>
                                <div className="flex justify-between text-sm border-t pt-2">
                                    <span className="text-muted-foreground">Valor Residual Actual:</span>
                                    <span className="font-bold text-primary">{formatCLP(asset.currentResidualValue)}</span>
                                </div>
                                
                                {/* Progress Bar for Depreciation */}
                                <div className="w-full bg-gray-200 rounded-full h-2 mt-2">
                                    <div 
                                        className="bg-primary h-2 rounded-full transition-all" 
                                        style={{ width: `${(asset.currentResidualValue / asset.purchaseValue) * 100}%` }}
                                    ></div>
                                </div>
                                <p className="text-xs text-right text-muted-foreground">Vida Útil Restante: {Math.round((asset.currentResidualValue / asset.purchaseValue) * 100)}%</p>
                            </div>
                        </CardContent>
                    </Card>
                ))}
            </div>
        </div>
    );
}