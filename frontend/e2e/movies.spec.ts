import { expect, test } from '@playwright/test';
import { rows } from './helpers';

test.describe('Lista de filmes (spec 006, US2)', () => {
  test('US2-1 e US2-2: total do arquivo e paginação', async ({ page }) => {
    await page.goto('/filmes');
    await expect(page).toHaveTitle('Filmes — Golden Raspberry Awards');
    await expect(page.getByText('206 filmes')).toBeVisible();
    await expect(page.getByText('Página 1 de 21')).toBeVisible();
    await expect(page.locator('tbody tr')).toHaveCount(10);
    expect((await rows(page.locator('table')))[0][0]).toBe('1980');
    await expect(page.getByRole('button', { name: 'Primeira' })).toBeDisabled();
    await expect(page.getByRole('button', { name: 'Anterior' })).toBeDisabled();

    await page.getByRole('button', { name: 'Próxima' }).click();
    await expect(page).toHaveURL(/\/filmes\?pagina=2$/);
    await expect(page.getByText('Página 2 de 21')).toBeVisible();

    await page.getByRole('button', { name: 'Última' }).click();
    await expect(page.getByText('Página 21 de 21')).toBeVisible();
    await expect(page.getByRole('button', { name: 'Próxima' })).toBeDisabled();
  });

  test('US2-3: filtro 1990 e vencedores, de volta à primeira página', async ({ page }) => {
    await page.goto('/filmes?pagina=3');
    await page.getByLabel('Ano').fill('1990');
    await page.getByRole('button', { name: 'Filtrar' }).click();
    await page.getByLabel('Vencedor').selectOption('sim');
    await expect(page).toHaveURL(/\/filmes\?ano=1990&vencedor=sim$/);
    await expect(page.locator('tbody tr')).toHaveCount(2);
    const titles = (await rows(page.locator('table'))).map((row) => row[1]);
    expect(titles.sort()).toEqual(["Ghosts Can't Do It", 'The Adventures of Ford Fairlane']);
  });

  test('US2-6 e US5-5: link com filtros aberto direto e recarregado', async ({ page }) => {
    await page.goto('/filmes?ano=1990&vencedor=sim');
    await expect(page.locator('tbody tr')).toHaveCount(2);
    await page.reload();
    await expect(page.locator('tbody tr')).toHaveCount(2);
    await expect(page.getByLabel('Ano')).toHaveValue('1990');
    await expect(page.getByLabel('Vencedor')).toHaveValue('sim');
  });

  test('US2-4: filtro sem resultado', async ({ page }) => {
    await page.goto('/filmes?ano=1800');
    await expect(page.getByText('Nenhum filme encontrado.')).toBeVisible();
  });
});
