import api from "@/lib/axios";
import { useAuthStore } from '../stores/auth.store';
import { LoginCredentials, RegisterData, AuthResponse } from '../types/auth.types';
import { UpdateCompanyRequest, CreateUserRequest } from '../types/auth-requests.types';
// CORRECCIÓN: importar desde el paquete real, no desde @types/
import Cookies from 'js-cookie';

export const authService = {
    login: async (credentials: LoginCredentials) => {
        const { data } = await api.post<AuthResponse>('/auth/login', credentials);
        authService.handleAuthSuccess(data, credentials.email);
        return data;
    },

    register: async (registerData: RegisterData) => {
        const { data } = await api.post<AuthResponse>('/auth/register', registerData);
        authService.handleAuthSuccess(data, registerData.adminEmail);
        return data;
    },

    handleAuthSuccess: (data: AuthResponse, email: string) => {
        if (data.token) {
            // SEGURIDAD: Cookie con flags seguros.
            // - secure: solo se envía por HTTPS (en prod)
            // - sameSite: 'strict' protege contra CSRF
            // - httpOnly: no puede ser accedida desde JS (protege contra XSS)
            // Nota: httpOnly solo puede setearse desde el servidor via Set-Cookie header.
            // Desde el cliente, usamos 'strict' + 'secure' como capa adicional.
            Cookies.set('token', data.token, {
                expires: 1,
                secure: process.env.NODE_ENV === 'production',
                sameSite: 'strict'
            });

            const user = {
                id: data.userId,
                email: email,
                name: data.userName,
                role: 'ADMIN' as const
            };

            useAuthStore.getState().setAuth(
                user,
                data.token,
                data.companyId,
                [] // companies array
            );
            // SEGURIDAD: No guardar datos de usuario en localStorage (vulnerable a XSS).
            // El estado se maneja en Zustand (sin incluir el token en el persist).
        }
    },

    logout: () => {
        Cookies.remove('token');
        useAuthStore.getState().logout();
        if (typeof window !== 'undefined') {
            window.location.href = '/login';
        }
    },

    // Company Management
    updateCompany: async (data: UpdateCompanyRequest) => {
        const { data: response } = await api.put('/companies/me', data);
        return response;
    },

    // User Management
    createUser: async (user: CreateUserRequest) => {
        const { data } = await api.post('/users', user);
        return data;
    },

    updateUserRole: async (id: string, role: string) => {
        const { data } = await api.put(`/users/${id}/role`, { role });
        return data;
    }
};
