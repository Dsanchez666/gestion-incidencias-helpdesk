import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

interface EntraMockResponse {
  result: 'valid_token' | 'invalid_token' | 'error';
  accessToken?: string;
  error?: string;
}

@Injectable({ providedIn: 'root' })
export class EntraMockService {
  private readonly apiUrl = 'http://localhost:4000/api/auth/entra/mock-test';

  constructor(private http: HttpClient) {}

  validateToken(): Observable<EntraMockResponse> {
    return this.http.post<EntraMockResponse>(this.apiUrl, {});
  }
}
