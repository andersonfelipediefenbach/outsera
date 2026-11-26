import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { ProducerInterval } from '../api/awards-api';

/** Tabela de intervalos de produtores, usada para `max` e para `min`. */
@Component({
  selector: 'app-interval-table',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <table class="table">
      <caption class="visually-hidden">{{ caption() }}</caption>
      <thead>
        <tr>
          <th scope="col">Produtor</th>
          <th scope="col">Intervalo</th>
          <th scope="col">Vitória anterior</th>
          <th scope="col">Vitória seguinte</th>
        </tr>
      </thead>
      <tbody>
        @for (item of items(); track $index) {
          <tr>
            <td data-label="Produtor">{{ item.producer }}</td>
            <td data-label="Intervalo">{{ item.interval }}</td>
            <td data-label="Vitória anterior">{{ item.previousWin }}</td>
            <td data-label="Vitória seguinte">{{ item.followingWin }}</td>
          </tr>
        }
      </tbody>
    </table>
  `,
})
export class IntervalTable {
  readonly caption = input.required<string>();
  readonly items = input.required<ProducerInterval[]>();
}
