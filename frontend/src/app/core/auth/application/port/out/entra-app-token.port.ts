import { InjectionToken } from '@angular/core';
import { Observable } from 'rxjs';

export interface EntraAppTokenResponse {
  success: boolean;
  accessToken?: string | null;
  error?: string | null;
}

/**
 * Output port for requesting an Entra application token.
 */
export interface EntraAppTokenPort {
  getAppToken(): Observable<EntraAppTokenResponse>;
}

export const ENTRA_APP_TOKEN_PORT = new InjectionToken<EntraAppTokenPort>('ENTRA_APP_TOKEN_PORT');
