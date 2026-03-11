import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Incidencia } from '../domain/incidencia.model';

@Injectable({ providedIn: 'root' })
export class IncidenciaApiService {
  private readonly baseUrl = 'http://localhost:4000/api/incidencias';

  constructor(private http: HttpClient) {}

  create(payload: Incidencia): Observable<Incidencia> {
    return this.http.post<Incidencia>(this.baseUrl, payload);
  }

  list(): Observable<Incidencia[]> {
    return this.http.get<Incidencia[]>(this.baseUrl);
  }
}
