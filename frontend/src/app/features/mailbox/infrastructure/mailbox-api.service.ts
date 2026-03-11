import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Mailbox } from '../domain/mailbox.model';

@Injectable({ providedIn: 'root' })
export class MailboxApiService {
  private readonly baseUrl = 'http://localhost:4000/api/buzones';

  constructor(private http: HttpClient) {}

  list(): Observable<Mailbox[]> {
    return this.http.get<Mailbox[]>(this.baseUrl);
  }
}
