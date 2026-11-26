import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs';
import { AwardsApi } from '../api/awards-api';
import { createLoader } from '../shared/panel-state/loader';
import { PanelStateView } from '../shared/panel-state/panel-state';
import { YEAR_MESSAGE, parseYear, yearValidator } from '../shared/validators';

/**
 * Painel "Vencedores por ano": busca pela listagem existente, com `year` e `winner=true` (spec 006, FR-006 a
 * FR-008). Um ano inválido não gera chamada à API.
 */
@Component({
  selector: 'app-winners-by-year-panel',
  imports: [PanelStateView, ReactiveFormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="panel" aria-labelledby="winners-title">
      <h2 id="winners-title" class="panel__title">Vencedores por ano</h2>
      <form class="search" (submit)="search($event)" novalidate>
        <div class="field">
          <label for="winners-year">Ano</label>
          <input
            id="winners-year"
            type="text"
            inputmode="numeric"
            autocomplete="off"
            [formControl]="year"
            [attr.aria-invalid]="showError() ? 'true' : null"
            [attr.aria-describedby]="showError() ? 'winners-year-error' : null"
          />
        </div>
        <button type="submit" class="button" [disabled]="invalid()">Buscar</button>
        @if (showError()) {
          <p id="winners-year-error" class="field__error">{{ message }}</p>
        }
      </form>
      @if (searched() !== null) {
        <app-panel-state
          [status]="loader.state().status"
          [emptyMessage]="'Nenhum vencedor em ' + searched() + '.'"
          (retry)="loader.load()"
        />
        @if (loader.state().status === 'dados' && loader.state().data; as data) {
          <table class="table">
            <caption class="visually-hidden">Vencedores de {{ searched() }}</caption>
            <thead>
              <tr>
                <th scope="col">Ano</th>
                <th scope="col">Título</th>
                <th scope="col">Estúdios</th>
                <th scope="col">Produtores</th>
              </tr>
            </thead>
            <tbody>
              @for (movie of data.content; track movie.title) {
                <tr>
                  <td data-label="Ano">{{ movie.year }}</td>
                  <td data-label="Título">{{ movie.title }}</td>
                  <td data-label="Estúdios">{{ movie.studios ?? '—' }}</td>
                  <td data-label="Produtores">{{ movie.producers }}</td>
                </tr>
              }
            </tbody>
          </table>
        }
      }
    </section>
  `,
})
export class WinnersByYearPanel {
  private readonly api = inject(AwardsApi);
  protected readonly message = YEAR_MESSAGE;
  protected readonly year = new FormControl('', { nonNullable: true, validators: [yearValidator] });
  protected readonly searched = signal<number | null>(null);
  protected readonly invalid = toSignal(this.year.statusChanges.pipe(map((status) => status !== 'VALID')), {
    initialValue: this.year.invalid,
  });
  private readonly touched = toSignal(this.year.valueChanges.pipe(map(() => true)), { initialValue: false });
  protected readonly showError = computed(() => this.touched() && this.invalid());
  protected readonly loader = createLoader(
    () => this.api.winnersOf(this.searched()!),
    (page) => page.content.length === 0,
  );

  search(event?: Event): void {
    event?.preventDefault();
    const year = parseYear(this.year.value);
    if (year === undefined) {
      return;
    }
    this.searched.set(year);
    this.loader.load();
  }
}
