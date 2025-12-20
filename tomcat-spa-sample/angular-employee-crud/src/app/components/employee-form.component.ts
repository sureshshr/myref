import { NgIf } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { Department, Employee, departments } from '../models/employee';
import { EmployeeService } from '../services/employee.service';

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, NgIf, RouterLink],
  template: `
    <div class="card">
      <h2 style="margin-top:0;">{{ title() }}</h2>

      <form [formGroup]="form" (ngSubmit)="save()">
        <div class="row">
          <div>
            <label>
              Name
              <input formControlName="name" placeholder="Employee name" />
            </label>
            <div class="error" *ngIf="form.controls.name.touched && form.controls.name.invalid">
              Name is required (min 2 characters).
            </div>
          </div>

          <div>
            <label>
              Email
              <input formControlName="email" placeholder="name@company.com" />
            </label>
            <div class="error" *ngIf="form.controls.email.touched && form.controls.email.invalid">
              Valid email is required.
            </div>
          </div>

          <div>
            <label>
              Department
              <select formControlName="department">
                <option *ngFor="let d of departmentOptions" [value]="d">{{ d }}</option>
              </select>
            </label>
          </div>

          <div>
            <label>
              Salary
              <input type="number" formControlName="salary" />
            </label>
            <div class="error" *ngIf="form.controls.salary.touched && form.controls.salary.invalid">
              Salary must be 0 or higher.
            </div>
          </div>

          <div>
            <label>
              Active
              <select formControlName="active">
                <option [ngValue]="true">Active</option>
                <option [ngValue]="false">Inactive</option>
              </select>
            </label>
          </div>
        </div>

        <div style="margin-top: 14px; display:flex; gap:10px;">
          <button type="submit" [disabled]="form.invalid">Save</button>
          <a routerLink="/employees">Cancel</a>
        </div>

        <div class="error" *ngIf="errorMessage()" style="margin-top: 10px;">
          {{ errorMessage() }}
        </div>
      </form>
    </div>
  `
})
export class EmployeeFormComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly employeeService = inject(EmployeeService);

  readonly departmentOptions = departments;

  private readonly id = computed(() => this.route.snapshot.paramMap.get('id'));
  readonly isEdit = computed(() => !!this.id());

  readonly title = computed(() => (this.isEdit() ? 'Edit Employee' : 'New Employee'));
  readonly errorMessage = computed(() => this._errorMessage);
  private _errorMessage = '';

  readonly form = this.fb.nonNullable.group({
    name: this.fb.nonNullable.control('', [Validators.required, Validators.minLength(2)]),
    email: this.fb.nonNullable.control('', [Validators.required, Validators.email]),
    department: this.fb.nonNullable.control<Department>('Engineering'),
    salary: this.fb.nonNullable.control(0, [Validators.required, Validators.min(0)]),
    active: this.fb.nonNullable.control(true)
  });

  constructor() {
    const id = this.id();
    if (!id) return;

    const existing = this.employeeService.getById(id);
    if (!existing) {
      this._errorMessage = 'Employee not found.';
      return;
    }

    this.form.patchValue({
      name: existing.name,
      email: existing.email,
      department: existing.department,
      salary: existing.salary,
      active: existing.active
    });
  }

  save(): void {
    this._errorMessage = '';

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    const id = this.id();

    try {
      if (!id) {
        this.employeeService.create({
          name: value.name,
          email: value.email,
          department: value.department,
          salary: Number(value.salary),
          active: value.active
        });
      } else {
        const existing = this.employeeService.getById(id);
        if (!existing) {
          this._errorMessage = 'Employee not found.';
          return;
        }

        const updated: Omit<Employee, 'createdAtIso' | 'updatedAtIso'> = {
          ...existing,
          name: value.name,
          email: value.email,
          department: value.department,
          salary: Number(value.salary),
          active: value.active
        };

        this.employeeService.update(updated);
      }

      this.router.navigate(['/employees']);
    } catch (e) {
      this._errorMessage = e instanceof Error ? e.message : 'Save failed';
    }
  }
}
