import { generateKeyPairSync } from 'node:crypto';
import { expect, test } from '@playwright/test';
import { login, users } from './helpers';

/**
 * Flujo real de facturación de punta a punta, en orden: cargar CAF → emitir
 * factura → ver el detalle. Cada paso depende del anterior (sin CAF no hay
 * folio; sin factura emitida no hay detalle), por eso corre en serie.
 */
test.describe.serial('facturación con folios reales', () => {
  /**
   * CAF de prueba con una llave RSA real generada en cada corrida.
   *
   * <p>
   * Formato que acepta HOY CafParser: RSASK DENTRO de <DA> y en base64 PKCS#8
   * sin cabeceras PEM. No es el formato del SII (RSASK va fuera de <CAF>, en
   * PKCS#1 con "-----BEGIN RSA PRIVATE KEY-----"); ese caso lo cubre el test
   * marcado test.fail al final del archivo. Se usa este formato aquí para que
   * el resto del flujo (emitir y ver el detalle) quede cubierto mientras el
   * parser se corrige.
   * </p>
   */
  function cafXmlAceptadoHoy(rutEmisor: string) {
    const { privateKey } = generateKeyPairSync('rsa', { modulusLength: 1024 });
    const rsask = privateKey.export({ type: 'pkcs8', format: 'der' }).toString('base64');
    return `<?xml version="1.0" encoding="ISO-8859-1"?>
<AUTORIZACION><CAF version="1.0"><DA><RE>${rutEmisor}</RE><RS>QA E2E</RS><TD>33</TD><RNG><D>1</D><H>10</H></RNG><FA>2026-01-01</FA><RSAPK><M>AA==</M><E>AQAB</E></RSAPK><IDK>100</IDK><RSASK>${rsask}</RSASK></DA><FRMA algoritmo="SHA1withRSA">AA==</FRMA></CAF></AUTORIZACION>`;
  }

  test('sii/caf permite subir un CAF y muestra el consumo', async ({ page }) => {
    const { admin, companyRut } = users();

    await login(page, admin.email, admin.password);
    await page.goto('/sii/caf');
    await page.getByLabel(/Archivo XML/i).setInputFiles({
      name: 'caf-e2e.xml',
      mimeType: 'application/xml',
      buffer: Buffer.from(cafXmlAceptadoHoy(companyRut)),
    });
    const subida = page.waitForResponse((r) => r.url().endsWith('/api/v1/sii/caf') && r.request().method() === 'POST');
    await page.getByRole('button', { name: /Cargar CAF/i }).click();
    expect((await subida).status()).toBe(200);

    // Confirmación de la carga y fila del CAF con su rango y folio actual.
    await expect(page.getByText(/Se han cargado los folios desde el/).first()).toBeVisible();
    const fila = page.getByRole('row').filter({ hasText: /1\s*–\s*10/ });
    await expect(fila.first()).toBeVisible();
  });

  /**
   * El paso "Totales" no debe emitir: solo "Emitir documento" en "Revisar y
   * emitir" crea la factura, una sola vez.
   *
   * <p>
   * Hoy el tercer "Siguiente" ya hace POST /invoices: en InvoiceForm.tsx
   * :688-693 el botón "Siguiente" (type=button) y "Emitir documento"
   * (type=submit) ocupan la misma posición, React reutiliza el mismo <button>
   * y al pasar al último paso dentro del mismo click el navegador ejecuta el
   * submit. Resultado: se emite sin revisión y, al pulsar "Emitir", se emite
   * otra factura (dos folios consumidos por una venta).
   * Bug: https://github.com/iacastillo90/casrusil/issues/40
   * </p>
   */
  test('invoices/new emite una factura por pasos', async ({ page }) => {
    test.fail(true, 'Bug del producto: https://github.com/iacastillo90/casrusil/issues/40');
    const { admin } = users();
    const emisiones: string[] = [];
    page.on('request', (r) => {
      if (r.url().endsWith('/api/v1/invoices') && r.method() === 'POST') emisiones.push(r.url());
    });

    await login(page, admin.email, admin.password);
    await page.goto('/invoices/new');
    await expect(page.getByRole('heading', { name: 'Nueva Factura' })).toBeVisible();

    await page.getByLabel('RUT del receptor').fill('76123456-0');
    await page.getByLabel('Razón social del receptor').fill('Cliente E2E SpA');
    await page.getByRole('button', { name: /Siguiente/i }).click();

    await page.getByPlaceholder('Producto o servicio').fill('Servicio E2E');
    const numeros = page.locator('input[type=number]');
    await numeros.nth(0).fill('1');
    await numeros.nth(1).fill('1000');

    await page.getByRole('button', { name: /Siguiente/i }).click();
    await page.getByRole('button', { name: /Siguiente/i }).click();
    await expect(page.getByRole('heading', { name: 'Revisar y emitir' })).toBeVisible();
    // Se da tiempo a que una emisión indebida llegue a salir antes de medir.
    await page.waitForLoadState('networkidle');
    expect(emisiones, 'El paso Totales no debe emitir').toHaveLength(0);

    const emision = page.waitForResponse((r) => r.url().endsWith('/api/v1/invoices') && r.request().method() === 'POST');
    await page.getByRole('button', { name: /Emitir documento/i }).click();
    const respuesta = await emision;
    expect(respuesta.status(), await respuesta.text()).toBe(200);

    // Tras emitir, el formulario lleva al detalle con el folio asignado del CAF.
    await expect(page).toHaveURL(/\/invoices\/[0-9a-f-]{36}$/);
    await expect(page.getByRole('heading', { name: /Factura #\d+/ })).toBeVisible();
    expect(emisiones, 'Una venta debe emitir un solo documento').toHaveLength(1);
  });

  test('invoices/[id] muestra folio y estado del documento emitido', async ({ page }) => {
    const { admin } = users();
    await login(page, admin.email, admin.password);
    await page.goto('/invoices');

    const fila = page.getByRole('row').filter({ hasText: 'Cliente E2E SpA' }).first();
    await expect(fila).toBeVisible();
    await fila.getByRole('button', { name: 'Ver detalle' }).click();

    await expect(page).toHaveURL(/\/invoices\/[0-9a-f-]{36}$/);
    await expect(page.getByRole('heading', { name: /Factura #\d+/ })).toBeVisible();
    await expect(page.getByText(/Borrador|Pendiente de envío|Enviado|Aceptado|Rechazado/).first()).toBeVisible();
  });
});

/**
 * CAF con la estructura real del SII: <RSASK> es hermano de <CAF> (no hijo de
 * <DA>) y trae la llave en PKCS#1 con cabeceras PEM. Hoy POST /sii/caf
 * responde 500 "Error parsing CAF XML": CafParser.java:59 busca RSASK dentro
 * de <DA> y CafParser.java:107-117 solo decodifica PKCS#8. Con un CAF real
 * descargado del SII no se pueden cargar folios.
 * Bug: https://github.com/iacastillo90/casrusil/issues/39
 */
test('CAF con RSASK en PKCS#1 fuera de <CAF> (formato SII) se carga', async ({ page }) => {
  test.fail(true, 'Bug del producto: https://github.com/iacastillo90/casrusil/issues/39');
  const { admin, companyRut } = users();
  const { privateKey } = generateKeyPairSync('rsa', { modulusLength: 1024 });
  const rsask = privateKey.export({ type: 'pkcs1', format: 'pem' }).toString();
  const xml = `<?xml version="1.0" encoding="ISO-8859-1"?>
<AUTORIZACION><CAF version="1.0"><DA><RE>${companyRut}</RE><RS>QA E2E</RS><TD>34</TD><RNG><D>1</D><H>5</H></RNG><FA>2026-01-01</FA><RSAPK><M>AA==</M><E>AQAB</E></RSAPK><IDK>100</IDK></DA><FRMA algoritmo="SHA1withRSA">AA==</FRMA></CAF><RSASK>${rsask}</RSASK><RSAPUBK>AA==</RSAPUBK></AUTORIZACION>`;

  await login(page, admin.email, admin.password);
  await page.goto('/sii/caf');
  await page.getByLabel(/Archivo XML/i).setInputFiles({
    name: 'caf-sii-real.xml',
    mimeType: 'application/xml',
    buffer: Buffer.from(xml),
  });
  const subida = page.waitForResponse((r) => r.url().endsWith('/api/v1/sii/caf') && r.request().method() === 'POST');
  await page.getByRole('button', { name: /Cargar CAF/i }).click();
  expect((await subida).status()).toBe(200);
});
