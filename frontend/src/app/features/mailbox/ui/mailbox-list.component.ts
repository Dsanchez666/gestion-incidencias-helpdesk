import { Component } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { Router } from '@angular/router';
import { MailboxApiService } from '../infrastructure/mailbox-api.service';
import { Mailbox } from '../domain/mailbox.model';
import { AuthService } from '../../../core/auth/auth.service';

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

  constructor(
    private mailboxApiService: MailboxApiService,
    private authService: AuthService,
    private router: Router
  ) {
    this.refresh();
  }

  refresh(): void {
    this.error = '';
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

  gestionarIncidencias(): void {
    this.router.navigateByUrl('/incidencias');
  }

  logout(): void {
    this.authService.logout();
    this.router.navigateByUrl('/login');
  }
}
