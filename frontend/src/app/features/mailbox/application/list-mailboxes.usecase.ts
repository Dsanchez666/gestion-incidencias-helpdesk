import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Mailbox } from '../domain/mailbox.model';
import { MAILBOX_API_PORT, MailboxApiPort } from './port/out/mailbox-api.port';

/**
 * Application use case for the configured mailbox list.
 */
@Injectable({ providedIn: 'root' })
export class ListMailboxesUseCase {
  private readonly api = inject(MAILBOX_API_PORT) as MailboxApiPort;

  execute(): Observable<Mailbox[]> {
    return this.api.list();
  }
}
