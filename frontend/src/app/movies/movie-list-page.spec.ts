import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { MoviePage } from '../api/awards-api';
import { SERVER_ERROR, rows } from '../testing';
import { MovieListPage } from './movie-list-page';

function page(content: MoviePage['content'], page = 0, totalElements = content.length, totalPages = 1): MoviePage {
  return { content, page, size: 10, totalElements, totalPages };
}

const MOVIE = { year: 1990, title: 'The Adventures of Ford Fairlane', studios: null, producers: 'Joel Silver', winner: true };

describe('MovieListPage', () => {
  let harness: RouterTestingHarness;
  let http: HttpTestingController;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([{ path: 'filmes', component: MovieListPage }]),
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });
    harness = await RouterTestingHarness.create();
    http = TestBed.inject(HttpTestingController);
  });

  async function open(url: string): Promise<HTMLElement> {
    await harness.navigateByUrl(url);
    await harness.fixture.whenStable();
    return harness.routeNativeElement as HTMLElement;
  }

  async function settle(): Promise<HTMLElement> {
    harness.detectChanges();
    await harness.fixture.whenStable();
    return harness.routeNativeElement as HTMLElement;
  }

  it('a URL com filtro gera a consulta certa e mostra a tabela, o resumo e a paginação', async () => {
    const el = await open('/filmes?ano=1990&vencedor=sim&pagina=2');
    http.expectOne('/api/movies?page=1&size=10&year=1990&winner=true').flush(page([MOVIE], 1, 12, 2));
    await settle();
    expect(rows(el)).toEqual([['1990', 'The Adventures of Ford Fairlane', '—', 'Joel Silver', 'Sim']]);
    expect(el.textContent).toContain('12 filmes');
    expect(el.textContent).toContain('Página 2 de 2');
    expect((el.querySelector('#movies-year') as HTMLInputElement).value).toBe('1990');
    expect((el.querySelector('#movies-winner') as HTMLSelectElement).value).toBe('sim');
  });

  it('parâmetros inválidos na URL são ignorados', async () => {
    await open('/filmes?ano=abc&vencedor=talvez&pagina=x');
    http.expectOne('/api/movies?page=0&size=10').flush(page([MOVIE]));
  });

  it('mudar o vencedor volta à página 1 e grava a URL', async () => {
    const el = await open('/filmes?pagina=3');
    http.expectOne('/api/movies?page=2&size=10').flush(page([MOVIE], 2, 30, 3));
    await settle();
    const select = el.querySelector('#movies-winner') as HTMLSelectElement;
    select.value = 'nao';
    select.dispatchEvent(new Event('change'));
    await settle();
    expect(TestBed.inject(Router).url).toBe('/filmes?vencedor=nao');
    http.expectOne('/api/movies?page=0&size=10&winner=false').flush(page([]));
  });

  it('filtrar pelo ano volta à página 1; ano inválido não chama a API', async () => {
    const el = await open('/filmes?pagina=2');
    http.expectOne('/api/movies?page=1&size=10').flush(page([MOVIE], 1, 20, 2));
    await settle();
    const input = el.querySelector('#movies-year') as HTMLInputElement;
    input.value = 'abc';
    input.dispatchEvent(new Event('input'));
    await settle();
    expect(el.textContent).toContain('Informe um ano válido (só números).');
    expect((el.querySelector('button[type=submit]') as HTMLButtonElement).disabled).toBe(true);
    el.querySelector('form')!.dispatchEvent(new Event('submit'));
    await settle();
    http.expectNone(() => true);

    input.value = '1990';
    input.dispatchEvent(new Event('input'));
    el.querySelector('form')!.dispatchEvent(new Event('submit'));
    await settle();
    expect(TestBed.inject(Router).url).toBe('/filmes?ano=1990');
    http.expectOne('/api/movies?page=0&size=10&year=1990').flush(page([MOVIE]));
  });

  it('a paginação muda só a página na URL', async () => {
    const el = await open('/filmes?vencedor=sim');
    http.expectOne('/api/movies?page=0&size=10&winner=true').flush(page([MOVIE], 0, 25, 3));
    await settle();
    (Array.from(el.querySelectorAll('button')).find((b) => b.textContent?.trim() === 'Próxima') as HTMLButtonElement).click();
    await settle();
    expect(TestBed.inject(Router).url).toBe('/filmes?vencedor=sim&pagina=2');
    http.expectOne('/api/movies?page=1&size=10&winner=true').flush(page([MOVIE], 1, 25, 3));
  });

  it('vazio na página 1 mostra só a mensagem; além da última, também o link para a primeira', async () => {
    await open('/filmes?ano=1800');
    http.expectOne('/api/movies?page=0&size=10&year=1800').flush(page([], 0, 0, 0));
    let el = await settle();
    expect(el.textContent).toContain('Nenhum filme encontrado.');
    expect(el.textContent).not.toContain('Voltar à primeira página');

    await open('/filmes?pagina=999');
    http.expectOne('/api/movies?page=998&size=10').flush(page([], 998, 206, 21));
    el = await settle();
    const link = Array.from(el.querySelectorAll('a')).find((a) => a.textContent === 'Voltar à primeira página');
    expect(link?.getAttribute('href')).toBe('/filmes');
  });

  it('erro da API mostra a mensagem e "tentar de novo" repete a consulta', async () => {
    const el = await open('/filmes');
    http.expectOne('/api/movies?page=0&size=10').flush(null, SERVER_ERROR);
    await settle();
    expect(el.textContent).toContain('Não foi possível carregar os dados.');
    (Array.from(el.querySelectorAll('button')).find((b) => b.textContent === 'Tentar de novo') as HTMLButtonElement).click();
    http.expectOne('/api/movies?page=0&size=10').flush(page([MOVIE]));
    expect(rows(await settle())).toHaveLength(1);
  });
});
