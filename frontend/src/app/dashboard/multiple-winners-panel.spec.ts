import { MultipleWinnersPanel } from './multiple-winners-panel';
import { SERVER_ERROR, rows, settle, setup } from '../testing';

const URL = '/api/aggregations/years-with-multiple-winners';

describe('MultipleWinnersPanel', () => {
  it('mostra os anos com a quantidade de vencedores, na ordem da API', async () => {
    const { fixture, http } = await setup(MultipleWinnersPanel);
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Carregando…');
    http.expectOne(URL).flush({ years: [{ year: 1986, winnerCount: 2 }, { year: 1990, winnerCount: 2 }] });
    const el = await settle(fixture);
    expect(rows(el)).toEqual([['1986', '2'], ['1990', '2']]);
    expect(el.querySelector('h2')?.textContent).toBe('Anos com mais de um vencedor');
    expect(el.querySelector('td')?.getAttribute('data-label')).toBe('Ano');
  });

  it('mostra a mensagem de vazio', async () => {
    const { fixture, http } = await setup(MultipleWinnersPanel);
    http.expectOne(URL).flush({ years: [] });
    const el = await settle(fixture);
    expect(el.textContent).toContain('Nenhum ano com mais de um vencedor.');
    expect(el.querySelector('table')).toBeNull();
  });

  it('no erro, "tentar de novo" refaz só a chamada deste painel', async () => {
    const { fixture, http } = await setup(MultipleWinnersPanel);
    http.expectOne(URL).flush(null, SERVER_ERROR);
    const el = await settle(fixture);
    expect(el.textContent).toContain('Não foi possível carregar os dados.');
    el.querySelector('button')!.click();
    http.expectOne(URL).flush({ years: [{ year: 2015, winnerCount: 2 }] });
    expect(rows(await settle(fixture))).toEqual([['2015', '2']]);
    http.verify();
  });
});
