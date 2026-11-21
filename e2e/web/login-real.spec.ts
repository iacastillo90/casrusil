import { expect, test } from '@playwright/test';
import { users } from './helpers';

/**
 * Login real por UI, sin cookie preparada.
 *
 * <p>
 * Este test es deliberadamente estricto: escribe email y contraseña, envía el
 * formulario y exige llegar al dashboard. Hoy falla porque el backend rechaza
 * el preflight CORS de POST /auth/login (falta `X-Company-Id` en
 * `allowedHeaders`); debe pasar cuando el fix de CORS se integre.
 * </p>
 */
test('login real por UI llega al dashboard', async ({ page }) => {
  const { admin } = users();

  await page.goto('/login', { waitUntil: 'domcontentloaded' });
  await page.getByLabel('Correo Electrónico').fill(admin.email);
  await page.getByLabel('Contraseña').fill(admin.password);
  await page.getByRole('button', { name: /Iniciar Sesión/i }).click();

  await expect(page).toHaveURL(/\/dashboard/, { timeout: 90_000 });
  // El dashboard tiene más de un h1 (header + contenido): se valida el primero.
  await expect(page.locator('h1').first()).toBeVisible();
});
