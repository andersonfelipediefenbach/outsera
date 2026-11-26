import { ChangeDetectionStrategy, Component, computed, effect, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { map } from 'rxjs';
import { AwardsApi, MovieFilter } from '../api/awards-api';
import { Paginator } from '../shared/paginator/paginator';
import { createLoader } from '../shared/panel-state/loader';
import { PanelStateView } from '../shared/panel-state/panel-state';
import { YEAR_MESSAGE, parseYear, yearValidator } from '../shared/validators';
import { readFilter, toQueryParams } from './movie-filter';

/**
 * Lista de filmes paginada, com filtro por ano e por vencedor (spec 006, FR-010 a FR-014). A URL é a fonte do
 * estado: abrir, recarregar ou voltar no navegador refaz a consulta.
 */
@Component({
  selector: 'app-movie-list-page',
  imports: [ReactiveFormsModule, RouterLink, Paginator, PanelStateView],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h1 class="page-title">Filmes</h1>
    <section class="panel" aria-label="Lista de filmes">
      <form class="filters" (submit)="applyYear($event)" novalidate>
        <div class="field">
          <label for="movies-year">Ano</label>
          <input
            id="movies-year"
            type="text"
            inputmode="numeric"
            autocomplete="off"
            [formControl]="year"
            [attr.aria-invalid]="yearInvalid() ? 'true' : null"
            [attr.aria-describedby]="yearInvalid() ? 'movies-year-error' : null"
          />
        </div>
        <button type="submit" class="button" [disabled]="yearInvalid()">Filtrar</button>
        <div class="field">
          <label for="movies-winner">Vencedor</label>
          <select id="movies-winner" [formControl]="winner" (change)="applyWinner()">
            <option value="">Todos</option>
            <option value="sim">Sim</option>
            <option value="nao">Não</option>
          </select>
        </div>
        @if (yearInvalid()) {
          <p id="movies-year-error" class="field__error">{{ message }}</p>
        }
      </form>

      <app-panel-state [status]="loader.state().status" emptyMessage="Nenhum filme encontrado." (retry)="loader.load()" />
      @if (loader.state().status === 'vazio' && filter().pagina > 1) {
        <p><a [routerLink]="[]" [queryParams]="firstPage()">Voltar à primeira página</a></p>
      }
      @if (loader.state().status === 'dados' && loader.state().data; as data) {
        <p class="summary">{{ data.totalElements }} filmes</p>
        <table class="table">
          <caption class="visually-hidden">Filmes, ordenados por ano e título</caption>
          <thead>
            <tr>
              <th scope="col">Ano</th>
              <th scope="col">Título</th>
              <th scope="col">Estúdios</th>
              <th scope="col">Produtores</th>
              <th scope="col">Vencedor</th>
            </tr>
          </thead>
          <tbody>
            @for (movie of data.content; track movie.year + '|' + movie.title) {
              <tr>
                <td data-label="Ano">{{ movie.year }}</td>
                <td data-label="Título">{{ movie.title }}</td>
                <td data-label="Estúdios">{{ movie.studios ?? '—' }}</td>
                <td data-label="Produtores">{{ movie.producers }}</td>
                <td data-label="Vencedor">{{ movie.winner ? 'Sim' : 'Não' }}</td>
              </tr>
            }
          </tbody>
        </table>
        <app-paginator [page]="filter().pagina" [totalPages]="data.totalPages" (pageChange)="goTo($event)" />
      }
    </section>
  `,
})
export class MovieListPage {
  private readonly api = inject(AwardsApi);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly message = YEAR_MESSAGE;
  protected readonly filter = toSignal(this.route.queryParamMap.pipe(map(readFilter)), {
    initialValue: readFilter(this.route.snapshot.queryParamMap),
  });
  protected readonly year = new FormControl('', { nonNullable: true, validators: [yearValidator] });
  protected readonly winner = new FormControl<'' | 'sim' | 'nao'>('', { nonNullable: true });
  private readonly yearText = toSignal(this.year.valueChanges, { initialValue: '' });
  /** O campo vazio é válido aqui: significa "sem filtro de ano". */
  protected readonly yearInvalid = computed(() => {
    const text = this.yearText().trim();
    return text !== '' && parseYear(text) === undefined;
  });
  protected readonly firstPage = computed(() => toQueryParams({ ...this.filter(), pagina: 1 }));
  protected readonly loader = createLoader(
    () => this.api.movies(this.filter()),
    (page) => page.content.length === 0,
  );

  constructor() {
    // A cada mudança da URL: os campos mostram o filtro, e a consulta é refeita.
    effect(() => {
      const filter = this.filter();
      this.year.setValue(filter.ano === undefined ? '' : String(filter.ano));
      this.winner.setValue(filter.vencedor ?? '');
      this.loader.load();
    });
  }

  applyYear(event?: Event): void {
    event?.preventDefault();
    if (this.yearInvalid()) {
      return;
    }
    this.navigate({ ...this.filter(), ano: parseYear(this.year.value), pagina: 1 });
  }

  applyWinner(): void {
    const value = this.winner.value;
    this.navigate({ ...this.filter(), vencedor: value === '' ? undefined : value, pagina: 1 });
  }

  goTo(page: number): void {
    this.navigate({ ...this.filter(), pagina: page });
  }

  private navigate(filter: MovieFilter): void {
    void this.router.navigate([], { relativeTo: this.route, queryParams: toQueryParams(filter) });
  }
}
