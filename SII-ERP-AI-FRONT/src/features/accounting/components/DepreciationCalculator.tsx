"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Calculator, Percent, Calendar, CheckCircle2, ArrowBigRightDash } from "lucide-react";
import { formatCurrency } from "@/lib/utils";

interface DepreciationCalculation {
    period: string;
    monthlyDepreciation: number;
    accumulatedDepreciation: number;
    remainingValue: number;
}

export const DepreciationCalculator = () => {
    // Inputs
    const [assetName, setAssetName] = useState("");
    const [initialValue, setInitialValue] = useState<number>(0);
    const [acquisitionDate, setAcquisitionDate] = useState<string>("");
    const [usefulLifeMonths, setUsefulLifeMonths] = useState<number>(120);
    const [method, setMethod] = useState<'LINEAL' | 'DECILONAL'>('LINEAL');

    // Calculated values
    const [result, setResult] = useState<DepreciationCalculation | null>(null);
    const [showResult, setShowResult] = useState(false);

    const currentMonth = new Date().toISOString().slice(0, 7); // YYYY-MM

    const calculateDepreciation = (): DepreciationCalculation => {
        const acquisition = new Date(acquisitionDate);
        const current = new Date();
        
        const monthsElapsed = Math.max(0, 
            (current.getFullYear() - acquisition.getFullYear()) * 12 + 
            (current.getMonth() - acquisition.getMonth())
        );

        if (method === 'LINEAL') {
            const monthlyDepreciation = initialValue / usefulLifeMonths;
            const accumulatedDepreciation = monthlyDepreciation * monthsElapsed;
            
            return {
                period: currentMonth,
                monthlyDepreciation,
                accumulatedDepreciation,
                remainingValue: Math.max(0, initialValue - accumulatedDepreciation),
            };
        } else {
            // Declining balance with 2x rate
            const rate = 2 / usefulLifeMonths;
            let bookValue = initialValue;
            let accumulated = 0;

            for (let i = 0; i < monthsElapsed; i++) {
                const monthly = bookValue * rate;
                accumulated += monthly;
                bookValue = initialValue - accumulated;
            }

            const monthlyDepreciation = Math.max(0, (initialValue - accumulated) * rate);
            
            return {
                period: currentMonth,
                monthlyDepreciation,
                accumulatedDepreciation: accumulated,
                remainingValue: Math.max(0, initialValue - accumulated),
            };
        }
    };

    const handleCalculate = () => {
        if (!initialValue || initialValue <= 0) {
            return;
        }
        
        const calculation = calculateDepreciation();
        setResult(calculation);
        setShowResult(true);
    };

    const handleApply = () => {
        // Aquí se enviaría a la API
        console.log("Applying depreciation:", result);
        if (!result) return;
        
        // Simplificado - en producción se llamaría al API
        alert(`Depreciación calculada y aplicada para: ${assetName}\n` +
              `Depreciación mensual: ${formatCurrency(result.monthlyDepreciation)}\n` +
              `Acumulada: ${formatCurrency(result.accumulatedDepreciation)}\n` +
              `Valor restante: ${formatCurrency(result.remainingValue)}`);
    };

    const reset = () => {
        setAssetName("");
        setInitialValue(0);
        setAcquisitionDate("");
        setUsefulLifeMonths(120);
        setMethod('LINEAL');
        setResult(null);
        setShowResult(false);
    };

    return (
        <Card className="w-full">
            <CardHeader>
                <CardTitle className="flex items-center gap-2">
                    <Calculator className="h-5 w-5" />
                    Calculadora de Depreciación
                </CardTitle>
                <CardDescription>
                    Calcula la depreciación de activos fijos según método lineal o declinado
                </CardDescription>
            </CardHeader>

            <CardContent className="space-y-6">
                {/* Inputs */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="space-y-2">
                        <Label htmlFor="assetName">Nombre del Activo</Label>
                        <Input
                            id="assetName"
                            placeholder="Ej: Equipo de cómputo"
                            value={assetName}
                            onChange={(e) => setAssetName(e.target.value)}
                        />
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="initialValue">Valor Inicial ({'>'}CLP)</Label>
                        <Input
                            id="initialValue"
                            type="number"
                            placeholder="1500000"
                            value={initialValue}
                            onChange={(e) => setInitialValue(Number(e.target.value))}
                        />
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="acquisitionDate">Fecha de Adquisición</Label>
                        <Input
                            id="acquisitionDate"
                            type="date"
                            value={acquisitionDate}
                            onChange={(e) => setAcquisitionDate(e.target.value)}
                        />
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="usefulLifeMonths">Vida Útil (meses)</Label>
                        <Input
                            id="usefulLifeMonths"
                            type="number"
                            placeholder="120"
                            value={usefulLifeMonths}
                            onChange={(e) => setUsefulLifeMonths(Number(e.target.value))}
                        />
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="method">Método de Depreciación</Label>
                        <Select
                            value={method}
                            onValueChange={(value: 'LINEAL' | 'DECILONAL') => setMethod(value)}
                        >
                            <SelectTrigger>
                                <SelectValue placeholder="Selecciona un método" />
                            </SelectTrigger>
                            <SelectContent>
                                <SelectItem value="LINEAL">Lineal</SelectItem>
                                <SelectItem value="DECILONAL">Declinado (2x)</SelectItem>
                            </SelectContent>
                        </Select>
                    </div>
                </div>

                {/* Botones */}
                <div className="flex gap-3">
                    <Button 
                        onClick={handleCalculate} 
                        disabled={!initialValue || initialValue <= 0 || !acquisitionDate}
                        variant="default"
                    >
                        <Calculator className="h-4 w-4 mr-2" />
                        Calcular
                    </Button>
                    <Button variant="outline" onClick={reset}>
                        Limpiar
                    </Button>
                </div>

                {/* Resultado */}
                {showResult && result && (
                    <div className="border-t pt-6">
                        <div className="bg-gray-50 rounded-lg p-4 space-y-4">
                            <h3 className="font-medium flex items-center gap-2">
                                <CheckCircle2 className="h-4 w-4 text-green-600" />
                                Resultado de Depreciación
                            </h3>

                            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-center">
                                <div className="bg-white p-3 rounded-lg border">
                                    <p className="text-xs text-muted-foreground">Mes a Medir</p>
                                    <p className="font-semibold">{result.period}</p>
                                </div>
                                <div className="bg-white p-3 rounded-lg border">
                                    <p className="text-xs text-muted-foreground">Mensual</p>
                                    <p className="font-semibold text-red-600">
                                        -{formatCurrency(result.monthlyDepreciation)}
                                    </p>
                                </div>
                                <div className="bg-white p-3 rounded-lg border">
                                    <p className="text-xs text-muted-foreground">Acumulada</p>
                                    <p className="font-semibold text-red-600">
                                        -{formatCurrency(result.accumulatedDepreciation)}
                                    </p>
                                </div>
                                <div className="bg-white p-3 rounded-lg border">
                                    <p className="text-xs text-muted-foreground">Valor Restante</p>
                                    <p className="font-semibold">
                                        {formatCurrency(result.remainingValue)}
                                    </p>
                                </div>
                            </div>

                            {/* Barra de progreso */}
                            {initialValue > 0 && (
                                <div>
                                    <div className="flex justify-between text-xs text-muted-foreground mb-1">
                                        <span>Depreciación acumulada</span>
                                        <span>{((result.accumulatedDepreciation / initialValue) * 100).toFixed(1)}%</span>
                                    </div>
                                    <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
                                        <div 
                                            className="h-full bg-red-200 rounded-full"
                                            style={{ 
                                                width: `${Math.min((result.accumulatedDepreciation / initialValue) * 100, 100)}%` 
                                            }}
                                        />
                                    </div>
                                </div>
                            )}

                            <Button 
                                onClick={handleApply}
                                className="w-full md:w-auto"
                            >
                                <ArrowBigRightDash className="h-4 w-4 mr-2" />
                                Aplicar Depreciación
                            </Button>
                        </div>
                    </div>
                )}
            </CardContent>
        </Card>
    );
};

export default DepreciationCalculator;