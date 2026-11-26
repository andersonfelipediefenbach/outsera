// Teste de carga da API (spec 004, FR-010 a FR-013, SC-004; research R4 e R5; data-model.md, "Cenário de
// carga"). Roda no serviço k6 do compose (perfil load), contra BASE_URL, depois da readiness.
//
// - Dois cenários de taxa constante, por 2 min: intervalos (50 req/s) e listagem (10 req/s, com página e
//   filtros sorteados). Um aquecimento de 15 s vem antes e fica fora dos limites, que são filtrados pelo
//   cenário.
// - contract_errors conta a resposta fora do contrato: status diferente de 200 ou, nos intervalos, corpo
//   sem exatamente as chaves min e max e itens com exatamente producer, interval, previousWin e
//   followingWin (FR-012). Um 401 conta como erro, e não como latência boa (US2-5).
// - Limites provisórios (R5), por variável de ambiente; vazios, valem os padrões abaixo. Os limites finais
//   saem da medição no runner (T025 e T033 da spec 004). O k6 sai com 99 quando um limite falha.
// - handleSummary grava /out/summary.json e /out/summary.md e imprime o resumo na saída.
import http from 'k6/http';
import { Rate } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://app:8080';
const TOKEN = __ENV.API_TOKEN || '';

function limit(name, fallback) {
  const value = __ENV[name];
  return value === undefined || value === '' ? fallback : Number(value);
}

const P95_INTERVALS = limit('K6_P95_INTERVALS_MS', 2000);
const P99_INTERVALS = limit('K6_P99_INTERVALS_MS', 4000);
const P95_MOVIES = limit('K6_P95_MOVIES_MS', 2000);
const P99_MOVIES = limit('K6_P99_MOVIES_MS', 4000);
const MAX_ERROR_RATE = limit('K6_MAX_ERROR_RATE', 0.05);

const WARMUP = '15s';
const DURATION = '2m';

const contractErrors = new Rate('contract_errors');

function errorThreshold() {
  return [{ threshold: `rate<${MAX_ERROR_RATE}`, abortOnFail: true, delayAbortEval: '10s' }];
}

export const options = {
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(95)', 'p(99)'],
  scenarios: {
    warmup: {
      executor: 'constant-arrival-rate',
      exec: 'warmup',
      rate: 20,
      timeUnit: '1s',
      duration: WARMUP,
      preAllocatedVUs: 10,
      maxVUs: 50,
    },
    intervals: {
      executor: 'constant-arrival-rate',
      exec: 'intervals',
      rate: 50,
      timeUnit: '1s',
      duration: DURATION,
      startTime: WARMUP,
      preAllocatedVUs: 20,
      maxVUs: 200,
    },
    movies: {
      executor: 'constant-arrival-rate',
      exec: 'movies',
      rate: 10,
      timeUnit: '1s',
      duration: DURATION,
      startTime: WARMUP,
      preAllocatedVUs: 5,
      maxVUs: 50,
    },
  },
  thresholds: {
    'http_req_failed{endpoint:intervals,scenario:intervals}': errorThreshold(),
    'http_req_failed{endpoint:movies,scenario:movies}': errorThreshold(),
    'contract_errors{endpoint:intervals,scenario:intervals}': errorThreshold(),
    'contract_errors{endpoint:movies,scenario:movies}': errorThreshold(),
    'http_req_duration{endpoint:intervals,scenario:intervals}': [`p(95)<${P95_INTERVALS}`, `p(99)<${P99_INTERVALS}`],
    'http_req_duration{endpoint:movies,scenario:movies}': [`p(95)<${P95_MOVIES}`, `p(99)<${P99_MOVIES}`],
    // Só para o resumo ter a vazão por endpoint (submétricas existem quando um limite as cita).
    'http_reqs{endpoint:intervals,scenario:intervals}': ['count>0'],
    'http_reqs{endpoint:movies,scenario:movies}': ['count>0'],
  },
};

function params(endpoint) {
  const headers = TOKEN ? { Authorization: `Bearer ${TOKEN}` } : {};
  return { headers, tags: { endpoint } };
}

const ITEM_KEYS = 'followingWin,interval,previousWin,producer';

function intervalsBodyOk(response) {
  let body;
  try {
    body = response.json();
  } catch (e) {
    return false;
  }
  if (body === null || typeof body !== 'object' || Array.isArray(body)) {
    return false;
  }
  if (Object.keys(body).sort().join(',') !== 'max,min') {
    return false;
  }
  return [body.min, body.max].every((list) => Array.isArray(list)
      && list.every((item) => item !== null && typeof item === 'object'
          && Object.keys(item).sort().join(',') === ITEM_KEYS));
}

export function warmup() {
  http.get(`${BASE_URL}/api/producers/intervals`, params('warmup'));
}

export function intervals() {
  const response = http.get(`${BASE_URL}/api/producers/intervals`, params('intervals'));
  contractErrors.add(response.status !== 200 || !intervalsBodyOk(response), { endpoint: 'intervals' });
}

export function movies() {
  const page = Math.floor(Math.random() * 50);
  let query = `page=${page}&size=20`;
  const filter = Math.floor(Math.random() * 3);
  if (filter === 1) {
    query += `&year=${1900 + Math.floor(Math.random() * 200)}`;
  } else if (filter === 2) {
    query += `&winner=${Math.random() < 0.5}`;
  }
  const response = http.get(`${BASE_URL}/api/movies?${query}`, params('movies'));
  contractErrors.add(response.status !== 200, { endpoint: 'movies' });
}

function value(data, metric, stat) {
  const entry = data.metrics[metric];
  if (!entry || entry.values[stat] === undefined) {
    return 'n/d';
  }
  const number = entry.values[stat];
  return Number.isInteger(number) ? String(number) : number.toFixed(stat === 'rate' ? 4 : 2);
}

export function handleSummary(data) {
  const lines = [
    '#### k6: limites por endpoint (aquecimento de 15 s fora)',
    '',
    '| Endpoint | Requisições | Vazão (req/s) | p95 (ms) | p99 (ms) | Erro HTTP | Erro de contrato |',
    '| --- | --- | --- | --- | --- | --- | --- |',
  ];
  for (const endpoint of ['intervals', 'movies']) {
    const tag = `{endpoint:${endpoint},scenario:${endpoint}}`;
    lines.push(`| ${endpoint} | ${value(data, `http_reqs${tag}`, 'count')} | ${value(data, `http_reqs${tag}`, 'rate')}`
        + ` | ${value(data, `http_req_duration${tag}`, 'p(95)')} | ${value(data, `http_req_duration${tag}`, 'p(99)')}`
        + ` | ${value(data, `http_req_failed${tag}`, 'rate')} | ${value(data, `contract_errors${tag}`, 'rate')} |`);
  }
  lines.push('');
  lines.push(`Limites: p95 ${P95_INTERVALS} ms e p99 ${P99_INTERVALS} ms nos intervalos; p95 ${P95_MOVIES} ms e`
      + ` p99 ${P99_MOVIES} ms na listagem; erro abaixo de ${MAX_ERROR_RATE}.`);
  const failed = Object.entries(data.metrics)
    .filter(([, metric]) => metric.thresholds && Object.values(metric.thresholds).some((t) => !t.ok))
    .map(([name]) => name);
  lines.push(failed.length === 0 ? 'Todos os limites respeitados.' : `Limites ultrapassados: ${failed.join(', ')}.`);
  const markdown = `${lines.join('\n')}\n`;
  return {
    '/out/summary.json': JSON.stringify(data, null, 2),
    '/out/summary.md': markdown,
    stdout: markdown,
  };
}
