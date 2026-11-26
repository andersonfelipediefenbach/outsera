import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

/** Tipos do contrato OpenAPI 1.3.0 (specs/006-frontend-angular/contracts/openapi.yaml). */
export interface ProducerInterval {
  producer: string;
  interval: number;
  previousWin: number;
  followingWin: number;
}

export interface ProducerIntervals {
  min: ProducerInterval[];
  max: ProducerInterval[];
}

export interface Movie {
  year: number;
  title: string;
  studios: string | null;
  producers: string;
  winner: boolean;
}

export interface MoviePage {
  content: Movie[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface YearWinnerCount {
  year: number;
  winnerCount: number;
}

export interface YearsWithMultipleWinners {
  years: YearWinnerCount[];
}

export interface StudioWinCount {
  name: string;
  winCount: number;
}

export interface StudiosWithWinCount {
  studios: StudioWinCount[];
}

/** Filtro da lista como a URL do front o guarda (data-model, "Filtro da lista"). */
export interface MovieFilter {
  ano?: number;
  vencedor?: 'sim' | 'nao';
  pagina: number;
}

/** Itens por página da lista (spec 006, FR-010). */
export const PAGE_SIZE = 10;

/** Máximo da listagem da API, usado na busca de vencedores de um ano (FR-006). */
export const MAX_PAGE_SIZE = 100;

/**
 * Chamadas à API, sempre por caminho relativo (`/api/...`): o servidor do front repassa ao serviço da API, e o
 * navegador fala com uma única origem (spec 006, FR-018).
 */
@Injectable({ providedIn: 'root' })
export class AwardsApi {
  private readonly http = inject(HttpClient);

  intervals(): Observable<ProducerIntervals> {
    return this.http.get<ProducerIntervals>('/api/producers/intervals');
  }

  yearsWithMultipleWinners(): Observable<YearsWithMultipleWinners> {
    return this.http.get<YearsWithMultipleWinners>('/api/aggregations/years-with-multiple-winners');
  }

  studiosWithWinCount(): Observable<StudiosWithWinCount> {
    return this.http.get<StudiosWithWinCount>('/api/aggregations/studios-with-win-count');
  }

  /** Vencedores de um ano pela listagem existente (FR-006). */
  winnersOf(year: number): Observable<MoviePage> {
    const params = new HttpParams().set('year', year).set('winner', true).set('size', MAX_PAGE_SIZE);
    return this.http.get<MoviePage>('/api/movies', { params });
  }

  /** Página da lista, traduzindo o filtro do front para a API (data-model, "Tradução entre a URL do front e a API"). */
  movies(filter: MovieFilter): Observable<MoviePage> {
    let params = new HttpParams().set('page', filter.pagina - 1).set('size', PAGE_SIZE);
    if (filter.ano !== undefined) {
      params = params.set('year', filter.ano);
    }
    if (filter.vencedor !== undefined) {
      params = params.set('winner', filter.vencedor === 'sim');
    }
    return this.http.get<MoviePage>('/api/movies', { params });
  }
}
