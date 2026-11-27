import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Paginator } from './paginator';

@Component({
  imports: [Paginator],
  template: `<app-paginator [page]="page()" [totalPages]="total()" (pageChange)="changes.push($event)" />`,
})
class Host {
  readonly page = signal(1);
  readonly total = signal(3);
  readonly changes: number[] = [];
}

describe('Paginator', () => {
  async function render(page: number, total: number) {
    const fixture = TestBed.createComponent(Host);
    fixture.componentInstance.page.set(page);
    fixture.componentInstance.total.set(total);
    await fixture.whenStable();
    const el: HTMLElement = fixture.nativeElement;
    const buttons = Object.fromEntries(
      Array.from(el.querySelectorAll('button')).map((b) => [b.textContent?.trim() ?? '', b as HTMLButtonElement]),
    );
    return { fixture, el, buttons };
  }

  it('na primeira página, Primeira e Anterior ficam desabilitados', async () => {
    const { el, buttons } = await render(1, 3);
    expect(buttons['Primeira'].disabled).toBe(true);
    expect(buttons['Anterior'].disabled).toBe(true);
    expect(buttons['Próxima'].disabled).toBe(false);
    expect(el.textContent).toContain('Página 1 de 3');
  });

  it('na última página, Próxima e Última ficam desabilitados', async () => {
    const { buttons } = await render(3, 3);
    expect(buttons['Próxima'].disabled).toBe(true);
    expect(buttons['Última'].disabled).toBe(true);
    expect(buttons['Anterior'].disabled).toBe(false);
  });

  it('emite a página de destino de cada botão', async () => {
    const { fixture, buttons } = await render(2, 5);
    buttons['Primeira'].click();
    buttons['Anterior'].click();
    buttons['Próxima'].click();
    buttons['Última'].click();
    expect(fixture.componentInstance.changes).toEqual([1, 1, 3, 5]);
  });
});
