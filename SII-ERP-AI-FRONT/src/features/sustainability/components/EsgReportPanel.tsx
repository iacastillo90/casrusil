import React from 'react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { useEsgReport } from '../hooks/useSustainability';
import { Leaf, Lightbulb } from 'lucide-react';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts';

export const EsgReportPanel = () => {
    const { data: esg, isLoading } = useEsgReport();

    if (isLoading) return <div className="text-sm text-gray-500">Cargando reporte ESG...</div>;
    if (!esg) return null;

    const chartData = [
        { name: 'Scope 1 (Directas)', value: esg.scope1Emissions, color: '#16a34a' },
        { name: 'Scope 2 (Electricidad)', value: esg.scope2Emissions, color: '#2563eb' },
        { name: 'Scope 3 (Cadena Suministro)', value: esg.scope3Emissions, color: '#d97706' },
    ];

    return (
        <Card className="border-green-200">
            <CardHeader className="bg-green-50/50">
                <CardTitle className="flex items-center text-green-800">
                    <Leaf className="mr-2 h-5 w-5" />
                    Reporte de Emisiones ESG (Huella de Carbono)
                </CardTitle>
            </CardHeader>
            <CardContent className="pt-6 grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                    <div className="h-[250px]">
                        <ResponsiveContainer width="100%" height="100%">
                            <PieChart>
                                <Pie 
                                    data={chartData} 
                                    cx="50%" cy="50%" 
                                    innerRadius={60} 
                                    outerRadius={80} 
                                    paddingAngle={5}
                                    dataKey="value"
                                >
                                    {chartData.map((entry, index) => (
                                        <Cell key={`cell-${index}`} fill={entry.color} />
                                    ))}
                                </Pie>
                                <Tooltip formatter={(value: number) => [`${value} tCO2e`, 'Emisiones']} />
                                <Legend />
                            </PieChart>
                        </ResponsiveContainer>
                    </div>
                    <div className="text-center mt-2 font-semibold text-lg text-gray-700">
                        Total: {esg.totalEmissions} tCO2e
                    </div>
                </div>

                <div className="space-y-4">
                    <h4 className="font-semibold text-green-700 flex items-center">
                        <Lightbulb className="mr-2 h-5 w-5" /> Recomendaciones IA
                    </h4>
                    <ul className="space-y-3">
                        {esg.aiRecommendations.map((rec, idx) => (
                            <li key={idx} className="bg-green-50 text-sm text-green-900 p-3 rounded-md border border-green-100 flex items-start">
                                <span className="mr-2">•</span> {rec}
                            </li>
                        ))}
                    </ul>
                </div>
            </CardContent>
        </Card>
    );
};
