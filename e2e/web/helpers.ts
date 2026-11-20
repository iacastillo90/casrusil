import { expect, type Page } from '@playwright/test';
import { readFileSync } from 'node:fs';
import path from 'node:path';

let loginIp = 20;

export interface E2eUsers {
  admin: { email: string; password: string; role: string };
  sales: { email: string; password: string; role: string };
  accountant: { email: string; password: string; role: string };
  companyId: string;
}

export function users(): E2eUsers {
  return JSON.parse(readFileSync(path.resolve('.generated/users.json'), 'utf8')) as E2eUsers;
}

export async function login(page: Page, email: string, password: string) {
  loginIp += 1;
  await page.setExtraHTTPHeaders({ 'X-Forwarded-For': `10.77.0.${loginIp}` });
  await page.goto('/login', { waitUntil: 'domcontentloaded', timeout: 180_000 });
  await page.waitForLoadState('networkidle', { timeout: 15_000 }).catch(() => {});
  // Señal de hidratación de React: el botón de TanStack DevTools solo existe
  // después de montar el árbol cliente. Sin esto el submit cae al GET nativo.
  await page.getByRole('button', { name: /Tanstack query devtools/i }).waitFor({ timeout: 60_000 }).catch(() => {});
  await page.waitForTimeout(500);
  await page.getByLabel('Correo Electrónico').fill(email);
  await page.getByLabel('Contraseña').fill(password);
  await page.getByRole('button', { name: /Iniciar Sesión/i }).click();
  await expect(page).toHaveURL(/\/dashboard/, { timeout: 90_000 });
}

export function collectConsoleErrors(page: Page): string[] {
  const errors: string[] = [];
  page.on('console', (message) => {
    if (message.type() === 'error') errors.push(message.text());
  });
  page.on('pageerror', (error) => errors.push(error.message));
  return errors;
}

export async function expectPageTitle(page: Page) {
  await expect(page.locator('h1').first()).toBeVisible({ timeout: 90_000 });
}
