import { Component } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CreateIncidenciaUseCase } from '../application/create-incidencia.usecase';
import { ListIncidenciasUseCase } from '../application/list-incidencias.usecase';
import { Incidencia } from '../domain/incidencia.model';
import { LogoutUseCase } from '../../../core/auth/application/logout.usecase';

@Component({
  selector: 'app-incidencia-form',
  standalone: true,
  imports: [FormsModule, NgIf, NgFor],
  templateUrl: './incidencia-form.component.html',
  styleUrl: './incidencia-form.component.scss'
})
export class IncidenciaFormComponent {
  model: Incidencia = { asunto: '', descripcion: '', emailSolicitante: '', prioridad: 'MEDIA' };
  incidencias: Incidencia[] = [];
  error = '';

  constructor(
    private createIncidenciaUseCase: CreateIncidenciaUseCase,
    private listIncidenciasUseCase: ListIncidenciasUseCase,
    private logoutUseCase: LogoutUseCase,
    private router: Router
  ) {
    this.refresh();
  }

  save(): void {
    this.error = '';
    this.createIncidenciaUseCase.execute(this.model).subscribe({
      next: () => {
        this.model = { asunto: '', descripcion: '', emailSolicitante: '', prioridad: 'MEDIA' };
        this.refresh();
      },
      error: () => {
        this.error = 'No se pudo registrar la incidencia';
      }
    });
  }

  refresh(): void {
    this.listIncidenciasUseCase.execute().subscribe({
      next: (data: Incidencia[]) => {
        this.incidencias = data;
      },
      error: () => {
        this.incidencias = [];
      }
    });
  }

  goToMailboxes(): void {
    this.router.navigateByUrl('/buzones');
  }

  logout(): void {
    this.logoutUseCase.execute();
    this.router.navigateByUrl('/login');
  }
}
