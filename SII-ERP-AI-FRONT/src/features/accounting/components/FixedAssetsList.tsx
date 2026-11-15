"use client";

import { useState } from "react";
import { useFixedAssets, useRunDepreciation } from "@/features/fixed_assets/hooks/useFixedAssets";
import type { FixedAsset } from "@/features/fixed_assets/services/fixedAssets.service";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { 
    Calculator, 
    Calendar, 
    TrendingDown, 
    CheckCircle2, 
    AlertCircle,
    RefreshCw
} from "lucide-react";
import { formatCLP } from "@/lib/formatters";
import { toast } from "sonner";

export const FixedAssetsList = () => {
    const [isRunningDepreciation, setIsRunningDepreciation] = useState(false);
    
    const { data: assets, isLoading, error } = useFixedAssets();
    const runDepreciationMutation = useRunDepreciation();

    const handleRunDepreciation = () => {
        setIsRunningDepreciation(true);
        runDepreciationMutation.mutate(undefined, {
            onSuccess: () => {
                toast.success("Depreciación ejecutada correctamente", {
                    description: "Los cálculos se han actualizado en el sistema",
                });
            },
            onError: (error: Error) => {
                toast.error("Error al ejecutar depreciación", {
                    description: error.message,
                });
            },
        });
    };

    const getStatusBadge = (status: string) => {
        switch (status) {
            case 'ACTIVE':
                return <Badge className="bg-green-100 text-green-800">Activo</Badge>;
            case 'DEPRECIATED':
                return <Badge className="bg-amber-100 text-amber-800">Depreciado</Badge>;
            case 'DISPOSED':
                return <Badge className="bg-gray-100 text-gray-800">Dado de Baja</Badge>;
            default:
                return <Badge variant="outline">Desconocido</Badge>;
        }
    };

    const getMethodText = (method: string) => {
        return method === 'LINEAL' ? 'Lineal' : 'Declinado';
    };

    if (isLoading) {
        return (
            <Card>
                <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                        <Calculator className="h-5 w-5" />
                        Activos Fijos
                    </CardTitle>
                </CardHeader>
                <CardContent>
                    <div className="animate-pulse space-y-4">
                        {[...Array(5)].map((_, i) => (
                            <div key={i} className="h-12 bg-gray-200 rounded"></div>
                        ))}
                    </div>
                </CardContent>
            </Card>
        );
    }

    if (error) {
        return (
            <Card>
                <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                        <Calculator className="h-5 w-5" />
                        Activos Fijos
                    </CardTitle>
                </CardHeader>
                <CardContent>
                    <div className="text-center py-8">
                        <AlertCircle className="h-8 w-8 mx-auto text-red-500 mb-2" />
                        <p className="text-muted-foreground">Error al cargar activos fijos</p>
                    </div>
                </CardContent>
            </Card>
        );
    }

    const totalValue = assets?.reduce((sum: number, asset: FixedAsset) => sum + asset.currentValue, 0) ?? 0;
    const totalDepreciation = assets?.reduce((sum: number, asset: FixedAsset) => sum + asset.accumulatedDepreciation, 0) ?? 0;
    const activeCount = assets?.filter((a: FixedAsset) => a.status === 'ACTIVE').length ?? 0;

    return (
        <Card className="w-full">
            <CardHeader>
                <div className="flex justify-between items-center">
                    <CardTitle className="flex items-center gap-2">
                        <Calculator className="h-5 w-5" />
                        Activos Fijos
                    </CardTitle>
                    <Button
                        onClick={handleRunDepreciation}
                        disabled={runDepreciationMutation.isPending || isRunningDepreciation}
                        variant="outline"
                        size="sm"
                    >
                        {runDepreciationMutation.isPending || isRunningDepreciation ? (
                            <RefreshCw className="h-4 w-4 mr-2 animate-spin" />
                        ) : (
                            <Calculator className="h-4 w-4 mr-2" />
                        )}
                        Ejecutar Depreciación
                    </Button>
                </div>
                <CardDescription>
                    Lista de activos fijos con estado de depreciación
                </CardDescription>
            </CardHeader>

            <CardContent className="space-y-4">
                {/* Resumen rápido */}
                <div className="grid grid-cols-3 gap-4 p-4 bg-gray-50 rounded-lg">
                    <div>
                        <p className="text-xs text-muted-foreground">Valor Total</p>
                        <p className="text-lg font-semibold">{formatCLP(totalValue)}</p>
                    </div>
                    <div>
                        <p className="text-xs text-muted-foreground">Deprec. Acumulada</p>
                        <p className="text-lg font-semibold text-red-600">
                            -{formatCLP(totalDepreciation)}
                        </p>
                    </div>
                    <div>
                        <p className="text-xs text-muted-foreground">Activos Activos</p>
                        <p className="text-lg font-semibold">{activeCount}</p>
                    </div>
                </div>

                {/* Lista o estado vacío */}
                {assets && assets.length > 0 ? (
                    <div className="space-y-3">
                        {assets.map((asset) => (
                            <div 
                                key={asset.id}
                                className="border rounded-lg p-4 hover:shadow-sm transition-shadow"
                            >
                                <div className="flex justify-between items-start mb-2">
                                    <div>
                                        <h4 className="font-medium">{asset.name}</h4>
                                        <p className="text-sm text-muted-foreground">
                                            {asset.code} • {getMethodText(asset.depreciationMethod)}
                                        </p>
                                    </div>
                                    {getStatusBadge(asset.status)}
                                </div>
                                
                                <div className="grid grid-cols-4 gap-4 text-sm">
                                    <div>
                                        <p className="text-xs text-muted-foreground">Valor Inicial</p>
                                        <p>{formatCLP(asset.initialValue)}</p>
                                    </div>
                                    <div>
                                        <p className="text-xs text-muted-foreground">Valor Actual</p>
                                        <p className="font-medium">{formatCLP(asset.currentValue)}</p>
                                    </div>
                                    <div>
                                        <p className="text-xs text-muted-foreground">Depreciación Mensual</p>
                                        <p className="text-red-600">-{formatCLP(asset.monthlyDepreciation)}</p>
                                    </div>
                                    <div>
                                        <p className="text-xs text-muted-foreground">Adquirido</p>
                                        <p>{new Date(asset.acquisitionDate).toLocaleDateString('es-CL')}</p>
                                    </div>
                                </div>

                                {/* Barra de progreso de depreciación */}
                                {asset.initialValue > 0 && (
                                    <div className="mt-3">
                                        <div className="flex justify-between text-xs text-muted-foreground mb-1">
                                            <span>Depreciación acumulada</span>
                                            <span>{((asset.accumulatedDepreciation / asset.initialValue) * 100).toFixed(1)}%</span>
                                        </div>
                                        <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
                                            <div 
                                                className="h-full bg-red-200 rounded-full"
                                                style={{ 
                                                    width: `${Math.min((asset.accumulatedDepreciation / asset.initialValue) * 100, 100)}%` 
                                                }}
                                            />
                                        </div>
                                    </div>
                                )}
                            </div>
                        ))}
                    </div>
                ) : (
                    <div className="text-center py-8">
                        <AlertCircle className="h-8 w-8 mx-auto text-muted-foreground mb-2" />
                        <p className="text-muted-foreground">No hay activos fijos registrados</p>
                    </div>
                )}
            </CardContent>
        </Card>
    );
};

export default FixedAssetsList;