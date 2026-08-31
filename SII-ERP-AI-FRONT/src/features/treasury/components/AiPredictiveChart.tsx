import React from 'react';
import { useAiForecast } from '../hooks/useTreasury';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { AlertCircle, TrendingDown, TrendingUp, Activity } from 'lucide-react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, ReferenceLine } from 'recharts';

export const AiPredictiveChart = () => {
    const { data: forecast, isLoading } = useAiForecast(30);

    if (isLoading) {
        return <div className="h-64 flex items-center justify-center text-gray-500">Analizando proyecciones de caja con IA...</div>;
    }

    if (!forecast) return null;

    return (
        <div className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <Card className={forecast.trend === 'POSITIVE' ? 'border-green-200' : 'border-red-200'}>
                    <CardHeader className="pb-2">
                        <CardTitle className="text-sm text-gray-500">Liquidez Actual (Hoy)</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">${forecast.currentLiquidity.toLocaleString('es-CL')}</div>
                    </CardContent>
                </Card>
                <Card className={forecast.trend === 'POSITIVE' ? 'border-green-200' : 'border-red-200'}>
                    <CardHeader className="pb-2">
                        <CardTitle className="text-sm text-gray-500">Proyección a 30 días (IA)</CardTitle>
                    </CardHeader>
                    <CardContent className="flex justify-between items-center">
                        <div className="text-2xl font-bold">${forecast.predictedLiquidity.toLocaleString('es-CL')}</div>
                        {forecast.trend === 'POSITIVE' ? 
                            <TrendingUp className="text-green-500 h-6 w-6" /> : 
                            <TrendingDown className="text-red-500 h-6 w-6" />
                        }
                    </CardContent>
                </Card>
            </div>

            {forecast.riskAlerts.length > 0 && (
                <div className="bg-red-50 border border-red-200 rounded-md p-4 flex gap-3 text-red-700">
                    <AlertCircle className="h-5 w-5 shrink-0" />
                    <ul className="list-disc list-inside text-sm">
                        {forecast.riskAlerts.map((alert, idx) => (
                            <li key={idx}>{alert}</li>
                        ))}
                    </ul>
                </div>
            )}

            <Card>
                <CardHeader>
                    <CardTitle className="text-lg flex items-center">
                        <Activity className="mr-2 h-5 w-5 text-indigo-600" />
                        Flujo de Caja Predictivo
                    </CardTitle>
                </CardHeader>
                <CardContent>
                    <div className="h-[300px] w-full">
                        <ResponsiveContainer width="100%" height="100%">
                            <LineChart data={forecast.dailyForecast}>
                                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                                <XAxis dataKey="date" tick={{fontSize: 12}} tickFormatter={(val) => val.substring(5)} />
                                <YAxis tick={{fontSize: 12}} tickFormatter={(val) => `$${(val/1000000).toFixed(1)}M`} />
                                <Tooltip formatter={(value: number) => [`$${value.toLocaleString('es-CL')}`, 'Liquidez Estimada']} />
                                <ReferenceLine y={0} stroke="red" strokeDasharray="3 3" />
                                <Line 
                                    type="monotone" 
                                    dataKey="estimatedAmount" 
                                    stroke="#4f46e5" 
                                    strokeWidth={2}
                                    dot={false}
                                />
                            </LineChart>
                        </ResponsiveContainer>
                    </div>
                </CardContent>
            </Card>
        </div>
    );
};
