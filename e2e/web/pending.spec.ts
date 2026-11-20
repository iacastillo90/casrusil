import { test } from '@playwright/test';
import { login, users } from './helpers';

// Pendientes hasta que el agente C integre las pantallas.
test.fixme('invoices/new muestra el formulario de factura', async ({ page }) => {
  const { admin } = users();
  await login(page, admin.email, admin.password);
  await page.goto('/invoices/new');
  await page.getByRole('heading', { name: /Nueva Factura/i }).waitFor();
  await page.getByLabel(/RUT Receptor/i).fill('76123456-7');
});

test.fixme('invoices/[id] muestra el detalle de la factura', async ({ page }) => {
  const { admin } = users();
  await login(page, admin.email, admin.password);
  await page.goto('/invoices/00000000-0000-0000-0000-000000000000');
  await page.getByRole('heading', { name: /Factura/i }).waitFor();
});

test.fixme('sii/caf permite subir un archivo CAF', async ({ page }) => {
  const { admin } = users();
  await login(page, admin.email, admin.password);
  await page.goto('/sii/caf');
  await page.getByLabel(/CAF|archivo/i).setInputFiles({ name: 'dummy.caf.xml', mimeType: 'application/xml', buffer: Buffer.from('<CAF/>') });
});
