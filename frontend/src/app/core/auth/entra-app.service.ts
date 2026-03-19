import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

interface EntraAppTokenResponse {
  success: boolean;
  accessToken?: string | null;
  error?: string | null;
}

@Injectable({ providedIn: 'root' })
export class EntraAppService {
  private readonly apiUrl = 'http://localhost:4000/api/auth/entra/app-token';

  constructor(private http: HttpClient) {}

  getAppToken(): Observable<EntraAppTokenResponse> {
    return this.http.post<EntraAppTokenResponse>(this.apiUrl, {});
  }
}
