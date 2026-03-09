import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Incidencia } from '../domain/incidencia.model';
import { IncidenciaApiService } from '../infrastructure/incidencia-api.service';

@Injectable({ providedIn: 'root' })
export class CreateIncidenciaUseCase {
  constructor(private api: IncidenciaApiService) {}

  execute(incidencia: Incidencia): Observable<Incidencia> {
    return this.api.create(incidencia);
  }
}
