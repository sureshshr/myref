import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, firstValueFrom } from 'rxjs';
import { Department, Employee } from '../models/employee';

type EmployeeCreate = Omit<Employee, 'id' | 'createdAtIso' | 'updatedAtIso'>;
type EmployeeUpdate = Omit<Employee, 'createdAtIso' | 'updatedAtIso'>;

const API_BASE = new URL('../api/employees', document.baseURI).toString();

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private readonly http = inject(HttpClient);

  private readonly employeesSubject = new BehaviorSubject<Employee[]>([]);
  readonly employees$ = this.employeesSubject.asObservable();

  constructor() {
    void this.refresh();
  }

  getSnapshot(): Employee[] {
    return this.employeesSubject.value;
  }

  getById(id: string): Employee | undefined {
    return this.getSnapshot().find((e) => e.id === id);
  }

  async create(input: EmployeeCreate): Promise<Employee> {
    const created = await firstValueFrom(this.http.post<Employee>(API_BASE, input));
    await this.refresh();
    return created;
  }

  async update(input: EmployeeUpdate): Promise<Employee> {
    const id = input.id;
    const body = {
      name: input.name,
      email: input.email,
      department: input.department,
      salary: input.salary,
      active: input.active,
    };
    const updated = await firstValueFrom(
      this.http.put<Employee>(`${API_BASE}/${encodeURIComponent(id)}`, body)
    );
    await this.refresh();
    return updated;
  }

  async remove(id: string): Promise<void> {
    await firstValueFrom(this.http.delete(`${API_BASE}/${encodeURIComponent(id)}`));
    await this.refresh();
  }

  async refresh(): Promise<void> {
    const employees = await firstValueFrom(this.http.get<Employee[]>(API_BASE));
    this.employeesSubject.next(employees);
  }
}

export function summarizeEmployees(employees: Employee[]): {
  total: number;
  active: number;
  inactive: number;
  salaryTotal: number;
  salaryAvg: number;
  byDepartment: Record<Department, number>;
} {
  const byDepartment: Record<Department, number> = {
    Engineering: 0,
    HR: 0,
    Sales: 0,
    Finance: 0,
    Operations: 0
  };

  let active = 0;
  let salaryTotal = 0;

  for (const e of employees) {
    if (e.active) active += 1;
    salaryTotal += Number(e.salary || 0);
    byDepartment[e.department] = (byDepartment[e.department] || 0) + 1;
  }

  const total = employees.length;
  const inactive = total - active;
  const salaryAvg = total === 0 ? 0 : salaryTotal / total;

  return { total, active, inactive, salaryTotal, salaryAvg, byDepartment };
}
