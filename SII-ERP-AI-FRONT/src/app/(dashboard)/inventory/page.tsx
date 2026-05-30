"use client";

import { useState } from "react";
import { Package, AlertTriangle, ArrowUpRight, ArrowDownRight, Search, Plus, Boxes } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { formatCLP } from "@/lib/formatters";

// Mock Data
const MOCK_INVENTORY = [
    { id: "INV-001", name: "Laptop ThinkPad T14", sku: "TECH-TP14", stock: 12, minStock: 5, price: 850000, category: "Electrónica" },
    { id: "INV-002", name: "Monitor Dell 27'", sku: "TECH-MD27", stock: 3, minStock: 10, price: 250000, category: "Electrónica" },
    { id: "INV-003", name: "Silla Ergonómica Pro", sku: "FURN-SEP", stock: 45, minStock: 15, price: 120000, category: "Mobiliario" },
    { id: "INV-004", name: "Escritorio Elevable", sku: "FURN-EE", stock: 8, minStock: 5, price: 350000, category: "Mobiliario" },
    { id: "INV-005", name: "MacBook Pro 16", sku: "TECH-MBP16", stock: 2, minStock: 3, price: 2150000, category: "Electrónica" },
];

export default function InventoryPage() {
    const [searchTerm, setSearchTerm] = useState("");

    const filteredItems = MOCK_INVENTORY.filter(item => 
        item.name.toLowerCase().includes(searchTerm.toLowerCase()) || 
        item.sku.toLowerCase().includes(searchTerm.toLowerCase())
    );

    const totalValue = MOCK_INVENTORY.reduce((acc, curr) => acc + (curr.stock * curr.price), 0);
    const lowStockCount = MOCK_INVENTORY.filter(i => i.stock <= i.minStock).length;

    return (
        <div className="space-y-6 p-6 animate-in fade-in duration-500">
            <div className="flex justify-between items-center">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight flex items-center gap-2">
                        <Boxes className="h-8 w-8 text-indigo-600" />
                        Control de Inventario
                    </h1>
                    <p className="text-muted-foreground mt-1">Gestión de stock, bodegas y valorización.</p>
                </div>
                <div className="flex gap-3">
                    <Button variant="outline" className="gap-2">
                        <ArrowDownRight className="w-4 h-4" /> Entrada
                    </Button>
                    <Button variant="outline" className="gap-2">
                        <ArrowUpRight className="w-4 h-4" /> Salida
                    </Button>
                    <Button className="gap-2 bg-indigo-600 hover:bg-indigo-700">
                        <Plus className="w-4 h-4" /> Nuevo Producto
                    </Button>
                </div>
            </div>

            {/* KPIs */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <Card className="border-l-4 border-l-indigo-500 shadow-sm">
                    <CardHeader className="pb-2">
                        <CardTitle className="text-sm font-medium text-slate-500">Valorización Total</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <div className="text-3xl font-bold text-slate-900">{formatCLP(totalValue)}</div>
                        <p className="text-xs text-emerald-600 mt-1 flex items-center">
                            <ArrowUpRight className="w-3 h-3 mr-1" /> +2.4% vs mes anterior
                        </p>
                    </CardContent>
                </Card>

                <Card className="border-l-4 border-l-emerald-500 shadow-sm">
                    <CardHeader className="pb-2">
                        <CardTitle className="text-sm font-medium text-slate-500">Total Productos</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <div className="text-3xl font-bold text-slate-900">{MOCK_INVENTORY.length}</div>
                        <p className="text-xs text-slate-500 mt-1">SKUs activos en bodega</p>
                    </CardContent>
                </Card>

                <Card className="border-l-4 border-l-amber-500 shadow-sm bg-amber-50/30">
                    <CardHeader className="pb-2">
                        <CardTitle className="text-sm font-medium text-amber-700 flex items-center gap-2">
                            <AlertTriangle className="w-4 h-4" /> Alertas de Stock
                        </CardTitle>
                    </CardHeader>
                    <CardContent>
                        <div className="text-3xl font-bold text-amber-700">{lowStockCount}</div>
                        <p className="text-xs text-amber-600 mt-1">Productos bajo el mínimo sugerido</p>
                    </CardContent>
                </Card>
            </div>

            {/* Data Table */}
            <Card className="shadow-sm">
                <div className="p-4 border-b border-slate-100 flex justify-between items-center bg-slate-50/50">
                    <div className="relative w-96">
                        <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-slate-400" />
                        <Input
                            placeholder="Buscar por nombre o SKU..."
                            className="pl-9 bg-white"
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                        />
                    </div>
                    <Badge variant="secondary" className="font-mono">
                        Bodega Principal
                    </Badge>
                </div>
                <div className="overflow-x-auto">
                    <table className="w-full text-sm text-left">
                        <thead className="text-xs text-slate-500 uppercase bg-slate-50 border-b border-slate-100">
                            <tr>
                                <th className="px-6 py-4 font-semibold">Producto</th>
                                <th className="px-6 py-4 font-semibold">SKU</th>
                                <th className="px-6 py-4 font-semibold">Categoría</th>
                                <th className="px-6 py-4 font-semibold text-right">Stock</th>
                                <th className="px-6 py-4 font-semibold text-right">Precio Unit.</th>
                                <th className="px-6 py-4 font-semibold text-right">Valor Total</th>
                                <th className="px-6 py-4 font-semibold text-center">Estado</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100">
                            {filteredItems.map((item) => (
                                <tr key={item.id} className="hover:bg-slate-50/80 transition-colors">
                                    <td className="px-6 py-4 font-medium text-slate-900 flex items-center gap-3">
                                        <div className="w-8 h-8 rounded bg-indigo-100 flex items-center justify-center text-indigo-600">
                                            <Package className="w-4 h-4" />
                                        </div>
                                        {item.name}
                                    </td>
                                    <td className="px-6 py-4 font-mono text-xs text-slate-500">{item.sku}</td>
                                    <td className="px-6 py-4 text-slate-600">{item.category}</td>
                                    <td className="px-6 py-4 text-right font-semibold">
                                        <span className={item.stock <= item.minStock ? "text-amber-600" : "text-slate-700"}>
                                            {item.stock} und
                                        </span>
                                    </td>
                                    <td className="px-6 py-4 text-right text-slate-600">{formatCLP(item.price)}</td>
                                    <td className="px-6 py-4 text-right font-medium text-slate-900">{formatCLP(item.stock * item.price)}</td>
                                    <td className="px-6 py-4 text-center">
                                        {item.stock <= item.minStock ? (
                                            <Badge variant="outline" className="bg-amber-50 text-amber-700 border-amber-200">
                                                Reabastecer
                                            </Badge>
                                        ) : (
                                            <Badge variant="outline" className="bg-emerald-50 text-emerald-700 border-emerald-200">
                                                Óptimo
                                            </Badge>
                                        )}
                                    </td>
                                </tr>
                            ))}
                            {filteredItems.length === 0 && (
                                <tr>
                                    <td colSpan={7} className="px-6 py-12 text-center text-slate-500">
                                        No se encontraron productos que coincidan con la búsqueda.
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </Card>
        </div>
    );
}
