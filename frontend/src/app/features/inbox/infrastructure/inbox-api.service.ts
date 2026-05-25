import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Categoria,
  InboxContext,
  InboxItem,
  IncidenciaInboxItem,
  IncidenciaNota,
  IncidenciasStatsResponse,
  Prioridad,
  Tecnico
} from '../domain/inbox.model';

@Injectable({ providedIn: 'root' })
export class InboxApiService {
  private readonly inboxUrl = 'http://localhost:4000/api/inbox/gestion';
  private readonly tecnicosUrl = 'http://localhost:4000/api/tecnicos';
  private readonly categoriasUrl = 'http://localhost:4000/api/categorias';
  private readonly prioridadesUrl = 'http://localhost:4000/api/prioridades';

  constructor(private readonly http: HttpClient) {}

  list(summaryLength: number): Observable<InboxItem[]> {
    return this.http.get<InboxItem[]>(`${this.inboxUrl}?summaryLength=${summaryLength}`);
  }

  context(): Observable<InboxContext> {
    return this.http.get<InboxContext>(`${this.inboxUrl}/context`);
  }

  listIncidencias(): Observable<IncidenciaInboxItem[]> {
    return this.http.get<IncidenciaInboxItem[]>(`${this.inboxUrl}/incidencias`);
  }

  updateIncidencia(messageId: string, incidenciaGenerada: boolean): Observable<void> {
    return this.http.patch<void>(`${this.inboxUrl}/${encodeURIComponent(messageId)}/incidencia`, {
      incidenciaGenerada
    });
  }

  updateAsignacion(messageId: string, asignada: boolean, tecnicoAsignado: string): Observable<void> {
    return this.http.patch<void>(`${this.inboxUrl}/${encodeURIComponent(messageId)}/asignacion`, {
      asignada,
      tecnicoAsignado
    });
  }

  listTecnicos(): Observable<Tecnico[]> {
    return this.http.get<Tecnico[]>(this.tecnicosUrl);
  }

  assignIncidencia(messageId: string, tecnicoNombre: string, prioridad: string, summaryLength: number): Observable<void> {
    return this.http.post<void>(
      `${this.inboxUrl}/${encodeURIComponent(messageId)}/asignar-incidencia?summaryLength=${summaryLength}`,
      { tecnicoNombre, prioridad }
    );
  }

  assignIncidencias(messageIds: string[], tecnicoNombre: string, prioridad: string, summaryLength: number): Observable<void> {
    return this.http.post<void>(`${this.inboxUrl}/asignar-incidencias?summaryLength=${summaryLength}`, {
      messageIds,
      tecnicoNombre,
      prioridad
    });
  }

  listCategorias(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(this.categoriasUrl);
  }

  listPrioridades(): Observable<Prioridad[]> {
    return this.http.get<Prioridad[]>(this.prioridadesUrl);
  }

  createCategoria(nombre: string, abreviatura: string, colorHex: string): Observable<void> {
    return this.http.post<void>(this.categoriasUrl, { nombre, abreviatura, colorHex });
  }

  updateCategoriaConfig(id: number, nombre: string, abreviatura: string, colorHex: string): Observable<void> {
    return this.http.patch<void>(`${this.categoriasUrl}/${id}`, { nombre, abreviatura, colorHex });
  }

  updateCategoria(incidenciaId: number, categoriaId: number): Observable<void> {
    return this.http.patch<void>(`${this.inboxUrl}/incidencias/${incidenciaId}/categoria`, { categoriaId });
  }

  updateIncidenciaTecnico(incidenciaId: number, tecnicoNombre: string): Observable<void> {
    return this.http.patch<void>(`${this.inboxUrl}/incidencias/${incidenciaId}/tecnico`, { tecnicoNombre });
  }

  updateResuelta(incidenciaId: number, resuelta: boolean): Observable<void> {
    return this.http.patch<void>(`${this.inboxUrl}/incidencias/${incidenciaId}/resuelta`, { resuelta });
  }
  updatePrioridad(incidenciaId: number, prioridad: string): Observable<void> {
    return this.http.patch<void>(`${this.inboxUrl}/incidencias/${incidenciaId}/prioridad`, { prioridad });
  }
  redirectIncidencia(incidenciaId: number, tecnicoNombre: string): Observable<void> {
    return this.http.patch<void>(`${this.inboxUrl}/incidencias/${incidenciaId}/redirigir`, { tecnicoNombre });
  }
  resolveIncidencia(incidenciaId: number, descripcionResolucion: string): Observable<void> {
    return this.http.patch<void>(`${this.inboxUrl}/incidencias/${incidenciaId}/resolver`, { descripcionResolucion });
  }
  rejectResolution(incidenciaId: number, motivo: string): Observable<void> {
    return this.http.patch<void>(`${this.inboxUrl}/incidencias/${incidenciaId}/rechazar-resolucion`, { motivo });
  }
  getSeguimiento(token: string): Observable<{ incidencia: IncidenciaInboxItem; historico: IncidenciaNota[]; tiempoResolucion: string }> {
    return this.http.get<{ incidencia: IncidenciaInboxItem; historico: IncidenciaNota[]; tiempoResolucion: string }>(
      `${this.inboxUrl}/incidencias/seguimiento/${encodeURIComponent(token)}`
    );
  }

  getStats(): Observable<IncidenciasStatsResponse> {
    return this.http.get<IncidenciasStatsResponse>(`${this.inboxUrl}/incidencias/stats`);
  }

  createTecnico(nombre: string, email: string): Observable<void> {
    return this.http.post<void>('http://localhost:4000/api/tecnicos', { nombre, email });
  }

  listNotas(incidenciaId: number): Observable<IncidenciaNota[]> {
    return this.http.get<IncidenciaNota[]>(`${this.inboxUrl}/incidencias/${incidenciaId}/notas`);
  }

  listHistorico(incidenciaId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.inboxUrl}/incidencias/${incidenciaId}/historico`);
  }

  addNota(
    incidenciaId: number,
    tecnico: string,
    observacion: string,
    detalle: string,
    accionRealizada: string
  ): Observable<void> {
    return this.http.post<void>(`${this.inboxUrl}/incidencias/${incidenciaId}/notas`, {
      tecnico,
      observacion,
      detalle,
      accionRealizada
    });
  }
}
