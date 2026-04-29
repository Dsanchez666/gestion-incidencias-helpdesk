import { Injectable, inject } from '@angular/core';
import { AUTH_SESSION_PORT, AuthSessionPort } from './port/out/auth-session.port';

/**
 * Application use case that reports whether a session is active.
 */
@Injectable({ providedIn: 'root' })
export class IsAuthenticatedUseCase {
  private readonly authSessionPort = inject(AUTH_SESSION_PORT) as AuthSessionPort;

  execute(): boolean {
    return this.authSessionPort.isAuthenticated();
  }
}
