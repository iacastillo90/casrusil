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
}

const DEFAULT_MIN_STOCK = 5;

const fetchInventory = async (): Promise<Product[]> => {
    const { data } = await api.get('/inventory/products');
    // Map backend fields to frontend expectations
    // Backend ProductResponse includes: id, name, sku, stock, minStock, price, category
    interface InventoryApiResponse {
        id: string | number;
        name: string;
        sku: string;
        currentStock: number;
        minStock: number;
        salePrice: number;
        category: string;
    }
    return data.map((item: InventoryApiResponse) => ({
        id: item.id.toString(),
        name: item.name,
        sku: item.sku,
        stock: item.currentStock,
        minStock: item.minStock ?? DEFAULT_MIN_STOCK, // Fallback to DEFAULT_MIN_STOCK if not provided
        price: item.salePrice,
        category: item.category,
    }));
};

export function useInventory() {
    return useQuery({
        queryKey: ['inventory'],
        queryFn: fetchInventory,
    });
}