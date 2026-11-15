import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Badge } from "@/components/ui/badge";
import { Leaf, Cloud, Lightbulb } from "lucide-react";
import type { InvoiceSustainability } from "../services/sustainability.service";

interface SustainabilityCardProps {
    data?: InvoiceSustainability | null;
    isLoading: boolean;
}

export function SustainabilityCard({ data, isLoading }: SustainabilityCardProps) {
    if (isLoading) {
        return <Skeleton className="h-48 w-full" />;
    }

    if (!data) {
        return (
            <Card>
                <CardContent className="py-12 text-center text-muted-foreground">
                    El detalle de sostenibilidad por documento todavía no está disponible. Consultá el panel de sostenibilidad para ver tu Eco-Score global.
                </CardContent>
            </Card>
        );
    }

    return (
        <Card className="border-green-500/20">
            <CardHeader>
                <CardTitle className="flex items-center gap-2 text-base">
                    <Leaf className="h-5 w-5 text-green-600" />
                    Impacto Ambiental
                </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                    <div className="rounded-lg border p-4">
                        <p className="text-sm text-muted-foreground">Eco-Score</p>
                        <p className="text-2xl font-bold text-green-600">{Math.round(data.ecoScore)}</p>
                    </div>
                    <div className="rounded-lg border p-4">
                        <p className="flex items-center gap-1 text-sm text-muted-foreground">
                            <Cloud className="h-4 w-4" /> Huella de Carbono
                        </p>
                        <p className="text-2xl font-bold">{data.carbonFootprint.toFixed(2)} tCO2e</p>
                    </div>
                </div>

                {data.isGreenProvider && (
                    <Badge variant="outline" className="gap-1 border-green-500/30 text-green-700">
                        <Leaf className="h-3 w-3" /> Proveedor Verde
                    </Badge>
                )}

                {data.recommendations.length > 0 && (
                    <div>
                        <h4 className="mb-2 flex items-center gap-2 text-sm font-semibold text-green-700">
                            <Lightbulb className="h-4 w-4" /> Recomendaciones
                        </h4>
                        <ul className="space-y-2">
                            {data.recommendations.map((rec, idx) => (
                                <li key={idx} className="rounded-md border border-green-100 bg-green-50 p-2 text-sm text-green-900">
                                    {rec}
                                </li>
                            ))}
                        </ul>
                    </div>
                )}
            </CardContent>
        </Card>
    );
}
