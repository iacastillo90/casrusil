import { expect, test } from '@playwright/test';
import { login, users } from './helpers';

test('ADMIN activa modo oscuro y persiste la clase dark', async ({ page }) => {
  const { admin } = users();
  await login(page, admin.email, admin.password);

  await page.evaluate(() => localStorage.setItem('theme', 'dark'));
  await page.reload();

  await expect
    .poll(() => page.evaluate(() => document.documentElement.classList.contains('dark')))
    .toBe(true);

  const bodyBackground = await page.evaluate(() => getComputedStyle(document.body).backgroundColor);
  expect(bodyBackground).not.toBe('rgb(255, 255, 255)');
});
