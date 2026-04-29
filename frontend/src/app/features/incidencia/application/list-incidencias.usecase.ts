import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Incidencia } from '../domain/incidencia.model';
import { INCIDENCIA_API_PORT, IncidenciaApiPort } from './port/out/incidencia-api.port';

/**
 * Application use case that retrieves all incidencias.
 */
@Injectable({ providedIn: 'root' })
export class ListIncidenciasUseCase {
  private readonly api = inject(INCIDENCIA_API_PORT) as IncidenciaApiPort;

  execute(): Observable<Incidencia[]> {
    return this.api.list();
  }
}
