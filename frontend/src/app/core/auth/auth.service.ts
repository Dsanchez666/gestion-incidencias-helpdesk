import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, tap } from 'rxjs';

interface LoginResponse {
  username: string;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly tokenKey = 'helpdesk_basic_auth_token';
  private readonly apiUrl = 'http://localhost:4000/api/auth/login';

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

  getToken(): string | null {
    return sessionStorage.getItem(this.tokenKey);
  }
}
