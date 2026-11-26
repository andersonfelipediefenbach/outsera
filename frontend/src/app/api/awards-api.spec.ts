import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AwardsApi } from './awards-api';

describe('AwardsApi', () => {
  let api: AwardsApi;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    api = TestBed.inject(AwardsApi);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('chama os endpoints do painel por caminho relativo', () => {
    api.intervals().subscribe();
    api.yearsWithMultipleWinners().subscribe();
    api.studiosWithWinCount().subscribe();
    http.expectOne('/api/producers/intervals').flush({ min: [], max: [] });
    http.expectOne('/api/aggregations/years-with-multiple-winners').flush({ years: [] });
    http.expectOne('/api/aggregations/studios-with-win-count').flush({ studios: [] });
  });

  it('busca os vencedores de um ano pela listagem, com até 100 itens', () => {
    api.winnersOf(1990).subscribe();
    http.expectOne('/api/movies?year=1990&winner=true&size=100').flush({});
  });

  it('traduz o filtro da lista: página 1 vira page=0 e sem filtros não manda year nem winner', () => {
    api.movies({ pagina: 1 }).subscribe();
    http.expectOne('/api/movies?page=0&size=10').flush({});
  });

  it('traduz ano, vencedor=sim e página 3', () => {
    api.movies({ ano: 1990, vencedor: 'sim', pagina: 3 }).subscribe();
    http.expectOne('/api/movies?page=2&size=10&year=1990&winner=true').flush({});
  });

  it('traduz vencedor=nao para winner=false', () => {
    api.movies({ vencedor: 'nao', pagina: 1 }).subscribe();
    http.expectOne('/api/movies?page=0&size=10&winner=false').flush({});
  });
});
