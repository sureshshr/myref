import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, firstValueFrom } from 'rxjs';

import { Student, StudentCreate, StudentUpdate } from '../models/student';

const API_BASE = new URL('../api-jaxrs/students', document.baseURI).toString();

@Injectable({ providedIn: 'root' })
export class StudentService {
  private readonly http = inject(HttpClient);

  private readonly studentsSubject = new BehaviorSubject<Student[]>([]);
  readonly students$ = this.studentsSubject.asObservable();

  constructor() {
    void this.refresh();
  }

  getSnapshot(): Student[] {
    return this.studentsSubject.value;
  }

  getById(id: string): Student | undefined {
    return this.getSnapshot().find((s) => s.id === id);
  }

  async refresh(): Promise<void> {
    const students = await firstValueFrom(this.http.get<Student[]>(API_BASE));
    this.studentsSubject.next(students);
  }

  async create(input: StudentCreate): Promise<Student> {
    const created = await firstValueFrom(this.http.post<Student>(API_BASE, input));
    await this.refresh();
    return created;
  }

  async update(input: StudentUpdate): Promise<Student> {
    const id = input.id;
    const body = {
      name: input.name,
      email: input.email,
      major: input.major,
      year: input.year,
      active: input.active,
    };

    const updated = await firstValueFrom(
      this.http.put<Student>(`${API_BASE}/${encodeURIComponent(id)}`, body)
    );
    await this.refresh();
    return updated;
  }

  async remove(id: string): Promise<void> {
    await firstValueFrom(this.http.delete(`${API_BASE}/${encodeURIComponent(id)}`));
    await this.refresh();
  }
}
