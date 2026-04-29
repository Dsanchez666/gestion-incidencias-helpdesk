import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { EntraAppTokenPort, EntraAppTokenResponse } from './application/port/out/entra-app-token.port';

@Injectable({ providedIn: 'root' })
export class EntraAppService implements EntraAppTokenPort {
  private readonly apiUrl = 'http://localhost:4000/api/auth/entra/app-token';

  constructor(private http: HttpClient) {}

  getAppToken(): Observable<EntraAppTokenResponse> {
    return this.http.post<EntraAppTokenResponse>(this.apiUrl, {});
  }
}
