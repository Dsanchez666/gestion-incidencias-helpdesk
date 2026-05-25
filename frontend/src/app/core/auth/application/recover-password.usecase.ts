import { Injectable, inject } from '@angular/core';
import { AUTH_SESSION_PORT, AuthSessionPort } from './port/out/auth-session.port';

@Injectable({ providedIn: 'root' })
export class RecoverPasswordUseCase {
  private readonly authSessionPort = inject(AUTH_SESSION_PORT) as AuthSessionPort;

  execute(userOrEmail: string) {
    return this.authSessionPort.recoverPassword(userOrEmail);
  }
}

