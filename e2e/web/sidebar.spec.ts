import { expect, test } from '@playwright/test';
import { login, users } from './helpers';

test('ADMIN ve todos los grupos del Sidebar', async ({ page }) => {
  const { admin } = users();
  await login(page, admin.email, admin.password);

  await expect(page.getByRole('button', { name: /Contabilidad/i })).toBeVisible();
  await expect(page.getByRole('button', { name: /Tesorería y bancos/i })).toBeVisible();
  await expect(page.getByRole('button', { name: /Configuración/i })).toBeVisible();
});

/**
 * Hoy SALES vuelve a /login?expired=1 apenas entra: SessionGuard.tsx:58-69
 * valida la sesión con GET /company/profile, que es solo ADMIN
 * (CompanyController.java:30), y trata el 403 como sesión expirada. Cualquier
 * rol que no sea ADMIN queda fuera de la app.
 * Bug: https://github.com/iacastillo90/casrusil/issues/41
 */
test('SALES solo ve Inicio, Ventas y Módulos', async ({ page }) => {
  test.fail(true, 'Bug del producto: https://github.com/iacastillo90/casrusil/issues/41');
  const { sales } = users();
  await login(page, sales.email, sales.password);

  await expect(page.getByRole('button', { name: /Ventas/i })).toBeVisible();
  await expect(page.getByRole('button', { name: /Módulos/i })).toBeVisible();
  await expect(page.getByRole('button', { name: /Contabilidad/i })).toHaveCount(0);
  await expect(page.getByRole('button', { name: /Configuración/i })).toHaveCount(0);
});
