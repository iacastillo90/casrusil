import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { User, Company } from '../types/auth.types';

interface AuthState {
    user: User | null;
    token: string | null;
    isAuthenticated: boolean;
    companyId: string | null;
    companies: Company[];

    // Actions
    setAuth: (user: User, token: string, companyId?: string, companies?: Company[]) => void;
    setCompanies: (companies: Company[]) => void;
    switchCompany: (companyId: string) => void;
    logout: () => void;
}

export const useAuthStore = create<AuthState>()(
    persist(
        (set) => ({
            user: null,
            token: null,
            isAuthenticated: false,
            companyId: null,
            companies: [],

            setAuth: (user, token, companyId, companies) => set({
                user,
                token,
                isAuthenticated: true,
                companyId: companyId || null,
                companies: companies || []
            }),

            setCompanies: (companies) => set({ companies }),

            switchCompany: (companyId) => set({ companyId }),

            logout: () => set({
                user: null,
                token: null,
                isAuthenticated: false,
                companyId: null,
                companies: []
            }),
        }),
        {
            name: 'sii-erp-auth',
            // SEGURIDAD: Excluir el token del almacenamiento persistente.
            // El token JWT no debe vivir en localStorage (vulnerable a XSS).
            // Vive en Cookie con sameSite: 'strict' y se re-hidrata al montar.
            partialize: (state) => ({
                user: state.user,
                companyId: state.companyId,
                companies: state.companies,
                isAuthenticated: state.isAuthenticated,
                // token: EXCLUIDO intencionalmente por seguridad
            }),
        }
    )
);
