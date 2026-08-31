import { useQuery } from '@tanstack/react-query';
import api from '@/lib/axios';

export interface Product {
    id: string; // The ID is used as string in frontend typically, wait, backend has Long, let's use number or string
    sku: string;
    name: string;
    description?: string;
    currentStock: number;
    averageCost: number;
    salePrice: number;
    category: string;
}

const fetchInventory = async (): Promise<Product[]> => {
    const { data } = await api.get('/inventory/products');
    // Map backend fields to frontend expectations if necessary
    // MOCK_INVENTORY used: id, name, sku, stock, minStock, price, category
    return data.map((item: any) => ({
        id: item.id.toString(),
        name: item.name,
        sku: item.sku,
        stock: item.currentStock,
        minStock: 5, // We can default minStock or fetch from backend if added
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
