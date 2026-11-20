import { expect, test } from '@playwright/test';
import { collectConsoleErrors, expectPageTitle, login, users } from './helpers';

const pages = [
  '/invoices',
  '/crm/leads',
  '/inventory',
  '/accounting/balance',
  '/reports',
];

test('ADMIN navega 5 páginas sin errores de consola', async ({ page }) => {
  test.setTimeout(300_000);
  const { admin } = users();
  await login(page, admin.email, admin.password);

  const errors = collectConsoleErrors(page);
  for (const path of pages) {
    await page.goto(path);
    await expectPageTitle(page);
    await expect(page).toHaveURL(new RegExp(path.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')));
  }

  expect(errors, `Errores de consola:\n${errors.join('\n')}`).toEqual([]);
});
