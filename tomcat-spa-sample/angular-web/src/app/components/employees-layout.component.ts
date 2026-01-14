import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="employees-shell">
      <aside class="left-menu" aria-label="Employees menu">
        <div class="left-menu-title">Employees</div>

        <a
          routerLink="/employees"
          routerLinkActive="active"
          [routerLinkActiveOptions]="{ exact: true }"
          ariaCurrentWhenActive="page">
          Employee List
        </a>

        <a routerLink="/employees/new" routerLinkActive="active" ariaCurrentWhenActive="page">
          Add Employee
        </a>
      </aside>

      <section class="employees-content">
        <router-outlet />
      </section>
    </div>
  `,
})
export class EmployeesLayoutComponent {}
