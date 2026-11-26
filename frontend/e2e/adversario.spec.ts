/**
 * Casos do testador adversário do front (spec 006; casos fora do repositório, como no Marco 4) que pedem a URL,
 * o histórico ou o teclado do navegador, contra a aplicação real (a API do modo padrão com o arquivo fornecido e a
 * imagem do front). As requisições a /api/movies são só observadas, nunca simuladas.
 */
import { Page, expect, test } from '@playwright/test';
import { panel, rows } from './helpers';

/** Guarda a query de cada GET /api/movies feito pela página. */
function watchMovies(page: Page): URLSearchParams[] {
  const seen: URLSearchParams[] = [];
  page.on('request', (request) => {
    const url = new URL(request.url());
    if (url.pathname === '/api/movies') {
      seen.push(url.searchParams);
    }
  });
  return seen;
}

/** A query como texto ordenado, para comparar sem depender da ordem dos parâmetros. */
function sorted(params: URLSearchParams): string {
  return [...params.entries()].map(([k, v]) => `${k}=${v}`).sort().join('&');
}

function query(text: string): string {
  return sorted(new URLSearchParams(text));
}

async function lastMoviesQuery(seen: URLSearchParams[]): Promise<string> {
  await expect.poll(() => seen.length).toBeGreaterThan(0);
  return sorted(seen[seen.length - 1]);
}

