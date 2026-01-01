import { Routes } from '@angular/router';
import { EmployeeListComponent } from './components/employee-list.component';
import { EmployeeFormComponent } from './components/employee-form.component';
import { SearchComponent } from './components/search.component';
import { SummaryComponent } from './components/summary.component';
import { ContactComponent } from './components/contact.component';
import { StudentListComponent } from './components/student-list.component';
import { StudentFormComponent } from './components/student-form.component';
import { NotFoundComponent } from './components/not-found.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'employees' },
  { path: 'employees', component: EmployeeListComponent },
  { path: 'employees/new', component: EmployeeFormComponent },
  { path: 'employees/:id/edit', component: EmployeeFormComponent },
  { path: 'students', component: StudentListComponent },
  { path: 'students/new', component: StudentFormComponent },
  { path: 'students/:id/edit', component: StudentFormComponent },
  { path: 'search', component: SearchComponent },
  { path: 'summary', component: SummaryComponent },
  { path: 'contact', component: ContactComponent },
  { path: '**', component: NotFoundComponent }
];
