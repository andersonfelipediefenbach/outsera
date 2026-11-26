import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Type } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';

/** Monta um componente com o HttpClient de teste e devolve o fixture e o controlador das chamadas. */
export async function setup<T>(component: Type<T>): Promise<{ fixture: ComponentFixture<T>; http: HttpTestingController }> {
  TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
  const fixture = TestBed.createComponent(component);
  fixture.detectChanges();
  await fixture.whenStable();
  return { fixture, http: TestBed.inject(HttpTestingController) };
}

/** Atualiza a tela depois de uma resposta. */
export async function settle(fixture: ComponentFixture<unknown>): Promise<HTMLElement> {
  fixture.detectChanges();
  await fixture.whenStable();
  return fixture.nativeElement as HTMLElement;
}

/** Texto das células de cada linha do corpo das tabelas. */
export function rows(el: HTMLElement, selector = 'table'): string[][] {
  return Array.from(el.querySelectorAll(`${selector} tbody tr`)).map((tr) =>
    Array.from(tr.querySelectorAll('td')).map((td) => td.textContent?.trim() ?? ''),
  );
}

/** Responde a chamada com erro de servidor. */
export const SERVER_ERROR = { status: 500, statusText: 'Erro' };
