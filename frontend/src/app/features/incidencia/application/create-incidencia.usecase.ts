import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Incidencia } from '../domain/incidencia.model';
import { INCIDENCIA_API_PORT, IncidenciaApiPort } from './port/out/incidencia-api.port';

@Injectable({ providedIn: 'root' })
export class CreateIncidenciaUseCase {
  private readonly api = inject(INCIDENCIA_API_PORT) as IncidenciaApiPort;

  execute(incidencia: Incidencia): Observable<Incidencia> {
    return this.api.create(incidencia);
  }
}
