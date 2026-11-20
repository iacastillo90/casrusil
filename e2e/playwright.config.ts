import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './web',
  globalSetup: './web/global-setup.ts',
  timeout: 240_000,
  expect: { timeout: 10_000 },
  fullyParallel: false,
  workers: 1,
  reporter: [
    ['list'],
    ['html', { outputFolder: 'playwright-report', open: 'never' }],
  ],
  use: {
    baseURL: process.env.WEB_BASE_URL || 'http://localhost:3000',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
    video: 'off',
    channel: 'chrome',
  },
});
