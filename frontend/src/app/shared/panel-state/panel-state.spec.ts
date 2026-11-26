import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Subject, of, throwError } from 'rxjs';
import { Loader, PanelStatus } from './loader';
import { PanelStateView } from './panel-state';

@Component({
  imports: [PanelStateView],
  template: `<app-panel-state [status]="status()" emptyMessage="Nada aqui." (retry)="retries = retries + 1" />`,
})
class Host {
  readonly status = signal<PanelStatus>('carregando');
  retries = 0;
}

describe('PanelStateView', () => {
  async function render(status: PanelStatus) {
    const fixture = TestBed.createComponent(Host);
    fixture.componentInstance.status.set(status);
    await fixture.whenStable();
    return fixture;
  }

  it('mostra "Carregando…" com aria-live', async () => {
    const el: HTMLElement = (await render('carregando')).nativeElement;
    const message = el.querySelector('[aria-live="polite"]');
    expect(message?.textContent).toContain('Carregando…');
  });

  it('mostra a mensagem de vazio recebida', async () => {
    const el: HTMLElement = (await render('vazio')).nativeElement;
    expect(el.textContent).toContain('Nada aqui.');
  });

  it('no erro, mostra a mensagem e o botão que emite "tentar de novo"', async () => {
    const fixture = await render('erro');
    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('Não foi possível carregar os dados.');
    el.querySelector('button')!.click();
    expect(fixture.componentInstance.retries).toBe(1);
  });

  it('com dados, não mostra nada', async () => {
    const el: HTMLElement = (await render('dados')).nativeElement;
    expect(el.textContent?.trim()).toBe('');
  });
});

describe('Loader', () => {
  it('passa de carregando para dados ou vazio conforme a resposta', () => {
    const loader = new Loader(() => of([1, 2]), (list: number[]) => list.length === 0);
    loader.load();
    expect(loader.state()).toEqual({ status: 'dados', data: [1, 2] });

    const empty = new Loader(() => of([] as number[]), (list) => list.length === 0);
    empty.load();
    expect(empty.state().status).toBe('vazio');
  });

  it('vai para erro quando a chamada falha e volta a carregar no "tentar de novo"', () => {
    let fail = true;
    const pending = new Subject<string>();
    const loader = new Loader(() => (fail ? throwError(() => new Error('x')) : pending), () => false);
    loader.load();
    expect(loader.state().status).toBe('erro');
    fail = false;
    loader.load();
    expect(loader.state().status).toBe('carregando');
    pending.next('ok');
    expect(loader.state()).toEqual({ status: 'dados', data: 'ok' });
  });
});
