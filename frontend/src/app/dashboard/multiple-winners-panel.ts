import { ChangeDetectionStrategy, Component, OnInit, inject } from '@angular/core';
import { AwardsApi } from '../api/awards-api';
import { createLoader } from '../shared/panel-state/loader';
import { PanelStateView } from '../shared/panel-state/panel-state';

/** Painel "Anos com mais de um vencedor" (spec 006, FR-007; contracts/frontend-ui.md). */
@Component({
  selector: 'app-multiple-winners-panel',
  imports: [PanelStateView],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="panel" aria-labelledby="multiple-winners-title">
      <h2 id="multiple-winners-title" class="panel__title">Anos com mais de um vencedor</h2>
      <app-panel-state
        [status]="loader.state().status"
        emptyMessage="Nenhum ano com mais de um vencedor."
        (retry)="loader.load()"
      />
      @if (loader.state().status === 'dados' && loader.state().data; as data) {
        <table class="table">
          <caption class="visually-hidden">Anos com mais de um vencedor</caption>
          <thead>
            <tr>
              <th scope="col">Ano</th>
              <th scope="col">Vencedores</th>
            </tr>
          </thead>
          <tbody>
            @for (item of data.years; track item.year) {
              <tr>
                <td data-label="Ano">{{ item.year }}</td>
                <td data-label="Vencedores">{{ item.winnerCount }}</td>
              </tr>
            }
          </tbody>
        </table>
      }
    </section>
  `,
})
export class MultipleWinnersPanel implements OnInit {
  private readonly api = inject(AwardsApi);
  protected readonly loader = createLoader(
    () => this.api.yearsWithMultipleWinners(),
    (value) => value.years.length === 0,
  );

  ngOnInit(): void {
    this.loader.load();
  }
}
