"use client";

import { useState, useEffect } from "react";
import { useQuery } from "@tanstack/react-query";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { Button } from "@/components/ui/button";
import { 
    RefreshCw, 
    Clock, 
    CheckCircle2, 
    Activity, 
    FileText,
    BarChart3,
    AlertCircle 
} from "lucide-react";
import { format, parseISO } from "date-fns";
import { es } from "date-fns/locale";
import { siiService } from "../services/sii.service";

interface ModuleStatus {
    id: string;
    name: string;
    icon: React.ElementType;
    status: 'SYNCED' | 'PENDING' | 'ERROR';
    lastSync?: string;
    description: string;
}

const moduleConfig: { id: string; name: string; icon: React.ElementType; description: string }[] = [
    { 
        id: 'sii', 
        name: 'SII', 
        icon: BarChart3, 
        description: 'Sincronización de impuestos con SII' 
    },
    { 
        id: 'caf', 
        name: 'CAF', 
        icon: FileText, 
        description: 'Archivos de Autorización de Facturación' 
    },
    { 
        id: 'dte', 
        name: 'DTE', 
        icon: Activity, 
        description: 'Documentos Tributarios Electrónicos' 
    },
];

export const SyncStatusDashboard = () => {
    const [lastRefresh, setLastRefresh] = useState<Date>(new Date());
    const [isSyncing, setIsSyncing] = useState(false);

    // Fetch sync status from API
    const { 
        data: syncStatus, 
        isLoading,
        error,
        refetch 
    } = useQuery({
        queryKey: ['sii-sync-status'],
        queryFn: () => siiService.getSyncStatus(),
        refetchInterval: 30000, // Polling every 30 seconds
        staleTime: 10000,
    });

    const handleForceSync = async () => {
        setIsSyncing(true);
        try {
            await siiService.syncSii();
            setLastRefresh(new Date());
            refetch();
        } catch (error) {
            console.error('Sync error:', error);
        } finally {
            setIsSyncing(false);
        }
    };

    const getStatusBadge = (status: string) => {
        switch (status) {
            case 'SYNCED':
                return <Badge className="bg-green-100 text-green-800">Sincronizado</Badge>;
            case 'PENDING':
                return <Badge className="bg-amber-100 text-amber-800">Pendiente</Badge>;
            case 'ERROR':
                return <Badge className="bg-red-100 text-red-800">Error</Badge>;
            default:
                return <Badge variant="outline">Desconocido</Badge>;
        }
    };

    const formatDateTime = (dateString: string) => {
        try {
            return format(parseISO(dateString), "d MMM yyyy 'a las' HH:mm", { locale: es });
        } catch {
            return dateString;
        }
    };

    const formatRelative = (dateString: string) => {
        const date = parseISO(dateString);
        const now = new Date();
        const diffMs = now.getTime() - date.getTime();
        const diffMins = Math.floor(diffMs / 60000);
        const diffHours = Math.floor(diffMs / 3600000);
        const diffDays = Math.floor(diffMs / 86400000);

        if (diffMins < 1) return "hace un momento";
        if (diffMins < 60) return `hace ${diffMins} minuto${diffMins > 1 ? 's' : ''}`;
        if (diffHours < 24) return `hace ${diffHours} hora${diffHours > 1 ? 's' : ''}`;
        return `hace ${diffDays} día${diffDays > 1 ? 's' : ''}`;
    };

    if (isLoading) {
        return (
            <Card>
                <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                        <Activity className="h-5 w-5" />
                        Estado de Sincronización
                    </CardTitle>
                </CardHeader>
                <CardContent>
                    <div className="animate-pulse space-y-4">
                        <div className="h-4 bg-gray-200 rounded w-3/4"></div>
                        <div className="h-4 bg-gray-200 rounded w-1/2"></div>
                        <div className="space-y-3">
                            {[...Array(3)].map((_, i) => (
                                <div key={i} className="flex items-center justify-between p-3 border rounded-lg">
                                    <div className="h-4 bg-gray-200 rounded w-1/3"></div>
                                    <div className="h-6 bg-gray-200 rounded w-20"></div>
                                </div>
                            ))}
                        </div>
                    </div>
                </CardContent>
            </Card>
        );
    }

    return (
        <Card className="w-full">
            <CardHeader>
                <div className="flex justify-between items-center">
                    <CardTitle className="flex items-center gap-2">
                        <Activity className="h-5 w-5" />
                        Estado de Sincronización
                    </CardTitle>
                    <Button
                        variant="outline"
                        size="sm"
                        onClick={handleForceSync}
                        loading={isSyncing}
                    >
                        {isSyncing ? 'Sincronizando...' : 'Forzar Sincronización'}
                    </Button>
                </div>
                <CardDescription>
                    Estado actual de sincronización con el SII
                </CardDescription>
            </CardHeader>
            
            <CardContent className="space-y-4">
                {error && (
                    <div className="p-4 bg-red-50 border border-red-200 rounded-lg">
                        <div className="flex items-center gap-2 text-red-800">
                            <AlertCircle className="h-4 w-4" />
                            <span>Error al cargar el estado de sincronización</span>
                        </div>
                    </div>
                )}

                {!error && syncStatus && (
                    <div className="space-y-4">
                        {/* Resumen general */}
                        <div className="grid grid-cols-3 gap-4 text-center">
                            <div>
                                <p className="text-2xl font-bold text-green-600">
                                    {syncStatus.modules.sii === 'SYNCED' ? '✓' : '…'}
                                </p>
                                <p className="text-sm text-muted-foreground">SII</p>
                            </div>
                            <div>
                                <p className="text-2xl font-bold text-green-600">
                                    {syncStatus.modules.caf === 'SYNCED' ? '✓' : '…'}
                                </p>
                                <p className="text-sm text-muted-foreground">CAF</p>
                            </div>
                            <div>
                                <p className="text-2xl font-bold text-green-600">
                                    {syncStatus.modules.dte === 'SYNCED' ? '✓' : '…'}
                                </p>
                                <p className="text-sm text-muted-foreground">DTE</p>
                            </div>
                        </div>

                        {/* Estado de módulos */}
                        <div className="space-y-3">
                            {moduleConfig.map((module) => {
                                const moduleStatus = syncStatus.modules[module.id as 'sii' | 'caf' | 'dte'];
                                const Icon = module.icon;
                                
                                return (
                                    <div 
                                        key={module.id}
                                        className="flex items-center justify-between p-4 border rounded-lg"
                                    >
                                        <div className="flex items-center gap-3">
                                            <div className="p-2 bg-gray-100 rounded-full">
                                                <Icon className="h-5 w-5 text-gray-600" />
                                            </div>
                                            <div>
                                                <h4 className="font-medium">{module.name}</h4>
                                                <p className="text-sm text-muted-foreground">
                                                    {module.description}
                                                </p>
                                                {syncStatus.lastSync && (
                                                    <p className="text-xs text-muted-foreground">
                                                    Última sincronización: {formatRelative(syncStatus.lastSync)}
                                                </p>
                                                )}
                                            </div>
                                        </div>
                                        <div className="text-right">
                                            {getStatusBadge(moduleStatus)}
                                            {moduleStatus === 'ERROR' && syncStatus.lastError && (
                                                <p className="text-xs text-red-600 mt-1">
                                                    {syncStatus.lastError}
                                                </p>
                                            )}
                                        </div>
                                    </div>
                                );
                            })}
                        </div>

                        {/* Próxima sincronización */}
                        {syncStatus.nextSync && (
                            <div className="pt-4 border-t">
                                <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                                    <div className="flex items-center gap-2">
                                        <Clock className="h-4 w-4 text-gray-500" />
                                        <span className="text-sm font-medium">Próxima sincronización automática</span>
                                    </div>
                                    <span className="text-sm font-medium">
                                        {formatDateTime(syncStatus.nextSync)}
                                    </span>
                                </div>
                            </div>
                        )}
                    </div>
                )}

                {/* Sin datos */}
                {!error && !syncStatus && (
                    <div className="text-center py-8">
                        <AlertCircle className="h-8 w-8 mx-auto text-muted-foreground mb-2" />
                        <p className="text-muted-foreground">No hay datos de sincronización disponibles</p>
                    </div>
                )}

                {/* Última actualización */}
                <div className="pt-4 border-t">
                    <p className="text-xs text-muted-foreground text-center">
                        Última actualización: {format(new Date(lastRefresh), "d MMM yyyy 'a las' HH:mm", { locale: es })}
                    </p>
                </div>
            </CardContent>
        </Card>
    );
};

export default SyncStatusDashboard;