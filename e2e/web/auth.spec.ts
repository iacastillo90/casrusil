import { expect, test } from '@playwright/test';
import { login, users } from './helpers';

test('ADMIN inicia sesión y llega al dashboard', async ({ page }) => {
  const { admin } = users();
  await login(page, admin.email, admin.password);
  await expect(page.getByRole('heading', { name: /Dashboard 🚀/ })).toBeVisible();
});
