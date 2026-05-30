"use client";

import { useQuery } from "@tanstack/react-query";
import { sustainabilityService } from "@/features/sustainability/services/sustainability.service";
import { EcoScoreGauge } from "@/features/sustainability/components/EcoScoreGauge";
import { SustainablePurchasesChart } from "@/features/sustainability/components/SustainablePurchasesChart";
import { TopGreenProviders } from "@/features/sustainability/components/TopGreenProviders";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Leaf, Award, TrendingUp, Sparkles } from "lucide-react";
import { formatCLP } from "@/lib/formatters";

export default function SustainabilityDashboardPage() {
    const { data: greenPassport, isLoading } = useQuery({
        queryKey: ['green-passport'],
        queryFn: () => sustainabilityService.getGreenPassport(),
    });

    if (isLoading) {
        return (
            <div className="flex items-center justify-center min-h-[400px]">
                <div className="text-center">
                    <div className="h-8 w-8 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto mb-4" />
                    <p className="text-muted-foreground">Cargando Pasaporte Verde...</p>
                </div>
            </div>
        );
    }

    if (!greenPassport) {
        return (
            <div className="flex items-center justify-center min-h-[400px]">
                <div className="text-center">
                    <Leaf className="h-16 w-16 text-muted-foreground mx-auto mb-4 opacity-50" />
                    <h2 className="text-2xl font-bold mb-2">Datos no disponibles</h2>
                    <p className="text-muted-foreground">No se pudieron cargar los datos de sostenibilidad.</p>
                </div>
            </div>
        );
    }

    return (
        <div className="space-y-6 max-w-7xl mx-auto">
            {/* Hero Header */}
            <div className="relative overflow-hidden rounded-lg bg-gradient-to-br from-green-500/10 via-emerald-500/10 to-teal-500/10 border border-green-500/20 p-8">
                <div className="absolute top-0 right-0 w-64 h-64 bg-green-500/5 rounded-full blur-3xl" />
                <div className="absolute bottom-0 left-0 w-64 h-64 bg-emerald-500/5 rounded-full blur-3xl" />

                <div className="relative">
                    <div className="flex items-center gap-3 mb-4">
                        <div className="p-3 rounded-full bg-green-500/10 border border-green-500/20">
                            <Leaf className="h-8 w-8 text-green-600 dark:text-green-400" />
                        </div>
                        <div>
                            <h1 className="text-4xl font-bold tracking-tight">Pasaporte Verde</h1>
                            <p className="text-muted-foreground mt-1">
                                Dashboard de Sostenibilidad Empresarial
                            </p>
                        </div>
                    </div>

                    <div className="flex flex-wrap gap-2 mt-4">
                        <Badge variant="outline" className="gap-1 bg-green-500/10 text-green-700 dark:text-green-400 border-green-500/20">
                            <Sparkles className="h-3 w-3" />
                            {greenPassport.scoreLevel}
                        </Badge>
                        {greenPassport.certifications.map((cert: string, idx: number) => (
                            <Badge key={idx} variant="outline" className="gap-1">
                                <Award className="h-3 w-3" />
                                {cert}
                            </Badge>
                        ))}
                    </div>

                    <p className="text-xs text-muted-foreground mt-4">
                        Última actualización: {new Date(greenPassport.lastUpdated).toLocaleString('es-CL')}
                    </p>
                </div>
            </div>

            {/* Key Metrics Grid */}
            <div className="grid gap-6 md:grid-cols-3">
                {/* Eco-Score Gauge */}
                <div className="md:col-span-1">
                    <EcoScoreGauge score={greenPassport.ecoScore} />
                </div>

                {/* Purchase Metrics */}
                <div className="md:col-span-2 grid gap-6 sm:grid-cols-2">
                    <Card className="border-primary/20">
                        <CardHeader className="pb-3">
                            <CardTitle className="text-sm font-medium text-muted-foreground">
                                Compras Totales
                            </CardTitle>
                        </CardHeader>
                        <CardContent>
                            <p className="text-3xl font-bold">
                                {formatCLP(greenPassport.totalPurchases)}
                            </p>
                            <p className="text-xs text-muted-foreground mt-1">
                                Últimos 6 meses
                            </p>
                        </CardContent>
                    </Card>

                    <Card className="border-green-500/20 bg-gradient-to-br from-background to-green-500/5">
                        <CardHeader className="pb-3">
                            <CardTitle className="text-sm font-medium text-muted-foreground flex items-center gap-2">
                                <Leaf className="h-4 w-4 text-green-600 dark:text-green-400" />
                                Compras Sostenibles
                            </CardTitle>
                        </CardHeader>
                        <CardContent>
                            <p className="text-3xl font-bold text-green-600 dark:text-green-400">
                                {formatCLP(greenPassport.sustainablePurchases)}
                            </p>
                            <div className="flex items-center gap-2 mt-2">
                                <TrendingUp className="h-4 w-4 text-green-600 dark:text-green-400" />
                                <p className="text-sm font-semibold text-green-600 dark:text-green-400">
                                    {greenPassport.sustainablePercentage.toFixed(1)}% del total
                                </p>
                            </div>
                        </CardContent>
                    </Card>
                </div>
            </div>

            {/* Charts Section */}
            <div className="grid gap-6 lg:grid-cols-2">
                {/* Sustainable Purchases Chart */}
                <div className="lg:col-span-2">
                    <SustainablePurchasesChart data={greenPassport.monthlyComparison} />
                </div>
            </div>

            {/* Top Green Providers */}
            <TopGreenProviders providers={greenPassport.topGreenProviders} />

            {/* CORFO Call-to-Action */}
            <Card className="border-primary/20 bg-gradient-to-br from-primary/5 to-primary/10">
                <CardContent className="pt-6">
                    <div className="flex items-start gap-4">
                        <div className="p-3 rounded-full bg-primary/10 border border-primary/20 shrink-0">
                            <Award className="h-6 w-6 text-primary" />
                        </div>
                        <div className="flex-1">
                            <h3 className="text-lg font-semibold mb-2">
                                ¿Listo para postular a CORFO?
                            </h3>
                            <p className="text-sm text-muted-foreground mb-4">
                                Tu Eco-Score de <strong>{greenPassport.ecoScore}/100</strong> y el{' '}
                                <strong>{greenPassport.sustainablePercentage.toFixed(1)}%</strong> de compras sostenibles
                                demuestran tu compromiso ambiental. Estos datos pueden fortalecer tu postulación a fondos CORFO
                                para empresas sustentables.
                            </p>
                            <div className="flex flex-wrap gap-2">
                                <Badge variant="default" className="gap-1">
                                    <Sparkles className="h-3 w-3" />
                                    Datos verificables
                                </Badge>
                                <Badge variant="outline" className="gap-1">
                                    <TrendingUp className="h-3 w-3" />
                                    Tendencia positiva
                                </Badge>
                            </div>
                        </div>
                    </div>
                </CardContent>
            </Card>
        </div>
    );
}
