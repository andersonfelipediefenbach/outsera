import { ChangeDetectionStrategy, Component, OnInit, computed, inject } from '@angular/core';
import { AwardsApi } from '../api/awards-api';
import { createLoader } from '../shared/panel-state/loader';
import { PanelStateView } from '../shared/panel-state/panel-state';

/** Quantos estúdios o painel mostra (spec 006, FR-007). */
export const TOP_STUDIOS = 3;

/** Painel "Estúdios com mais vitórias": os três primeiros, na ordem da API (spec 006, FR-007). */
@Component({
  selector: 'app-top-studios-panel',
  imports: [PanelStateView],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="panel" aria-labelledby="top-studios-title">
      <h2 id="top-studios-title" class="panel__title">Estúdios com mais vitórias</h2>
      <app-panel-state [status]="loader.state().status" emptyMessage="Nenhum estúdio vencedor." (retry)="loader.load()" />
      @if (loader.state().status === 'dados') {
        <table class="table">
          <caption class="visually-hidden">Os três estúdios com mais vitórias</caption>
          <thead>
            <tr>
              <th scope="col">Estúdio</th>
              <th scope="col">Vitórias</th>
            </tr>
          </thead>
          <tbody>
            @for (studio of top(); track studio.name) {
              <tr>
                <td data-label="Estúdio">{{ studio.name }}</td>
                <td data-label="Vitórias">{{ studio.winCount }}</td>
              </tr>
            }
          </tbody>
        </table>
      }
    </section>
  `,
})
export class TopStudiosPanel implements OnInit {
  private readonly api = inject(AwardsApi);
  protected readonly loader = createLoader(
    () => this.api.studiosWithWinCount(),
    (value) => value.studios.length === 0,
  );
  protected readonly top = computed(() => (this.loader.state().data?.studios ?? []).slice(0, TOP_STUDIOS));

  ngOnInit(): void {
    this.loader.load();
  }
}
