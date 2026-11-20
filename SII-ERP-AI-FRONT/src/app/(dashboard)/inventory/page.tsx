"use client";

import { useMemo, useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { AlertTriangle, ArrowDownRight, ArrowUpRight, Boxes, Package, Plus, Search } from "lucide-react";
import { toast } from "sonner";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { PageHeader } from "@/features/accounting/components/PageHeader";
import { EmptyState, ErrorState, TableSkeleton } from "@/features/accounting/components/DataStates";
import { useInventory, type Product } from "@/features/inventory/hooks/useInventory";
import { api } from "@/lib/api";
import { formatCLP } from "@/lib/formatters";

type MovementType = "ENTRADA" | "SALIDA";

export default function InventoryPage() {
    const [searchTerm, setSearchTerm] = useState("");
    const [movementOpen, setMovementOpen] = useState(false);
    const [movementType, setMovementType] = useState<MovementType>("ENTRADA");
    const [productOpen, setProductOpen] = useState(false);
    const [selectedProductId, setSelectedProductId] = useState<string>("");
    const [quantity, setQuantity] = useState("1");
    const [newProduct, setNewProduct] = useState({ sku: "", name: "", category: "", salePrice: "", minStock: "5" });

    const queryClient = useQueryClient();
    const { data, isLoading, isFetching, error, refetch } = useInventory();
    const inventory = useMemo(() => data ?? [], [data]);

    const movement = useMutation({
        mutationFn: async () => {
            if (!selectedProductId) throw new Error("Selecciona un producto");
            const value = Number(quantity);
            if (!Number.isFinite(value) || value <= 0) throw new Error("La cantidad debe ser mayor a 0");
            await api.post(`/inventory/products/${selectedProductId}/movements`, {
                productId: Number(selectedProductId),
                type: movementType,
                quantity: value,
            });
        },
        onSuccess: async () => {
            toast.success(`Movimiento de ${movementType.toLowerCase()} registrado`);
            setMovementOpen(false);
            setQuantity("1");
            await queryClient.invalidateQueries({ queryKey: ["inventory"] });
        },
        onError: (mutationError: Error) => toast.error(mutationError.message),
    });

    const createProduct = useMutation({
        mutationFn: async () => {
            const price = Number(newProduct.salePrice);
            if (!newProduct.sku.trim() || !newProduct.name.trim()) {
                throw new Error("SKU y nombre son obligatorios");
            }
            if (!Number.isFinite(price) || price <= 0) throw new Error("El precio de venta debe ser mayor a 0");
            await api.post("/inventory/products", {
                sku: newProduct.sku.trim(),
                name: newProduct.name.trim(),
                category: newProduct.category.trim() || undefined,
                salePrice: price,
                minStock: Number(newProduct.minStock) || 0,
            });
        },
        onSuccess: async () => {
            toast.success("Producto creado");
            setProductOpen(false);
            setNewProduct({ sku: "", name: "", category: "", salePrice: "", minStock: "5" });
            await queryClient.invalidateQueries({ queryKey: ["inventory"] });
        },
        onError: (mutationError: Error) => toast.error(mutationError.message),
    });

    const filteredItems = useMemo(
        () =>
            inventory.filter(
                (item: Product) =>
                    item.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                    item.sku.toLowerCase().includes(searchTerm.toLowerCase()),
            ),
        [inventory, searchTerm],
    );

    const totalValue = inventory.reduce((acc, item) => acc + item.stock * item.cost, 0);
    const lowStockCount = inventory.filter((item) => item.stock <= item.minStock).length;

    return (
        <div className="space-y-6">
            <PageHeader
                title="Control de inventario"
                description="Stock, valorización al costo promedio y movimientos por producto."
                actions={
                    <>
                        <Button
                            variant="outline"
                            onClick={() => {
                                setMovementType("ENTRADA");
                                setSelectedProductId(inventory[0]?.id ?? "");
                                setMovementOpen(true);
                            }}
                            disabled={inventory.length === 0}
                        >
                            <ArrowDownRight className="mr-2 h-4 w-4" /> Entrada
                        </Button>
                        <Button
                            variant="outline"
                            onClick={() => {
                                setMovementType("SALIDA");
                                setSelectedProductId(inventory[0]?.id ?? "");
                                setMovementOpen(true);
                            }}
                            disabled={inventory.length === 0}
                        >
                            <ArrowUpRight className="mr-2 h-4 w-4" /> Salida
                        </Button>
                        <Button onClick={() => setProductOpen(true)}>
                            <Plus className="mr-2 h-4 w-4" /> Nuevo producto
                        </Button>
                    </>
                }
            />

            {error ? (
                <ErrorState
                    message={error instanceof Error ? error.message : "Error al cargar el inventario"}
                    onRetry={() => refetch()}
                    retrying={isFetching && !isLoading}
                />
            ) : (
                <>
                    <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
                        <Card>
                            <CardHeader className="pb-2">
                                <CardTitle className="text-sm font-medium text-muted-foreground">
                                    Valorización al costo
                                </CardTitle>
                            </CardHeader>
                            <CardContent>
                                <div className="text-3xl font-semibold">{formatCLP(totalValue)}</div>
                                <p className="mt-1 text-xs text-muted-foreground">Stock × costo promedio</p>
                            </CardContent>
                        </Card>
                        <Card>
                            <CardHeader className="pb-2">
                                <CardTitle className="text-sm font-medium text-muted-foreground">Productos</CardTitle>
                            </CardHeader>
                            <CardContent>
                                <div className="text-3xl font-semibold">{inventory.length}</div>
                                <p className="mt-1 text-xs text-muted-foreground">SKUs registrados</p>
                            </CardContent>
                        </Card>
                        <Card>
                            <CardHeader className="flex flex-row items-center justify-between pb-2">
                                <CardTitle className="text-sm font-medium text-muted-foreground">
                                    Bajo el mínimo
                                </CardTitle>
                                <AlertTriangle className="h-4 w-4 text-warning" aria-hidden="true" />
                            </CardHeader>
                            <CardContent>
                                <div className="text-3xl font-semibold">{lowStockCount}</div>
                                <p className="mt-1 text-xs text-muted-foreground">Requieren reposición</p>
                            </CardContent>
                        </Card>
                    </div>

                    <Card>
                        <div className="flex flex-col gap-3 border-b p-4 md:flex-row md:items-center md:justify-between">
                            <div className="relative w-full md:w-96">
                                <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
                                <Input
                                    placeholder="Buscar por nombre o SKU…"
                                    className="pl-9"
                                    value={searchTerm}
                                    onChange={(event) => setSearchTerm(event.target.value)}
                                />
                            </div>
                            <Badge variant="secondary">Bodega principal</Badge>
                        </div>

                        {isLoading ? (
                            <div className="p-4">
                                <TableSkeleton rows={6} cols={6} />
                            </div>
                        ) : filteredItems.length === 0 ? (
                            <EmptyState
                                title={inventory.length === 0 ? "Sin productos" : "Sin resultados"}
                                description={
                                    inventory.length === 0
                                        ? "Crea el primer producto para comenzar a controlar el stock."
                                        : "Ningún producto coincide con la búsqueda."
                                }
                                icon={Boxes}
                                action={
                                    inventory.length === 0 ? (
                                        <Button onClick={() => setProductOpen(true)}>
                                            <Plus className="mr-2 h-4 w-4" /> Nuevo producto
                                        </Button>
                                    ) : undefined
                                }
                            />
                        ) : (
                            <div className="overflow-x-auto">
                                <table className="w-full text-left text-sm">
                                    <thead className="border-b bg-muted/40 text-xs uppercase text-muted-foreground">
                                        <tr>
                                            <th className="px-6 py-3 font-semibold">Producto</th>
                                            <th className="px-6 py-3 font-semibold">SKU</th>
                                            <th className="px-6 py-3 font-semibold">Categoría</th>
                                            <th className="px-6 py-3 text-right font-semibold">Stock</th>
                                            <th className="px-6 py-3 text-right font-semibold">Costo</th>
                                            <th className="px-6 py-3 text-right font-semibold">Precio venta</th>
                                            <th className="px-6 py-3 text-right font-semibold">Valor</th>
                                            <th className="px-6 py-3 text-center font-semibold">Estado</th>
                                        </tr>
                                    </thead>
                                    <tbody className="divide-y">
                                        {filteredItems.map((item) => (
                                            <tr key={item.id} className="hover:bg-muted/30">
                                                <td className="flex items-center gap-3 px-6 py-4 font-medium">
                                                    <div className="flex h-8 w-8 items-center justify-center rounded bg-primary/10 text-primary">
                                                        <Package className="h-4 w-4" aria-hidden="true" />
                                                    </div>
                                                    {item.name}
                                                </td>
                                                <td className="px-6 py-4 font-mono text-xs text-muted-foreground">
                                                    {item.sku}
                                                </td>
                                                <td className="px-6 py-4 text-muted-foreground">{item.category || "—"}</td>
                                                <td className="px-6 py-4 text-right font-semibold">{item.stock}</td>
                                                <td className="px-6 py-4 text-right">{formatCLP(item.cost)}</td>
                                                <td className="px-6 py-4 text-right">{formatCLP(item.price)}</td>
                                                <td className="px-6 py-4 text-right font-medium">
                                                    {formatCLP(item.stock * item.cost)}
                                                </td>
                                                <td className="px-6 py-4 text-center">
                                                    {item.stock <= item.minStock ? (
                                                        <Badge variant="outline" className="border-warning/40 bg-warning-subtle text-warning-subtle-foreground">
                                                            Reabastecer
                                                        </Badge>
                                                    ) : (
                                                        <Badge variant="outline" className="border-success/40 bg-success-subtle text-success-subtle-foreground">
                                                            Óptimo
                                                        </Badge>
                                                    )}
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </Card>
                </>
            )}

            <Dialog open={movementOpen} onOpenChange={setMovementOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Registrar {movementType.toLowerCase()}</DialogTitle>
                    </DialogHeader>
                    <div className="space-y-4">
                        <div className="space-y-2">
                            <Label htmlFor="movement-product">Producto</Label>
                            <Select value={selectedProductId} onValueChange={setSelectedProductId}>
                                <SelectTrigger id="movement-product">
                                    <SelectValue placeholder="Selecciona un producto" />
                                </SelectTrigger>
                                <SelectContent>
                                    {inventory.map((item) => (
                                        <SelectItem key={item.id} value={item.id}>
                                            {item.name} ({item.sku})
                                        </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                        </div>
                        <div className="space-y-2">
                            <Label htmlFor="movement-quantity">Cantidad</Label>
                            <Input
                                id="movement-quantity"
                                type="number"
                                min="1"
                                value={quantity}
                                onChange={(event) => setQuantity(event.target.value)}
                            />
                        </div>
                    </div>
                    <DialogFooter>
                        <Button variant="outline" onClick={() => setMovementOpen(false)}>
                            Cancelar
                        </Button>
                        <Button loading={movement.isPending} onClick={() => movement.mutate()}>
                            Registrar
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            <Dialog open={productOpen} onOpenChange={setProductOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Nuevo producto</DialogTitle>
                    </DialogHeader>
                    <div className="space-y-4">
                        <div className="space-y-2">
                            <Label htmlFor="product-sku">SKU</Label>
                            <Input
                                id="product-sku"
                                value={newProduct.sku}
                                onChange={(event) => setNewProduct({ ...newProduct, sku: event.target.value })}
                            />
                        </div>
                        <div className="space-y-2">
                            <Label htmlFor="product-name">Nombre</Label>
                            <Input
                                id="product-name"
                                value={newProduct.name}
                                onChange={(event) => setNewProduct({ ...newProduct, name: event.target.value })}
                            />
                        </div>
                        <div className="space-y-2">
                            <Label htmlFor="product-category">Categoría</Label>
                            <Input
                                id="product-category"
                                value={newProduct.category}
                                onChange={(event) => setNewProduct({ ...newProduct, category: event.target.value })}
                            />
                        </div>
                        <div className="grid grid-cols-2 gap-4">
                            <div className="space-y-2">
                                <Label htmlFor="product-price">Precio venta</Label>
                                <Input
                                    id="product-price"
                                    type="number"
                                    min="0"
                                    value={newProduct.salePrice}
                                    onChange={(event) => setNewProduct({ ...newProduct, salePrice: event.target.value })}
                                />
                            </div>
                            <div className="space-y-2">
                                <Label htmlFor="product-min">Stock mínimo</Label>
                                <Input
                                    id="product-min"
                                    type="number"
                                    min="0"
                                    value={newProduct.minStock}
                                    onChange={(event) => setNewProduct({ ...newProduct, minStock: event.target.value })}
                                />
                            </div>
                        </div>
                    </div>
                    <DialogFooter>
                        <Button variant="outline" onClick={() => setProductOpen(false)}>
                            Cancelar
                        </Button>
                        <Button loading={createProduct.isPending} onClick={() => createProduct.mutate()}>
                            Crear producto
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </div>
    );
}
