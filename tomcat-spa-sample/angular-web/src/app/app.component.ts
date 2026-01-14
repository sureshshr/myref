import { AsyncPipe, NgIf } from '@angular/common';
import { Component, inject } from '@angular/core';
import {
  NavigationCancel,
  NavigationEnd,
  NavigationError,
  NavigationStart,
  Router,
  RouterLink,
  RouterLinkActive,
  RouterOutlet,
} from '@angular/router';
import { AppId, AppShellService } from './services/app-shell.service';
import { LoadingService } from './services/loading.service';
import {
  combineLatest,
  distinctUntilChanged,
  filter,
  map,
  shareReplay,
  startWith,
  switchMap,
  take,
  timer,
} from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [NgIf, AsyncPipe, RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="shell" [class.has-sidebar]="sidebarVisible$ | async" [attr.aria-busy]="(isBusy$ | async) ? 'true' : null">
      <header class="topbar">
        <strong class="topbar-brand">Employee CRUD</strong>
        <nav class="topbar-nav">
          <a routerLink="/" (click)="onHomeClick()" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }" ariaCurrentWhenActive="page">Home</a>
          <a routerLink="/contact" routerLinkActive="active" ariaCurrentWhenActive="page">Contact</a>
        </nav>
      </header>

      <div class="shell-body">
        <aside *ngIf="sidebarVisible$ | async" class="sidebar" aria-label="App menu">
          <div class="sidebar-brand">{{ (sidebarVm$ | async)?.title }}</div>
          <nav class="sidebar-nav">
            <a
              *ngFor="let item of (sidebarVm$ | async)?.items"
              [routerLink]="item.link"
              [queryParams]="item.queryParams"
              routerLinkActive="active"
              [routerLinkActiveOptions]="item.exact ? { exact: true } : undefined"
              ariaCurrentWhenActive="page">
              {{ item.label }}
            </a>
          </nav>
        </aside>

        <main class="content">
          <div class="content-inner">
            <router-outlet />
          </div>
        </main>
      </div>

      <footer class="footer" aria-label="Footer">
        <div class="footer-inner">Sample app shell • Tomcat SPA</div>
      </footer>
    </div>

    <div *ngIf="isBusy$ | async" class="global-loading-overlay" aria-live="polite" aria-label="Loading">
      <div class="global-loading">Loading…</div>
    </div>
  `
})
export class AppComponent {
  private readonly router = inject(Router);
  private readonly loading = inject(LoadingService);
  private readonly shell = inject(AppShellService);

  private readonly MIN_LOADING_VISIBLE_MS = 350;

  private readonly navigationEnd$ = this.router.events.pipe(
    filter(
      (e): e is NavigationEnd | NavigationCancel | NavigationError =>
        e instanceof NavigationEnd || e instanceof NavigationCancel || e instanceof NavigationError
    )
  );

  private readonly url$ = this.router.events.pipe(
    filter((e): e is NavigationEnd => e instanceof NavigationEnd),
    map((e) => e.urlAfterRedirects),
    startWith(this.router.url),
    distinctUntilChanged(),
    shareReplay({ bufferSize: 1, refCount: true })
  );

  private readonly isHomeRoute$ = this.url$.pipe(
    map((url) => url === '/' || url === ''),
    distinctUntilChanged(),
    shareReplay({ bufferSize: 1, refCount: true })
  );

  private readonly inferredAppId$ = this.url$.pipe(
    map((url): AppId | null => {
      if (url.startsWith('/employees')) return 'employees';
      if (url.startsWith('/contact')) return 'contact';
      return null;
    }),
    distinctUntilChanged(),
    shareReplay({ bufferSize: 1, refCount: true })
  );

  private readonly activeAppId$ = combineLatest([
    this.shell.selectedAppId$,
    this.inferredAppId$,
  ]).pipe(
    map(([selected, inferred]) => selected ?? inferred),
    distinctUntilChanged(),
    shareReplay({ bufferSize: 1, refCount: true })
  );

  readonly sidebarVisible$ = combineLatest([
    this.isHomeRoute$,
    this.activeAppId$,
  ]).pipe(
    map(([isHome, activeApp]) => !isHome && !!activeApp),
    distinctUntilChanged(),
    shareReplay({ bufferSize: 1, refCount: true })
  );

  readonly sidebarVm$ = this.activeAppId$.pipe(
    map((appId) => buildSidebarVm(appId)),
    shareReplay({ bufferSize: 1, refCount: true })
  );

  private readonly isNavigating$ = this.router.events.pipe(
    filter((e): e is NavigationStart => e instanceof NavigationStart),
    switchMap(() => {
      const startedAt = Date.now();
      return this.navigationEnd$.pipe(
        take(1),
        switchMap(() => {
          const elapsedMs = Date.now() - startedAt;
          const remainingMs = Math.max(0, this.MIN_LOADING_VISIBLE_MS - elapsedMs);
          return timer(remainingMs).pipe(map(() => false));
        }),
        startWith(true)
      );
    }),
    startWith(false),
    distinctUntilChanged(),
    shareReplay({ bufferSize: 1, refCount: true })
  );

  readonly isBusy$ = combineLatest([
    this.isNavigating$,
    this.loading.isLoading$,
  ]).pipe(
    map(([isNavigating, isLoading]) => isNavigating || isLoading),
    distinctUntilChanged(),
    shareReplay({ bufferSize: 1, refCount: true })
  );

  onHomeClick(): void {
    this.shell.clearSelection();
  }
}

type SidebarItemVm = { label: string; link: string; exact?: boolean; queryParams?: Record<string, string> };
type SidebarVm = { title: string; items: ReadonlyArray<SidebarItemVm> };

function buildSidebarVm(appId: AppId | null): SidebarVm {
  switch (appId) {
    case 'contact':
      return {
        title: 'Contact',
        items: [
          { label: 'Contact', link: '/contact', exact: true },
          { label: 'Home', link: '/', exact: true },
        ],
      };
    case 'employees':
    default:
      return {
        title: 'Employees',
        items: [
          { label: 'Employee List', link: '/employees', exact: true },
          { label: 'Add Employee', link: '/employees/new' },
          { label: 'Active Employees', link: '/employees', queryParams: { active: 'true' } },
          { label: 'Inactive Employees', link: '/employees', queryParams: { active: 'false' } },
          { label: 'Engineering Dept', link: '/employees', queryParams: { department: 'Engineering' } },
          { label: 'HR Dept', link: '/employees', queryParams: { department: 'HR' } },
        ],
      };
  }
}
