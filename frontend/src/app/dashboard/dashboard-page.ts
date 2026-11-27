import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MultipleWinnersPanel } from './multiple-winners-panel';
import { ProducerIntervalsPanel } from './producer-intervals-panel';
import { TopStudiosPanel } from './top-studios-panel';
import { WinnersByYearPanel } from './winners-by-year-panel';

/** Dashboard ("Painel" na interface): quatro painéis independentes (spec 006, FR-007 e FR-009). */
@Component({
  selector: 'app-dashboard-page',
  imports: [MultipleWinnersPanel, TopStudiosPanel, ProducerIntervalsPanel, WinnersByYearPanel],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h1 class="page-title">Painel</h1>
    <div class="dashboard">
      <app-multiple-winners-panel />
      <app-top-studios-panel />
      <app-producer-intervals-panel />
      <app-winners-by-year-panel />
    </div>
  `,
})
export class DashboardPage {}
