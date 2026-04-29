import { Injectable, inject } from '@angular/core';
import { tap } from 'rxjs';
import { ENTRA_APP_TOKEN_PORT, EntraAppTokenPort, EntraAppTokenResponse } from './port/out/entra-app-token.port';
import { AUTH_SESSION_PORT, AuthSessionPort } from './port/out/auth-session.port';

/**
 * Application use case that requests an Entra app token and stores it locally.
 */
@Injectable({ providedIn: 'root' })
export class InitializeEntraSessionUseCase {
  private readonly entraAppTokenPort = inject(ENTRA_APP_TOKEN_PORT) as EntraAppTokenPort;
  private readonly authSessionPort = inject(AUTH_SESSION_PORT) as AuthSessionPort;

  execute() {
    return this.entraAppTokenPort.getAppToken().pipe(
      tap((response: EntraAppTokenResponse) => {
        if (response.success && response.accessToken) {
          this.authSessionPort.setToken(response.accessToken);
        }
      })
    );
  }
}
