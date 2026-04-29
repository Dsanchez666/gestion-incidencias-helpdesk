import { InjectionToken } from '@angular/core';
import { Observable } from 'rxjs';

export interface EntraUserTokenRequest {
  username: string;
  password: string;
}

export interface EntraUserTokenResponse {
  success: boolean;
  accessToken?: string | null;
  error?: string | null;
}

/**
 * Output port for requesting a delegated Entra token with user credentials.
 */
export interface EntraUserTokenPort {
  getUserToken(request: EntraUserTokenRequest): Observable<EntraUserTokenResponse>;
}

export const ENTRA_USER_TOKEN_PORT = new InjectionToken<EntraUserTokenPort>('ENTRA_USER_TOKEN_PORT');

