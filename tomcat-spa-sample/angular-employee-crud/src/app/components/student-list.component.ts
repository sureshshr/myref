import { AsyncPipe, DatePipe, NgFor, NgIf } from '@angular/common';
import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { map } from 'rxjs';

import { StudentService } from '../services/student.service';

@Component({
  standalone: true,
  imports: [NgIf, NgFor, AsyncPipe, RouterLink, DatePipe],
  template: `
    <div class="card">
      <div style="display:flex; justify-content: space-between; align-items:center; gap:12px;">
        <div>
          <h2 style="margin:0;">Students</h2>
          <div class="muted">Create, edit, delete students (JAX-RS + H2 database).</div>
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
              <th>Major</th>
              <th>Year</th>
              <th>Status</th>
              <th>Updated</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let s of vm.students">
              <td>{{ s.name }}</td>
              <td>{{ s.email }}</td>
              <td>{{ s.major }}</td>
              <td>{{ s.year }}</td>
              <td>{{ s.active ? 'Active' : 'Inactive' }}</td>
              <td>{{ s.updatedAtIso | date:'medium' }}</td>
              <td class="actions">
                <button type="button" (click)="edit(s.id)">Edit</button>
                <button type="button" (click)="remove(s.id, s.name)">Delete</button>
              </td>
            </tr>
          </tbody>
        </table>

        <ng-template #empty>
          <p class="muted">No students yet. Click <strong>New</strong>.</p>
        </ng-template>
      </div>
    </div>
  `
})
export class StudentListComponent {
  private readonly router = inject(Router);
  private readonly studentService = inject(StudentService);

  readonly vm$ = this.studentService.students$.pipe(
    map((students) => ({ students, total: students.length }))
  );

  create(): void {
    this.router.navigate(['/students/new']);
  }

  edit(id: string): void {
    this.router.navigate(['/students', id, 'edit']);
  }

  async remove(id: string, name: string): Promise<void> {
    const ok = confirm(`Delete ${name}?`);
    if (!ok) return;
    try {
      await this.studentService.remove(id);
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Delete failed');
    }
  }
}
