import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { InboxItem, Tecnico } from '../domain/inbox.model';

@Injectable({ providedIn: 'root' })
export class InboxApiService {
  private readonly inboxUrl = 'http://localhost:4000/api/inbox/gestion';
  private readonly tecnicosUrl = 'http://localhost:4000/api/tecnicos';

  constructor(private http: HttpClient) {}

  list(summaryLength: number): Observable<InboxItem[]> {
    return this.http.get<InboxItem[]>(`${this.inboxUrl}?summaryLength=${summaryLength}`);
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
}
