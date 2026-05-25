import { Injectable, inject } from '@angular/core';
import { AUTH_SESSION_PORT, AuthSessionPort } from './port/out/auth-session.port';

@Injectable({ providedIn: 'root' })
export class ResetPasswordUseCase {
  private readonly authSessionPort = inject(AUTH_SESSION_PORT) as AuthSessionPort;

  execute(token: string, newPassword: string) {
    return this.authSessionPort.resetPassword(token, newPassword);
  }
}

