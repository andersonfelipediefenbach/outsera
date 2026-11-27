import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { PanelStatus } from './loader';

/**
 * Mostra os estados de carregando, vazio e erro de um painel ou da lista (spec 006, FR-009 e FR-014;
 * contracts/frontend-ui.md). No estado `dados`, não mostra nada: o conteúdo fica com quem usa.
 */
@Component({
  selector: 'app-panel-state',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @switch (status()) {
      @case ('carregando') {
        <p class="state" role="status" aria-live="polite">Carregando…</p>
      }
      @case ('vazio') {
        <p class="state" role="status" aria-live="polite">{{ emptyMessage() }}</p>
      }
      @case ('erro') {
        <div class="state state--error" role="alert">
          <p>Não foi possível carregar os dados.</p>
          <button type="button" class="button" (click)="retry.emit()">Tentar de novo</button>
        </div>
      }
    }
  `,
})
export class PanelStateView {
  readonly status = input.required<PanelStatus>();
  readonly emptyMessage = input('');
  readonly retry = output<void>();
}
