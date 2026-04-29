import { Component } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { Router } from '@angular/router';
import { ConnectionResult } from '../application/port/out/mailbox-api.port';
import { GraphTraceResponse, MailboxFolderResult } from '../application/port/out/graph-trace.model';
import { ListMailboxesUseCase } from '../application/list-mailboxes.usecase';
import { TestGraphConnectionUseCase } from '../application/test-graph-connection.usecase';
import { TestExchangeConnectionUseCase } from '../application/test-exchange-connection.usecase';
import { TraceGraphUseCase } from '../application/trace-graph.usecase';
import { TraceGraphUserUseCase } from '../application/trace-graph-user.usecase';
import { Mailbox } from '../domain/mailbox.model';
import { LogoutUseCase } from '../../../core/auth/application/logout.usecase';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-mailbox-list',
  standalone: true,
  imports: [NgFor, NgIf],
  templateUrl: './mailbox-list.component.html',
  styleUrl: './mailbox-list.component.scss'
})
export class MailboxListComponent {
  mailboxes: Mailbox[] = [];
  error = '';
  testMessage = '';
  graphTraceMessage = '';
  graphTraceError = '';
  graphAppTrace: GraphTraceResponse | null = null;
  graphUserTrace: GraphTraceResponse | null = null;
  graphTraceLoading = false;

  constructor(
    private listMailboxesUseCase: ListMailboxesUseCase,
    private testGraphConnectionUseCase: TestGraphConnectionUseCase,
    private testExchangeConnectionUseCase: TestExchangeConnectionUseCase,
    private traceGraphUseCase: TraceGraphUseCase,
    private traceGraphUserUseCase: TraceGraphUserUseCase,
    private logoutUseCase: LogoutUseCase,
    private router: Router
  ) {
    this.refresh();
  }

  refresh(): void {
    this.error = '';
    this.testMessage = '';
    this.graphTraceMessage = '';
    this.graphTraceError = '';
    this.graphAppTrace = null;
    this.graphUserTrace = null;
    console.groupCollapsed('Buzones UI: refresh');
    console.info('Solicitando listado de buzones');
    console.groupEnd();
    this.listMailboxesUseCase.execute().subscribe({
      next: (data) => {
        this.mailboxes = data;
        console.groupCollapsed('Buzones UI: refresh resultado');
        console.info(`Recibidos ${data.length} buzones`);
        console.table(data);
        console.groupEnd();
      },
      error: (error) => {
        this.mailboxes = [];
        this.error = 'No se pudieron cargar los buzones configurados';
        console.error('Buzones UI: error cargando buzones', error);
      }
    });
  }

  testGraph(): void {
    this.runTest('MS Graph', () => this.testGraphConnectionUseCase.execute());
  }

  testExchange(): void {
    this.runTest('Exchange', () => this.testExchangeConnectionUseCase.execute());
  }

  traceGraph(): void {
    this.runTrace('Graph app', () => this.traceGraphUseCase.execute(), 'app');
  }

  traceGraphUser(): void {
    this.runTrace('Graph user', () => this.traceGraphUserUseCase.execute(), 'user');
  }

  private runTest(label: string, call: () => Observable<ConnectionResult[]>): void {
    this.error = '';
    this.testMessage = `Probando conexion ${label}...`;
    console.groupCollapsed(`Buzones UI: ${label} start`);
    console.info('Estado UI', { error: this.error, testMessage: this.testMessage });
    console.groupEnd();
    call().subscribe({
      next: (results) => {
        const failed = results.filter((r) => r.status !== 'ok');
        console.groupCollapsed(`Buzones UI: ${label} resultado`);
        console.table(results);
        console.info('Resumen', { total: results.length, failed: failed.length });
        console.groupEnd();
        if (failed.length === 0) {
          this.testMessage = `${label}: conexion OK (${results.length} buzones).`;
          return;
        }
        this.testMessage = `${label}: errores en ${failed.length}/${results.length} buzones.`;
      },
      error: (error) => {
        this.testMessage = `${label}: no se pudo completar la prueba.`;
        console.error(`Buzones UI: ${label} error`, error);
      }
    });
  }

  private runTrace(
    label: string,
    call: () => Observable<GraphTraceResponse>,
    mode: 'app' | 'user'
  ): void {
    this.error = '';
    this.graphTraceError = '';
    this.graphTraceMessage = `Probando trazado ${label}...`;
    this.graphTraceLoading = true;
    console.groupCollapsed(`Buzones UI: trace ${label} start`);
    console.info('Estado UI', {
      graphTraceMessage: this.graphTraceMessage,
      graphTraceLoading: this.graphTraceLoading
    });
    console.groupEnd();
    call().subscribe({
      next: (response) => {
        this.graphTraceLoading = false;
        this.graphTraceMessage = `${label}: ${response.success ? 'respuesta recibida' : 'respuesta con error'}.`;
        if (mode === 'app') {
          this.graphAppTrace = response;
        } else {
          this.graphUserTrace = response;
        }
        this.logTraceResponse(label, response);
      },
      error: (err) => {
        this.graphTraceLoading = false;
        this.graphTraceMessage = `${label}: no se pudo completar la prueba.`;
        this.graphTraceError = this.readErrorMessage(err);
        console.error(`Buzones UI: trace ${label} error`, err);
      }
    });
  }

  getMailboxTrace(mailbox: Mailbox, mode: 'app' | 'user'): MailboxFolderResult | null {
    const response = mode === 'app' ? this.graphAppTrace : this.graphUserTrace;
    const mailboxes = response?.mailboxes ?? [];
    if (mailboxes.length === 0) {
      return null;
    }
    return mailboxes.find((entry) => entry.direccionCorreo === mailbox.direccionCorreo) ?? null;
  }

  trackByMailbox(_index: number, mailbox: Mailbox): string {
    return mailbox.id;
  }

  trackByFolder(index: number, folder: { id?: string | null; displayName?: string | null }): string {
    return folder.id ?? folder.displayName ?? `${index}`;
  }

  private readErrorMessage(err: unknown): string {
    if (err && typeof err === 'object' && 'error' in err) {
      const errorBody = (err as { error?: unknown }).error;
      if (typeof errorBody === 'string') {
        return errorBody;
      }
      if (errorBody && typeof errorBody === 'object' && 'error' in errorBody) {
        const nestedError = (errorBody as { error?: unknown }).error;
        if (typeof nestedError === 'string') {
          return nestedError;
        }
      }
    }
    return 'No se pudo obtener el detalle del error.';
  }

  private logTraceResponse(label: string, response: GraphTraceResponse): void {
    console.groupCollapsed(`Buzones UI: trace ${label} resultado`);
    console.info('Success', response.success);
    console.info('Error', response.error);
    console.info('Trace lines', response.traces?.length ?? 0);
    if ((response.traces?.length ?? 0) > 0) {
      console.info('Traces', response.traces);
    }
    const mailboxRows = (response.mailboxes ?? []).map((m) => ({
      id: m.id,
      nombre: m.nombre,
      correo: m.direccionCorreo,
      status: m.status,
      error: m.error,
      folderCount: m.folders?.length ?? 0
    }));
    if (mailboxRows.length > 0) {
      console.table(mailboxRows);
    }
    console.groupEnd();
  }

  gestionarIncidencias(): void {
    this.router.navigateByUrl('/incidencias');
  }

  logout(): void {
    this.logoutUseCase.execute();
    this.router.navigateByUrl('/login');
  }
}
