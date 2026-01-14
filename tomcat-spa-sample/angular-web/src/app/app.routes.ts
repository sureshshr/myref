import { Routes } from '@angular/router';
import { EmployeeListComponent } from './components/employee-list.component';
import { EmployeeFormComponent } from './components/employee-form.component';
import { ContactComponent } from './components/contact.component';
import { NotFoundComponent } from './components/not-found.component';
import { HomeComponent } from './components/home.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'home', redirectTo: '', pathMatch: 'full' },
  { path: 'employees', component: EmployeeListComponent },
  { path: 'employees/new', component: EmployeeFormComponent },
  { path: 'employees/:id/edit', component: EmployeeFormComponent },
  { path: 'contact', component: ContactComponent },
  { path: '**', component: NotFoundComponent }
];
