import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AUTH_SESSION_PORT, AuthSessionPort } from './port/out/auth-session.port';

/**
 * Application use case that authenticates a user against the local auth adapter.
 */
@Injectable({ providedIn: 'root' })
export class LoginUseCase {
  private readonly authSessionPort = inject(AUTH_SESSION_PORT) as AuthSessionPort;

  execute(username: string, password: string): Observable<void> {
    return this.authSessionPort.login(username, password);
  }
}
