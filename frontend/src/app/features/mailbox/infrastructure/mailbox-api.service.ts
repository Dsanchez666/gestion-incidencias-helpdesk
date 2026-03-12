import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Mailbox } from '../domain/mailbox.model';

export interface ConnectionResult {
  id: string;
  nombre: string;
  direccionCorreo: string;
  status: string;
  error?: string;
}

@Injectable({ providedIn: 'root' })
export class MailboxApiService {
  private readonly baseUrl = 'http://localhost:4000/api/buzones';
  private readonly testGraphUrl = 'http://localhost:4000/api/mailboxes/graph/test';
  private readonly testExchangeUrl = 'http://localhost:4000/api/mailboxes/exchange/test';

  constructor(private http: HttpClient) {}

  list(): Observable<Mailbox[]> {
    return this.http.get<Mailbox[]>(this.baseUrl);
  }

  testGraph(): Observable<ConnectionResult[]> {
    return this.http.post<ConnectionResult[]>(this.testGraphUrl, {});
  }

  testExchange(): Observable<ConnectionResult[]> {
    return this.http.post<ConnectionResult[]>(this.testExchangeUrl, {});
  }
}
