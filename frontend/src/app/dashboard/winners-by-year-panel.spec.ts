import { WinnersByYearPanel } from './winners-by-year-panel';
import { SERVER_ERROR, rows, settle, setup } from '../testing';

function type(el: HTMLElement, value: string): void {
  const input = el.querySelector('input')!;
  input.value = value;
  input.dispatchEvent(new Event('input'));
}

function submit(el: HTMLElement): void {
  el.querySelector('form')!.dispatchEvent(new Event('submit'));
}

const WINNERS_1990 = {
  content: [
    { year: 1990, title: 'The Adventures of Ford Fairlane', studios: '20th Century Fox', producers: 'Steven Perry and Joel Silver', winner: true },
    { year: 1990, title: "Ghosts Can't Do It", studios: null, producers: 'Bo Derek', winner: true },
  ],
  page: 0,
  size: 100,
  totalElements: 2,
  totalPages: 1,
};

describe('WinnersByYearPanel', () => {
  it('antes da busca, mostra só o formulário, com o botão desabilitado', async () => {
    const { fixture, http } = await setup(WinnersByYearPanel);
    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('label')?.textContent).toBe('Ano');
    expect(el.querySelector('button')!.disabled).toBe(true);
    expect(el.querySelector('table')).toBeNull();
    http.verify();
  });

  it.each(['abc', '-1', '19.5'])('ano inválido (%s) mostra a mensagem e não chama a API', async (value) => {
    const { fixture, http } = await setup(WinnersByYearPanel);
    const el: HTMLElement = fixture.nativeElement;
    type(el, value);
    await settle(fixture);
    expect(el.textContent).toContain('Informe um ano válido (só números).');
    expect(el.querySelector('button')!.disabled).toBe(true);
    expect(el.querySelector('input')!.getAttribute('aria-invalid')).toBe('true');
    submit(el);
    http.verify();
  });

  it('com um ano válido, busca os vencedores e mostra a tabela, com "—" para estúdios nulos', async () => {
    const { fixture, http } = await setup(WinnersByYearPanel);
    const el: HTMLElement = fixture.nativeElement;
    type(el, '1990');
    await settle(fixture);
    expect(el.querySelector('button')!.disabled).toBe(false);
    submit(el);
    http.expectOne('/api/movies?year=1990&winner=true&size=100').flush(WINNERS_1990);
    expect(rows(await settle(fixture))).toEqual([
      ['1990', 'The Adventures of Ford Fairlane', '20th Century Fox', 'Steven Perry and Joel Silver'],
      ['1990', "Ghosts Can't Do It", '—', 'Bo Derek'],
    ]);
  });

  it('ano sem vencedor mostra a mensagem com o ano', async () => {
    const { fixture, http } = await setup(WinnersByYearPanel);
    const el: HTMLElement = fixture.nativeElement;
    type(el, '1979');
    await settle(fixture);
    submit(el);
    http.expectOne('/api/movies?year=1979&winner=true&size=100').flush({ ...WINNERS_1990, content: [], totalElements: 0 });
    expect((await settle(fixture)).textContent).toContain('Nenhum vencedor em 1979.');
  });

  it('erro da API mostra a mensagem e "tentar de novo" repete a busca', async () => {
    const { fixture, http } = await setup(WinnersByYearPanel);
    const el: HTMLElement = fixture.nativeElement;
    type(el, '1990');
    await settle(fixture);
    submit(el);
    http.expectOne('/api/movies?year=1990&winner=true&size=100').flush(null, SERVER_ERROR);
    await settle(fixture);
    expect(el.textContent).toContain('Não foi possível carregar os dados.');
    (Array.from(el.querySelectorAll('button')).find((b) => b.textContent === 'Tentar de novo') as HTMLButtonElement).click();
    http.expectOne('/api/movies?year=1990&winner=true&size=100').flush(WINNERS_1990);
    expect(rows(await settle(fixture))).toHaveLength(2);
  });
});
