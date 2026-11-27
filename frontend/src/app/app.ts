import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

/** Cabeçalho com o menu e a tela atual (spec 006, FR-016). */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <header class="header">
      <div class="header__inner">
        <span class="header__brand">Golden Raspberry Awards</span>
        <nav aria-label="Principal">
          <ul class="menu">
            <li>
              <a
                routerLink="/"
                routerLinkActive="menu__link--active"
                [routerLinkActiveOptions]="{ exact: true }"
                ariaCurrentWhenActive="page"
                class="menu__link"
                >Painel</a
              >
            </li>
            <li>
              <a routerLink="/filmes" routerLinkActive="menu__link--active" ariaCurrentWhenActive="page" class="menu__link"
                >Filmes</a
              >
            </li>
          </ul>
        </nav>
      </div>
    </header>
    <main class="main">
      <router-outlet />
    </main>
  `,
})
export class App {}
