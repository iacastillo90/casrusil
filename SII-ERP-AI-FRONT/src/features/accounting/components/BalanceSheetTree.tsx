"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { ChevronDown, ChevronRight, Folder, FolderOpen, Calculator, TrendingUp, TrendingDown } from "lucide-react";
import { formatCLP } from "@/lib/formatters";
import { accountingService, AccountNode, BalanceSheet } from "../services/accounting.service";

interface BalanceSheetTreeProps {
    date?: Date;
    data?: BalanceSheet;
}

export const BalanceSheetTree = ({ date, data: externalData }: BalanceSheetTreeProps = {}) => {
    const [expandedNodes, setExpandedNodes] = useState<Set<string>>(new Set());

    const queryKey = date ? ['balance-sheet-tree', date.toISOString().split('T')[0]] : ['balance-sheet-tree'];
    
    const { data: accountTree, isLoading, error } = useQuery({
        queryKey,
        queryFn: async () => {
            // En producción, esto llamaría a accountingService.getBalanceSheetTree(date!)
            // Por ahora, devuelve datos mock estructurados
            const mockData: AccountNode[] = [
                {
                    code: '1',
                    name: 'Activo',
                    type: 'ASSET',
                    isLeaf: false,
                    children: [
                        {
                            code: '1.1',
                            name: 'Activo Corriente',
                            type: 'ASSET',
                            isLeaf: false,
                            children: [
                                { code: '1.1.1', name: 'Caja y Bancos', type: 'ASSET', isLeaf: true, balance: 5000000 },
                                { code: '1.1.2', name: 'Cuentas por Cobrar', type: 'ASSET', isLeaf: true, balance: 2500000 },
                                { code: '1.1.3', name: 'Inventarios', type: 'ASSET', isLeaf: true, balance: 1800000 },
                            ],
                        },
                        {
                            code: '1.2',
                            name: 'Activo No Corriente',
                            type: 'ASSET',
                            isLeaf: false,
                            children: [
                                { code: '1.2.1', name: 'Propiedades, Plantas y Equipos', type: 'ASSET', isLeaf: true, balance: 15000000 },
                                { code: '1.2.2', name: 'Activos Intangibles', type: 'ASSET', isLeaf: true, balance: 3000000 },
                            ],
                        },
                    ],
                },
                {
                    code: '2',
                    name: 'Pasivo',
                    type: 'LIABILITY',
                    isLeaf: false,
                    children: [
                        {
                            code: '2.1',
                            name: 'Pasivo Corriente',
                            type: 'LIABILITY',
                            isLeaf: false,
                            children: [
                                { code: '2.1.1', name: 'Proveedores', type: 'LIABILITY', isLeaf: true, balance: -1200000 },
                                { code: '2.1.2', name: 'Deudas a Corto Plazo', type: 'LIABILITY', isLeaf: true, balance: -3000000 },
                            ],
                        },
                        {
                            code: '2.2',
                            name: 'Pasivo No Corriente',
                            type: 'LIABILITY',
                            isLeaf: false,
                            children: [
                                { code: '2.2.1', name: 'Deudas a Largo Plazo', type: 'LIABILITY', isLeaf: true, balance: -8000000 },
                            ],
                        },
                    ],
                },
                {
                    code: '3',
                    name: 'Patrimonio',
                    type: 'EQUITY',
                    isLeaf: false,
                    children: [
                        { code: '3.1', name: 'Capital', type: 'EQUITY', isLeaf: true, balance: 10000000 },
                        { code: '3.2', name: 'Reservas', type: 'EQUITY', isLeaf: true, balance: 2500000 },
                    ],
                },
            ];
            return mockData;
        },
        enabled: !externalData, // Solo hacer query si no hay data externa
    });

    // Si hay data externa, usar esa
    const displayData = externalData ?? undefined;
    const displayTree = externalData ? [] : (accountTree ?? []);

    const toggleNode = (nodeId: string) => {
        const newExpanded = new Set(expandedNodes);
        if (newExpanded.has(nodeId)) {
            newExpanded.delete(nodeId);
        } else {
            newExpanded.add(nodeId);
        }
        setExpandedNodes(newExpanded);
    };

    const getTypeColor = (type: string) => {
        switch (type) {
            case 'ASSET': return 'text-green-600';
            case 'LIABILITY': return 'text-red-600';
            case 'EQUITY': return 'text-blue-600';
            case 'REVENUE': return 'text-violet-600';
            case 'EXPENSE': return 'text-orange-600';
            default: return '';
        }
    };

    const getTypeIcon = (type: string) => {
        switch (type) {
            case 'ASSET': return TrendingUp;
            case 'LIABILITY': return TrendingDown;
            default: return Calculator;
        }
    };

    const renderTreeNode = (node: AccountNode, level: number = 0) => {
        const hasChildren = node.children && node.children.length > 0;
        const isExpanded = expandedNodes.has(node.code);
        const Icon = hasChildren 
            ? (isExpanded ? FolderOpen : Folder)
            : getTypeIcon(node.type);
        
        const paddingLeft = level * 4;
        const totalBalance = node.balance ?? node.children?.reduce((sum: number, child: AccountNode) => 
            sum + (child.balance ?? 0), 0
        ) ?? 0;

        return (
            <div key={node.code}>
                {hasChildren ? (
                    <>
                        <div 
                            className="flex items-center py-2 px-3 hover:bg-gray-50 cursor-pointer rounded"
                            style={{ paddingLeft: `${paddingLeft + 8}px` }}
                            onClick={() => toggleNode(node.code)}
                        >
                            <ChevronRight className={`h-4 w-4 mr-1 transition-transform ${isExpanded ? 'rotate-90' : ''}`} />
                            <Folder className={`h-4 w-4 mr-2 ${getTypeColor(node.type)}`} />
                            <span className="font-medium mr-2">{node.name}</span>
                            <span className="text-xs text-muted-foreground ml-auto">
                                {formatCLP(Math.abs(totalBalance), false)}
                            </span>
                        </div>
                        {isExpanded && node.children && (
                            <div className="border-l border-gray-200">
                                {node.children.map((child: AccountNode) => renderTreeNode(child, level + 1))}
                            </div>
                        )}
                    </>
                ) : (
                    <div 
                        className="flex items-center py-2 px-3 hover:bg-gray-50 rounded"
                        style={{ paddingLeft: `${paddingLeft + 16}px` }}
                    >
                        <div className="w-4 mr-2" />
                        <Icon className={`h-4 w-4 mr-2 ${getTypeColor(node.type)}`} />
                        <span className="text-sm mr-2">{node.name}</span>
                        <span className="text-xs text-muted-foreground ml-auto">
                            {formatCLP(Math.abs(node.balance ?? 0), false)}
                        </span>
                    </div>
                )}
            </div>
        );
    };

    // Calcular totales
    const calculateTotal = (node: AccountNode): number => {
        if (node.isLeaf && node.balance !== undefined) {
            return node.balance;
        }
        if (node.children) {
            return node.children.reduce((sum: number, child: AccountNode) => sum + calculateTotal(child), 0);
        }
        return 0;
    };

    // Calcular totales del balance sheet externo o del árbol
    let totalAssets = 0, totalLiabilities = 0, totalEquity = 0;
    
    if (displayData) {
        totalAssets = displayData.totalAssets;
        totalLiabilities = displayData.totalLiabilities;
        totalEquity = displayData.totalEquity;
    } else if (displayTree) {
        const assetsNode = displayTree.find(a => a.code === '1');
        const liabilitiesNode = displayTree.find(a => a.code === '2');
        const equityNode = displayTree.find(a => a.code === '3');
        
        totalAssets = assetsNode ? calculateTotal(assetsNode) : 0;
        totalLiabilities = liabilitiesNode ? calculateTotal(liabilitiesNode) : 0;
        totalEquity = equityNode ? calculateTotal(equityNode) : 0;
    }

    if (isLoading || !displayTree) {
        return (
            <Card>
                <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                        <Calculator className="h-5 w-5" />
                        Árbol de Cuentas (Balance)
                    </CardTitle>
                </CardHeader>
                <CardContent>
                    {isLoading ? (
                        <div className="animate-pulse space-y-4">
                            {[...Array(6)].map((_, i) => (
                                <div key={i} className="h-8 bg-gray-200 rounded"></div>
                            ))}
                        </div>
                    ) : (
                        <div className="text-center py-8 text-muted-foreground">
                            No hay datos de balance para mostrar
                        </div>
                    )}
                </CardContent>
            </Card>
        );
    }

    if (error) {
        return (
            <Card>
                <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                        <Calculator className="h-5 w-5" />
                        Árbol de Cuentas (Balance)
                    </CardTitle>
                </CardHeader>
                <CardContent>
                    <p className="text-red-600 text-sm">Error al cargar el árbol de cuentas</p>
                </CardContent>
            </Card>
        );
    }

    return (
        <Card className="w-full">
            <CardHeader>
                <CardTitle className="flex items-center gap-2">
                    <Calculator className="h-5 w-5" />
                    Árbol de Cuentas (Balance)
                </CardTitle>
                <CardDescription>
                    Estructura jerárquica de cuentas con su balance actual
                </CardDescription>
            </CardHeader>

            <CardContent>
                {/* Totales resumen */}
                <div className="grid grid-cols-3 gap-4 mb-4 p-4 bg-gray-50 rounded-lg">
                    <div className="text-center">
                        <p className="text-xs text-muted-foreground">Activos</p>
                        <p className="font-semibold text-green-600">{formatCLP(Math.abs(totalAssets), false)}</p>
                    </div>
                    <div className="text-center">
                        <p className="text-xs text-muted-foreground">Pasivo</p>
                        <p className="font-semibold text-red-600">{formatCLP(Math.abs(totalLiabilities), false)}</p>
                    </div>
                    <div className="text-center">
                        <p className="text-xs text-muted-foreground">Patrimonio</p>
                        <p className="font-semibold text-blue-600">{formatCLP(Math.abs(totalEquity), false)}</p>
                    </div>
                </div>

                {/* Verificar equilibrio */}
                <div className="flex justify-between items-center mb-3 text-sm">
                    <span className="text-muted-foreground">
                        Verificación: {Math.abs(totalAssets + totalLiabilities + totalEquity) < 1 ? 
                            '✓ Equilibrio correcto' : 
                            'Desbalance detectado'
                        }
                    </span>
                    <Button variant="ghost" size="sm">
                        Expandir todo
                    </Button>
                </div>

                {/* Árbol de cuentas */}
                {displayTree && displayTree.length > 0 && (
                    <div className="border rounded-lg overflow-hidden">
                        <div className="bg-gray-50 border-b p-2 font-medium">
                            {displayTree.map((node: AccountNode) => renderTreeNode(node))}
                        </div>
                    </div>
                )}

                {(!displayTree || displayTree.length === 0) && (
                    <div className="text-center py-8 text-muted-foreground">
                        No hay cuentas disponibles
                    </div>
                )}
            </CardContent>
        </Card>
    );
};

export default BalanceSheetTree;