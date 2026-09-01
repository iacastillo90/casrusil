"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { Leaf, Trophy } from "lucide-react";
import { formatCLP } from "@/lib/formatters";
import type { GreenProvider } from "../services/sustainability.service";

interface TopGreenProvidersProps {
    providers: GreenProvider[];
}

export function TopGreenProviders({ providers }: TopGreenProvidersProps) {
    return (
        <Card className="border-green-500/20">
            <CardHeader>
                <CardTitle className="flex items-center gap-2 text-base font-semibold">
                    <Trophy className="h-5 w-5 text-green-600" />
                    Top Proveedores Verdes
                </CardTitle>
            </CardHeader>
            <CardContent>
                {providers.length === 0 ? (
                    <div className="py-8 text-center text-sm text-muted-foreground">
                        Aún no hay proveedores verdes registrados.
                    </div>
                ) : (
                    <ul className="space-y-4">
                        {providers.map((provider, idx) => (
                            <li key={provider.id} className="flex items-center gap-3">
                                <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-green-100 text-sm font-bold text-green-700">
                                    {idx + 1}
                                </div>
                                <div className="min-w-0 flex-1">
                                    <div className="flex items-center justify-between gap-2">
                                        <p className="truncate font-medium">{provider.name}</p>
                                        <Badge variant="outline" className="gap-1 border-green-500/30 text-green-700">
                                            <Leaf className="h-3 w-3" />
                                            {Math.round(provider.ecoScore)}
                                        </Badge>
                                    </div>
                                    <Progress value={Math.max(0, Math.min(100, provider.ecoScore))} className="mt-1 h-1.5" />
                                    <div className="mt-1 flex items-center justify-between text-xs text-muted-foreground">
                                        <span>{provider.category ?? "General"}</span>
                                        <span>{formatCLP(provider.totalPurchased)}</span>
                                    </div>
                                </div>
                            </li>
                        ))}
                    </ul>
                )}
            </CardContent>
        </Card>
    );
}
