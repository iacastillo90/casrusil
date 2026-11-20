'use client';

import { Calculator, Cog, Monitor, Truck } from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Progress } from '@/components/ui/progress';
import { PageHeader } from '@/features/accounting/components/PageHeader';
import { EmptyState, ErrorState, TableSkeleton } from '@/features/accounting/components/DataStates';
import { useFixedAssets, useRunDepreciation } from '@/features/accounting/hooks/useAccounting';
import { formatCLP, formatDate } from '@/lib/formatters';

const methodLabel: Record<string, string> = {
    LINEAL: 'Lineal',
    DECILONAL: 'Decilonal',
};

const methodIcon = (method: string) => {
    switch (method) {
        case 'VEHICLE':
            return Truck;
        case 'IT':
            return Monitor;
        default:
            return Cog;
    }
};

export default function FixedAssetsPage() {
    const assets = useFixedAssets();
    const runDepreciation = useRunDepreciation();

    const handleRunDepreciation = () => {
        runDepreciation.mutate(undefined, {
            onSuccess: () => toast.success('Depreciación mensual calculada y guardada.'),
            onError: (error) =>
                toast.error('No se pudo ejecutar la depreciación', {
                    description: error instanceof Error ? error.message : undefined,
                }),
        });
    };

    return (
        <div className="space-y-6">
            <PageHeader
                title="Activos fijos y depreciación"
                description="Ciclo de vida, valor residual y depreciación acumulada de los activos de la empresa."
                actions={
                    <Button
                        onClick={handleRunDepreciation}
                        loading={runDepreciation.isPending}
                    >
                        {!runDepreciation.isPending && (
                            <Calculator aria-hidden="true" className="mr-2 h-4 w-4" />
                        )}
                        Ejecutar depreciación mensual
                    </Button>
                }
            />

            {assets.error ? (
                <ErrorState
                    message={
                        assets.error instanceof Error
                            ? assets.error.message
                            : 'Error desconocido'
                    }
                    onRetry={() => assets.refetch()}
                    retrying={assets.isFetching}
                />
            ) : assets.isLoading ? (
                <TableSkeleton rows={3} cols={3} />
            ) : !assets.data || assets.data.length === 0 ? (
                <EmptyState
                    title="No hay activos registrados"
                    description="Cuando registres un activo fijo podrás ver acá su vida útil, depreciación acumulada y valor residual."
                />
            ) : (
                <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                    {assets.data.map((asset) => {
                        const Icon = methodIcon(asset.depreciationMethod);
                        const remainingRatio =
                            asset.initialValue > 0
                                ? Math.min(
                                      100,
                                      Math.max(0, (asset.currentValue / asset.initialValue) * 100),
                                  )
                                : 0;
                        return (
                            <Card key={asset.id}>
                                <CardHeader className="flex flex-row items-start justify-between gap-3 pb-2">
                                    <div className="flex items-center gap-2">
                                        <Icon
                                            aria-hidden="true"
                                            className="h-5 w-5 text-muted-foreground"
                                        />
                                        <CardTitle className="text-base font-semibold">
                                            {asset.name}
                                        </CardTitle>
                                    </div>
                                    <Badge variant="outline">
                                        {methodLabel[asset.depreciationMethod] ??
                                            asset.depreciationMethod}
                                    </Badge>
                                </CardHeader>
                                <CardContent className="space-y-3 text-sm">
                                    <div className="flex justify-between gap-4">
                                        <span className="text-muted-foreground">Adquisición</span>
                                        <span>{formatDate(asset.acquisitionDate)}</span>
                                    </div>
                                    <div className="flex justify-between gap-4">
                                        <span className="text-muted-foreground">Valor de compra</span>
                                        <span className="font-mono tabular-nums">
                                            {formatCLP(asset.initialValue)}
                                        </span>
                                    </div>
                                    <div className="flex justify-between gap-4">
                                        <span className="text-muted-foreground">
                                            Depreciación acumulada
                                        </span>
                                        <span className="font-mono tabular-nums">
                                            {formatCLP(asset.accumulatedDepreciation)}
                                        </span>
                                    </div>
                                    <div className="flex justify-between gap-4 border-t pt-2">
                                        <span className="text-muted-foreground">
                                            Valor residual actual
                                        </span>
                                        <span className="font-mono font-semibold tabular-nums">
                                            {formatCLP(asset.currentValue)}
                                        </span>
                                    </div>
                                    <div className="space-y-1">
                                        <Progress value={remainingRatio} aria-label="Valor residual" />
                                        <p className="text-xs text-muted-foreground text-right">
                                            {Math.round(remainingRatio)}% del valor original
                                        </p>
                                    </div>
                                </CardContent>
                            </Card>
                        );
                    })}
                </div>
            )}
        </div>
    );
}
