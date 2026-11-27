import { Locator, Page } from '@playwright/test';

/** O painel (section) cujo título é o texto dado. */
export function panel(page: Page, title: string): Locator {
  return page.locator('section').filter({ has: page.getByRole('heading', { level: 2, name: title, exact: true }) });
}

/** Texto das células de cada linha do corpo das tabelas dentro do escopo (tabela ou cartões). */
export async function rows(scope: Locator): Promise<string[][]> {
  return scope.locator('tbody tr').evaluateAll((trs) =>
    trs.map((tr) => Array.from(tr.querySelectorAll('td')).map((td) => (td.textContent ?? '').trim())),
  );
}

/** Aperta Tab até o foco chegar no elemento com o texto ou o rótulo dado (navegação só pelo teclado). */
export async function tabUntil(page: Page, matches: (el: { text: string; label: string }) => boolean, limit = 40): Promise<void> {
  for (let i = 0; i < limit; i++) {
    await page.keyboard.press('Tab');
    const focused = await page.evaluate(() => {
      const el = document.activeElement as HTMLElement | null;
      const id = el?.id;
      const label = id ? (document.querySelector(`label[for="${id}"]`)?.textContent ?? '') : '';
      return { text: (el?.textContent ?? '').trim(), label: label.trim() };
    });
    if (matches(focused)) {
      return;
    }
  }
  throw new Error('o foco não chegou ao elemento esperado só com Tab');
}
