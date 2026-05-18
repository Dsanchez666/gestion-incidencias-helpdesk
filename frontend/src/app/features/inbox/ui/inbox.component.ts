import { Component } from '@angular/core';
import { DatePipe, NgFor, NgIf, NgStyle } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { InboxApiService } from '../infrastructure/inbox-api.service';
import {
  Categoria,
  InboxContext,
  InboxItem,
  IncidenciaInboxItem,
  IncidenciaNota,
  IncidenciasStatsResponse,
  Tecnico
} from '../domain/inbox.model';
import { MailboxUiConfigService } from '../../../core/config/mailbox-ui-config.service';
import { LogoutUseCase } from '../../../core/auth/application/logout.usecase';

type SortDir = 'asc' | 'desc';
type PendingSortKey = 'receivedDateTime' | 'sender' | 'subject' | 'summary';
type IncSortKey =
  | 'receivedDateTime'
  | 'sender'
  | 'subject'
  | 'tecnicoAsignado'
  | 'categoriaAbreviatura'
  | 'assignedAt'
  | 'resuelta';

@Component({
  selector: 'app-inbox',
  standalone: true,
  imports: [NgIf, NgFor, NgStyle, FormsModule, DatePipe],
  templateUrl: './inbox.component.html',
  styleUrl: './inbox.component.scss'
})
export class InboxComponent {
  private static readonly INTERACTIVE_CLICK_SELECTOR =
    'select, input, textarea, button, a, label, option, [role="button"], [data-no-open-modal]';
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
  stats: IncidenciasStatsResponse | null = null;
  showStatsModal = false;
  showTreatmentModal = false;
  selectedIncidencia: IncidenciaInboxItem | null = null;
  notasIncidencia: IncidenciaNota[] = [];
  notaTecnico = '';
  notaObservacion = '';
  notaDetalle = '';
  notaAccion = '';
  redirigirTecnico = '';
  resolucionDescripcion = '';
  rechazoMotivo = '';
  summaryLength = 50;
  error = '';
  loading = true;
  hideAutomatic = true;
  selectedTecnicoByMessageId: Record<string, string> = {};
  selectedCategoriaByIncidenciaId: Record<number, number> = {};
  selectedTecnicoByIncidenciaId: Record<number, string> = {};
  selectedResueltaByIncidenciaId: Record<number, boolean> = {};
  selectedPrioridadByIncidenciaId: Record<number, string> = {};
  savingIncidencias = false;
  incidenciasTextFilter = '';
  showAdminModal = false;
  adminTab: 'tecnico' | 'categoria' = 'tecnico';
  newTecnicoNombre = '';
  newTecnicoEmail = '';
  newCategoriaNombre = '';
  newCategoriaAbreviatura = '';
  newCategoriaColorHex = '#f3f4f6';
  editCategoriaId: number | null = null;
  editCategoriaNombre = '';
  editCategoriaAbreviatura = '';
  editCategoriaColorHex = '#f3f4f6';
  assigningByMessageId: Record<string, boolean> = {};
  assigningBulk = false;
  pendingSingleAssign: InboxItem | null = null;
  showPrioridadModal = false;
  pendingPrioridad: 'URGENTE' | 'ALTA' | 'NORMAL' | 'BAJA' = 'NORMAL';
  assignFeedback = '';
  assignFeedbackType: 'success' | 'error' | 'info' | '' = '';
  selectedMessageIds = new Set<string>();
  bulkTecnicoNombre = '';
  pendingSort: { key: PendingSortKey; dir: SortDir } = { key: 'receivedDateTime', dir: 'desc' };
  incidenciasSort: { key: IncSortKey; dir: SortDir } = { key: 'assignedAt', dir: 'desc' };
  filters = { generic: '', sender: '', subject: '', summary: '', tecnico: '' };

