import { Injectable, inject } from '@angular/core';
import { tap } from 'rxjs';
import { AUTH_SESSION_PORT, AuthSessionPort } from './port/out/auth-session.port';
import {
  ENTRA_USER_TOKEN_PORT,
  EntraUserTokenPort,
  EntraUserTokenRequest,
  EntraUserTokenResponse
} from './port/out/entra-user-token.port';

@Injectable({ providedIn: 'root' })
export class InitializeEntraUserSessionUseCase {
  private readonly entraUserTokenPort = inject(ENTRA_USER_TOKEN_PORT) as EntraUserTokenPort;
  private readonly authSessionPort = inject(AUTH_SESSION_PORT) as AuthSessionPort;

  execute(request: EntraUserTokenRequest) {
    return this.entraUserTokenPort.getUserToken(request).pipe(
      tap((response: EntraUserTokenResponse) => {
        if (response.success && response.accessToken) {
          this.authSessionPort.setToken(response.accessToken);
        }
      })
    );
  }
}

