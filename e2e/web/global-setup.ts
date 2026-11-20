import { readFileSync } from 'node:fs';
import path from 'node:path';

export default async function globalSetup() {
  const file = path.resolve('.generated/users.json');
  try {
    JSON.parse(readFileSync(file, 'utf8'));
  } catch {
    throw new Error(`Falta ${file}. Ejecuta primero "npm run api" para sembrar la empresa y los usuarios E2E.`);
  }
}
