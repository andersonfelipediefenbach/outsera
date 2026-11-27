import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { App } from './app';
import { routes } from './app.routes';

describe('App', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter(routes), provideHttpClient(), provideHttpClientTesting()],
    });
  });

  it('marca o item do menu da tela atual com aria-current', async () => {
    const fixture = TestBed.createComponent(App);
    await TestBed.inject(Router).navigateByUrl('/');
    await fixture.whenStable();
    const links: HTMLAnchorElement[] = Array.from(fixture.nativeElement.querySelectorAll('nav a'));
    expect(links.map((a) => a.textContent?.trim())).toEqual(['Painel', 'Filmes']);
    expect(links[0].getAttribute('aria-current')).toBe('page');
    expect(links[1].getAttribute('aria-current')).toBeNull();
    TestBed.inject(HttpTestingController).match(() => true);
  });

  it('redireciona uma rota inexistente para o painel', async () => {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/nao-existe');
    expect(TestBed.inject(Router).url).toBe('/');
    TestBed.inject(HttpTestingController).match(() => true);
  });
});
