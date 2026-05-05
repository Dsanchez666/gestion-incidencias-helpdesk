import { Component } from '@angular/core';
import { DatePipe, NgFor, NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { InboxApiService } from '../infrastructure/inbox-api.service';
import { Categoria, InboxContext, InboxItem, IncidenciaInboxItem, Tecnico } from '../domain/inbox.model';
import { MailboxUiConfigService } from '../../../core/config/mailbox-ui-config.service';
import { LogoutUseCase } from '../../../core/auth/application/logout.usecase';

type SortDir = 'asc' | 'desc';
type PendingSortKey = 'receivedDateTime' | 'sender' | 'subject' | 'summary';
type IncSortKey = 'receivedDateTime' | 'sender' | 'subject' | 'tecnicoAsignado' | 'categoriaAbreviatura' | 'assignedAt';

@Component({
  selector: 'app-inbox',
  standalone: true,
  imports: [NgIf, NgFor, FormsModule, DatePipe],
  templateUrl: './inbox.component.html',
  styleUrl: './inbox.component.scss'
})
export class InboxComponent {
  context: InboxContext = {
    appName: 'Gestion de Buzon de Incidencias',
    mailboxNombre: '-',
    mailboxCorreo: '-',
    usuarioConectado: '-',
    permisos: '-',
    perfil: 'CONSULTA',
    puedeVerCorreos: false
  };
  items: InboxItem[] = [];
  incidencias: IncidenciaInboxItem[] = [];
  tecnicos: Tecnico[] = [];
  categorias: Categoria[] = [];
  summaryLength = 50;
  error = '';
  loading = true;
  hideAutomatic = true;
  selectedTecnicoByMessageId: Record<string, string> = {};
  selectedCategoriaByIncidenciaId: Record<number, number> = {};
  savingCategoriaByIncidenciaId: Record<number, boolean> = {};
  savingCategorias = false;
  assigningByMessageId: Record<string, boolean> = {};
  selectedMessageIds = new Set<string>();
  bulkTecnicoNombre = '';
  pendingSort: { key: PendingSortKey; dir: SortDir } = { key: 'receivedDateTime', dir: 'desc' };
  incidenciasSort: { key: IncSortKey; dir: SortDir } = { key: 'assignedAt', dir: 'desc' };
  filters = {
    generic: '',
    sender: '',
    subject: '',
    summary: '',
    tecnico: ''
  };

  constructor(
    private inboxApi: InboxApiService,
    private config: MailboxUiConfigService,
    private logoutUseCase: LogoutUseCase,
    private router: Router
  ) {
    this.config.getPreviewLength().subscribe((length) => {
      this.summaryLength = length;
      if (this.context.puedeVerCorreos) {
        this.refresh();
      }
    });
    this.loadContext();
    this.loadIncidencias();
    this.loadCategorias();
  }

  refresh(): void {
    this.loading = true;
    this.error = '';
    this.selectedMessageIds.clear();
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

  get pendingItems(): InboxItem[] {
    const base = this.filteredItems().filter((item) => !item.incidenciaGenerada);
    return [...base].sort((a, b) => this.compareValues(a[this.pendingSort.key], b[this.pendingSort.key], this.pendingSort.dir));
  }

  get pendingShownCount(): number {
    return this.pendingItems.length;
  }

  get pendingTotalCount(): number {
    return this.items.filter((item) => !item.incidenciaGenerada).length;
  }

  get sortedIncidencias(): IncidenciaInboxItem[] {
    return [...this.incidencias].sort((a, b) =>
      this.compareValues(a[this.incidenciasSort.key], b[this.incidenciasSort.key], this.incidenciasSort.dir)
    );
  }

  get incidenciasShownCount(): number {
    return this.sortedIncidencias.length;
  }

  get incidenciasTotalCount(): number {
    return this.incidencias.length;
  }

  togglePendingSort(key: PendingSortKey): void {
    this.pendingSort =
      this.pendingSort.key === key ? { key, dir: this.pendingSort.dir === 'asc' ? 'desc' : 'asc' } : { key, dir: 'asc' };
  }

  toggleIncidenciasSort(key: IncSortKey): void {
    this.incidenciasSort =
      this.incidenciasSort.key === key
        ? { key, dir: this.incidenciasSort.dir === 'asc' ? 'desc' : 'asc' }
        : { key, dir: 'asc' };
  }

  toggleSelectMessage(messageId: string, checked: boolean): void {
    if (checked) {
      this.selectedMessageIds.add(messageId);
    } else {
      this.selectedMessageIds.delete(messageId);
    }
  }

  assignSelectedIncidencias(): void {
    const messageIds = Array.from(this.selectedMessageIds);
    if (messageIds.length === 0 || !this.bulkTecnicoNombre) {
      return;
    }
    this.inboxApi.assignIncidencias(messageIds, this.bulkTecnicoNombre, this.summaryLength).subscribe({
      next: () => {
        this.bulkTecnicoNombre = '';
        this.selectedMessageIds.clear();
        this.refresh();
        this.loadIncidencias();
      }
    });
  }

  assignIncidencia(item: InboxItem): void {
    const tecnicoNombre = this.selectedTecnicoByMessageId[item.messageId] ?? '';
    if (!tecnicoNombre) {
      return;
    }
    this.assigningByMessageId[item.messageId] = true;
    this.inboxApi.assignIncidencia(item.messageId, tecnicoNombre, this.summaryLength).subscribe({
      next: () => {
        this.assigningByMessageId[item.messageId] = false;
        this.refresh();
        this.loadIncidencias();
      },
      error: () => {
        this.assigningByMessageId[item.messageId] = false;
      }
    });
  }

  saveCategoriasPendientes(): void {
    const cambios = this.incidencias
      .map((inc) => ({
        incidenciaId: inc.id,
        actual: inc.categoriaId ?? null,
        nuevo: this.selectedCategoriaByIncidenciaId[inc.id] ?? null
      }))
      .filter((x) => x.nuevo !== null && x.nuevo !== x.actual);

    if (cambios.length === 0) {
      return;
    }

    this.savingCategorias = true;
    let pendientes = cambios.length;

    for (const cambio of cambios) {
      this.inboxApi.updateCategoria(cambio.incidenciaId, cambio.nuevo as number).subscribe({
        next: () => {
          pendientes--;
          if (pendientes === 0) {
            this.savingCategorias = false;
            this.loadIncidencias();
          }
        },
        error: () => {
          pendientes--;
          if (pendientes === 0) {
            this.savingCategorias = false;
            this.loadIncidencias();
          }
        }
      });
    }
  }

  categorizarAutomatico(): void {
    // Placeholder intencional: funcionalidad futura.
  }

  private filteredItems(): InboxItem[] {
    return this.items.filter((item) => {
      if (this.hideAutomatic && item.sender.toLowerCase() === 'servicionotificacionetna@enaire.es') {
        return false;
      }
      if (this.filters.sender && !item.sender.toLowerCase().includes(this.filters.sender.toLowerCase())) {
        return false;
      }
      if (this.filters.subject && !item.subject.toLowerCase().includes(this.filters.subject.toLowerCase())) {
        return false;
      }
      if (this.filters.summary && !item.summary.toLowerCase().includes(this.filters.summary.toLowerCase())) {
        return false;
      }
      if (this.filters.tecnico && !(item.tecnicoAsignado ?? '').toLowerCase().includes(this.filters.tecnico.toLowerCase())) {
        return false;
      }
      if (this.filters.generic) {
        const allText = `${item.sender} ${item.subject} ${item.summary} ${item.tecnicoAsignado ?? ''}`.toLowerCase();
        if (!allText.includes(this.filters.generic.toLowerCase())) {
          return false;
        }
      }
      return true;
    });
  }

  private compareValues(a: unknown, b: unknown, dir: SortDir): number {
    const av = String(a ?? '').toLowerCase();
    const bv = String(b ?? '').toLowerCase();
    const base = av.localeCompare(bv);
    return dir === 'asc' ? base : -base;
  }

  private loadContext(): void {
    this.inboxApi.context().subscribe({
      next: (context) => {
        this.context = context;
        if (this.context.puedeVerCorreos) {
          this.refresh();
          this.loadTecnicos();
        }
      }
    });
  }

  private loadIncidencias(): void {
    this.inboxApi.listIncidencias().subscribe({
      next: (items) => {
        this.incidencias = items;
        for (const it of items) {
          if (it.categoriaId) {
            this.selectedCategoriaByIncidenciaId[it.id] = it.categoriaId;
          }
        }
      },
      error: () => {
        this.incidencias = [];
      }
    });
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

  private loadCategorias(): void {
    this.inboxApi.listCategorias().subscribe({
      next: (categorias) => {
        this.categorias = categorias;
      },
      error: () => {
        this.categorias = [];
      }
    });
  }

  logout(): void {
    this.logoutUseCase.execute();
    this.router.navigateByUrl('/login');
  }
}
