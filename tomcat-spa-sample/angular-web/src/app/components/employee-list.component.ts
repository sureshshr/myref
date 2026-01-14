import { AsyncPipe, CurrencyPipe, DatePipe, NgFor, NgIf } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { combineLatest, map, startWith } from 'rxjs';

import { EmployeeService } from '../services/employee.service';
import { Department, Employee, departments } from '../models/employee';

type SortKey = 'id' | 'name' | 'email' | 'department' | 'salary' | 'active' | 'updatedAtIso';
type SortDir = 'asc' | 'desc';

@Component({
  standalone: true,
  imports: [NgIf, NgFor, AsyncPipe, CurrencyPipe, DatePipe, ReactiveFormsModule],
  templateUrl: './employee-list.component.html',
})
export class EmployeeListComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly employeeService = inject(EmployeeService);

  readonly departmentOptions = departments;

  readonly filters = this.fb.nonNullable.group({
    id: this.fb.nonNullable.control(''),
    name: this.fb.nonNullable.control(''),
    email: this.fb.nonNullable.control(''),
    department: this.fb.nonNullable.control('' as '' | Department),
    active: this.fb.nonNullable.control('' as '' | 'true' | 'false'),
    pageIndex: this.fb.nonNullable.control(0),
    pageSize: this.fb.nonNullable.control(10),
  });

  private sortKey: SortKey = 'updatedAtIso';
  private sortDir: SortDir = 'desc';

  readonly vm$ = combineLatest([
    this.employeeService.employees$,
    this.filters.valueChanges.pipe(startWith(this.filters.getRawValue())),
  ]).pipe(
    map(([employees, filters]) => {
      const idFilter = (filters.id ?? '').trim().toLowerCase();
      const nameFilter = (filters.name ?? '').trim().toLowerCase();
      const emailFilter = (filters.email ?? '').trim().toLowerCase();
      const departmentFilter = (filters.department ?? '') as '' | Department;
      const activeFilter = (filters.active ?? '') as '' | 'true' | 'false';

      const filtered = employees.filter((e) => {
        if (idFilter && !e.id.toLowerCase().includes(idFilter)) return false;
        if (nameFilter && !e.name.toLowerCase().includes(nameFilter)) return false;
        if (emailFilter && !e.email.toLowerCase().includes(emailFilter)) return false;
        if (departmentFilter && e.department !== departmentFilter) return false;
        if (activeFilter === 'true' && !e.active) return false;
        if (activeFilter === 'false' && e.active) return false;
        return true;
      });

      const sorted = [...filtered].sort((a, b) => compareEmployees(a, b, this.sortKey, this.sortDir));

      const pageSize = Math.max(1, Number(filters.pageSize ?? 10));
      const pageCount = Math.max(1, Math.ceil(sorted.length / pageSize));
      const pageIndex = clampNumber(Number(filters.pageIndex ?? 0), 0, pageCount - 1);

      if (pageIndex !== filters.pageIndex) {
        this.filters.patchValue({ pageIndex }, { emitEvent: false });
      }

      const start = pageIndex * pageSize;
      const end = Math.min(sorted.length, start + pageSize);
      const pageEmployees = sorted.slice(start, end);

      const pages = Array.from({ length: pageCount }, (_, i) => i);

      return {
        total: sorted.length,
        pageEmployees,
        pageIndex,
        pageCount,
        pages,
        pageStart: sorted.length === 0 ? 0 : start,
        pageEnd: sorted.length === 0 ? 0 : end,
      };
    })
  );

  ngOnInit(): void {
    const params = this.route.snapshot.queryParamMap;
    const active = params.get('active');
    const department = params.get('department');

    const patch: Partial<{
      active: '' | 'true' | 'false';
      department: '' | Department;
      pageIndex: number;
    }> = {};

    if (active === 'true' || active === 'false') {
      patch.active = active;
    }

    if (
      department === 'Engineering' ||
      department === 'HR' ||
      department === 'Sales' ||
      department === 'Finance' ||
      department === 'Operations'
    ) {
      patch.department = department;
    }

    if (Object.keys(patch).length > 0) {
      patch.pageIndex = 0;
      this.filters.patchValue(patch);
    }
  }

  create(): void {
    this.router.navigate(['/employees/new']);
  }

  edit(id: string): void {
    this.router.navigate(['/employees', id, 'edit']);
  }

  sortBy(key: SortKey): void {
    if (this.sortKey === key) {
      this.sortDir = this.sortDir === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortKey = key;
      this.sortDir = key === 'updatedAtIso' ? 'desc' : 'asc';
    }
    this.filters.patchValue({ pageIndex: 0 });
  }

  sortLabel(key: SortKey): string {
    if (this.sortKey !== key) return '';
    return this.sortDir === 'asc' ? ' ▲' : ' ▼';
  }

  prevPage(): void {
    const current = this.filters.controls.pageIndex.value;
    this.filters.patchValue({ pageIndex: Math.max(0, current - 1) });
  }

  nextPage(): void {
    const current = this.filters.controls.pageIndex.value;
    this.filters.patchValue({ pageIndex: current + 1 });
  }

  goToPage(pageIndex: number): void {
    this.filters.patchValue({ pageIndex: Math.max(0, Number(pageIndex || 0)) });
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

function clampNumber(value: number, min: number, max: number): number {
  if (!Number.isFinite(value)) return min;
  return Math.min(max, Math.max(min, value));
}

function compareEmployees(a: Employee, b: Employee, key: SortKey, dir: SortDir): number {
  const direction = dir === 'asc' ? 1 : -1;

  switch (key) {
    case 'id':
      return direction * a.id.localeCompare(b.id);
    case 'salary':
      return direction * (Number(a.salary || 0) - Number(b.salary || 0));
    case 'active':
      return direction * (Number(a.active) - Number(b.active));
    case 'updatedAtIso':
      return direction * a.updatedAtIso.localeCompare(b.updatedAtIso);
    case 'department':
      return direction * a.department.localeCompare(b.department);
    case 'email':
      return direction * a.email.localeCompare(b.email);
    case 'name':
    default:
      return direction * a.name.localeCompare(b.name);
  }
}
