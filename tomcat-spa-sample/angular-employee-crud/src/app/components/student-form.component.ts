import { NgIf } from '@angular/common';
import { Component, OnInit, computed, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { Student, StudentCreate } from '../models/student';
import { StudentService } from '../services/student.service';

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
              <input formControlName="name" placeholder="Student name" />
            </label>
            <div class="error" *ngIf="form.controls.name.touched && form.controls.name.invalid">
              Name is required (min 2 characters).
            </div>
          </div>

          <div>
            <label>
              Email
              <input formControlName="email" placeholder="student&#64;example.invalid" />
            </label>
            <div class="error" *ngIf="form.controls.email.touched && form.controls.email.invalid">
              Valid email is required.
            </div>
          </div>

          <div>
            <label>
              Major
              <input formControlName="major" placeholder="Major" />
            </label>
            <div class="error" *ngIf="form.controls.major.touched && form.controls.major.invalid">
              Major is required.
            </div>
          </div>

          <div>
            <label>
              Year
              <input type="number" formControlName="year" min="1" max="10" />
            </label>
            <div class="error" *ngIf="form.controls.year.touched && form.controls.year.invalid">
              Year must be 1 or higher.
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
          <a routerLink="/students">Cancel</a>
        </div>

        <div class="error" *ngIf="errorMessage()" style="margin-top: 10px;">
          {{ errorMessage() }}
        </div>
      </form>
    </div>
  `
})
export class StudentFormComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly studentService = inject(StudentService);

  private readonly id = computed(() => this.route.snapshot.paramMap.get('id'));
  readonly isEdit = computed(() => !!this.id());

  readonly title = computed(() => (this.isEdit() ? 'Edit Student' : 'New Student'));
  private _errorMessage = '';

  errorMessage(): string {
    return this._errorMessage;
  }

  readonly form = this.fb.nonNullable.group({
    name: this.fb.nonNullable.control('', [Validators.required, Validators.minLength(2)]),
    email: this.fb.nonNullable.control('', [Validators.required, Validators.email]),
    major: this.fb.nonNullable.control('', [Validators.required]),
    year: this.fb.nonNullable.control(1, [Validators.required, Validators.min(1)]),
    active: this.fb.nonNullable.control(true)
  });

  constructor() {}

  async ngOnInit(): Promise<void> {
    const id = this.id();
    if (!id) return;

    try {
      await this.studentService.refresh();
      const existing = this.studentService.getById(id);
      if (!existing) {
        this._errorMessage = 'Student not found.';
        return;
      }

      this.form.patchValue({
        name: existing.name,
        email: existing.email,
        major: existing.major,
        year: existing.year,
        active: existing.active
      });
    } catch (e) {
      this._errorMessage = e instanceof Error ? e.message : 'Failed to load student';
    }
  }

  async save(): Promise<void> {
    this._errorMessage = '';

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    const id = this.id();

    try {
      if (!id) {
        const input: StudentCreate = {
          name: value.name,
          email: value.email,
          major: value.major,
          year: Number(value.year),
          active: value.active
        };
        await this.studentService.create(input);
      } else {
        const existing = this.studentService.getById(id);
        if (!existing) {
          this._errorMessage = 'Student not found.';
          return;
        }

        const updated: Omit<Student, 'createdAtIso' | 'updatedAtIso'> = {
          ...existing,
          name: value.name,
          email: value.email,
          major: value.major,
          year: Number(value.year),
          active: value.active
        };

        await this.studentService.update(updated);
      }

      this.router.navigate(['/students']);
    } catch (e) {
      this._errorMessage = e instanceof Error ? e.message : 'Save failed';
    }
  }
}
