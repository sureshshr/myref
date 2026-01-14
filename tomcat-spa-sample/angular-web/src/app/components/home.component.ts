import { NgFor } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';

import { AppId, AppShellService } from '../services/app-shell.service';

type HomeApp = {
  title: string;
  route: string;
  description: string;
  appId: AppId;
};

@Component({
  standalone: true,
  imports: [NgFor, RouterLink],
  templateUrl: './home.component.html',
})
export class HomeComponent {
  private readonly shell = inject(AppShellService);

  readonly apps: ReadonlyArray<HomeApp> = [
    {
      title: 'Employees',
      route: '/employees',
      description: 'Create, edit, delete employees (CRUD).',
      appId: 'employees',
    },
    {
      title: 'Contact',
      route: '/contact',
      description: 'Contact page.',
      appId: 'contact',
    },
  ];

  selectApp(appId: AppId): void {
    this.shell.selectApp(appId);
  }
}
