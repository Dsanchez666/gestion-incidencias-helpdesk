import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  EntraUserTokenPort,
  EntraUserTokenRequest,
  EntraUserTokenResponse
} from './application/port/out/entra-user-token.port';

@Injectable({ providedIn: 'root' })
export class EntraUserService implements EntraUserTokenPort {
  private readonly apiUrl = 'http://localhost:4000/api/auth/entra/test';

  constructor(private http: HttpClient) {}

  getUserToken(request: EntraUserTokenRequest): Observable<EntraUserTokenResponse> {
    return this.http.post<EntraUserTokenResponse>(this.apiUrl, request);
  }
}

