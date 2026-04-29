import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { NgIf } from '@angular/common';
import { LogoutUseCase } from '../../../core/auth/application/logout.usecase';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-startup',
  standalone: true,
  imports: [NgIf],
  templateUrl: './startup.component.html',
  styleUrl: './startup.component.scss'
})
export class StartupComponent {
  statusMessage = 'Este tenant exige MFA. Inicia sesión con Microsoft para continuar.';
  hasError = false;
  isLoading = false;
  readonly loginUrl = 'http://localhost:4000/api/auth/entra/login';
  readonly statusUrl = 'http://localhost:4000/api/auth/entra/status';

  constructor(
    private logoutUseCase: LogoutUseCase,
    private authService: AuthService,
    private http: HttpClient,
    private router: Router
  ) {
    console.info('Startup: comprobando si ya existe sesión interactiva en backend');
    this.comprobarEstadoLoginSilencioso();
  }

  iniciarSesionMicrosoft(): void {
    this.hasError = false;
    this.statusMessage = 'Redirigiendo a Microsoft Entra ID...';
    console.info('Startup: redirigiendo a login interactivo', { loginUrl: this.loginUrl });
    window.location.href = this.loginUrl;
  }

  comprobarEstadoLogin(): void {
    this.hasError = false;
    this.isLoading = true;
    this.statusMessage = 'Comprobando sesión interactiva en backend...';
    this.http.get<{ loggedIn: boolean; account?: string | null }>(this.statusUrl).subscribe({
      next: (response) => {
        this.isLoading = false;
        console.info('Startup: estado sesión interactiva', response);
        if (response.loggedIn) {
          this.authService.setToken(`entra-interactive-${Date.now()}`);
          this.statusMessage = 'Sesión activa. Accediendo a buzones...';
          this.router.navigateByUrl('/buzones');
          return;
        }
        this.hasError = true;
        this.statusMessage = 'No hay sesión interactiva todavía. Completa el login y vuelve a comprobar.';
      },
      error: (error) => {
        this.isLoading = false;
        this.hasError = true;
        this.statusMessage = 'No se pudo comprobar el estado del login interactivo.';
        console.error('Startup: error comprobando estado interactivo', error);
      }
    });
  }

  private comprobarEstadoLoginSilencioso(): void {
    this.http.get<{ loggedIn: boolean; account?: string | null }>(this.statusUrl).subscribe({
      next: (response) => {
        console.info('Startup: estado silencioso', response);
        if (response.loggedIn) {
          this.authService.setToken(`entra-interactive-${Date.now()}`);
          this.statusMessage = 'Sesión detectada. Accediendo a buzones...';
          this.router.navigateByUrl('/buzones');
        }
      },
      error: (error) => {
        console.warn('Startup: no se pudo comprobar estado silencioso', error);
      }
    });
  }
}