  constructor(
    private inboxApi: InboxApiService,
    private config: MailboxUiConfigService,
    private logoutUseCase: LogoutUseCase,
    private router: Router
  ) {
    this.config.getPreviewLength().subscribe((length) => {
      this.summaryLength = length;
      if (this.context.puedeVerCorreos) this.refresh();
    });
    this.loadContext();
    this.loadIncidencias();
    this.loadCategorias();
    this.loadTecnicos();
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

  get sortedIncidencias(): IncidenciaInboxItem[] {
    const filtered = this.incidencias.filter((i) => {
      if (!this.incidenciasTextFilter) return true;
      const t = this.incidenciasTextFilter.toLowerCase();
      return `${i.subject} ${i.sender} ${i.tecnicoAsignado} ${i.categoriaAbreviatura ?? ''} ${i.prioridad ?? 'NORMAL'} ${i.receivedDateTime ?? ''}`
        .toLowerCase()
        .includes(t);
    });
    return [...filtered].sort((a, b) => {
      const pa = this.priorityRank(a.prioridad);
      const pb = this.priorityRank(b.prioridad);
      if (pa !== pb) return pa - pb;
      return this.toMillis(a.assignedAt) - this.toMillis(b.assignedAt);
    });
  }

  get pendingShownCount(): number {
    return this.pendingItems.length;
  }
  get pendingTotalCount(): number {
    return this.items.filter((item) => !item.incidenciaGenerada).length;
  }
  get incidenciasShownCount(): number {
    return this.sortedIncidencias.length;
  }
  get incidenciasTotalCount(): number {
    return this.incidencias.length;
  }
  get isTecnicoProfile(): boolean {
    const perfil = (this.context.perfil ?? '').toUpperCase();
    return perfil === 'TECNICO' || perfil === 'RESOLUTOR';
  }
  get isReadOnlyIncidenciasProfile(): boolean {
    return (this.context.perfil ?? '').toUpperCase() !== 'ADMIN';
  }
  get isConsultaProfile(): boolean {
    return (this.context.perfil ?? '').toUpperCase() === 'CONSULTA';
  }

  togglePendingSort(key: PendingSortKey): void {
    this.pendingSort = this.pendingSort.key === key ? { key, dir: this.pendingSort.dir === 'asc' ? 'desc' : 'asc' } : { key, dir: 'asc' };
  }
  toggleIncidenciasSort(key: IncSortKey): void {
    this.incidenciasSort =
      this.incidenciasSort.key === key ? { key, dir: this.incidenciasSort.dir === 'asc' ? 'desc' : 'asc' } : { key, dir: 'asc' };
  }

  toggleSelectMessage(messageId: string, checked: boolean): void {
    if (checked) this.selectedMessageIds.add(messageId);
    else this.selectedMessageIds.delete(messageId);
  }

  assignSelectedIncidencias(): void {
    const messageIds = Array.from(this.selectedMessageIds);
    if (messageIds.length === 0 || !this.bulkTecnicoNombre || this.assigningBulk) return;
    if (this.context.perfil === 'ADMIN') {
      this.pendingSingleAssign = null;
      this.openPrioridadModal();
      return;
    }
    this.executeBulkAssign('NORMAL');
  }

  private executeBulkAssign(prioridad: string): void {
    const messageIds = Array.from(this.selectedMessageIds);
    if (messageIds.length === 0 || !this.bulkTecnicoNombre || this.assigningBulk) return;
    this.assigningBulk = true;
    this.setAssignFeedback(`Procesando creación de ${messageIds.length} incidencia(s)...`, 'info');
    this.inboxApi.assignIncidencias(messageIds, this.bulkTecnicoNombre, prioridad, this.summaryLength).subscribe({
      next: () => {
        this.bulkTecnicoNombre = '';
        this.selectedMessageIds.clear();
        this.assigningBulk = false;
        this.setAssignFeedback(`Incidencias creadas correctamente: ${messageIds.length}.`, 'success');
        this.refresh();
        this.loadIncidencias();
      },
      error: (err) => {
        this.assigningBulk = false;
        this.setAssignFeedback(`Error al crear incidencias: ${this.extractErrorMessage(err)}`, 'error');
      }
    });
  }

  assignIncidencia(item: InboxItem): void {
    const tecnicoNombre = this.selectedTecnicoByMessageId[item.messageId] ?? '';
    if (!tecnicoNombre) return;
    if (this.context.perfil === 'ADMIN') {
      this.pendingSingleAssign = item;
      this.openPrioridadModal();
      return;
    }
    this.executeSingleAssign(item, 'NORMAL');
  }

  private executeSingleAssign(item: InboxItem, prioridad: string): void {
    const tecnicoNombre = this.selectedTecnicoByMessageId[item.messageId] ?? '';
    if (!tecnicoNombre) return;
    this.assigningByMessageId[item.messageId] = true;
    this.setAssignFeedback('Procesando creación de incidencia...', 'info');
    this.inboxApi.assignIncidencia(item.messageId, tecnicoNombre, prioridad, this.summaryLength).subscribe({
      next: () => {
        this.assigningByMessageId[item.messageId] = false;
        this.setAssignFeedback('Incidencia creada correctamente.', 'success');
        this.refresh();
        this.loadIncidencias();
      },
      error: (err) => {
        this.assigningByMessageId[item.messageId] = false;
        this.setAssignFeedback(`Error al crear incidencia: ${this.extractErrorMessage(err)}`, 'error');
      }
    });
  }

  saveIncidenciasPendientes(): void {
    if (this.isTecnicoProfile) return;
    const categoriaChanges = this.incidencias
      .map((inc) => ({ id: inc.id, actual: inc.categoriaId ?? null, nuevo: this.selectedCategoriaByIncidenciaId[inc.id] ?? null }))
      .filter((x) => x.nuevo !== x.actual && x.nuevo !== null);
    const tecnicoChanges = this.incidencias
      .map((inc) => ({ id: inc.id, actual: inc.tecnicoAsignado, nuevo: this.selectedTecnicoByIncidenciaId[inc.id] ?? inc.tecnicoAsignado }))
      .filter((x) => x.nuevo && x.nuevo !== x.actual);
    const resueltaChanges = this.incidencias
      .map((inc) => ({ id: inc.id, actual: inc.resuelta, nuevo: this.selectedResueltaByIncidenciaId[inc.id] ?? inc.resuelta }))
      .filter((x) => x.nuevo !== x.actual);
    const prioridadChanges = this.incidencias
      .map((inc) => ({ id: inc.id, actual: (inc.prioridad ?? 'NORMAL').toUpperCase(), nuevo: (this.selectedPrioridadByIncidenciaId[inc.id] ?? inc.prioridad ?? 'NORMAL').toUpperCase() }))
      .filter((x) => x.nuevo !== x.actual);

    const totalOps = categoriaChanges.length + tecnicoChanges.length + resueltaChanges.length + prioridadChanges.length;
    if (totalOps === 0) return;

    this.savingIncidencias = true;
    let pending = totalOps;
    const done = () => {
      pending--;
      if (pending === 0) {
        this.savingIncidencias = false;
        this.loadIncidencias();
      }
    };

    categoriaChanges.forEach((c) => this.inboxApi.updateCategoria(c.id, c.nuevo as number).subscribe({ next: done, error: done }));
    tecnicoChanges.forEach((t) => this.inboxApi.updateIncidenciaTecnico(t.id, t.nuevo).subscribe({ next: done, error: done }));
    resueltaChanges.forEach((r) => this.inboxApi.updateResuelta(r.id, r.nuevo).subscribe({ next: done, error: done }));
    prioridadChanges.forEach((p) => this.inboxApi.updatePrioridad(p.id, p.nuevo).subscribe({ next: done, error: done }));
  }

  categorizarAutomatico(): void {}

  openStats(): void {
    this.inboxApi.getStats().subscribe({
      next: (stats) => {
        this.stats = stats;
        this.showStatsModal = true;
      }
    });
  }
  closeStats(): void {
    this.showStatsModal = false;
  }

  categoryStyleClass(inc: IncidenciaInboxItem): string {
    const token = (inc.categoriaAbreviatura ?? 'NA').toUpperCase();
    let acc = 0;
    for (let i = 0; i < token.length; i++) acc += token.charCodeAt(i);
    return `cat-style-${(acc % 4) + 1}`;
  }

  categoryStyle(inc: IncidenciaInboxItem): Record<string, string> {
    return {
      background: inc.categoriaColorHex ?? '#f8fafc'
    };
  }

  openTreatmentModal(inc: IncidenciaInboxItem): void {
    this.selectedIncidencia = inc;
    this.notaTecnico = this.selectedTecnicoByIncidenciaId[inc.id] ?? inc.tecnicoAsignado;
    this.notaObservacion = '';
    this.notaDetalle = '';
    this.notaAccion = '';
    this.inboxApi.listNotas(inc.id).subscribe({
      next: (notas) => {
        this.notasIncidencia = notas;
        this.showTreatmentModal = true;
      },
      error: () => {
        this.notasIncidencia = [];
        this.showTreatmentModal = true;
      }
    });
  }

  onIncCardClick(event: MouseEvent, inc: IncidenciaInboxItem): void {
    const target = event.target as HTMLElement | null;
    if (target?.closest(InboxComponent.INTERACTIVE_CLICK_SELECTOR)) return;
    this.openTreatmentModal(inc);
  }

  closeTreatmentModal(): void {
    this.showTreatmentModal = false;
    this.selectedIncidencia = null;
  }

  addNotaTratamiento(): void {
    if (!this.selectedIncidencia || !this.notaTecnico || !this.notaObservacion) return;
    this.inboxApi
      .addNota(
        this.selectedIncidencia.id,
        this.notaTecnico,
        this.notaObservacion,
        this.notaDetalle,
        this.notaAccion
      )
      .subscribe({
        next: () => {
          if (this.selectedIncidencia) {
            this.openTreatmentModal(this.selectedIncidencia);
            this.loadIncidencias();
          }
        }
      });
  }

  openAdminModal(tab: 'tecnico' | 'categoria'): void {
    this.adminTab = tab;
    this.showAdminModal = true;
  }

  closeAdminModal(): void {
    this.showAdminModal = false;
  }

  createTecnicoAdmin(): void {
    if (!this.newTecnicoNombre || !this.newTecnicoEmail) return;
    this.inboxApi.createTecnico(this.newTecnicoNombre, this.newTecnicoEmail).subscribe({
      next: () => {
        this.newTecnicoNombre = '';
        this.newTecnicoEmail = '';
        this.loadTecnicos();
      }
    });
  }

  createCategoriaAdmin(): void {
    if (!this.newCategoriaNombre || !this.newCategoriaAbreviatura) return;
    this.inboxApi.createCategoria(this.newCategoriaNombre, this.newCategoriaAbreviatura, this.newCategoriaColorHex).subscribe({
      next: () => {
        this.newCategoriaNombre = '';
        this.newCategoriaAbreviatura = '';
        this.newCategoriaColorHex = '#f3f4f6';
        this.loadCategorias();
      }
    });
  }

  startEditCategoria(categoriaId: number | null | undefined): void {
    if (!categoriaId) return;
    const cat = this.categorias.find((c) => c.id === categoriaId);
    if (!cat) return;
    this.editCategoriaId = cat.id;
    this.editCategoriaNombre = cat.nombre;
    this.editCategoriaAbreviatura = cat.abreviatura;
    this.editCategoriaColorHex = cat.colorHex ?? '#f3f4f6';
    this.openAdminModal('categoria');
  }

  saveEditCategoria(): void {
    if (!this.editCategoriaId) return;
    this.inboxApi
      .updateCategoriaConfig(this.editCategoriaId, this.editCategoriaNombre, this.editCategoriaAbreviatura, this.editCategoriaColorHex)
      .subscribe({
        next: () => {
          this.editCategoriaId = null;
          this.loadCategorias();
          this.loadIncidencias();
        }
      });
  }

  private filteredItems(): InboxItem[] {
    return this.items.filter((item) => {
      if (this.hideAutomatic && item.sender.toLowerCase() === 'servicionotificacionetna@enaire.es') return false;
      if (this.filters.sender && !item.sender.toLowerCase().includes(this.filters.sender.toLowerCase())) return false;
      if (this.filters.subject && !item.subject.toLowerCase().includes(this.filters.subject.toLowerCase())) return false;
      if (this.filters.summary && !item.summary.toLowerCase().includes(this.filters.summary.toLowerCase())) return false;
      if (this.filters.tecnico && !(item.tecnicoAsignado ?? '').toLowerCase().includes(this.filters.tecnico.toLowerCase())) return false;
      if (this.filters.generic) {
        const received = new Date(item.receivedDateTime);
        const dateDdMmYyyy = Number.isNaN(received.getTime())
          ? ''
          : `${String(received.getDate()).padStart(2, '0')}/${String(received.getMonth() + 1).padStart(2, '0')}/${received.getFullYear()}`;
        const dateDdMmYyyyHhMm = Number.isNaN(received.getTime())
          ? ''
          : `${dateDdMmYyyy} ${String(received.getHours()).padStart(2, '0')}:${String(received.getMinutes()).padStart(2, '0')}`;
        const allText =
          `${item.sender} ${item.subject} ${item.summary} ${item.tecnicoAsignado ?? ''} ${item.receivedDateTime ?? ''} ${dateDdMmYyyy} ${dateDdMmYyyyHhMm}`
            .toLowerCase();
        if (!allText.includes(this.filters.generic.toLowerCase())) return false;
      }
      return true;
    });
  }

  redirigirIncidencia(): void {
    if (!this.selectedIncidencia || !this.redirigirTecnico) return;
    this.inboxApi.redirectIncidencia(this.selectedIncidencia.id, this.redirigirTecnico).subscribe({
      next: () => {
        this.openTreatmentModal(this.selectedIncidencia as IncidenciaInboxItem);
        this.loadIncidencias();
      }
    });
  }

  resolverIncidencia(): void {
    if (!this.selectedIncidencia || !this.resolucionDescripcion) return;
    this.inboxApi.resolveIncidencia(this.selectedIncidencia.id, this.resolucionDescripcion).subscribe({
      next: () => {
        this.resolucionDescripcion = '';
        this.openTreatmentModal(this.selectedIncidencia as IncidenciaInboxItem);
        this.loadIncidencias();
      }
    });
  }

  rechazarResolucion(): void {
    if (!this.selectedIncidencia || !this.rechazoMotivo) return;
    this.inboxApi.rejectResolution(this.selectedIncidencia.id, this.rechazoMotivo).subscribe({
      next: () => {
        this.rechazoMotivo = '';
        this.openTreatmentModal(this.selectedIncidencia as IncidenciaInboxItem);
        this.loadIncidencias();
      }
    });
  }

  elapsedFromReceived(inc: IncidenciaInboxItem): string {
    const start = Date.parse(inc.receivedDateTime ?? '');
    if (Number.isNaN(start)) return '';
    const diffMin = Math.max(0, Math.floor((Date.now() - start) / 60000));
    const d = Math.floor(diffMin / (60 * 24));
    const h = Math.floor((diffMin % (60 * 24)) / 60);
    const m = diffMin % 60;
    return `${d}d ${h}h ${m}m`;
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
        if (this.context.puedeVerCorreos) this.refresh();
      }
    });
  }

  private loadIncidencias(): void {
    this.inboxApi.listIncidencias().subscribe({
      next: (items) => {
        this.incidencias = items;
        for (const it of items) {
          if (it.categoriaId) this.selectedCategoriaByIncidenciaId[it.id] = it.categoriaId;
          this.selectedTecnicoByIncidenciaId[it.id] = it.tecnicoAsignado;
          this.selectedResueltaByIncidenciaId[it.id] = it.resuelta;
          this.selectedPrioridadByIncidenciaId[it.id] = (it.prioridad ?? 'NORMAL').toUpperCase();
        }
      },
      error: () => (this.incidencias = [])
    });
  }

  private loadTecnicos(): void {
    this.inboxApi.listTecnicos().subscribe({ next: (tecnicos) => (this.tecnicos = tecnicos), error: () => (this.tecnicos = []) });
  }
  private loadCategorias(): void {
    this.inboxApi.listCategorias().subscribe({ next: (categorias) => (this.categorias = categorias), error: () => (this.categorias = []) });
  }

  logout(): void {
    this.logoutUseCase.execute();
    this.router.navigateByUrl('/login');
  }

  openPrioridadModal(): void {
    this.pendingPrioridad = 'NORMAL';
    this.showPrioridadModal = true;
  }

  closePrioridadModal(): void {
    this.showPrioridadModal = false;
    this.pendingSingleAssign = null;
  }

  confirmPrioridadAsignacion(): void {
    const prioridad = this.pendingPrioridad;
    const single = this.pendingSingleAssign;
    this.closePrioridadModal();
    if (single) {
      this.executeSingleAssign(single, prioridad);
      return;
    }
    this.executeBulkAssign(prioridad);
  }

  private setAssignFeedback(message: string, type: 'success' | 'error' | 'info' | ''): void {
    this.assignFeedback = message;
    this.assignFeedbackType = type;
  }

  private extractErrorMessage(err: unknown): string {
    const fallback = 'Error desconocido';
    if (!err || typeof err !== 'object') return fallback;
    const errorObj = err as { error?: { message?: string } | string; message?: string; status?: number; statusText?: string };
    if (typeof errorObj.error === 'string' && errorObj.error.trim()) return errorObj.error;
    if (typeof errorObj.error === 'object' && errorObj.error?.message) return errorObj.error.message;
    if (errorObj.message) return errorObj.message;
    if (errorObj.status) return `${errorObj.status}${errorObj.statusText ? ` ${errorObj.statusText}` : ''}`;
    return fallback;
  }

  private priorityRank(prioridad: string | undefined): number {
    const p = (prioridad ?? 'NORMAL').toUpperCase();
    if (p === 'URGENTE') return 0;
    if (p === 'ALTA') return 1;
    if (p === 'NORMAL') return 2;
    if (p === 'BAJA') return 3;
    return 4;
  }

  private toMillis(value: string | undefined): number {
    const ms = Date.parse(value ?? '');
    return Number.isNaN(ms) ? Number.MAX_SAFE_INTEGER : ms;
  }
}
