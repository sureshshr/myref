import { AsyncPipe, CurrencyPipe, DatePipe, NgFor, NgIf } from '@angular/common';
import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { map } from 'rxjs';

import { EmployeeService } from '../services/employee.service';

@Component({
  standalone: true,
  imports: [NgIf, NgFor, AsyncPipe, RouterLink, CurrencyPipe, DatePipe],
  template: `
    <div class="card">
      <div style="display:flex; justify-content: space-between; align-items:center; gap:12px;">
        <div>
          <h2 style="margin:0;">Employees</h2>
          <div class="muted">Create, edit, delete employees (stored in H2 database).</div>
        </div>
        <button type="button" (click)="create()">+ New</button>
      </div>

      <div *ngIf="(vm$ | async) as vm" style="margin-top: 14px;">
        <div class="muted" style="margin-bottom: 10px;">Total: {{ vm.total }}</div>

        <table class="table" *ngIf="vm.total > 0; else empty">
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Department</th>
              <th>Salary</th>
              <th>Status</th>
              <th>Updated</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let e of vm.employees">
              <td>{{ e.name }}</td>
              <td>{{ e.email }}</td>
              <td>{{ e.department }}</td>
              <td>{{ e.salary | currency }}</td>
              <td>{{ e.active ? 'Active' : 'Inactive' }}</td>
              <td>{{ e.updatedAtIso | date:'medium' }}</td>
              <td class="actions">
                <button type="button" (click)="edit(e.id)">Edit</button>
                <button type="button" (click)="remove(e.id, e.name)">Delete</button>
              </td>
            </tr>
          </tbody>
        </table>

        <ng-template #empty>
          <p class="muted">No employees yet. Click <strong>New</strong>.</p>
        </ng-template>
      </div>
    </div>
  `
})
export class EmployeeListComponent {
  private readonly router = inject(Router);
  private readonly employeeService = inject(EmployeeService);

  readonly vm$ = this.employeeService.employees$.pipe(
    map((employees) => ({ employees, total: employees.length }))
  );

  create(): void {
    this.router.navigate(['/employees/new']);
  }

  edit(id: string): void {
    this.router.navigate(['/employees', id, 'edit']);
  }

  async remove(id: string, name: string): Promise<void> {
    const ok = confirm(`Delete ${name}?`);
    if (!ok) return;
    try {
      await this.employeeService.remove(id);
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Delete failed');
    }
  }
}
