import { expect, type Page } from '@playwright/test';
import { readFileSync } from 'node:fs';
import path from 'node:path';

let loginIp = 20;

export interface E2eUsers {
  admin: { email: string; password: string; role: string };
  sales: { email: string; password: string; role: string };
  accountant: { email: string; password: string; role: string };
  companyId: string;
  companyRut: string;
}

export function users(): E2eUsers {
  return JSON.parse(readFileSync(path.resolve('.generated/users.json'), 'utf8')) as E2eUsers;
}

export async function login(page: Page, email: string, password: string) {
  loginIp += 1;
  await page.setExtraHTTPHeaders({ 'X-Forwarded-For': `10.77.0.${loginIp}` });
  await page.goto('/login', { waitUntil: 'domcontentloaded', timeout: 180_000 });
  // LoginForm deja el botón deshabilitado hasta que React hidrata; sin esa
  // espera el submit cae al POST nativo del formulario. Se espera por estado,
  // no por tiempo fijo (antes: botón de TanStack DevTools, que no existe en el
  // build de producción y costaba 60 s por login, + waitForTimeout).
  await expect(page.getByRole('button', { name: /Iniciar Sesión/i })).toBeEnabled({ timeout: 60_000 });
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
