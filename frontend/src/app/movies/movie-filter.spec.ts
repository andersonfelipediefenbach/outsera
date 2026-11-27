import { convertToParamMap } from '@angular/router';
import { readFilter, toQueryParams } from './movie-filter';

describe('filtro da lista na URL', () => {
  it('lê ano, vencedor e página válidos', () => {
    expect(readFilter(convertToParamMap({ ano: '1990', vencedor: 'sim', pagina: '3' }))).toEqual({
      ano: 1990,
      vencedor: 'sim',
      pagina: 3,
    });
    expect(readFilter(convertToParamMap({ vencedor: 'nao' }))).toEqual({ vencedor: 'nao', pagina: 1 });
  });

  it('sem parâmetros, é a página 1 sem filtros', () => {
    expect(readFilter(convertToParamMap({}))).toEqual({ pagina: 1 });
  });

  it.each([
    { ano: 'abc' },
    { ano: '-1' },
    { vencedor: 'talvez' },
    { pagina: '0' },
    { pagina: 'x' },
    { pagina: '-2' },
  ])('descarta o parâmetro inválido %j', (params) => {
    expect(readFilter(convertToParamMap(params))).toEqual({ pagina: 1 });
  });

  it('escreve o filtro na URL, removendo o que está vazio e a página 1', () => {
    expect(toQueryParams({ ano: 1990, vencedor: 'sim', pagina: 1 })).toEqual({ ano: 1990, vencedor: 'sim', pagina: null });
    expect(toQueryParams({ pagina: 4 })).toEqual({ ano: null, vencedor: null, pagina: 4 });
  });
});
