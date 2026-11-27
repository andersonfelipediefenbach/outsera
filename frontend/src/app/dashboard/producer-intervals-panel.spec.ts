import { ProducerIntervalsPanel } from './producer-intervals-panel';
import { SERVER_ERROR, rows, settle, setup } from '../testing';

const URL = '/api/producers/intervals';

describe('ProducerIntervalsPanel', () => {
  it('mostra todos os itens de max e de min, inclusive os empatados', async () => {
    const { fixture, http } = await setup(ProducerIntervalsPanel);
    http.expectOne(URL).flush({
      min: [
        { producer: 'Joel Silver', interval: 1, previousWin: 1990, followingWin: 1991 },
        { producer: 'Outro', interval: 1, previousWin: 2000, followingWin: 2001 },
      ],
      max: [{ producer: 'Matthew Vaughn', interval: 13, previousWin: 2002, followingWin: 2015 }],
    });
    const el = await settle(fixture);
    const tables = el.querySelectorAll('table');
    expect(tables).toHaveLength(2);
    expect(Array.from(el.querySelectorAll('h3')).map((h) => h.textContent)).toEqual(['Maior intervalo', 'Menor intervalo']);
    expect(rows(el, 'app-interval-table:nth-of-type(1)')).toEqual([['Matthew Vaughn', '13', '2002', '2015']]);
    expect(rows(el, 'app-interval-table:nth-of-type(2)')).toEqual([
      ['Joel Silver', '1', '1990', '1991'],
      ['Outro', '1', '2000', '2001'],
    ]);
  });

  it('sem produtor com duas vitórias, mostra a mensagem de vazio', async () => {
    const { fixture, http } = await setup(ProducerIntervalsPanel);
    http.expectOne(URL).flush({ min: [], max: [] });
    const el = await settle(fixture);
    expect(el.textContent).toContain('Nenhum produtor com duas vitórias.');
    expect(el.querySelector('table')).toBeNull();
  });

  it('mostra a mensagem de erro', async () => {
    const { fixture, http } = await setup(ProducerIntervalsPanel);
    http.expectOne(URL).flush(null, SERVER_ERROR);
    expect((await settle(fixture)).textContent).toContain('Não foi possível carregar os dados.');
  });
});
