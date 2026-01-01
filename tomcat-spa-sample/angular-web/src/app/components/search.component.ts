import { AsyncPipe, DatePipe, NgFor, NgIf } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { combineLatest, map, startWith } from 'rxjs';

import { departments } from '../models/employee';
import { EmployeeService } from '../services/employee.service';

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, NgIf, NgFor, AsyncPipe, DatePipe],
  template: `
    <div class="card">
      <h2 style="margin-top:0;">Search</h2>
      <div class="muted">Search employees by name/email and optionally filter by department.</div>

      <form [formGroup]="form" class="row" style="margin-top: 14px; align-items: end;">
        <div>
          <label>
            Query
            <input formControlName="query" placeholder="e.g. employee" />
          </label>
        </div>

        <div>
          <label>
            Department
            <select formControlName="department">
              <option value="">Any</option>
              <option *ngFor="let d of departmentOptions" [value]="d">{{ d }}</option>
            </select>
          </label>
        </div>
      </form>

      <div *ngIf="vm$ | async as vm" style="margin-top: 14px;">
        <div class="muted" style="margin-bottom: 10px;">Matches: {{ vm.total }}</div>

        <table class="table" *ngIf="vm.total > 0; else empty">
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Department</th>
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
              <td>{{ e.active ? 'Active' : 'Inactive' }}</td>
              <td>{{ e.updatedAtIso | date:'medium' }}</td>
              <td class="actions">
                <button type="button" (click)="edit(e.id)">Edit</button>
              </td>
            </tr>
          </tbody>
        </table>

        <ng-template #empty>
          <p class="muted">No matches. Try a different query.</p>
        </ng-template>
      </div>
    </div>
  `
})
export class SearchComponent {
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly employeeService = inject(EmployeeService);

  readonly departmentOptions = departments;

  readonly form = this.fb.nonNullable.group({
    query: this.fb.nonNullable.control(''),
    department: this.fb.nonNullable.control('')
  });

  readonly vm$ = combineLatest([
    this.employeeService.employees$,
    this.form.valueChanges.pipe(startWith(this.form.getRawValue()))
  ]).pipe(
    map(([employees, filters]) => {
      const query = (filters.query || '').trim().toLowerCase();
      const department = (filters.department || '').trim();

      const filtered = employees.filter((e) => {
        if (department && e.department !== department) return false;

        if (!query) return true;
        const haystack = `${e.name} ${e.email}`.toLowerCase();
        return haystack.includes(query);
      });

      return { employees: filtered, total: filtered.length };
    })
  );

  edit(id: string): void {
    this.router.navigate(['/employees', id, 'edit']);
  }
}
