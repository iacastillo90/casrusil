import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { useAuthStore } from '@/features/auth/stores/auth.store';

// URL relativa: las peticiones salen al mismo origen del frontend y next.config.mjs
// las reenvía al backend (BACKEND_INTERNAL_URL). Así la cookie httpOnly del login
// viaja siempre en el mismo origen y no depende de CORS.
const BASE_URL = process.env.NEXT_PUBLIC_API_URL || '/api/v1';

// Create axios instance
const api = axios.create({
    baseURL: BASE_URL,
    // headers: {
    //     'Content-Type': 'application/json',
    // },
    withCredentials: true, // Importante para que coincida con el backend allowCredentials(true)
    timeout: 30000,
});

// Alias for backward compatibility if needed, but 'api' is the main export now.
export const apiClient = api;

// Request interceptor: companyId opcional (el token viaja en la cookie httpOnly,
// enviada automáticamente por el navegador gracias a withCredentials + proxy same-origin).
api.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        const { companyId } = useAuthStore.getState();

        if (companyId) {
            config.headers['X-Company-Id'] = companyId;
        }

        return config;
    },
    (error) => Promise.reject(error)
);

// Response interceptor: Handle 401 (auto logout)
api.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
        const { logout } = useAuthStore.getState();

        if (error.response?.status === 401) {
            logout();
            if (typeof window !== 'undefined') {
                window.location.href = '/login';
            }
        }

        // Format error message
        const payload = error.response?.data as { message?: string } | undefined;
        const message = payload?.message || error.message || 'Ocurrió un error inesperado';

        return Promise.reject(new Error(message));
    }
);

export default api;

// Helper types
export interface ApiResponse<T = unknown> {
    data: T;
    message?: string;
    success: boolean;
}

export interface ApiError {
    message: string;
    code?: string;
    details?: Record<string, unknown>;
}
