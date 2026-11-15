"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Leaf } from "lucide-react";
import { useFinancialShieldHealth } from "@/features/financial-shield/hooks/useFinancialShieldHealth";

export const EcoScoreCard = () => {
    const { data, isLoading, isError } = useFinancialShieldHealth();

    if (isLoading) {
        return (
            <Card>
                <CardHeader>
                    <CardTitle className="text-base font-semibold">Eco Score</CardTitle>
                </CardHeader>
                <CardContent>
                    <div className="h-16 w-full animate-pulse rounded bg-muted/20" />
                </CardContent>
            </Card>
        );
    }

    const greenScore = data?.greenScore;

    if (isError || !greenScore) {
        return (
            <Card>
                <CardHeader>
                    <CardTitle className="text-base font-semibold">Eco Score</CardTitle>
                </CardHeader>
                <CardContent>
                    <p className="text-sm text-muted-foreground">Sin evaluación de sostenibilidad disponible.</p>
                </CardContent>
            </Card>
        );
    }

    return (
        <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-base font-semibold">Eco Score</CardTitle>
                <Leaf className="h-4 w-4 text-green-600" />
            </CardHeader>
            <CardContent>
                <div className="text-3xl font-bold" suppressHydrationWarning>{greenScore.score}/100</div>
                <p className="text-sm text-muted-foreground mt-1">{greenScore.level}</p>
                <p className="text-xs text-muted-foreground mt-3">
                    {greenScore.benefits?.length ?? 0} beneficios verdes activos
                </p>
            </CardContent>
        </Card>
    );
};
