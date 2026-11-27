import { ParamMap, Params } from '@angular/router';
import { MovieFilter } from '../api/awards-api';
import { parseYear } from '../shared/validators';

/** Página: só dígitos, de 1 até o maior inteiro seguro (spec 006, FR-013; caso 03 do testador adversário). */
const PAGE = /^[1-9]\d*$/;

/**
 * Lê o filtro da URL da lista (contracts/frontend-ui.md, "Parâmetros da URL"). Um valor inválido é descartado:
 * vira "sem filtro de ano", "todos" ou a página 1 (spec 006, FR-013).
 */
export function readFilter(params: ParamMap): MovieFilter {
  const filter: MovieFilter = { pagina: 1 };
  const ano = parseYear(params.get('ano'));
  if (ano !== undefined) {
    filter.ano = ano;
  }
  const vencedor = params.get('vencedor');
  if (vencedor === 'sim' || vencedor === 'nao') {
    filter.vencedor = vencedor;
  }
  const pagina = params.get('pagina') ?? '';
  if (PAGE.test(pagina) && Number.isSafeInteger(Number(pagina))) {
    filter.pagina = Number(pagina);
  }
  return filter;
}

/** Parâmetros da URL para o filtro; `null` remove o parâmetro, e a página 1 não aparece na URL. */
export function toQueryParams(filter: MovieFilter): Params {
  return {
    ano: filter.ano ?? null,
    vencedor: filter.vencedor ?? null,
    pagina: filter.pagina > 1 ? filter.pagina : null,
  };
}
