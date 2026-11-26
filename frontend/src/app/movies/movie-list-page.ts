import { ChangeDetectionStrategy, Component } from '@angular/core';

/** Lista de filmes: provisória até a etapa 4 (spec 006, FR-010 a FR-014). */
@Component({
  selector: 'app-movie-list-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<h1 class="page-title">Filmes</h1>`,
})
export class MovieListPage {}
