import { defineConfig, devices } from '@playwright/test';

/**
 * Ponta a ponta contra a aplicação real: a API do modo padrão com o arquivo fornecido e a imagem do front
 * (nginx) com o repasse de /api, sem respostas simuladas (spec 006, FR-023; research R5).
 */
export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  forbidOnly: !!process.env['CI'],
  retries: process.env['CI'] ? 1 : 0,
  reporter: process.env['CI'] ? [['list'], ['html', { open: 'never' }]] : 'list',
  use: {
    baseURL: process.env['E2E_BASE_URL'] ?? 'http://127.0.0.1:8081',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
    locale: 'pt-BR',
  },
  projects: [
    { name: 'desktop', use: { ...devices['Desktop Chrome'], viewport: { width: 1280, height: 800 } } },
    {
      name: 'celular',
      use: { ...devices['Pixel 5'], viewport: { width: 360, height: 740 }, isMobile: true, hasTouch: true },
    },
  ],
});