test.describe('testador adversário: lista de filmes pela URL', () => {
  for (const pagina of ['-1', '0', 'abc', '2abc', '1.5']) {
    test(`caso 01: pagina=${pagina} mostra a primeira página`, async ({ page }) => {
      const seen = watchMovies(page);
      await page.goto(`/filmes?pagina=${encodeURIComponent(pagina)}`);
      await expect(page.getByText('Página 1 de 21')).toBeVisible();
      await expect(page.locator('tbody tr')).toHaveCount(10);
      await expect(page.getByRole('button', { name: 'Primeira' })).toBeDisabled();
      expect(await lastMoviesQuery(seen)).toBe(query('page=0&size=10'));
    });
  }

  test('caso 02: página além da última com filtros e a volta com os mesmos filtros', async ({ page }) => {
    const seen = watchMovies(page);
    await page.goto('/filmes?ano=1990&vencedor=sim&pagina=999');
    await expect(page.getByText('Nenhum filme encontrado.')).toBeVisible();
    expect(await lastMoviesQuery(seen)).toBe(query('year=1990&winner=true&page=998&size=10'));
    await page.getByRole('link', { name: 'Voltar à primeira página' }).click();
    await expect(page.locator('tbody tr')).toHaveCount(2);
    expect(await lastMoviesQuery(seen)).toBe(query('year=1990&winner=true&page=0&size=10'));
  });

  test('caso 03: pagina=2147483649 é processada e mostra o vazio; 9007199254740993 vira a página 1', async ({ page }) => {
    const seen = watchMovies(page);
    await page.goto('/filmes?pagina=2147483649');
    await expect(page.getByText('Nenhum filme encontrado.')).toBeVisible();
    await expect(page.getByRole('link', { name: 'Voltar à primeira página' })).toBeVisible();
    expect(await lastMoviesQuery(seen)).toBe(query('page=2147483647&size=10'));

    await page.goto('/filmes?pagina=9007199254740993');
    await expect(page.getByText('Página 1 de 21')).toBeVisible();
    expect(await lastMoviesQuery(seen)).toBe(query('page=0&size=10'));
  });

  for (const ano of ['abc', '1990abc', '-1', '1990.5', '1e3']) {
    test(`caso 04: ano=${ano} é ignorado e "Sim" fica`, async ({ page }) => {
      const seen = watchMovies(page);
      await page.goto(`/filmes?ano=${encodeURIComponent(ano)}&vencedor=sim`);
      await expect(page.locator('tbody tr').first()).toBeVisible();
      await expect(page.getByLabel('Ano')).toHaveValue('');
      await expect(page.getByLabel('Vencedor')).toHaveValue('sim');
      expect(await lastMoviesQuery(seen)).toBe(query('winner=true&page=0&size=10'));
    });
  }

  for (const [raw, expected] of [['', 'page=0&size=10'], ['%20%20', 'page=0&size=10'], ['19%2090', 'page=0&size=10'], ['%201990%20', 'year=1990&page=0&size=10']]) {
    test(`caso 05: ano=${raw} consulta ${expected}`, async ({ page }) => {
      const seen = watchMovies(page);
      await page.goto(`/filmes?ano=${raw}`);
      await expect(page.locator('tbody tr').first()).toBeVisible();
      expect(await lastMoviesQuery(seen)).toBe(query(expected));
    });
  }

  for (const vencedor of ['true', 'false', '1', '0', 'SIM', 'n%C3%A3o', 'sim%20', '']) {
    test(`caso 06: vencedor=${vencedor} vira "Todos" e mantém o ano`, async ({ page }) => {
      const seen = watchMovies(page);
      await page.goto(`/filmes?ano=1990&vencedor=${vencedor}`);
      await expect(page.locator('tbody tr').first()).toBeVisible();
      await expect(page.getByLabel('Vencedor')).toHaveValue('');
      expect(await lastMoviesQuery(seen)).toBe(query('year=1990&page=0&size=10'));
    });
  }

  test('caso 06: os controles sim e nao', async ({ page }) => {
    const seen = watchMovies(page);
    await page.goto('/filmes?ano=1990&vencedor=sim');
    await expect(page.locator('tbody tr')).toHaveCount(2);
    expect(await lastMoviesQuery(seen)).toBe(query('year=1990&winner=true&page=0&size=10'));
    await page.goto('/filmes?ano=1990&vencedor=nao');
    await expect(page.getByLabel('Vencedor')).toHaveValue('nao');
    expect(await lastMoviesQuery(seen)).toBe(query('year=1990&winner=false&page=0&size=10'));
  });

  for (const [url, expected, ano, vencedor] of [
    ['ano=1986&ano=1990&vencedor=sim', 'year=1986&winner=true&page=0&size=10', '1986', 'sim'],
    ['ano=1990&ano=1986&vencedor=sim', 'year=1990&winner=true&page=0&size=10', '1990', 'sim'],
    ['ano=abc&ano=1990&vencedor=sim', 'winner=true&page=0&size=10', '', 'sim'],
    ['ano=1990&vencedor=sim&vencedor=nao', 'year=1990&winner=true&page=0&size=10', '1990', 'sim'],
    ['ano=1990&vencedor=nao&vencedor=sim', 'year=1990&winner=false&page=0&size=10', '1990', 'nao'],
    ['pagina=2&pagina=3', 'page=1&size=10', '', ''],
    ['pagina=0&pagina=2', 'page=0&size=10', '', ''],
  ]) {
    test(`caso 07: ${url} usa a primeira ocorrência, na tela e na consulta, também ao recarregar`, async ({ page }) => {
      const seen = watchMovies(page);
      await page.goto(`/filmes?${url}`);
      await expect(page.locator('tbody tr').first()).toBeVisible();
      expect(await lastMoviesQuery(seen)).toBe(query(expected));
      await expect(page.getByLabel('Ano')).toHaveValue(ano);
      await expect(page.getByLabel('Vencedor')).toHaveValue(vencedor);
      await page.reload();
      await expect(page.locator('tbody tr').first()).toBeVisible();
      expect(await lastMoviesQuery(seen)).toBe(query(expected));
    });
  }

  test('caso 08: parâmetros desconhecidos não chegam à API, e page=999 é ignorado', async ({ page }) => {
    const seen = watchMovies(page);
    await page.goto('/filmes?ano=1990&vencedor=sim&inexistente=teste&size=1&year=1986&winner=false');
    await expect(page.locator('tbody tr')).toHaveCount(2);
    expect(await lastMoviesQuery(seen)).toBe(query('year=1990&winner=true&page=0&size=10'));

    await page.goto('/filmes?page=999');
    await expect(page.getByText('Página 1 de 21')).toBeVisible();
    expect(await lastMoviesQuery(seen)).toBe(query('page=0&size=10'));

    await page.goto('/filmes?pagina=999');
    await expect(page.getByText('Nenhum filme encontrado.')).toBeVisible();
    await expect(page.getByRole('link', { name: 'Voltar à primeira página' })).toBeVisible();
  });

  test('caso 09: recarregar, voltar, avançar e a volta à página 1 ao trocar o filtro', async ({ page }) => {
    const seen = watchMovies(page);
    await page.goto('/filmes?pagina=2');
    await page.reload();
    await expect(page.getByText('Página 2 de 21')).toBeVisible();
    expect(await lastMoviesQuery(seen)).toBe(query('page=1&size=10'));

    await page.goto('/filmes?ano=1990&vencedor=sim');
    await expect(page.locator('tbody tr')).toHaveCount(2);
    await page.goBack();
    await expect(page.getByText('Página 2 de 21')).toBeVisible();
    await expect(page.getByLabel('Ano')).toHaveValue('');
    expect(await lastMoviesQuery(seen)).toBe(query('page=1&size=10'));
    await page.goForward();
    await expect(page.locator('tbody tr')).toHaveCount(2);
    expect(await lastMoviesQuery(seen)).toBe(query('year=1990&winner=true&page=0&size=10'));

    await page.goto('/filmes?pagina=2');
    await expect(page.getByText('Página 2 de 21')).toBeVisible();
    await page.getByLabel('Ano').fill('1990');
    await page.getByLabel('Ano').press('Enter');
    await expect(page).toHaveURL(/\/filmes\?ano=1990$/);
    expect(await lastMoviesQuery(seen)).toBe(query('year=1990&page=0&size=10'));

    await page.goto('/filmes?pagina=2');
    await expect(page.getByText('Página 2 de 21')).toBeVisible();
    await page.getByLabel('Vencedor').selectOption('sim');
    await expect(page).toHaveURL(/\/filmes\?vencedor=sim$/);
    expect(await lastMoviesQuery(seen)).toBe(query('winner=true&page=0&size=10'));
  });
});

