import { expect, test } from '@playwright/test';
import { login, users } from './helpers';

// Flujos reales de C (facturación y CAF). Requieren CORS corregido para los
// POST; los GET y la navegación funcionan con sesión establecida.

test('invoices/new emite una factura por pasos', async ({ page }) => {
  const { admin } = users();
  await login(page, admin.email, admin.password);
  await page.goto('/invoices/new');
  await page.getByRole('heading', { name: /Nueva Factura|Nuevo documento/i }).waitFor();

  await page.getByLabel('RUT del receptor').fill('76123456-7');
  await page.getByLabel('Razón social del receptor').fill('Cliente E2E SpA');
  await page.getByRole('button', { name: /Siguiente/i }).click();

  await page.getByPlaceholder('Producto o servicio').fill('Servicio E2E');
  const numeros = page.locator('input[type=number]');
  await numeros.nth(0).fill('1');
  await numeros.nth(1).fill('1000');

  await page.getByRole('button', { name: /Siguiente/i }).click();
  await page.getByRole('button', { name: /Siguiente/i }).click();
  await page.getByRole('button', { name: /Emitir documento/i }).click();

  await expect(page.getByText(/folio/i).first()).toBeVisible({ timeout: 60_000 });
});

test('invoices/[id] muestra folio y estado del documento emitido', async ({ page }) => {
  const { admin } = users();
  await login(page, admin.email, admin.password);
  await page.goto('/invoices');
  await page.getByRole('row').nth(1).getByRole('link').first().click();

  await expect(page.getByRole('heading', { name: /Factura/i })).toBeVisible();
  await expect(page.getByText(/folio/i).first()).toBeVisible();
  await expect(page.getByText(/pendiente|enviado|aceptado|rechazado/i).first()).toBeVisible();
});

test('sii/caf permite subir un CAF y muestra el consumo', async ({ page }) => {
  const { admin } = users();
  await login(page, admin.email, admin.password);
  await page.goto('/sii/caf');

  const rango = `1-10`;
  const caf = `<AUTORIZACION><CAF version="1.0"><DA><RE>76123456-7</RE><RS>Cliente E2E SpA</RS><TD>33</TD><RNG><D>1</D><H>10</H></RNG><FA>2026-01-01</FA><RSAPK><M>MIIB</M><E>AQAB</E></RSAPK><IDK>100</IDK></DA><FRMA algoritmo="SHA1withRSA">AQAB</FRMA></CAF></AUTORIZACION>`;
  await page.getByLabel(/CAF|archivo/i).setInputFiles({
    name: 'caf-e2e.xml',
    mimeType: 'application/xml',
    buffer: Buffer.from(caf),
  });

  await expect(page.getByText(new RegExp(rango)).first()).toBeVisible({ timeout: 60_000 });
});
