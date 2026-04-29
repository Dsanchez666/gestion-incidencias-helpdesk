import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { MAILBOX_API_PORT, MailboxApiPort } from './port/out/mailbox-api.port';
import { GraphTraceResponse } from './port/out/graph-trace.model';

@Injectable({ providedIn: 'root' })
export class TraceGraphUseCase {
  private readonly api = inject(MAILBOX_API_PORT) as MailboxApiPort;

  execute(): Observable<GraphTraceResponse> {
    return this.api.traceGraph();
  }
}