test.describe('testador adversário: Dashboard', () => {
  test('caso 11: busca por 0 e por 10000', async ({ page }) => {
    const seen = watchMovies(page);
    await page.goto('/');
    const winners = panel(page, 'Vencedores por ano');
    for (const year of ['0', '10000']) {
      await winners.getByLabel('Ano').fill(year);
      await expect(winners.getByRole('button', { name: 'Buscar' })).toBeEnabled();
      await winners.getByRole('button', { name: 'Buscar' }).click();
      await expect(winners.getByText(`Nenhum vencedor em ${year}.`)).toBeVisible();
      expect(await lastMoviesQuery(seen)).toBe(query(`year=${year}&winner=true&size=100`));
    }
  });

  test('caso 12: Enter com valor inválido ou com o campo limpo não busca', async ({ page }) => {
    const seen = watchMovies(page);
    await page.goto('/');
    const winners = panel(page, 'Vencedores por ano');
    const field = winners.getByLabel('Ano');
    for (const value of ['abc', '-1', '1990.5']) {
      await field.fill(value);
      await field.press('Enter');
      await expect(winners.getByText('Informe um ano válido (só números).')).toBeVisible();
      await expect(winners.getByRole('button', { name: 'Buscar' })).toBeDisabled();
    }
    await field.fill('1990');
    await field.fill('');
    await field.press('Enter');
    await expect(winners.getByText('Informe um ano válido (só números).')).toBeVisible();
    expect(seen).toHaveLength(0);
  });
});

