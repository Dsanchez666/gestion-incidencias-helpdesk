import { InjectionToken } from '@angular/core';

/**
 * Output port for client-side auth session state and credential handling.
 */
export interface AuthSessionPort {
  login(username: string, password: string): import('rxjs').Observable<void>;
  logout(): void;
  isAuthenticated(): boolean;
  setToken(token: string): void;
  getToken(): string | null;
}

export const AUTH_SESSION_PORT = new InjectionToken<AuthSessionPort>('AUTH_SESSION_PORT');
