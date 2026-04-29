import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Mailbox } from '../domain/mailbox.model';
import { GraphTraceResponse } from '../application/port/out/graph-trace.model';
import { ConnectionResult, MailboxApiPort } from '../application/port/out/mailbox-api.port';

@Injectable({ providedIn: 'root' })
export class MailboxApiService implements MailboxApiPort {
  private readonly baseUrl = 'http://localhost:4000/api/buzones';
  private readonly testGraphUrl = 'http://localhost:4000/api/mailboxes/graph/test';
  private readonly testExchangeUrl = 'http://localhost:4000/api/mailboxes/exchange/test';
  private readonly graphTraceUrl = 'http://localhost:4000/api/mailboxes/graph/trace';
  private readonly graphUserTraceUrl = 'http://localhost:4000/api/mailboxes/graph/user/trace';

  constructor(private http: HttpClient) {}

  list(): Observable<Mailbox[]> {
    console.info('MailboxApi: GET /api/buzones start');
    return this.http.get<Mailbox[]>(this.baseUrl).pipe(
      tap({
        next: (mailboxes) => console.info('MailboxApi: GET /api/buzones ok', { count: mailboxes.length, mailboxes }),
        error: (error) => console.error('MailboxApi: GET /api/buzones error', error)
      })
    );
  }

  testGraph(): Observable<ConnectionResult[]> {
    console.info('MailboxApi: POST /api/mailboxes/graph/test start');
    return this.http.post<ConnectionResult[]>(this.testGraphUrl, {}).pipe(
      tap({
        next: (results) => console.info('MailboxApi: POST /graph/test ok', this.summarizeConnectionResults(results)),
        error: (error) => console.error('MailboxApi: POST /graph/test error', error)
      })
    );
  }

  testExchange(): Observable<ConnectionResult[]> {
    console.info('MailboxApi: POST /api/mailboxes/exchange/test start');
    return this.http.post<ConnectionResult[]>(this.testExchangeUrl, {}).pipe(
      tap({
        next: (results) => console.info('MailboxApi: POST /exchange/test ok', this.summarizeConnectionResults(results)),
        error: (error) => console.error('MailboxApi: POST /exchange/test error', error)
      })
    );
  }

  traceGraph(): Observable<GraphTraceResponse> {
    console.info('MailboxApi: POST /api/mailboxes/graph/trace start');
    return this.http.post<GraphTraceResponse>(this.graphTraceUrl, {}).pipe(
      tap({
        next: (response) => console.info('MailboxApi: POST /graph/trace ok', this.summarizeTrace(response)),
        error: (error) => console.error('MailboxApi: POST /graph/trace error', error)
      })
    );
  }

  traceGraphUser(): Observable<GraphTraceResponse> {
    console.info('MailboxApi: POST /api/mailboxes/graph/user/trace start');
    return this.http.post<GraphTraceResponse>(this.graphUserTraceUrl, {}).pipe(
      tap({
        next: (response) => console.info('MailboxApi: POST /graph/user/trace ok', this.summarizeTrace(response)),
        error: (error) => console.error('MailboxApi: POST /graph/user/trace error', error)
      })
    );
  }

  private summarizeConnectionResults(results: ConnectionResult[]): unknown {
    const failed = results.filter((r) => r.status !== 'ok');
    return {
      total: results.length,
      failed: failed.length,
      failures: failed
    };
  }

  private summarizeTrace(response: GraphTraceResponse): unknown {
    const mailboxes = response.mailboxes ?? [];
    const failed = mailboxes.filter((m) => m.status !== 'ok');
    return {
      success: response.success,
      error: response.error,
      traces: response.traces?.length ?? 0,
      mailboxes: mailboxes.length,
      failed: failed.length,
      failedEntries: failed
    };
  }
}
