import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <header class="header">
      <strong>Employee CRUD</strong>
      <nav class="nav">
        <a routerLink="/employees" routerLinkActive="active" ariaCurrentWhenActive="page">Employees</a>
        <a routerLink="/students" routerLinkActive="active" ariaCurrentWhenActive="page">Students</a>
        <a routerLink="/search" routerLinkActive="active" ariaCurrentWhenActive="page">Search</a>
        <a routerLink="/summary" routerLinkActive="active" ariaCurrentWhenActive="page">Summary</a>
        <a routerLink="/contact" routerLinkActive="active" ariaCurrentWhenActive="page">Contact</a>
      </nav>
    </header>

    <div class="container">
      <router-outlet />
    </div>
  `
})
export class AppComponent {}
