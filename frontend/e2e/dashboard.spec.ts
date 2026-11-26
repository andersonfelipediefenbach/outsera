import { expect, test } from '@playwright/test';
import { panel, rows } from './helpers';

test.describe('Dashboard (spec 006, US1)', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  test('US1-1 a US1-3 e SC-002: os três painéis automáticos com os valores de referência', async ({ page }) => {
    await expect(page).toHaveTitle('Painel — Golden Raspberry Awards');
    await expect(page.getByRole('heading', { level: 1, name: 'Painel' })).toBeVisible();

    const years = panel(page, 'Anos com mais de um vencedor');
    await expect(years.locator('tbody tr')).toHaveCount(3);
    expect(await rows(years)).toEqual([['1986', '2'], ['1990', '2'], ['2015', '2']]);

    const studios = panel(page, 'Estúdios com mais vitórias');
    await expect(studios.locator('tbody tr')).toHaveCount(3);
    expect(await rows(studios)).toEqual([
      ['Columbia Pictures', '7'],
      ['Paramount Pictures', '6'],
      ['Warner Bros.', '5'],
    ]);

    const intervals = panel(page, 'Produtores com o maior e o menor intervalo');
    await expect(intervals.locator('tbody tr')).toHaveCount(2);
    expect(await rows(intervals.locator('app-interval-table').nth(0))).toEqual([['Matthew Vaughn', '13', '2002', '2015']]);
    expect(await rows(intervals.locator('app-interval-table').nth(1))).toEqual([['Joel Silver', '1', '1990', '1991']]);
  });

  test('US1-4 e SC-003: vencedores de 1990 pela busca', async ({ page }) => {
    const winners = panel(page, 'Vencedores por ano');
    await winners.getByLabel('Ano').fill('1990');
    await winners.getByRole('button', { name: 'Buscar' }).click();
    await expect(winners.locator('tbody tr')).toHaveCount(2);
    const titles = (await rows(winners)).map((row) => row[1]);
    expect(titles).toEqual(["Ghosts Can't Do It", 'The Adventures of Ford Fairlane']);
  });

  test('US1-5: ano sem vencedor', async ({ page }) => {
    const winners = panel(page, 'Vencedores por ano');
    await winners.getByLabel('Ano').fill('1979');
    await winners.getByRole('button', { name: 'Buscar' }).click();
    await expect(winners.getByText('Nenhum vencedor em 1979.')).toBeVisible();
  });

  test('US1-6: valor que não é ano mostra a mensagem e desabilita a busca', async ({ page }) => {
    const winners = panel(page, 'Vencedores por ano');
    await winners.getByLabel('Ano').fill('abc');
    await expect(winners.getByText('Informe um ano válido (só números).')).toBeVisible();
    await expect(winners.getByRole('button', { name: 'Buscar' })).toBeDisabled();
  });
});
