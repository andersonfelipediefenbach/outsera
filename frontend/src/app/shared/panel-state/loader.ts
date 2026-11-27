import { DestroyRef, inject, signal } from '@angular/core';
import { Observable, Subscription } from 'rxjs';

/** Estados de um painel ou da lista (data-model, "Estado de painel"; spec 006, FR-009). */
export type PanelStatus = 'carregando' | 'dados' | 'vazio' | 'erro';

export interface PanelState<T> {
  status: PanelStatus;
  data?: T;
}

/**
 * Carrega os dados de um painel e guarda o estado num signal. `load()` também serve para "tentar de novo": cancela
 * a chamada anterior e volta a `carregando`.
 */
export class Loader<T> {
  readonly state = signal<PanelState<T>>({ status: 'carregando' });
  private subscription?: Subscription;

  constructor(
    private readonly source: () => Observable<T>,
    private readonly isEmpty: (value: T) => boolean,
    destroyRef?: DestroyRef,
  ) {
    destroyRef?.onDestroy(() => this.subscription?.unsubscribe());
  }

  load(): void {
    this.subscription?.unsubscribe();
    this.state.set({ status: 'carregando' });
    this.subscription = this.source().subscribe({
      next: (value) => this.state.set({ status: this.isEmpty(value) ? 'vazio' : 'dados', data: value }),
      error: () => this.state.set({ status: 'erro' }),
    });
  }
}

/** Cria um `Loader` ligado ao ciclo de vida do componente (chamar num contexto de injeção). */
export function createLoader<T>(source: () => Observable<T>, isEmpty: (value: T) => boolean): Loader<T> {
  return new Loader(source, isEmpty, inject(DestroyRef));
}
