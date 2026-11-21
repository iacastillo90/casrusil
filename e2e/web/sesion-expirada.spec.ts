import { expect, test } from '@playwright/test';
import { users } from './helpers';

/**
 * Sesión expirada: cookie inválida + store persistido con usuario.
 * El SessionGuard valida contra /auth/me (o /company/profile) y debe volver a
 * /login con el aviso visible.
 */
test('sesión expirada vuelve a /login con aviso', async ({ page, context }) => {
  const { admin, companyId } = users();

  await context.addCookies([
    { name: 'access_token', value: 'token-invalido-e2e', domain: 'localhost', path: '/' },
  ]);
  await context.addInitScript(
    ([user, company]) => {
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
