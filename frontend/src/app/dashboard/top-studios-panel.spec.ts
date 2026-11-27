import { TopStudiosPanel } from './top-studios-panel';
import { SERVER_ERROR, rows, settle, setup } from '../testing';

const URL = '/api/aggregations/studios-with-win-count';
const FIVE = [
  { name: 'Columbia Pictures', winCount: 7 },
  { name: 'Paramount Pictures', winCount: 6 },
  { name: 'Warner Bros.', winCount: 5 },
  { name: '20th Century Fox', winCount: 4 },
  { name: 'MGM', winCount: 3 },
];

describe('TopStudiosPanel', () => {
  it('mostra só os três primeiros, na ordem da API', async () => {
    const { fixture, http } = await setup(TopStudiosPanel);
    http.expectOne(URL).flush({ studios: FIVE });
    expect(rows(await settle(fixture))).toEqual([
      ['Columbia Pictures', '7'],
      ['Paramount Pictures', '6'],
      ['Warner Bros.', '5'],
    ]);
  });

  it('com menos de três, mostra os que houver', async () => {
    const { fixture, http } = await setup(TopStudiosPanel);
    http.expectOne(URL).flush({ studios: FIVE.slice(0, 2) });
    expect(rows(await settle(fixture))).toHaveLength(2);
  });

  it('mostra a mensagem de vazio e a de erro', async () => {
    const empty = await setup(TopStudiosPanel);
    empty.http.expectOne(URL).flush({ studios: [] });
    expect((await settle(empty.fixture)).textContent).toContain('Nenhum estúdio vencedor.');
  });

  it('mostra a mensagem de erro', async () => {
    const { fixture, http } = await setup(TopStudiosPanel);
    http.expectOne(URL).flush(null, SERVER_ERROR);
    expect((await settle(fixture)).textContent).toContain('Não foi possível carregar os dados.');
  });
});
