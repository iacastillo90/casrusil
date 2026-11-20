import { expect, test } from '@playwright/test';
import { login, users } from './helpers';

test('ADMIN ve todos los grupos del Sidebar', async ({ page }) => {
  const { admin } = users();
  await login(page, admin.email, admin.password);

  await expect(page.getByRole('button', { name: /Contabilidad/i })).toBeVisible();
  await expect(page.getByRole('button', { name: /Tesorería y bancos/i })).toBeVisible();
  await expect(page.getByRole('button', { name: /Configuración/i })).toBeVisible();
});

test('SALES solo ve Inicio, Ventas y Módulos', async ({ page }) => {
  const { sales } = users();
  await login(page, sales.email, sales.password);

  await expect(page.getByRole('button', { name: /Ventas/i })).toBeVisible();
  await expect(page.getByRole('button', { name: /Módulos/i })).toBeVisible();
  await expect(page.getByRole('button', { name: /Contabilidad/i })).toHaveCount(0);
  await expect(page.getByRole('button', { name: /Configuración/i })).toHaveCount(0);
});
