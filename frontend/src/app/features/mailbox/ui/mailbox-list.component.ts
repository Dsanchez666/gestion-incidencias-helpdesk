import { Component } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { Router } from '@angular/router';
import { ConnectionResult, MailboxApiService } from '../infrastructure/mailbox-api.service';
import { Mailbox } from '../domain/mailbox.model';
import { AuthService } from '../../../core/auth/auth.service';
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

  constructor(
    private mailboxApiService: MailboxApiService,
    private authService: AuthService,
    private router: Router
  ) {
    this.refresh();
  }

  refresh(): void {
    this.error = '';
    this.testMessage = '';
    this.mailboxApiService.list().subscribe({
      next: (data) => {
        this.mailboxes = data;
      },
      error: () => {
        this.mailboxes = [];
        this.error = 'No se pudieron cargar los buzones configurados';
      }
    });
  }

  testGraph(): void {
    this.runTest('MS Graph', () => this.mailboxApiService.testGraph());
  }

  testExchange(): void {
    this.runTest('Exchange', () => this.mailboxApiService.testExchange());
  }

  private runTest(label: string, call: () => Observable<ConnectionResult[]>): void {
    this.error = '';
    this.testMessage = `Probando conexion ${label}...`;
    call().subscribe({
      next: (results) => {
        const failed = results.filter((r) => r.status !== 'ok');
        if (failed.length === 0) {
          this.testMessage = `${label}: conexion OK (${results.length} buzones).`;
          return;
        }
        this.testMessage = `${label}: errores en ${failed.length}/${results.length} buzones.`;
      },
      error: () => {
        this.testMessage = `${label}: no se pudo completar la prueba.`;
      }
    });
  }

  gestionarIncidencias(): void {
    this.router.navigateByUrl('/incidencias');
  }

  logout(): void {
    this.authService.logout();
    this.router.navigateByUrl('/login');
  }
}
