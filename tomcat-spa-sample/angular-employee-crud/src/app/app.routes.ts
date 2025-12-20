import { Routes } from '@angular/router';
import { EmployeeListComponent } from './components/employee-list.component';
import { EmployeeFormComponent } from './components/employee-form.component';
import { SummaryComponent } from './components/summary.component';
import { NotFoundComponent } from './components/not-found.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'employees' },
  { path: 'employees', component: EmployeeListComponent },
  { path: 'employees/new', component: EmployeeFormComponent },
  { path: 'employees/:id/edit', component: EmployeeFormComponent },
  { path: 'summary', component: SummaryComponent },
  { path: '**', component: NotFoundComponent }
];
