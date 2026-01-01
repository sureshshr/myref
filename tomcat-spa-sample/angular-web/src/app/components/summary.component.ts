import { AsyncPipe, CurrencyPipe, DecimalPipe, KeyValuePipe, NgFor } from '@angular/common';
import { Component, inject } from '@angular/core';
import { map } from 'rxjs';

import { departments } from '../models/employee';
import { EmployeeService, summarizeEmployees } from '../services/employee.service';

@Component({
  standalone: true,
  imports: [AsyncPipe, CurrencyPipe, DecimalPipe, NgFor, KeyValuePipe],
  template: `
    <div class="card">
      <h2 style="margin-top:0;">Summary</h2>

      <div *ngIf="vm$ | async as vm" class="row">
        <div class="card">
          <div class="muted">Total employees</div>
          <div style="font-size: 28px; font-weight: 650;">{{ vm.total }}</div>
        </div>

        <div class="card">
          <div class="muted">Active / Inactive</div>
          <div style="font-size: 18px;">
            {{ vm.active }} / {{ vm.inactive }}
          </div>
        </div>

        <div class="card">
          <div class="muted">Total salary</div>
          <div style="font-size: 18px;">{{ vm.salaryTotal | currency }}</div>
        </div>

        <div class="card">
          <div class="muted">Average salary</div>
          <div style="font-size: 18px;">{{ vm.salaryAvg | currency }}</div>
        </div>
      </div>

      <div *ngIf="vm$ | async as vm" style="margin-top: 14px;">
        <h3 style="margin-bottom: 8px;">Employees by Department</h3>
        <table class="table">
          <thead>
            <tr>
              <th>Department</th>
              <th>Count</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let d of departmentList">
              <td>{{ d }}</td>
              <td>{{ vm.byDepartment[d] || 0 }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `
})
export class SummaryComponent {
  private readonly employeeService = inject(EmployeeService);

  readonly departmentList = departments;

  readonly vm$ = this.employeeService.employees$.pipe(
    map((employees) => summarizeEmployees(employees))
  );
}
