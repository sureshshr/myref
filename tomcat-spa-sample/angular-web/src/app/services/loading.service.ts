import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class LoadingService {
  private readonly MIN_VISIBLE_MS = 350;

  private activeRequests = 0;
  private becameBusyAt = 0;
  private hideTimer: ReturnType<typeof setTimeout> | undefined;

  private readonly isLoadingSubject = new BehaviorSubject<boolean>(false);
  readonly isLoading$ = this.isLoadingSubject.asObservable();

  begin(): void {
    if (this.hideTimer) {
      clearTimeout(this.hideTimer);
      this.hideTimer = undefined;
    }

    if (this.activeRequests === 0) {
      this.becameBusyAt = Date.now();
      this.isLoadingSubject.next(true);
    }

    this.activeRequests += 1;
  }

  end(): void {
    if (this.activeRequests === 0) return;

    this.activeRequests -= 1;
    if (this.activeRequests !== 0) return;

    const elapsedMs = Date.now() - this.becameBusyAt;
    const remainingMs = Math.max(0, this.MIN_VISIBLE_MS - elapsedMs);

    if (remainingMs === 0) {
      this.isLoadingSubject.next(false);
      return;
    }

    this.hideTimer = setTimeout(() => {
      this.hideTimer = undefined;
      this.isLoadingSubject.next(false);
    }, remainingMs);
  }
}
