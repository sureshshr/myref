import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Department, Employee } from '../models/employee';

type EmployeeCreate = Omit<Employee, 'id' | 'createdAtIso' | 'updatedAtIso'>;
type EmployeeUpdate = Omit<Employee, 'createdAtIso' | 'updatedAtIso'>;

const STORAGE_KEY = 'employeeCrud.employees.v1';

function nowIso(): string {
  return new Date().toISOString();
}

function newId(): string {
  // Works in modern browsers; fallback for older ones.
  // Tomcat does not generate IDs; this is a client-side app.
  return (globalThis.crypto && 'randomUUID' in globalThis.crypto)
    ? globalThis.crypto.randomUUID()
    : `${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

function safeParseEmployees(raw: string | null): Employee[] {
  if (!raw) return [];
  try {
    const parsed = JSON.parse(raw) as unknown;
    if (!Array.isArray(parsed)) return [];
    return parsed.filter(Boolean) as Employee[];
  } catch {
    return [];
  }
}

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private readonly employeesSubject = new BehaviorSubject<Employee[]>(this.loadInitial());
  readonly employees$ = this.employeesSubject.asObservable();

  getSnapshot(): Employee[] {
    return this.employeesSubject.value;
  }

  getById(id: string): Employee | undefined {
    return this.getSnapshot().find((e) => e.id === id);
  }

  create(input: EmployeeCreate): Employee {
    const timestamp = nowIso();
    const employee: Employee = {
      ...input,
      id: newId(),
      createdAtIso: timestamp,
      updatedAtIso: timestamp
    };

    const next = [employee, ...this.getSnapshot()];
    this.persist(next);
    return employee;
  }

  update(input: EmployeeUpdate): Employee {
    const existing = this.getById(input.id);
    if (!existing) {
      throw new Error('Employee not found');
    }

    const updated: Employee = {
      ...existing,
      ...input,
      updatedAtIso: nowIso()
    };

    const next = this.getSnapshot().map((e) => (e.id === input.id ? updated : e));
    this.persist(next);
    return updated;
  }

  remove(id: string): void {
    const next = this.getSnapshot().filter((e) => e.id !== id);
    this.persist(next);
  }

  private persist(next: Employee[]): void {
    this.employeesSubject.next(next);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(next));
  }

  private loadInitial(): Employee[] {
    const stored = safeParseEmployees(localStorage.getItem(STORAGE_KEY));
    if (stored.length > 0) return stored;

    // Seed data (first run)
    const timestamp = nowIso();
    const seed: Employee[] = [
      {
        id: newId(),
        name: 'Asha Kumar',
        email: 'asha.kumar@example.com',
        department: 'Engineering',
        salary: 95000,
        active: true,
        createdAtIso: timestamp,
        updatedAtIso: timestamp
      },
      {
        id: newId(),
        name: 'test tes',
        email: 'test.tess@example.com',
        department: 'Operations',
        salary: 78000,
        active: true,
        createdAtIso: timestamp,
        updatedAtIso: timestamp
      },
      {
        id: newId(),
        name: 'Meera Iyer',
        email: 'meera.iyer@example.com',
        department: 'Finance',
        salary: 88000,
        active: false,
        createdAtIso: timestamp,
        updatedAtIso: timestamp
      }
    ];

    localStorage.setItem(STORAGE_KEY, JSON.stringify(seed));
    return seed;
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
