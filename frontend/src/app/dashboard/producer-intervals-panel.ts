import { ChangeDetectionStrategy, Component, OnInit, inject } from '@angular/core';
import { AwardsApi } from '../api/awards-api';
import { createLoader } from '../shared/panel-state/loader';
import { PanelStateView } from '../shared/panel-state/panel-state';
import { IntervalTable } from './interval-table';

/**
 * Painel "Produtores com o maior e o menor intervalo": todos os itens de `max` e de `min`, como a API devolve
 * (spec 006, FR-007; constituição, Princípio III).
 */
@Component({
  selector: 'app-producer-intervals-panel',
  imports: [PanelStateView, IntervalTable],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="panel" aria-labelledby="intervals-title">
      <h2 id="intervals-title" class="panel__title">Produtores com o maior e o menor intervalo</h2>
      <app-panel-state
        [status]="loader.state().status"
        emptyMessage="Nenhum produtor com duas vitórias."
        (retry)="loader.load()"
      />
      @if (loader.state().status === 'dados' && loader.state().data; as data) {
        <h3 class="panel__subtitle">Maior intervalo</h3>
        <app-interval-table caption="Produtores com o maior intervalo" [items]="data.max" />
        <h3 class="panel__subtitle">Menor intervalo</h3>
        <app-interval-table caption="Produtores com o menor intervalo" [items]="data.min" />
      }
    </section>
  `,
})
export class ProducerIntervalsPanel implements OnInit {
  private readonly api = inject(AwardsApi);
  protected readonly loader = createLoader(
    () => this.api.intervals(),
    (value) => value.min.length === 0 && value.max.length === 0,
  );

  ngOnInit(): void {
    this.loader.load();
  }
}
