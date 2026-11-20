import { useQuery } from '@tanstack/react-query';
import api from '@/lib/axios';

export interface Product {
    id: string;
    sku: string;
    name: string;
    description?: string;
    category: string;
    stock: number;
    minStock: number;
    price: number;
    /** Costo promedio (valorización contable). */
    cost: number;
}

const fetchInventory = async (): Promise<Product[]> => {
    const { data } = await api.get('/inventory/products');
    // Map backend fields to frontend expectations
    // Backend ProductResponse includes: id, name, sku, stock, minStock, price, category
    interface InventoryApiResponse {
        id: string | number;
        name: string;
        sku: string;
        currentStock: number;
        minStock: number | null;
        averageCost: number | null;
        salePrice: number;
        category: string;
    }
    return data.map((item: InventoryApiResponse) => ({
        id: item.id.toString(),
        name: item.name,
        sku: item.sku,
        stock: item.currentStock,
        minStock: item.minStock ?? 0,
        price: item.salePrice,
        cost: item.averageCost ?? 0,
        category: item.category,
    }));
};

export function useInventory() {
    return useQuery({
        queryKey: ['inventory'],
        queryFn: fetchInventory,
    });
}