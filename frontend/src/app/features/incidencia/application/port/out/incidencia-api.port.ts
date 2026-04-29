import { InjectionToken } from '@angular/core';
import { Observable } from 'rxjs';
import { Incidencia } from '../../../domain/incidencia.model';

/**
 * Output port for incidencia persistence and retrieval.
 */
export interface IncidenciaApiPort {
  create(payload: Incidencia): Observable<Incidencia>;
  list(): Observable<Incidencia[]>;
}

export const INCIDENCIA_API_PORT = new InjectionToken<IncidenciaApiPort>('INCIDENCIA_API_PORT');
