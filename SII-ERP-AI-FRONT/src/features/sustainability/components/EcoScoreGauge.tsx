"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Leaf } from "lucide-react";

interface EcoScoreGaugeProps {
    score: number;
    max?: number;
}

function scoreColor(pct: number): string {
    if (pct >= 75) return "#16a34a"; // green
    if (pct >= 50) return "#ca8a04"; // amber
    if (pct >= 25) return "#ea580c"; // orange
    return "#dc2626"; // red
}

function scoreLabel(pct: number): string {
    if (pct >= 75) return "Excelente";
    if (pct >= 50) return "Bueno";
    if (pct >= 25) return "Regular";
    return "Bajo";
}

export function EcoScoreGauge({ score, max = 100 }: EcoScoreGaugeProps) {
    const safeMax = max > 0 ? max : 100;
    const pct = Math.max(0, Math.min(100, (score / safeMax) * 100));
    const color = scoreColor(pct);

    const radius = 70;
    const stroke = 12;
    const circumference = 2 * Math.PI * radius;
    const dash = (pct / 100) * circumference;

    return (
        <Card className="border-green-500/20 h-full">
            <CardHeader className="pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground flex items-center gap-2">
                    <Leaf className="h-4 w-4 text-green-600" />
                    Eco-Score
                </CardTitle>
            </CardHeader>
            <CardContent className="flex flex-col items-center justify-center">
                <div className="relative">
                    <svg width="180" height="180" viewBox="0 0 180 180">
                        <circle
                            cx="90"
                            cy="90"
                            r={radius}
                            fill="none"
                            stroke="#e5e7eb"
                            strokeWidth={stroke}
                        />
                        <circle
                            cx="90"
                            cy="90"
                            r={radius}
                            fill="none"
                            stroke={color}
                            strokeWidth={stroke}
                            strokeLinecap="round"
                            strokeDasharray={`${dash} ${circumference}`}
                            transform="rotate(-90 90 90)"
                        />
                    </svg>
                    <div className="absolute inset-0 flex flex-col items-center justify-center">
                        <span className="text-4xl font-bold" style={{ color }}>
                            {Math.round(score)}
                        </span>
                        <span className="text-xs text-muted-foreground">de {safeMax}</span>
                    </div>
                </div>
                <p className="mt-3 text-sm font-semibold" style={{ color }}>
                    {scoreLabel(pct)}
                </p>
            </CardContent>
        </Card>
    );
}
