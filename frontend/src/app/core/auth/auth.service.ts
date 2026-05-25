import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, tap } from 'rxjs';
import { AuthSessionPort } from './application/port/out/auth-session.port';

interface LoginResponse {
  username: string;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService implements AuthSessionPort {
  private readonly tokenKey = 'helpdesk_basic_auth_token';
  private readonly apiUrl = 'http://localhost:4000/api/auth/login';
  private readonly recoverUrl = 'http://localhost:4000/api/auth/password/recover';
  private readonly resetUrl = 'http://localhost:4000/api/auth/password/reset';

  constructor(private http: HttpClient) {}

  login(username: string, password: string): Observable<void> {
    const token = btoa(`${username}:${password}`);
    return this.http
      .post<LoginResponse>(this.apiUrl, { username, password })
      .pipe(
        tap(() => sessionStorage.setItem(this.tokenKey, token)),
        map(() => undefined)
      );
  }

  recoverPassword(userOrEmail: string): Observable<void> {
    return this.http.post<void>(this.recoverUrl, { userOrEmail });
  }

  resetPassword(token: string, newPassword: string): Observable<void> {
    return this.http.post<void>(this.resetUrl, { token, newPassword });
  }

  logout(): void {
    sessionStorage.removeItem(this.tokenKey);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  startEntraSession(): void {
    const token = btoa(`entra:${Date.now()}`);
    sessionStorage.setItem(this.tokenKey, token);
  }

  setToken(token: string): void {
    sessionStorage.setItem(this.tokenKey, token);
  }

  getToken(): string | null {
    return sessionStorage.getItem(this.tokenKey);
  }
}
