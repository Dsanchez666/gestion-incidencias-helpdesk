import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../../core/auth/auth.service';
import { TraceGraphUserUseCase } from '../../mailbox/application/trace-graph-user.usecase';

@Component({
  selector: 'app-startup',
  standalone: true,
  templateUrl: './startup.component.html',
  styleUrl: './startup.component.scss'
})
export class StartupComponent {
  statusMessage = 'Verificando sesión en Microsoft Entra ID...';
  hasError = false;
  readonly loginUrl = 'http://localhost:4000/api/auth/entra/login';
  readonly statusUrl = 'http://localhost:4000/api/auth/entra/status';

  constructor(
    private authService: AuthService,
    private http: HttpClient,
    private traceGraphUserUseCase: TraceGraphUserUseCase,
    private router: Router
  ) {
    this.start();
  }

  private start(): void {
    this.http.get<{ loggedIn: boolean }>(this.statusUrl).subscribe({
      next: (response) => {
        if (response.loggedIn) {
          this.authService.setToken(`entra-interactive-${Date.now()}`);
          this.statusMessage = 'Sesion verificada. Cargando carpetas de usuario...';
          this.loadUserFoldersAndGoInbox();
          return;
        }
        this.statusMessage = 'Redirigiendo a autenticación MFA...';
        window.location.href = this.loginUrl;
      },
      error: () => {
        this.hasError = true;
        this.statusMessage = 'No se pudo comprobar el estado de autenticación.';
      }
    });
  }

  private loadUserFoldersAndGoInbox(): void {
    this.traceGraphUserUseCase.execute().subscribe({
      next: (trace) => {
        if (!trace.success) {
          this.hasError = true;
          this.statusMessage = trace.error ?? 'Autenticado, pero no se pudieron cargar carpetas de usuario.';
          return;
        }
        this.statusMessage = 'Carpetas cargadas. Abriendo bandeja de entrada...';
        this.router.navigateByUrl('/inbox');
      },
      error: () => {
        this.hasError = true;
        this.statusMessage = 'Autenticado, pero fallo la carga de carpetas de usuario.';
      }
    });
  }
}
