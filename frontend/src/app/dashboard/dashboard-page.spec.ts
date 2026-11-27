import { DashboardPage } from './dashboard-page';
import { SERVER_ERROR, settle, setup } from '../testing';

describe('DashboardPage', () => {
  it('mostra os quatro painéis, e a falha de um não esconde os outros', async () => {
    const { fixture, http } = await setup(DashboardPage);
    http.expectOne('/api/aggregations/years-with-multiple-winners').flush(null, SERVER_ERROR);
    http.expectOne('/api/aggregations/studios-with-win-count').flush({ studios: [{ name: 'Columbia Pictures', winCount: 7 }] });
    http.expectOne('/api/producers/intervals').flush({
      min: [{ producer: 'Joel Silver', interval: 1, previousWin: 1990, followingWin: 1991 }],
      max: [{ producer: 'Matthew Vaughn', interval: 13, previousWin: 2002, followingWin: 2015 }],
    });
    const el = await settle(fixture);
    expect(el.querySelector('h1')?.textContent).toBe('Painel');
    expect(Array.from(el.querySelectorAll('h2')).map((h) => h.textContent)).toEqual([
      'Anos com mais de um vencedor',
      'Estúdios com mais vitórias',
      'Produtores com o maior e o menor intervalo',
      'Vencedores por ano',
    ]);
    expect(el.textContent).toContain('Não foi possível carregar os dados.');
    expect(el.textContent).toContain('Columbia Pictures');
    expect(el.textContent).toContain('Matthew Vaughn');
    http.verify();
  });
});
