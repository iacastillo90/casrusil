import { expect, test } from '@playwright/test';
import { users } from './helpers';

/**
 * Sesión expirada: cookie inválida + store persistido con usuario.
 * El SessionGuard valida contra /auth/me (o /company/profile) y debe volver a
 * /login con el aviso visible.
 */
/*
 * Hoy el aviso no aparece: con la cookie inválida, la primera petición que
 * responde 401 (p. ej. GET /sii/status del header) dispara el interceptor de
 * lib/axios.ts:43-47, que llama logout() (no expireSession()) y redirige a
 * /login abortando las peticiones del SessionGuard. sessionExpired nunca se
 * marca y LoginForm no muestra el toast.
 * Bug: https://github.com/iacastillo90/casrusil/issues/42
 */
test('sesión expirada vuelve a /login con aviso', async ({ page, context }) => {
  test.fail(true, 'Bug del producto: https://github.com/iacastillo90/casrusil/issues/42');
  const { admin, companyId } = users();

  await context.addCookies([
    { name: 'access_token', value: 'token-invalido-e2e', domain: 'localhost', path: '/' },
  ]);
  await context.addInitScript(
    ([user, company]) => {
      // addInitScript corre en CADA navegación, también en /login después de
      // la redirección: sin esta guarda volvía a escribir isAuthenticated=true
      // y sessionExpired=false encima de lo que dejó el SessionGuard, y el
      // aviso nunca aparecía. Se siembra una sola vez por pestaña.
      if (window.sessionStorage.getItem('e2e-sembrado')) return;
      window.sessionStorage.setItem('e2e-sembrado', '1');
      window.localStorage.setItem(
        'sii-erp-auth',
        JSON.stringify({
          state: { user, companyId: company, companies: [], isAuthenticated: true, sessionExpired: false },
          version: 0,
        }),
      );
    },
    [
      { id: 'e2e', email: admin.email, name: admin.email, role: admin.role ?? 'ADMIN' },
      companyId,
    ] as const,
  );

  await page.goto('/inventory', { waitUntil: 'domcontentloaded' });

  // El interceptor de axios puede redirigir antes de que el guard agregue el
  // query param; lo que importa es volver a /login y ver el aviso.
  await expect(page).toHaveURL(/\/login/, { timeout: 60_000 });
  await expect(page.getByText('Tu sesión expiró')).toBeVisible({ timeout: 20_000 });
});
