import { Injectable, inject } from '@angular/core';
import { AUTH_SESSION_PORT, AuthSessionPort } from './port/out/auth-session.port';

/**
 * Application use case that clears the active auth session.
 */
@Injectable({ providedIn: 'root' })
export class LogoutUseCase {
  private readonly authSessionPort = inject(AUTH_SESSION_PORT) as AuthSessionPort;

  execute(): void {
    this.authSessionPort.logout();
  }
}
