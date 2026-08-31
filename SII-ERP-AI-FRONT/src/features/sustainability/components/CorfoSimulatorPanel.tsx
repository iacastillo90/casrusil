import React from 'react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { useCorfoSimulation } from '../hooks/useSustainability';
import { Award, Percent, DollarSign, Target } from 'lucide-react';
import { Badge } from '@/components/ui/badge';

export const CorfoSimulatorPanel = () => {
    const { data: corfo, isLoading } = useCorfoSimulation();

    if (isLoading) return <div className="text-sm text-gray-500">Simulando beneficios CORFO...</div>;
    if (!corfo) return null;

    const getStatusBadge = () => {
        switch(corfo.eligibilityStatus) {
            case 'GOLD_ELIGIBLE': return <Badge className="bg-yellow-500 hover:bg-yellow-600">Gold</Badge>;
            case 'SILVER_ELIGIBLE': return <Badge className="bg-gray-400 hover:bg-gray-500">Silver</Badge>;
            default: return <Badge variant="secondary">No Elegible</Badge>;
        }
    };

    return (
        <Card className="border-blue-200">
            <CardHeader className="bg-blue-50/50">
                <CardTitle className="flex items-center text-blue-800 justify-between">
                    <div className="flex items-center">
                        <Award className="mr-2 h-5 w-5" />
                        Simulador Subsidios CORFO (Pasaporte Verde)
                    </div>
                    {getStatusBadge()}
                </CardTitle>
            </CardHeader>
            <CardContent className="pt-6 space-y-6">
                
                <div className="grid grid-cols-2 gap-4">
                    <div className="border border-blue-100 bg-white rounded-lg p-4 flex flex-col items-center justify-center text-center shadow-sm">
                        <Percent className="h-8 w-8 text-blue-500 mb-2" />
                        <div className="text-2xl font-bold text-gray-900">{corfo.taxRebatePercentage}%</div>
                        <div className="text-xs text-gray-500 uppercase font-semibold tracking-wider mt-1">Rebaja Fiscal Estimada</div>
                    </div>

                    <div className="border border-blue-100 bg-white rounded-lg p-4 flex flex-col items-center justify-center text-center shadow-sm">
                        <DollarSign className="h-8 w-8 text-blue-500 mb-2" />
                        <div className="text-2xl font-bold text-gray-900">{corfo.preferentialCreditRate}%</div>
                        <div className="text-xs text-gray-500 uppercase font-semibold tracking-wider mt-1">Tasa Crédito Preferencial</div>
                    </div>
                </div>

                <div className="bg-blue-50 rounded-lg p-4 flex items-start gap-3 border border-blue-100">
                    <Target className="h-5 w-5 text-blue-600 shrink-0 mt-0.5" />
                    <div>
                        <h4 className="font-semibold text-blue-900 text-sm">Próximo Nivel</h4>
                        <p className="text-sm text-blue-800 mt-1">{corfo.nextLevelRequirement}</p>
                    </div>
                </div>

            </CardContent>
        </Card>
    );
};
