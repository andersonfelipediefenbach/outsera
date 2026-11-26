import { Routes } from '@angular/router';
import { DashboardPage } from './dashboard/dashboard-page';

/** Rotas do front (contracts/frontend-ui.md, "Rotas"). */
export const routes: Routes = [
  { path: '', component: DashboardPage, title: 'Painel — Golden Raspberry Awards' },
  {
    path: 'filmes',
    loadComponent: () => import('./movies/movie-list-page').then((m) => m.MovieListPage),
    title: 'Filmes — Golden Raspberry Awards',
  },
  { path: '**', redirectTo: '' },
];
