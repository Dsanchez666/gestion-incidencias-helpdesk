import { Injectable, inject } from '@angular/core';
import { AUTH_SESSION_PORT, AuthSessionPort } from './port/out/auth-session.port';

/**
 * Application use case that exposes the stored bearer token to adapters.
 */
@Injectable({ providedIn: 'root' })
export class GetTokenUseCase {
  private readonly authSessionPort = inject(AUTH_SESSION_PORT) as AuthSessionPort;

  execute(): string | null {
    return this.authSessionPort.getToken();
  }
}
