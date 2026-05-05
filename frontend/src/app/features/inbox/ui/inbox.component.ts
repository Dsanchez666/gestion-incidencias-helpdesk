import { Component } from '@angular/core';
import { DatePipe, NgFor, NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { InboxApiService } from '../infrastructure/inbox-api.service';
import { InboxItem, Tecnico } from '../domain/inbox.model';
import { MailboxUiConfigService } from '../../../core/config/mailbox-ui-config.service';
import { LogoutUseCase } from '../../../core/auth/application/logout.usecase';

@Component({
  selector: 'app-inbox',
  standalone: true,
  imports: [NgIf, NgFor, FormsModule, DatePipe],
  templateUrl: './inbox.component.html',
  styleUrl: './inbox.component.scss'
})
export class InboxComponent {
  items: InboxItem[] = [];
  tecnicos: Tecnico[] = [];
  summaryLength = 50;
  error = '';
  loading = true;

  constructor(
    private inboxApi: InboxApiService,
    private config: MailboxUiConfigService,
    private logoutUseCase: LogoutUseCase,
    private router: Router
  ) {
    this.config.getPreviewLength().subscribe((length) => {
      this.summaryLength = length;
      this.refresh();
    });
    this.loadTecnicos();
  }

  refresh(): void {
    this.loading = true;
    this.error = '';
    this.inboxApi.list(this.summaryLength).subscribe({
      next: (items) => {
        this.items = items;
        this.loading = false;
      },
      error: () => {
        this.error = 'No se pudo cargar la bandeja de entrada';
        this.items = [];
        this.loading = false;
      }
    });
  }

  onIncidenciaChange(item: InboxItem): void {
    this.inboxApi.updateIncidencia(item.messageId, item.incidenciaGenerada).subscribe({ error: () => this.refresh() });
  }

  onAsignacionChange(item: InboxItem): void {
    if (!item.asignada) {
      item.tecnicoAsignado = '';
    }
    this.inboxApi
      .updateAsignacion(item.messageId, item.asignada, item.tecnicoAsignado ?? '')
      .subscribe({ error: () => this.refresh() });
  }

  private loadTecnicos(): void {
    this.inboxApi.listTecnicos().subscribe({
      next: (tecnicos) => {
        this.tecnicos = tecnicos;
      },
      error: () => {
        this.tecnicos = [];
      }
    });
  }

  logout(): void {
    this.logoutUseCase.execute();
    this.router.navigateByUrl('/startup');
  }
}
