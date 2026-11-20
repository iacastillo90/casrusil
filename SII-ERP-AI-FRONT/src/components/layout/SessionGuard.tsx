'use client';

import { useEffect, useRef } from 'react';

import { api } from '@/lib/api';
import { authService } from '@/features/auth/services/auth.service';
import { useAuthStore } from '@/features/auth/stores/auth.store';
import type { User } from '@/features/auth/types/auth.types';

/**
 * Valida la sesión al montar el dashboard.
 *
 * <p>
 * La cookie de sesión es httpOnly, así que el front no puede leer el JWT. El
 * backend no expone un endpoint de "usuario actual" (sólo login/register), por
 * lo que la identidad se conserva desde el store persistido y la validez se
 * comprueba con un endpoint autenticado (`GET /company/profile`).
 * </p>
 *
 * <p>
 * Si la sesión expiró, se marca `sessionExpired` y se redirige a /login para
 * que el formulario muestre el aviso.
 * </p>
 */
export function SessionGuard() {
    const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
    const validated = useRef(false);

    useEffect(() => {
        // El store se rehidrata después del primer render: se espera a que
        // isAuthenticated sea true y se valida una sola vez por montaje.
        if (!isAuthenticated || validated.current) {
            return;
        }
        validated.current = true;

        let cancelled = false;

        // `/auth/me` todavía no existe en el backend; cuando esté disponible se
        // usa para reconstruir la identidad sin depender del store. Si responde
        // 404 se cae al chequeo de sesión por /company/profile.
        api.get<Partial<User> & { companyId?: string }>('/auth/me')
            .then(({ data }) => {
                if (cancelled || !data?.id) return;
                const current = useAuthStore.getState();
                current.setAuth(
                    {
                        id: data.id,
                        email: data.email ?? current.user?.email ?? '',
                        name: data.name ?? current.user?.name ?? '',
                        role: data.role ?? current.user?.role ?? 'USER',
                    },
                    current.token ?? '',
                    data.companyId ?? current.companyId ?? undefined,
                    current.companies,
                );
            })
            .catch(() => undefined);

        api.get('/company/profile')
            .catch(async () => {
                if (cancelled) return;
                useAuthStore.getState().expireSession();
                // El backend borra la cookie httpOnly; sin esto el middleware
                // seguiría considerando la sesión válida y rebotaría /login.
                await authService.logout().catch(() => undefined);
                if (typeof window !== 'undefined' && !window.location.pathname.startsWith('/login')) {
                    window.location.href = '/login?expired=1';
                }
            });

        return () => {
            cancelled = true;
        };
    }, [isAuthenticated]);

    return null;
}

export default SessionGuard;
