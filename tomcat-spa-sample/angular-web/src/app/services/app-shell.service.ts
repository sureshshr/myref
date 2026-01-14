import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type AppId = 'employees' | 'contact';

const STORAGE_KEY = 'selectedAppId';

@Injectable({ providedIn: 'root' })
export class AppShellService {
  private readonly selectedAppIdSubject = new BehaviorSubject<AppId | null>(
    readSelectedAppId()
  );

  readonly selectedAppId$ = this.selectedAppIdSubject.asObservable();

  getSelectedAppIdSnapshot(): AppId | null {
    return this.selectedAppIdSubject.value;
  }

  selectApp(appId: AppId): void {
    try {
      localStorage.setItem(STORAGE_KEY, appId);
    } catch {
      // ignore
    }
    this.selectedAppIdSubject.next(appId);
  }

  clearSelection(): void {
    try {
      localStorage.removeItem(STORAGE_KEY);
    } catch {
      // ignore
    }
    this.selectedAppIdSubject.next(null);
  }
}

function readSelectedAppId(): AppId | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (
      raw === 'employees' ||
      raw === 'contact'
    ) {
      return raw;
    }
  } catch {
    // ignore
  }
  return null;
}
