import api from "@/lib/axios";
import { useAuthStore } from '../stores/auth.store';
import { LoginCredentials, RegisterData, AuthResponse } from '../types/auth.types';
import { UpdateCompanyRequest, CreateUserRequest } from '../types/auth-requests.types';

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
        // SEGURIDAD: el token viaja en una cookie httpOnly emitida por el backend
        // (Set-Cookie), NO accesible desde JavaScript. El frontend solo guarda el
        // estado no sensible (usuario, empresa) en memoria (Zustand, sin persistir token).
        const user = {
            id: data.userId,
            email: email,
            name: data.userName,
            // SEGURIDAD: usar el rol REAL devuelto por el backend, no un valor fijo.
            role: data.role,
        };

        useAuthStore.getState().setAuth(
            user,
            data.token, // se mantiene solo en memoria (excluido del persist)
            data.companyId,
            [] // companies array
        );
    },

    logout: async () => {
        try {
            // Revoca el token en el backend (blacklist) y borra la cookie httpOnly.
            await api.post('/auth/logout');
        } catch {
            // Aunque falle la llamada, limpiamos el estado local igualmente.
        }
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
