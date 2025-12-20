import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="card">
      <h2 style="margin-top:0;">Not Found</h2>
      <p class="muted">That route doesn’t exist.</p>
      <a routerLink="/employees">Go to Employees</a>
    </div>
  `
})
export class NotFoundComponent {}
