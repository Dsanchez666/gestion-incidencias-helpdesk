import { InjectionToken } from '@angular/core';
import { Observable } from 'rxjs';
import { Mailbox } from '../../../domain/mailbox.model';
import { GraphTraceResponse } from './graph-trace.model';

export interface ConnectionResult {
  id: string;
  nombre: string;
  direccionCorreo: string;
  status: string;
  error?: string;
}

/**
 * Output port for mailbox queries and connectivity checks.
 */
export interface MailboxApiPort {
  list(): Observable<Mailbox[]>;
  testGraph(): Observable<ConnectionResult[]>;
  testExchange(): Observable<ConnectionResult[]>;
  traceGraph(): Observable<GraphTraceResponse>;
  traceGraphUser(): Observable<GraphTraceResponse>;
}

export const MAILBOX_API_PORT = new InjectionToken<MailboxApiPort>('MAILBOX_API_PORT');