test.describe('testador adversário: teclado e largura', () => {
  test('caso 25: as jornadas completas só com o teclado', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('tbody tr').first()).toBeVisible();
    await expect(page.getByRole('link', { name: 'Painel' })).toHaveAttribute('aria-current', 'page');
    const winners = panel(page, 'Vencedores por ano');

    // Até o campo "Ano" do painel e, com Shift+Tab, de volta ao menu.
    const field = winners.getByLabel('Ano');
    for (let i = 0; i < 40 && !(await field.evaluate((el) => el === document.activeElement)); i++) {
      await page.keyboard.press('Tab');
    }
    await expect(field).toBeFocused();
    await page.keyboard.type('1990');
    // O botão só habilita depois da validação do campo: o Tab espera por isso, como uma pessoa esperaria.
    await expect(winners.getByRole('button', { name: 'Buscar' })).toBeEnabled();
    await page.keyboard.press('Tab');
    await expect(winners.getByRole('button', { name: 'Buscar' })).toBeFocused();
    await page.keyboard.press('Enter');
    await expect(winners.locator('tbody tr')).toHaveCount(2);

    const filmes = page.getByRole('link', { name: 'Filmes' });
    for (let i = 0; i < 60 && !(await filmes.evaluate((el) => el === document.activeElement)); i++) {
      await page.keyboard.press('Shift+Tab');
    }
    await expect(filmes).toBeFocused();
    const outline = await filmes.evaluate((el) => getComputedStyle(el).outlineStyle);
    expect(outline).not.toBe('none');
    await page.keyboard.press('Enter');
    await expect(page).toHaveURL(/\/filmes$/);
    await expect(page.getByRole('link', { name: 'Filmes' })).toHaveAttribute('aria-current', 'page');

    await expect(page.locator('tbody tr').first()).toBeVisible();
    for (const [button, status] of [['Próxima', 'Página 2 de 21'], ['Anterior', 'Página 1 de 21'], ['Última', 'Página 21 de 21'], ['Primeira', 'Página 1 de 21']]) {
      await page.getByRole('button', { name: button }).focus();
      await page.keyboard.press('Enter');
      await expect(page.getByText(status)).toBeVisible();
    }

    await page.getByLabel('Ano').focus();
    await page.keyboard.type('1990');
    await page.keyboard.press('Enter');
    await expect(page).toHaveURL(/ano=1990/);
    await page.getByLabel('Vencedor').focus();
    await page.keyboard.press('ArrowDown');
    await expect(page.getByLabel('Vencedor')).toHaveValue('sim');
    await expect(page).toHaveURL(/ano=1990&vencedor=sim/);
    await expect(page.locator('tbody tr')).toHaveCount(2);
    await expect(page.locator('caption').first()).toBeAttached();
    await expect(page.locator('th[scope="col"]').first()).toBeAttached();
  });
});

/**
 * Caso 26: palavras de 480 letras exigem um CSV próprio. O job web sobe uma segunda API com esse arquivo e uma
 * segunda instância do front, cuja URL vem em E2E_LONG_TEXT_URL. Sem ela, o caso só é pulado fora do pipeline.
 */
test.describe('testador adversário: caso 26, largura com palavras longas', () => {
  const base = process.env['E2E_LONG_TEXT_URL'];
  test.skip(!base && !process.env['CI'], 'sem E2E_LONG_TEXT_URL (só no pipeline)');

  for (const width of [360, 767, 768]) {
    test(`caso 26: ${width} px sem rolagem horizontal, com cartões abaixo de 768 px`, async ({ page }, info) => {
      test.skip(info.project.name !== 'desktop', 'a largura é fixada no próprio caso');
      expect(base, 'E2E_LONG_TEXT_URL').toBeTruthy();
      await page.setViewportSize({ width, height: 800 });
      const noOverflow = async () =>
        expect(await page.evaluate(() => document.documentElement.scrollWidth - window.innerWidth)).toBeLessThanOrEqual(0);

      await page.goto(`${base}/`);
      const winners = panel(page, 'Vencedores por ano');
      await winners.getByLabel('Ano').fill('1990');
      await winners.getByRole('button', { name: 'Buscar' }).click();
      await expect(winners.locator('tbody tr')).toHaveCount(1);
      await noOverflow();

      await page.goto(`${base}/filmes`);
      await expect(page.locator('tbody tr')).toHaveCount(10);
      await noOverflow();
      await page.getByRole('button', { name: 'Próxima' }).click();
      await expect(page.getByText('Página 2 de 2')).toBeVisible();
      await noOverflow();
      await page.getByRole('button', { name: 'Primeira' }).click();
      await expect(page.getByText('Página 1 de 2')).toBeVisible();
      await noOverflow();
      // Os controles continuam dentro da largura (a página pode rolar na vertical, com textos tão longos).
      const next = page.getByRole('button', { name: 'Próxima' });
      await next.scrollIntoViewIfNeeded();
      const box = (await next.boundingBox())!;
      expect(box.x).toBeGreaterThanOrEqual(0);
      expect(box.x + box.width).toBeLessThanOrEqual(width);

      const display = await page.locator('tbody tr').first().evaluate((tr) => getComputedStyle(tr).display);
      expect(display).toBe(width < 768 ? 'block' : 'table-row');
      expect((await rows(page.locator('table')))[0][1]).toHaveLength(480);
    });
  }
});
