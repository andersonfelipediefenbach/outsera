import { expect, test } from '@playwright/test';
import { panel, tabUntil } from './helpers';

test.describe('Layout e teclado (spec 006, US3)', () => {
  for (const path of ['/', '/filmes']) {
    test(`US3-1 e SC-004: ${path} sem rolagem horizontal`, async ({ page }) => {
      await page.goto(path);
      await expect(page.locator('tbody tr').first()).toBeVisible();
      const overflow = await page.evaluate(() => document.documentElement.scrollWidth - window.innerWidth);
      expect(overflow).toBeLessThanOrEqual(0);
    });
  }

  test('US3-1 e US3-2 (FR-015): painéis empilhados e cartões no celular, grade e tabela no desktop', async ({ page }, info) => {
    await page.goto('/');
    const first = panel(page, 'Anos com mais de um vencedor');
    const second = panel(page, 'Estúdios com mais vitórias');
    await expect(first.locator('tbody tr').first()).toBeVisible();
    const a = (await first.boundingBox())!;
    const b = (await second.boundingBox())!;

    await page.goto('/filmes');
    await expect(page.locator('tbody tr').first()).toBeVisible();
    const rowDisplay = await page.locator('tbody tr').first().evaluate((tr) => getComputedStyle(tr).display);
    const headPosition = await page.locator('thead').evaluate((thead) => getComputedStyle(thead).position);

    if (info.project.name === 'celular') {
      expect(a.x).toBe(b.x);
      expect(b.y).toBeGreaterThan(a.y);
      expect(rowDisplay).toBe('block');
      expect(headPosition).toBe('absolute');
    } else {
      expect(a.y).toBe(b.y);
      expect(b.x).toBeGreaterThan(a.x);
      expect(rowDisplay).toBe('table-row');
      expect(headPosition).not.toBe('absolute');
    }
  });

  test('US3-3: busca por ano e paginação só pelo teclado', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('tbody tr').first()).toBeVisible();
    await tabUntil(page, (el) => el.label === 'Ano');
    await page.keyboard.type('1990');
    await page.keyboard.press('Tab');
    await expect(page.getByRole('button', { name: 'Buscar' })).toBeFocused();
    await page.keyboard.press('Enter');
    await expect(panel(page, 'Vencedores por ano').locator('tbody tr')).toHaveCount(2);

    await page.goto('/filmes');
    await expect(page.locator('tbody tr').first()).toBeVisible();
    await tabUntil(page, (el) => el.text === 'Próxima', 80);
    await page.keyboard.press('Enter');
    await expect(page.getByText('Página 2 de 21')).toBeVisible();
  });
});
