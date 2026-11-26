/**
 * Casos do testador adversário do front (spec 006; casos fora do repositório, como no Marco 4), na parte que
 * pede dados especiais ou controle da rede: o HttpTestingController entrega as respostas no formato do contrato,
 * sem servidor. A parte da API desses casos está nos ITs de aggregations/adversario/, e os casos de URL e de
 * teclado, no e2e/adversario.spec.ts, contra a aplicação real.
 */
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { convertToParamMap, provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { AwardsApi, MoviePage } from './api/awards-api';
import { DashboardPage } from './dashboard/dashboard-page';
import { MultipleWinnersPanel } from './dashboard/multiple-winners-panel';
import { ProducerIntervalsPanel } from './dashboard/producer-intervals-panel';
import { TopStudiosPanel } from './dashboard/top-studios-panel';
import { WinnersByYearPanel } from './dashboard/winners-by-year-panel';
import { readFilter } from './movies/movie-filter';
import { MovieListPage } from './movies/movie-list-page';
import { SERVER_ERROR, mount, rows, settle, setup } from './testing';

const YEARS = '/api/aggregations/years-with-multiple-winners';
const STUDIOS = '/api/aggregations/studios-with-win-count';
const INTERVALS = '/api/producers/intervals';
const winnersUrl = (year: number) => `/api/movies?year=${year}&winner=true&size=100`;

function page(content: MoviePage['content'], totalElements = content.length, totalPages = content.length ? 1 : 0): MoviePage {
  return { content, page: 0, size: 100, totalElements, totalPages };
}

function type(el: HTMLElement, value: string): void {
  const input = el.querySelector('input')!;
  input.value = value;
  input.dispatchEvent(new Event('input'));
}

function submit(el: HTMLElement): void {
  el.querySelector('form')!.dispatchEvent(new Event('submit', { cancelable: true }));
}

describe('testador adversário do front (spec 006)', () => {
  describe('caso 01: página negativa, zero ou não numérica', () => {
    it.each(['-1', '0', 'abc', '2abc', '1.5'])('pagina=%s vira a página 1', (pagina) => {
      expect(readFilter(convertToParamMap({ pagina }))).toEqual({ pagina: 1 });
    });
  });

  describe('caso 03: páginas enormes (FR-013)', () => {
    it('pagina=2147483649 é válida: a página fica 2147483649', () => {
      expect(readFilter(convertToParamMap({ pagina: '2147483649' }))).toEqual({ pagina: 2147483649 });
    });

    it('pagina=9007199254740993 não cabe num inteiro seguro: vira a página 1', () => {
      expect(readFilter(convertToParamMap({ pagina: '9007199254740993' }))).toEqual({ pagina: 1 });
    });

    it('pagina=9007199254740991 (o maior inteiro seguro) é válida', () => {
      expect(readFilter(convertToParamMap({ pagina: '9007199254740991' }))).toEqual({ pagina: 9007199254740991 });
    });

    it('o índice enviado à API para no máximo da API (2147483647), sempre além da última página', () => {
      TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
      const api = TestBed.inject(AwardsApi);
      const http = TestBed.inject(HttpTestingController);
      api.movies({ pagina: 2147483649 }).subscribe();
      http.expectOne('/api/movies?page=2147483647&size=10').flush({});
      api.movies({ pagina: 2147483648 }).subscribe();
      http.expectOne('/api/movies?page=2147483647&size=10').flush({});
      http.verify();
    });
  });

  describe('caso 04: ano inválido na URL com vencedor válido', () => {
    it.each(['abc', '1990abc', '-1', '1990.5', '1e3'])('ano=%s é ignorado, e vencedor=sim fica', (ano) => {
      expect(readFilter(convertToParamMap({ ano, vencedor: 'sim' }))).toEqual({ vencedor: 'sim', pagina: 1 });
    });
  });

  describe('caso 05: ano vazio, em branco ou com espaços (FR-008)', () => {
    it.each(['', '  ', '19 90'])('ano=%j fica sem filtro de ano', (ano) => {
      expect(readFilter(convertToParamMap({ ano }))).toEqual({ pagina: 1 });
    });

    it('espaços nas pontas são removidos: " 1990 " vira 1990', () => {
      expect(readFilter(convertToParamMap({ ano: ' 1990 ' }))).toEqual({ ano: 1990, pagina: 1 });
    });
  });

  describe('caso 06: valores ambíguos do vencedor', () => {
    it.each(['true', 'false', '1', '0', 'SIM', 'não', 'sim ', ''])('vencedor=%j vira "todos"', (vencedor) => {
      expect(readFilter(convertToParamMap({ ano: '1990', vencedor }))).toEqual({ ano: 1990, pagina: 1 });
    });
  });

  describe('caso 07: parâmetros repetidos (FR-013, a primeira ocorrência vence)', () => {
    it.each([
      [{ ano: ['1986', '1990'], vencedor: 'sim' }, { ano: 1986, vencedor: 'sim', pagina: 1 }],
      [{ ano: ['1990', '1986'], vencedor: 'sim' }, { ano: 1990, vencedor: 'sim', pagina: 1 }],
      [{ ano: ['abc', '1990'], vencedor: 'sim' }, { vencedor: 'sim', pagina: 1 }],
      [{ ano: '1990', vencedor: ['sim', 'nao'] }, { ano: 1990, vencedor: 'sim', pagina: 1 }],
      [{ pagina: ['2', '3'] }, { pagina: 2 }],
      [{ pagina: ['0', '2'] }, { pagina: 1 }],
    ])('%j', (params, expected) => {
      expect(readFilter(convertToParamMap(params))).toEqual(expected);
    });
  });

  describe('caso 08: parâmetros desconhecidos', () => {
    it('size, year, winner, page e outros na URL do front são ignorados', () => {
      const params = { ano: '1990', vencedor: 'sim', inexistente: 'teste', size: '1', year: '1986', winner: 'false', page: '999' };
      expect(readFilter(convertToParamMap(params))).toEqual({ ano: 1990, vencedor: 'sim', pagina: 1 });
    });
  });

  describe('casos de dados e de rede (painéis e lista)', () => {
    it('caso 10: ano só com indicados mostra o vazio, e nenhuma chamada antes da busca', async () => {
      const ctx = await setup(WinnersByYearPanel);
      const http = ctx.http;
      const el: HTMLElement = ctx.fixture.nativeElement;
      http.expectNone(() => true);
      type(el, '1990');
      submit(el);
      http.expectOne(winnersUrl(1990)).flush(page([]));
      expect((await settle(ctx.fixture)).textContent).toContain('Nenhum vencedor em 1990.');
      expect(el.textContent).not.toContain('Tentar de novo');
      type(el, '1991');
      submit(el);
      http.expectOne(winnersUrl(1991)).flush(page([{ year: 1991, title: 'Filme vencedor', studios: 'Estudio Alfa', producers: 'Ana', winner: true }]));
      expect(rows(await settle(ctx.fixture))).toEqual([['1991', 'Filme vencedor', 'Estudio Alfa', 'Ana']]);
    });

    it.each([0, 10000])('caso 11: busca por %i é aceita e mostra o vazio', async (year) => {
      const ctx = await setup(WinnersByYearPanel);
      const el: HTMLElement = ctx.fixture.nativeElement;
      type(el, String(year));
      await settle(ctx.fixture);
      expect(el.querySelector('button')!.disabled).toBe(false);
      submit(el);
      ctx.http.expectOne(winnersUrl(year)).flush(page([]));
      expect((await settle(ctx.fixture)).textContent).toContain(`Nenhum vencedor em ${year}.`);
    });

    it.each(['abc', '-1', '1990.5'])('caso 12: "%s" com Enter não chama a API', async (value) => {
      const ctx = await setup(WinnersByYearPanel);
      const el: HTMLElement = ctx.fixture.nativeElement;
      type(el, value);
      submit(el);
      await settle(ctx.fixture);
      ctx.http.expectNone(() => true);
      expect(el.textContent).toContain('Informe um ano válido (só números).');
      expect(el.querySelector('button')!.disabled).toBe(true);
    });

    it('caso 12: campo limpo depois de digitar mostra a mensagem e não chama a API', async () => {
      const ctx = await setup(WinnersByYearPanel);
      const el: HTMLElement = ctx.fixture.nativeElement;
      type(el, '1990');
      type(el, '');
      submit(el);
      await settle(ctx.fixture);
      ctx.http.expectNone(() => true);
      expect(el.textContent).toContain('Informe um ano válido (só números).');
      expect(el.querySelector('button')!.disabled).toBe(true);
    });

    it('caso 13: 101 vencedores no ano mostram os 100 da única chamada (limite documentado)', async () => {
      const ctx = await setup(WinnersByYearPanel);
      const el: HTMLElement = ctx.fixture.nativeElement;
      type(el, '1990');
      submit(el);
      const content = Array.from({ length: 100 }, (_, i) => ({
        year: 1990, title: `Vencedor ${String(i + 1).padStart(3, '0')}`, studios: 'Estudio Alfa', producers: `Produtor ${i + 1}`, winner: true,
      }));
      ctx.http.expectOne(winnersUrl(1990)).flush(page(content, 101, 2));
      expect(rows(await settle(ctx.fixture))).toHaveLength(100);
      ctx.http.expectNone(() => true);
    });

    it('caso 14: marcação e script do CSV aparecem como texto, sem criar elementos (FR-017a)', async () => {
      const markup = { year: 1990, title: '<b>MARCA-TITULO</b>', studios: '<i>MARCA-ESTUDIO</i>', producers: '<script type=application/json>{}</script>', winner: true };
      const ctx = await setup(WinnersByYearPanel);
      const el: HTMLElement = ctx.fixture.nativeElement;
      type(el, '1990');
      submit(el);
      ctx.http.expectOne(winnersUrl(1990)).flush(page([markup]));
      await settle(ctx.fixture);
      expect(rows(el)).toEqual([['1990', '<b>MARCA-TITULO</b>', '<i>MARCA-ESTUDIO</i>', '<script type=application/json>{}</script>']]);
      expect(el.querySelectorAll('tbody b, tbody i, tbody script')).toHaveLength(0);

      const studios = await mount(TopStudiosPanel);
      studios.http.expectOne(STUDIOS).flush({ studios: [{ name: '<i>MARCA-ESTUDIO</i>', winCount: 2 }] });
      const studiosEl = await settle(studios.fixture);
      expect(rows(studiosEl)).toEqual([['<i>MARCA-ESTUDIO</i>', '2']]);
      expect(studiosEl.querySelectorAll('tbody i')).toHaveLength(0);
    });

    it('caso 14: produtor com script em min e max aparece como texto', async () => {
      const item = { producer: '<script type=application/json>{}</script>', interval: 1, previousWin: 1990, followingWin: 1991 };
      const ctx = await setup(ProducerIntervalsPanel);
      ctx.http.expectOne(INTERVALS).flush({ min: [item], max: [item] });
      const el = await settle(ctx.fixture);
      expect(rows(el, 'app-interval-table:nth-of-type(1)')).toEqual([['<script type=application/json>{}</script>', '1', '1990', '1991']]);
      expect(el.querySelectorAll('tbody script')).toHaveLength(0);
    });

    it('caso 15: Café composto e decomposto, emoji e japonês aparecem como a API entrega', async () => {
      const ctx = await setup(TopStudiosPanel);
      ctx.http.expectOne(STUDIOS).flush({ studios: [{ name: 'Cafe\u0301', winCount: 1 }, { name: 'Caf\u00e9', winCount: 1 }] });
      const names = rows(await settle(ctx.fixture)).map((row) => row[0]);
      expect(names).toEqual(['Cafe\u0301', 'Caf\u00e9']);

      const winners = await mount(WinnersByYearPanel);
      const el: HTMLElement = winners.fixture.nativeElement;
      type(el, '2001');
      submit(el);
      winners.http.expectOne(winnersUrl(2001)).flush(page([{ year: 2001, title: 'Estreia \u{1F3AC} — 東京', studios: 'Caf\u00e9', producers: 'Caf\u00e9', winner: true }]));
      expect(rows(await settle(winners.fixture))[0][1]).toBe('Estreia \u{1F3AC} — 東京');
    });

    it('caso 16: o painel de anos mostra a ordem da API', async () => {
      const ctx = await setup(MultipleWinnersPanel);
      ctx.http.expectOne(YEARS).flush({ years: [{ year: 1989, winnerCount: 2 }, { year: 1991, winnerCount: 3 }, { year: 1995, winnerCount: 2 }] });
      expect(rows(await settle(ctx.fixture))).toEqual([['1989', '2'], ['1991', '3'], ['1995', '2']]);
    });

    it('caso 17: empate na terceira posição: exatamente os três primeiros da API', async () => {
      const ctx = await setup(TopStudiosPanel);
      ctx.http.expectOne(STUDIOS).flush({
        studios: [
          { name: 'Zeta', winCount: 3 }, { name: 'Alfa', winCount: 2 }, { name: 'Beta', winCount: 1 },
          { name: 'Delta', winCount: 1 }, { name: 'Gama', winCount: 1 }, { name: 'Norte and Sul', winCount: 1 },
        ],
      });
      expect(rows(await settle(ctx.fixture))).toEqual([['Zeta', '3'], ['Alfa', '2'], ['Beta', '1']]);
    });

    it('caso 18: o front não reordena: os três primeiros na ordem Unicode da API', async () => {
      const ctx = await setup(TopStudiosPanel);
      ctx.http.expectOne(STUDIOS).flush({
        studios: [{ name: 'Alfa', winCount: 1 }, { name: 'Zeta', winCount: 1 }, { name: 'alfa', winCount: 1 }, { name: 'Álamo', winCount: 1 }],
      });
      expect(rows(await settle(ctx.fixture)).map((row) => row[0])).toEqual(['Alfa', 'Zeta', 'alfa']);
    });

    it('caso 19: agregações vazias e só dois estúdios', async () => {
      const dashboard = await setup(DashboardPage);
      dashboard.http.expectOne(YEARS).flush({ years: [] });
      dashboard.http.expectOne(STUDIOS).flush({ studios: [] });
      dashboard.http.expectOne(INTERVALS).flush({ min: [], max: [] });
      const el = await settle(dashboard.fixture);
      expect(el.textContent).toContain('Nenhum ano com mais de um vencedor.');
      expect(el.textContent).toContain('Nenhum estúdio vencedor.');
      expect(el.textContent).toContain('Nenhum produtor com duas vitórias.');

      const two = await mount(TopStudiosPanel);
      two.http.expectOne(STUDIOS).flush({ studios: [{ name: 'Alfa', winCount: 1 }, { name: 'Beta', winCount: 1 }] });
      expect(rows(await settle(two.fixture))).toEqual([['Alfa', '1'], ['Beta', '1']]);
    });

    it('caso 20: as duas listas de intervalos completas, na ordem da API, com dois pares de Rena', async () => {
      const ctx = await setup(ProducerIntervalsPanel);
      const min = [
        { producer: 'Rena', interval: 1, previousWin: 1989, followingWin: 1990 },
        { producer: 'Ana', interval: 1, previousWin: 1990, followingWin: 1991 },
        { producer: 'Rena', interval: 1, previousWin: 1990, followingWin: 1991 },
        { producer: 'Zoe', interval: 1, previousWin: 1990, followingWin: 1991 },
      ];
      const max = [
        { producer: 'Bruno', interval: 10, previousWin: 1980, followingWin: 1990 },
        { producer: 'Caio', interval: 10, previousWin: 1980, followingWin: 1990 },
      ];
      ctx.http.expectOne(INTERVALS).flush({ min, max });
      const el = await settle(ctx.fixture);
      expect(rows(el, 'app-interval-table:nth-of-type(1)')).toEqual(max.map((i) => [i.producer, '10', '1980', '1990']));
      expect(rows(el, 'app-interval-table:nth-of-type(2)')).toEqual(min.map((i) => [i.producer, '1', String(i.previousWin), String(i.followingWin)]));
    });

    it('caso 21: um painel lento não segura os outros, e a busca funciona enquanto ele carrega', async () => {
      const ctx = await setup(DashboardPage);
      const el: HTMLElement = ctx.fixture.nativeElement;
      const studios = ctx.http.expectOne(STUDIOS);
      ctx.http.expectOne(YEARS).flush({ years: [{ year: 1986, winnerCount: 2 }] });
      ctx.http.expectOne(INTERVALS).flush({ min: [], max: [] });
      await settle(ctx.fixture);
      const studiosPanel = el.querySelector('app-top-studios-panel')!;
      expect(studiosPanel.querySelector('[aria-live="polite"]')?.textContent).toContain('Carregando…');
      expect(rows(el.querySelector('app-multiple-winners-panel') as HTMLElement)).toEqual([['1986', '2']]);

      const winners = el.querySelector('app-winners-by-year-panel') as HTMLElement;
      type(winners, '1990');
      submit(winners);
      ctx.http.expectOne(winnersUrl(1990)).flush(page([{ year: 1990, title: 'X', studios: null, producers: 'Y', winner: true }]));
      await settle(ctx.fixture);
      expect(rows(winners)).toHaveLength(1);

      studios.flush({ studios: [{ name: 'Columbia Pictures', winCount: 7 }] });
      await settle(ctx.fixture);
      expect(rows(studiosPanel as HTMLElement)).toEqual([['Columbia Pictures', '7']]);
    });

    it('caso 22: falha de um painel e "tentar de novo" só dele', async () => {
      const ctx = await setup(DashboardPage);
      const el: HTMLElement = ctx.fixture.nativeElement;
      ctx.http.expectOne(YEARS).flush(null, SERVER_ERROR);
      ctx.http.expectOne(STUDIOS).flush({ studios: [{ name: 'Columbia Pictures', winCount: 7 }] });
      ctx.http.expectOne(INTERVALS).flush({ min: [], max: [] });
      await settle(ctx.fixture);
      const years = el.querySelector('app-multiple-winners-panel') as HTMLElement;
      expect(years.textContent).toContain('Não foi possível carregar os dados.');
      expect(el.querySelectorAll('[role="alert"]')).toHaveLength(1);
      (years.querySelector('button') as HTMLButtonElement).click();
      ctx.http.expectOne(YEARS).flush({ years: [{ year: 1986, winnerCount: 2 }, { year: 1990, winnerCount: 2 }, { year: 2015, winnerCount: 2 }] });
      ctx.http.verify();
      expect(rows(await settle(ctx.fixture) as HTMLElement, 'app-multiple-winners-panel table')).toHaveLength(3);
      expect(rows(el.querySelector('app-top-studios-panel') as HTMLElement)).toEqual([['Columbia Pictures', '7']]);
    });

    it('caso 23: API fora do ar (502 do repasse ou falha de rede) vira erro, e não vazio', async () => {
      const ctx = await setup(DashboardPage);
      ctx.http.expectOne(YEARS).flush('<html>502</html>', { status: 502, statusText: 'Bad Gateway' });
      ctx.http.expectOne(STUDIOS).error(new ProgressEvent('error'));
      ctx.http.expectOne(INTERVALS).flush(null, { status: 504, statusText: 'Gateway Timeout' });
      const el = await settle(ctx.fixture);
      expect(el.querySelectorAll('[role="alert"]')).toHaveLength(3);
      expect(el.textContent).not.toContain('Nenhum');
      expect(el.textContent).not.toContain('Carregando…');
    });

    it('caso 24: a busca mais recente vence, e a anterior é cancelada (FR-008a)', async () => {
      const ctx = await setup(WinnersByYearPanel);
      const el: HTMLElement = ctx.fixture.nativeElement;
      type(el, '1990');
      submit(el);
      const first = ctx.http.expectOne(winnersUrl(1990));
      type(el, '0');
      submit(el);
      const second = ctx.http.expectOne(winnersUrl(0));
      expect(first.cancelled).toBe(true);
      second.flush(page([]));
      await settle(ctx.fixture);
      expect(el.textContent).toContain('Nenhum vencedor em 0.');
      expect(rows(el)).toHaveLength(0);
    });
  });

  describe('caso 23 na lista: erro e "tentar de novo" com os mesmos filtros', () => {
    it('a nova tentativa repete page=1 com o filtro e mostra o vazio com o link de volta', async () => {
      TestBed.configureTestingModule({
        providers: [provideRouter([{ path: 'filmes', component: MovieListPage }]), provideHttpClient(), provideHttpClientTesting()],
      });
      const harness = await RouterTestingHarness.create();
      const http = TestBed.inject(HttpTestingController);
      await harness.navigateByUrl('/filmes?ano=1990&vencedor=sim&pagina=2');
      const url = '/api/movies?page=1&size=10&year=1990&winner=true';
      http.expectOne(url).flush(null, { status: 502, statusText: 'Bad Gateway' });
      harness.detectChanges();
      await harness.fixture.whenStable();
      const el = harness.routeNativeElement as HTMLElement;
      expect(el.textContent).toContain('Não foi possível carregar os dados.');
      (Array.from(el.querySelectorAll('button')).find((b) => b.textContent === 'Tentar de novo') as HTMLButtonElement).click();
      http.expectOne(url).flush({ content: [], page: 1, size: 10, totalElements: 2, totalPages: 1 });
      harness.detectChanges();
      await harness.fixture.whenStable();
      expect(el.textContent).toContain('Nenhum filme encontrado.');
      expect(el.textContent).toContain('Voltar à primeira página');
    });
  });
});
