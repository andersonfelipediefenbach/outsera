import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

/** Paginação da lista (spec 006, FR-012): os botões sem destino ficam desabilitados. */
@Component({
  selector: 'app-paginator',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <nav class="paginator" aria-label="Paginação">
      <button type="button" class="button button--secondary" [disabled]="page() <= 1" (click)="pageChange.emit(1)">
        Primeira
      </button>
      <button
        type="button"
        class="button button--secondary"
        [disabled]="page() <= 1"
        (click)="pageChange.emit(page() - 1)"
      >
        Anterior
      </button>
      <p class="paginator__status" aria-live="polite">Página {{ page() }} de {{ totalPages() }}</p>
      <button
        type="button"
        class="button button--secondary"
        [disabled]="page() >= totalPages()"
        (click)="pageChange.emit(page() + 1)"
      >
        Próxima
      </button>
      <button
        type="button"
        class="button button--secondary"
        [disabled]="page() >= totalPages()"
        (click)="pageChange.emit(totalPages())"
      >
        Última
      </button>
    </nav>
  `,
})
export class Paginator {
  readonly page = input.required<number>();
  readonly totalPages = input.required<number>();
  readonly pageChange = output<number>();
}
