import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ConnectionResult, MAILBOX_API_PORT, MailboxApiPort } from './port/out/mailbox-api.port';

/**
 * Application use case for Graph connectivity checks.
 */
@Injectable({ providedIn: 'root' })
export class TestGraphConnectionUseCase {
  private readonly api = inject(MAILBOX_API_PORT) as MailboxApiPort;

  execute(): Observable<ConnectionResult[]> {
    return this.api.testGraph();
  }
}
